package com.example.campusexpensemanager.Fragment;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.campusexpensemanager.DatabaseHelper;
import com.example.campusexpensemanager.HomeActivity;
import com.example.campusexpensemanager.R;
import com.example.campusexpensemanager.dao.ExpenseDAO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FixedcostsFragment extends Fragment {

    private EditText edtName, edtAmount, edtDescription;
    private Button btnSave, btnBack;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fixedcosts, container, false); // Trỏ đúng file XML bạn vừa sửa

        // Ánh xạ View (theo ID mới trong XML fixedcosts)
        edtName = view.findViewById(R.id.edtFixedName);
        edtAmount = view.findViewById(R.id.edtFixedAmount);
        edtDescription = view.findViewById(R.id.edtFixedDescription);
        btnSave = view.findViewById(R.id.btnSaveFixed);
        btnBack = view.findViewById(R.id.btnBackFixed);

        dbHelper = new DatabaseHelper(getContext());

        // Tự động điền tên ngân sách theo tháng hiện tại
        String currentMonthName = new SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(new Date());
        edtName.setText("Ngân sách tháng " + currentMonthName);

        // Xử lý sự kiện bấm nút
        btnSave.setOnClickListener(v -> saveBudget());

        btnBack.setOnClickListener(v -> {
            // Gọi hàm quay về Home
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).navigateToHome();
            }
        });

        return view;
    }

    private void saveBudget() {
        String amountStr = edtAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập số tiền!", Toast.LENGTH_SHORT).show();
            return;
        }

        double newBudgetAmount = Double.parseDouble(amountStr);

        // 1. Lấy tháng hiện tại (yyyy-MM) để làm khóa chính
        String currentMonthKey = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());

        // 2. Tính số tiền còn lại = (Ngân sách mới) - (Đã tiêu trong tháng này)
        ExpenseDAO expenseDAO = new ExpenseDAO(getContext());
        double totalSpent = expenseDAO.getTotalByMonth(currentMonthKey);
        double remainingAmount = newBudgetAmount - totalSpent;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Lưu vào bảng NGAN_SACH
        values.put(DatabaseHelper.NS_THANG_NAM, currentMonthKey);
        values.put(DatabaseHelper.NS_SO_TIEN_DU_KIEN, newBudgetAmount);
        values.put(DatabaseHelper.NS_SO_TIEN_CON_LAI, remainingAmount);

        try {
            // Kiểm tra xem tháng này đã có ngân sách chưa
            int existingId = getBudgetIdByMonth(currentMonthKey);

            if (existingId != -1) {
                // Đã có -> Cập nhật (Update)
                db.update(DatabaseHelper.TABLE_NGAN_SACH, values, DatabaseHelper.NS_ID + "=?", new String[]{String.valueOf(existingId)});
                Toast.makeText(getContext(), "Đã cập nhật ngân sách!", Toast.LENGTH_SHORT).show();
            } else {
                // Chưa có -> Tạo mới (Insert)
                db.insert(DatabaseHelper.TABLE_NGAN_SACH, null, values);
                Toast.makeText(getContext(), "Đã tạo ngân sách mới!", Toast.LENGTH_SHORT).show();
            }

            // Lưu xong thì quay về Home ngay
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).navigateToHome();
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Hàm kiểm tra xem đã có ngân sách tháng này chưa
    private int getBudgetIdByMonth(String monthYear) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_NGAN_SACH,
                new String[]{DatabaseHelper.NS_ID},
                DatabaseHelper.NS_THANG_NAM + "=?",
                new String[]{monthYear}, null, null, null);

        int id = -1;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                id = cursor.getInt(0);
            }
            cursor.close();
        }
        return id;
    }
}