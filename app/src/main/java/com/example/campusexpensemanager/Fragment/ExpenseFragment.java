package com.example.campusexpensemanager.Fragment;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.campusexpensemanager.DatabaseHelper;
import com.example.campusexpensemanager.HomeActivity; // Đã import HomeActivity
import com.example.campusexpensemanager.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseFragment extends Fragment {

    // Khai báo các View
    private EditText edtName, edtAmount, edtDescription;
    private Spinner spinnerCategory;
    private Button btnSave, btnBack;

    // Database
    private DatabaseHelper dbHelper;

    // Class phụ để hiển thị trong Spinner (Lưu cả ID và Tên)
    private static class CategoryItem {
        int id;
        String name;

        public CategoryItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @NonNull
        @Override
        public String toString() {
            return name; // Spinner sẽ hiển thị cái này
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense, container, false);

        // 1. Ánh xạ View từ XML
        edtName = view.findViewById(R.id.edtExpenseName);
        edtAmount = view.findViewById(R.id.edtExpenseMoney);
        edtDescription = view.findViewById(R.id.edtBudgetDescription);
        spinnerCategory = view.findViewById(R.id.spinner_budget_selection);
        btnSave = view.findViewById(R.id.btnSave);
        btnBack = view.findViewById(R.id.btnBackExpense);

        dbHelper = new DatabaseHelper(getContext());

        // 2. Load dữ liệu vào Spinner
        loadCategoriesToSpinner();

        // 3. Xử lý sự kiện nút Lưu
        btnSave.setOnClickListener(v -> saveExpense());

        // 4. Xử lý nút Back
        btnBack.setOnClickListener(v -> {
            // Quay lại màn hình trước đó
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        return view;
    }

    private void loadCategoriesToSpinner() {
        List<CategoryItem> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Truy vấn bảng Loai Chi Phi
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOAI_CHI_PHI, null, null, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.LOAI_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.LOAI_TEN));
                categories.add(new CategoryItem(id, name));
            }
            cursor.close();
        }

        // Gán vào Adapter
        ArrayAdapter<CategoryItem> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void saveExpense() {
        String name = edtName.getText().toString().trim();
        String amountStr = edtAmount.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();

        // Validation cơ bản
        if (name.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tên và số tiền!", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        CategoryItem selectedCategory = (CategoryItem) spinnerCategory.getSelectedItem();

        if (selectedCategory == null) {
            Toast.makeText(getContext(), "Vui lòng chọn loại chi phí!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy ngày hiện tại
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());

        // Gộp tên và mô tả vào ghi chú (nếu có mô tả)
        String finalNote = name;
        if (!description.isEmpty()) {
            finalNote += " (" + description + ")";
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // --- BẮT ĐẦU TRANSACTION ---
        db.beginTransaction();
        try {
            // 1. Insert vào bảng CHI_TIEU
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.CT_LOAI_ID, selectedCategory.id);
            values.put(DatabaseHelper.CT_SO_TIEN, amount);
            values.put(DatabaseHelper.CT_NGAY, currentDate);
            values.put(DatabaseHelper.CT_THANG_NAM, currentMonth);
            values.put(DatabaseHelper.CT_GHI_CHU, finalNote);

            long result = db.insert(DatabaseHelper.TABLE_CHI_TIEU, null, values);

            if (result == -1) {
                throw new Exception("Lỗi khi thêm chi tiêu");
            }

            // 2. Trừ tiền trong NGÂN SÁCH (Nếu có ngân sách tháng này)
            Cursor budgetCursor = db.query(DatabaseHelper.TABLE_NGAN_SACH,
                    new String[]{DatabaseHelper.NS_ID, DatabaseHelper.NS_SO_TIEN_CON_LAI},
                    DatabaseHelper.NS_THANG_NAM + "=?",
                    new String[]{currentMonth}, null, null, null);

            if (budgetCursor != null && budgetCursor.moveToFirst()) {
                int nsId = budgetCursor.getInt(budgetCursor.getColumnIndexOrThrow(DatabaseHelper.NS_ID));
                double currentBalance = budgetCursor.getDouble(budgetCursor.getColumnIndexOrThrow(DatabaseHelper.NS_SO_TIEN_CON_LAI));

                // Cập nhật số tiền còn lại
                double newBalance = currentBalance - amount;

                ContentValues budgetValues = new ContentValues();
                budgetValues.put(DatabaseHelper.NS_SO_TIEN_CON_LAI, newBalance);

                db.update(DatabaseHelper.TABLE_NGAN_SACH, budgetValues, DatabaseHelper.NS_ID + "=?", new String[]{String.valueOf(nsId)});
            }
            if(budgetCursor != null) budgetCursor.close();

            db.setTransactionSuccessful(); // Xác nhận giao dịch thành công
            Toast.makeText(getContext(), "Lưu chi phí thành công!", Toast.LENGTH_SHORT).show();

            // === PHẦN QUAN TRỌNG: CHUYỂN VỀ HOME SAU KHI LƯU ===
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).navigateToHome();
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
        }
    }
}