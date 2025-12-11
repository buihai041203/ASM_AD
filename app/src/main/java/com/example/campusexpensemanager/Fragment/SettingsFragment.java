package com.example.campusexpensemanager.Fragment;

import android.app.AlertDialog;
import android.content.Context; // Cần thiết cho SharedPreferences
import android.content.Intent; // Cần thiết cho chuyển hướng
import android.content.SharedPreferences; // Cần thiết cho lưu/xóa session
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.example.campusexpensemanager.R;
import com.example.campusexpensemanager.dao.BudgetDAO;
import com.example.campusexpensemanager.model.Fixedcosts;
import com.example.campusexpensemanager.LoginActivity; // Sử dụng LoginActivity làm trang đích đăng xuất

import java.text.DecimalFormat;

public class SettingsFragment extends Fragment {

    private TextView tvBudgetCurrent, tvBudgetRemaining;
    private Button btnSetBudget, btnLogout; // Đã khai báo btnLogout
    private BudgetDAO budgetDAO;
    private DecimalFormat df = new DecimalFormat("#,### đ");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Khởi tạo DAO
        // Sử dụng requireContext() để đảm bảo Context hợp lệ
        budgetDAO = new BudgetDAO(requireContext());

        // Ánh xạ các thành phần Ngân sách
        tvBudgetCurrent = view.findViewById(R.id.tvBudgetCurrent);
        tvBudgetRemaining = view.findViewById(R.id.tvBudgetRemaining);
        btnSetBudget = view.findViewById(R.id.btnSetBudget);

        // Ánh xạ nút Đăng xuất (Khớp với ID trong XML)
        btnLogout = view.findViewById(R.id.btnLogout);

        loadCurrentBudget();

        btnSetBudget.setOnClickListener(v -> showSetBudgetDialog());

        // --- LOGIC XỬ LÝ SỰ KIỆN ĐĂNG XUẤT ---
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        return view;
    }

    // --- LOGIC ĐĂNG XUẤT ---
    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext()) // Sử dụng requireContext()
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
            // 1. Xóa thông tin đăng nhập trong SharedPreferences ("UserPrefs" và KEY "USERNAME")
            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            // Xóa key "USERNAME" đã lưu khi đăng nhập
            editor.remove("USERNAME");

            editor.apply(); // Lưu thay đổi

            // 2. Chuyển hướng đến LoginActivity
            // Thay vì MainActivity, chuyển về LoginActivity để người dùng đăng nhập lại
            Intent intent = new Intent(getActivity(), LoginActivity.class);

            // Cờ quan trọng: Xóa tất cả Activity cũ trong stack
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            // 3. Kết thúc Activity chứa Fragment (ví dụ: HomeActivity)
            getActivity().finish();
        }
    }
    // --- KẾT THÚC LOGIC ĐĂNG XUẤT ---


    // 1. Tải và hiển thị Ngân sách tháng hiện tại
    private void loadCurrentBudget() {
        Fixedcosts currentBudget = budgetDAO.getOrCreateCurrentBudget();

        if (currentBudget != null) {
            String duKien = df.format(currentBudget.getSoTienDuKien());
            String conLai = df.format(currentBudget.getSoTienConLai());

            tvBudgetCurrent.setText("Ngân sách dự kiến: " + duKien);

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
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()); // Sử dụng requireContext()
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
                Toast.makeText(requireContext(), "Vui lòng nhập số tiền!", Toast.LENGTH_SHORT).show(); // Sử dụng requireContext()
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // 3. Xử lý cập nhật Ngân sách
    private void updateBudget(double newAmount) {
        String currentMonth = budgetDAO.getOrCreateCurrentBudget().getThangNam();

        if (budgetDAO.updateBudget(currentMonth, newAmount)) {
            Toast.makeText(requireContext(), "Ngân sách tháng đã được cập nhật!", Toast.LENGTH_SHORT).show(); // Sử dụng requireContext()
            loadCurrentBudget();
        } else {
            Toast.makeText(requireContext(), "Lỗi khi cập nhật Ngân sách!", Toast.LENGTH_SHORT).show(); // Sử dụng requireContext()
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCurrentBudget();
    }
}