package com.example.campusexpensemanager.Fragment;

import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.campusexpensemanager.DatabaseHelper;
import com.example.campusexpensemanager.R;
import com.example.campusexpensemanager.dao.BudgetDAO;
import com.example.campusexpensemanager.model.Fixedcosts;

import java.text.DecimalFormat;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private TextView tvBudgetCurrent, tvBudgetRemaining;
    private Button btnSetBudget;
    private BudgetDAO budgetDAO;
    private DecimalFormat df = new DecimalFormat("#,### đ");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Khởi tạo DAO
        budgetDAO = new BudgetDAO(getContext());

        // Ánh xạ các thành phần Ngân sách (Giả định bạn đã thêm chúng vào fragment_settings.xml)
        // Nếu chưa có, bạn cần thêm 2 TextView và 1 Button vào fragment_settings.xml
        tvBudgetCurrent = view.findViewById(R.id.tvBudgetCurrent);
        tvBudgetRemaining = view.findViewById(R.id.tvBudgetRemaining);
        btnSetBudget = view.findViewById(R.id.btnSetBudget);

        loadCurrentBudget();

        btnSetBudget.setOnClickListener(v -> showSetBudgetDialog());

        return view;
    }

    // 1. Tải và hiển thị Ngân sách tháng hiện tại
    private void loadCurrentBudget() {
        Fixedcosts currentBudget = budgetDAO.getOrCreateCurrentBudget();

        if (currentBudget != null) {
            String duKien = df.format(currentBudget.getSoTienDuKien());
            String conLai = df.format(currentBudget.getSoTienConLai());

            tvBudgetCurrent.setText("Ngân sách dự kiến: " + duKien);

            // Đổi màu sắc số dư còn lại để dễ theo dõi
            tvBudgetRemaining.setText("Số tiền còn lại: " + conLai);
            if (currentBudget.getSoTienConLai() < 0) {
                tvBudgetRemaining.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                tvBudgetRemaining.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }
        }
    }

    // 2. Hiển thị hộp thoại để người dùng nhập/cập nhật Ngân sách
    private void showSetBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thiết lập Ngân sách Tháng");

        // Tạo giao diện nhập liệu
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText edtAmount = new EditText(getContext());
        edtAmount.setHint("Nhập số tiền Ngân sách dự kiến");
        edtAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(edtAmount);

        // Lấy Ngân sách cũ để hiển thị trong EditText (nếu có)
        Fixedcosts currentBudget = budgetDAO.getOrCreateCurrentBudget();
        if (currentBudget != null && currentBudget.getSoTienDuKien() > 0) {
            edtAmount.setText(String.valueOf((int) currentBudget.getSoTienDuKien()));
        }

        builder.setView(layout);

        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String amountStr = edtAmount.getText().toString().trim();
            if (!amountStr.isEmpty()) {
                double newAmount = Double.parseDouble(amountStr);
                updateBudget(newAmount);
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập số tiền!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // 3. Xử lý cập nhật Ngân sách
    private void updateBudget(double newAmount) {
        String currentMonth = budgetDAO.getOrCreateCurrentBudget().getThangNam();

        if (budgetDAO.updateBudget(currentMonth, newAmount)) {
            Toast.makeText(getContext(), "Ngân sách tháng đã được cập nhật!", Toast.LENGTH_SHORT).show();
            loadCurrentBudget(); // Tải lại để hiển thị số dư mới
        } else {
            Toast.makeText(getContext(), "Lỗi khi cập nhật Ngân sách!", Toast.LENGTH_SHORT).show();
        }
    }

    // Khi Fragment được hiển thị lại, tải lại ngân sách để đảm bảo dữ liệu mới nhất
    @Override
    public void onResume() {
        super.onResume();
        loadCurrentBudget();
    }
}