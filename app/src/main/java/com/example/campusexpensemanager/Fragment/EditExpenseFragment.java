package com.example.campusexpensemanager.Fragment;

import android.app.AlertDialog;
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
import com.example.campusexpensemanager.HomeActivity;
import com.example.campusexpensemanager.R;
import java.util.ArrayList;
import java.util.List;

public class EditExpenseFragment extends Fragment {

    private EditText edtName, edtAmount, edtDesc;
    private Spinner spinnerCategory;
    private Button btnUpdate, btnDelete, btnBack;
    private DatabaseHelper dbHelper;
    private int expenseId = -1;

    // Class phụ cho Spinner
    private static class CategoryItem {
        int id; String name;
        public CategoryItem(int id, String name) { this.id = id; this.name = name; }
        @NonNull @Override public String toString() { return name; }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_expense, container, false);

        edtName = view.findViewById(R.id.edtEditName);
        edtAmount = view.findViewById(R.id.edtEditAmount);
        edtDesc = view.findViewById(R.id.edtEditDesc);
        spinnerCategory = view.findViewById(R.id.spinnerEditCategory);
        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnBack = view.findViewById(R.id.btnBackEdit);

        dbHelper = new DatabaseHelper(getContext());

        // Gọi hàm load category đã được lọc
        loadCategoriesFiltered();

        // Lấy ID được gửi từ Home
        if (getArguments() != null) {
            expenseId = getArguments().getInt("expense_id", -1);
            if (expenseId != -1) {
                loadExpenseData(expenseId);
            }
        }

        btnUpdate.setOnClickListener(v -> updateExpense());
        btnDelete.setOnClickListener(v -> confirmDelete());
        btnBack.setOnClickListener(v -> goHome());

        return view;
    }

    private void goHome() {
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).navigateToHome();
        }
    }

    // --- ĐOẠN CODE ĐÃ ĐƯỢC CHỈNH SỬA ---
    private void loadCategoriesFiltered() {
        List<CategoryItem> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Lọc bỏ 'Chi phí cố định hàng tháng' bằng mệnh đề WHERE !=
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_LOAI_CHI_PHI +
                        " WHERE " + DatabaseHelper.LOAI_TEN + " != ?",
                new String[]{"Chi phí cố định hàng tháng"});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                categories.add(new CategoryItem(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.LOAI_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.LOAI_TEN))));
            }
            cursor.close();
        }
        ArrayAdapter<CategoryItem> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }
    // -----------------------------------

    private void loadExpenseData(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_CHI_TIEU + " WHERE " + DatabaseHelper.CT_ID + " = ?", new String[]{String.valueOf(id)});
        if (c.moveToFirst()) {
            double amount = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.CT_SO_TIEN));
            String fullNote = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.CT_GHI_CHU));
            int catId = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.CT_LOAI_ID));

            edtAmount.setText(String.valueOf((int) amount));

            // Tách tên và mô tả
            if (fullNote.contains(" (")) {
                int splitIndex = fullNote.indexOf(" (");
                edtName.setText(fullNote.substring(0, splitIndex));
                String desc = fullNote.substring(splitIndex + 2);
                if (desc.endsWith(")")) {
                    desc = desc.substring(0, desc.length() - 1);
                }
                edtDesc.setText(desc);
            } else {
                edtName.setText(fullNote);
            }

            // Set Spinner Selection
            // Lưu ý: Nếu khoản chi này TRƯỚC ĐÓ là "Chi phí cố định", vòng lặp này sẽ không tìm thấy ID
            // và Spinner sẽ mặc định chọn mục đầu tiên (vì mục Cố định đã bị ẩn khỏi adapter).
            ArrayAdapter<CategoryItem> adapter = (ArrayAdapter<CategoryItem>) spinnerCategory.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).id == catId) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }
        c.close();
    }

    private void updateExpense() {
        String name = edtName.getText().toString().trim();
        String amountStr = edtAmount.getText().toString().trim();
        String desc = edtDesc.getText().toString().trim();

        if (name.isEmpty() || amountStr.isEmpty()) return;

        double newAmount = Double.parseDouble(amountStr);
        String finalNote = name + (desc.isEmpty() ? "" : " (" + desc + ")");
        CategoryItem selectedCat = (CategoryItem) spinnerCategory.getSelectedItem();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. Lấy số tiền cũ để tính chênh lệch
            Cursor c = db.rawQuery("SELECT " + DatabaseHelper.CT_SO_TIEN + ", " + DatabaseHelper.CT_THANG_NAM + " FROM " + DatabaseHelper.TABLE_CHI_TIEU + " WHERE " + DatabaseHelper.CT_ID + "=?", new String[]{String.valueOf(expenseId)});
            if (c.moveToFirst()) {
                double oldAmount = c.getDouble(0);
                String monthKey = c.getString(1);

                // 2. Update bảng Chi Tiêu
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.CT_SO_TIEN, newAmount);
                values.put(DatabaseHelper.CT_GHI_CHU, finalNote);

                // --- SỬA LỖI TẠI ĐÂY ---
                // Thay TABLE_LOAI_CHI_PHI bằng CT_LOAI_ID
                values.put(DatabaseHelper.CT_LOAI_ID, selectedCat.id);
                // -----------------------

                db.update(DatabaseHelper.TABLE_CHI_TIEU, values, DatabaseHelper.CT_ID + "=?", new String[]{String.valueOf(expenseId)});

                // 3. Update Ngân Sách
                double diff = oldAmount - newAmount;

                db.execSQL("UPDATE " + DatabaseHelper.TABLE_NGAN_SACH +
                        " SET " + DatabaseHelper.NS_SO_TIEN_CON_LAI + " = " + DatabaseHelper.NS_SO_TIEN_CON_LAI + " + ?" +
                        " WHERE " + DatabaseHelper.NS_THANG_NAM + " = ?", new Object[]{diff, monthKey});
            }
            c.close();

            db.setTransactionSuccessful();
            Toast.makeText(getContext(), "Đã cập nhật!", Toast.LENGTH_SHORT).show();
            goHome();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn chắc chắn muốn xóa khoản chi này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteExpense())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteExpense() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor c = db.rawQuery("SELECT " + DatabaseHelper.CT_SO_TIEN + ", " + DatabaseHelper.CT_THANG_NAM + " FROM " + DatabaseHelper.TABLE_CHI_TIEU + " WHERE " + DatabaseHelper.CT_ID + "=?", new String[]{String.valueOf(expenseId)});
            if (c.moveToFirst()) {
                double amount = c.getDouble(0);
                String monthKey = c.getString(1);

                db.delete(DatabaseHelper.TABLE_CHI_TIEU, DatabaseHelper.CT_ID + "=?", new String[]{String.valueOf(expenseId)});

                db.execSQL("UPDATE " + DatabaseHelper.TABLE_NGAN_SACH +
                        " SET " + DatabaseHelper.NS_SO_TIEN_CON_LAI + " = " + DatabaseHelper.NS_SO_TIEN_CON_LAI + " + ?" +
                        " WHERE " + DatabaseHelper.NS_THANG_NAM + " = ?", new Object[]{amount, monthKey});
            }
            c.close();

            db.setTransactionSuccessful();
            Toast.makeText(getContext(), "Đã xóa!", Toast.LENGTH_SHORT).show();
            goHome();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
        }
    }
}