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
import com.example.campusexpensemanager.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddExpenseFragment extends Fragment {

    private EditText edtName, edtAmount, edtDesc;
    private Spinner spinnerCategory;
    private Button btnSave, btnCancel;
    private DatabaseHelper dbHelper;

    private static class CategoryItem {
        int id; String name;
        public CategoryItem(int id, String name) { this.id = id; this.name = name; }
        @NonNull @Override public String toString() { return name; }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_expense, container, false);

        edtName = view.findViewById(R.id.edtAddName);
        edtAmount = view.findViewById(R.id.edtAddAmount);
        edtDesc = view.findViewById(R.id.edtAddDesc);
        spinnerCategory = view.findViewById(R.id.spinnerAddCategory);
        btnSave = view.findViewById(R.id.btnSaveAdd);
        btnCancel = view.findViewById(R.id.btnCancelAdd);
        dbHelper = new DatabaseHelper(getContext());

        loadCategoriesFiltered(); // Load danh mục đã lọc

        btnSave.setOnClickListener(v -> saveExpense());
        btnCancel.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

    private void loadCategoriesFiltered() {
        List<CategoryItem> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // --- QUAN TRỌNG: Lọc bỏ 'Chi phí cố định hàng tháng' ---
        // Sử dụng WHERE NOT để loại bỏ loại chi phí này
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_LOAI_CHI_PHI +
                        " WHERE " + DatabaseHelper.LOAI_TEN + " != ?",
                new String[]{"Chi phí cố định hàng tháng"}); // Đảm bảo tên này khớp 100% với tên trong DatabaseHelper

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

    private void saveExpense() {
        // ... (Logic lưu giống hệt bài trước, giữ nguyên) ...
        // Copy logic saveExpense() từ ExpenseFragment cũ sang đây
        // Lưu xong thì gọi getParentFragmentManager().popBackStack(); để quay lại list

        String name = edtName.getText().toString().trim();
        String amountStr = edtAmount.getText().toString().trim();
        if(name.isEmpty() || amountStr.isEmpty()) return;

        double amount = Double.parseDouble(amountStr);
        CategoryItem cat = (CategoryItem) spinnerCategory.getSelectedItem();
        String note = name + " " + edtDesc.getText().toString();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String month = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // Insert Chi Tieu
            ContentValues val = new ContentValues();
            val.put(DatabaseHelper.CT_LOAI_ID, cat.id);
            val.put(DatabaseHelper.CT_SO_TIEN, amount);
            val.put(DatabaseHelper.CT_NGAY, date);
            val.put(DatabaseHelper.CT_THANG_NAM, month);
            val.put(DatabaseHelper.CT_GHI_CHU, note);
            db.insert(DatabaseHelper.TABLE_CHI_TIEU, null, val);

            // Update Ngan Sach
            db.execSQL("UPDATE " + DatabaseHelper.TABLE_NGAN_SACH +
                    " SET " + DatabaseHelper.NS_SO_TIEN_CON_LAI + " = " + DatabaseHelper.NS_SO_TIEN_CON_LAI + " - " + amount +
                    " WHERE " + DatabaseHelper.NS_THANG_NAM + " = '" + month + "'");

            db.setTransactionSuccessful();
            Toast.makeText(getContext(), "Đã lưu!", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack(); // Quay lại màn hình danh sách
        } catch (Exception e) {
        } finally {
            db.endTransaction();
        }
    }
}