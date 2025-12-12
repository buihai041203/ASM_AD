package com.example.campusexpensemanager.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import com.example.campusexpensemanager.DatabaseHelper; // Thêm import DatabaseHelper
import com.example.campusexpensemanager.R;
import com.example.campusexpensemanager.dao.BudgetDAO;
import com.example.campusexpensemanager.model.Fixedcosts;
import com.example.campusexpensemanager.LoginActivity;
import com.example.campusexpensemanager.dao.ExpenseDAO;
import java.text.DecimalFormat;

public class SettingsFragment extends Fragment {

    private TextView tvBudgetCurrent, tvBudgetRemaining;
    private Button btnSetBudget, btnLogout;
    private Button btnResetAllExpenseData; // <-- Nút mới
    private BudgetDAO budgetDAO;
    private ExpenseDAO expenseDAO;
    private DatabaseHelper dbHelper; // <-- Khai báo DatabaseHelper
    private DecimalFormat df = new DecimalFormat("#,### đ");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Khởi tạo DAO và Helper
        budgetDAO = new BudgetDAO(requireContext());
        expenseDAO = new ExpenseDAO(requireContext());
        dbHelper = new DatabaseHelper(requireContext()); // <-- Khởi tạo DatabaseHelper

        // Ánh xạ các thành phần Ngân sách
        tvBudgetCurrent = view.findViewById(R.id.tvBudgetCurrent);
        tvBudgetRemaining = view.findViewById(R.id.tvBudgetRemaining);
        btnSetBudget = view.findViewById(R.id.btnSetBudget);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Ánh xạ nút Reset dữ liệu chi phí mới
        btnResetAllExpenseData = view.findViewById(R.id.btnResetAllExpenseData); // <-- Cần thêm ID này vào XML

        loadCurrentBudget();

        btnSetBudget.setOnClickListener(v -> showSetBudgetDialog());
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        // --- LOGIC XỬ LÝ SỰ KIỆN RESET TỔNG THỂ CHI PHÍ ---
        btnResetAllExpenseData.setOnClickListener(v -> showResetAllExpenseDataConfirmationDialog());

        return view;
    }

    // =======================================================
    // ======== PHƯƠNG THỨC RESET TỔNG THỂ CHI PHÍ ========
    // =======================================================
    private void showResetAllExpenseDataConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("⚠️ XÁC NHẬN XÓA DỮ LIỆU CHI PHÍ")
                .setMessage("Bạn có chắc chắn muốn xóa TOÀN BỘ Chi tiêu, Chi phí cố định và khôi phục Danh mục Chi phí về mặc định không? Hành động này không thể hoàn tác.")
                .setPositiveButton("XÓA HẾT", (dialog, which) -> {
                    resetAllExpenseDataAndRefresh();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void resetAllExpenseDataAndRefresh() {
        try {
            // 1. Gọi hàm reset trong DatabaseHelper
            dbHelper.resetAllExpenseData();

            Toast.makeText(requireContext(), "Đã xóa toàn bộ dữ liệu Chi phí và khôi phục Danh mục!", Toast.LENGTH_LONG).show();

            // 2. Khởi động lại Activity để cập nhật giao diện (Ngân sách, Báo cáo, Chi tiêu)
            Intent intent = requireActivity().getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finish();

        } catch (Exception e) {
            Log.e("SettingsFragment", "Lỗi khi reset dữ liệu chi phí: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi khi reset: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    // =======================================================


    // --- LOGIC ĐĂNG XUẤT --- (Giữ nguyên)
    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi ứng dụng không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performLogout() {
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.remove("USERNAME");
            editor.apply();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            getActivity().finish();
        }
    }
    // --- KẾT THÚC LOGIC ĐĂNG XUẤT ---


    // 1. Tải và hiển thị Ngân sách tháng hiện tại (Giữ nguyên)
    private void loadCurrentBudget() {
        Fixedcosts currentBudget = budgetDAO.getOrCreateCurrentBudget();

        if (currentBudget != null) {
            String currentMonth = budgetDAO.getOrCreateCurrentBudget().getThangNam();
            double variableExpense = expenseDAO.getTotalByMonth(currentMonth);
            double totalFixedCosts = budgetDAO.getTotalFixedCosts();

            double soTienConLaiTinhToan = currentBudget.getSoTienDuKien() - (variableExpense + totalFixedCosts);

            String duKien = df.format(currentBudget.getSoTienDuKien());
            String conLai = df.format(soTienConLaiTinhToan);

            tvBudgetCurrent.setText("Ngân sách dự kiến: " + duKien);
            tvBudgetRemaining.setText("Số tiền còn lại: " + conLai);

            if (soTienConLaiTinhToan < 0) {
                tvBudgetRemaining.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                tvBudgetRemaining.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }
        }
    }

    // 2. Hiển thị hộp thoại để người dùng nhập/cập nhật Ngân sách (Giữ nguyên)
    private void showSetBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Thiết lập Ngân sách Tháng");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText edtAmount = new EditText(requireContext());
        edtAmount.setHint("Nhập số tiền Ngân sách dự kiến");
        edtAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(edtAmount);

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
                Toast.makeText(requireContext(), "Vui lòng nhập số tiền!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // 3. Xử lý cập nhật Ngân sách (Giữ nguyên)
    private void updateBudget(double newAmount) {
        String currentMonth = budgetDAO.getOrCreateCurrentBudget().getThangNam();

        if (budgetDAO.updateBudget(currentMonth, newAmount)) {
            Toast.makeText(requireContext(), "Ngân sách tháng đã được cập nhật!", Toast.LENGTH_SHORT).show();
            loadCurrentBudget();
        } else {
            Toast.makeText(requireContext(), "Lỗi khi cập nhật Ngân sách!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCurrentBudget();
    }
}