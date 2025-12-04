package com.example.campusexpensemanager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.text.DecimalFormat;

public class BudgetFragment extends Fragment {

    private DatabaseHelper db;
    private TextView txtTotalBudget;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        // 1. Khởi tạo Database
        db = new DatabaseHelper(getContext());

        // 2. Ánh xạ (Đảm bảo ID này DUY NHẤT trong file xml)
        txtTotalBudget = view.findViewById(R.id.txtTotalBudgetValue);

        // Nút mở Dialog thêm ngân sách
        View btnAdd = view.findViewById(R.id.btnAddBudget);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> showDialog());
        }

        // 3. Load dữ liệu lần đầu
        loadData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData(); // Load lại khi quay lại màn hình này
    }

    private void loadData() {
        if (db == null) return;

        // Lấy tổng ngân sách từ DB (Hàm này đã có trong DatabaseHelper 5 bảng)
        double total = db.getTotalBudget();

        DecimalFormat df = new DecimalFormat("#,### đ");
        if (txtTotalBudget != null) {
            txtTotalBudget.setText(df.format(total));
        }
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Inflate layout cho Dialog
        LayoutInflater inflater = getLayoutInflater();
        View dView = inflater.inflate(R.layout.dialog_add_budget, null);

        // Ánh xạ EditText trong Dialog
        EditText edt = dView.findViewById(R.id.edtBudgetAmount);

        builder.setView(dView);
        builder.setTitle("Cài đặt ngân sách tháng này");

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String s = edt.getText().toString().trim();

            if (TextUtils.isEmpty(s)) {
                Toast.makeText(getContext(), "Vui lòng nhập số tiền!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // Parse số tiền và lưu vào DB
                double amount = Double.parseDouble(s);

                // Gọi hàm setBudget trong DatabaseHelper 5 bảng
                db.setBudget(amount);

                // Load lại giao diện ngay lập tức
                loadData();

                Toast.makeText(getContext(), "Đã cập nhật ngân sách!", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}