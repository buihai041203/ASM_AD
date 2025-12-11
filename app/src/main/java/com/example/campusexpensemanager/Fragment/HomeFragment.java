package com.example.campusexpensemanager.Fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

// Import các lớp Chart cần thiết từ thư viện MPAndroidChart
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import com.example.campusexpensemanager.R;
import com.example.campusexpensemanager.dao.BudgetDAO;
import com.example.campusexpensemanager.dao.ExpenseDAO;
import com.example.campusexpensemanager.model.ExpenseCategoryTotal;
import com.example.campusexpensemanager.model.Fixedcosts;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class HomeFragment extends Fragment {

    // Ánh xạ theo ID MỚI từ fragment_home.xml
    private TextView tvWelcomeUser, tvTotalBudget, tvTotalExpense, tvBudgetRemainingHome;
    private PieChart pieChart;
    // Nút Xuất Báo cáo (Hiện tại chưa cần logic)
    // private Button btnExportReport;

    private ExpenseDAO expenseDAO;
    private BudgetDAO budgetDAO;
    private DecimalFormat df = new DecimalFormat("#,### đ");
    private String currentMonth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 1. ÁNH XẠ CÁC THÀNH PHẦN (Đã sửa ID để khớp với XML)
        tvWelcomeUser = view.findViewById(R.id.tvWelcomeUser);
        tvTotalBudget = view.findViewById(R.id.tvTotalBudget); // Thay thế tvBudgetInitial
        tvTotalExpense = view.findViewById(R.id.tvTotalExpense); // Bổ sung mới
        tvBudgetRemainingHome = view.findViewById(R.id.tvBudgetRemainingHome); // Thay thế tvBudgetRemaining
        pieChart = view.findViewById(R.id.pieChart);
        // btnExportReport = view.findViewById(R.id.btnExportReport);

        expenseDAO = new ExpenseDAO(getContext());
        budgetDAO = new BudgetDAO(getContext());
        currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());

        // 2. Tải và hiển thị dữ liệu
        loadUserData();
        loadFinancialSummary();
        setupPieChart();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFinancialSummary(); // Đảm bảo cập nhật khi quay lại
    }

    private void loadUserData() {
        // TODO: Lấy tên người dùng từ SharedPreferences/Session hoặc truy vấn UserDAO
        String userName = "Người dùng"; // Giá trị mặc định
        // Ví dụ: Lấy tên từ SharedPreferences
        // SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        // String hoTen = prefs.getString("HO_TEN", "Người dùng");

        tvWelcomeUser.setText("Xin chào, " + userName);
    }

    private void loadFinancialSummary() {
        // 1. Lấy dữ liệu Ngân sách và Chi tiêu
        Fixedcosts budget = budgetDAO.getOrCreateCurrentBudget();
        double totalExpense = expenseDAO.getTotalByMonth(currentMonth);

        // 2. Hiển thị Tổng quan lên TextView
        double remaining = budget.getSoTienConLai();

        tvTotalBudget.setText(df.format(budget.getSoTienDuKien()));
        tvTotalExpense.setText(df.format(totalExpense));
        tvBudgetRemainingHome.setText(df.format(remaining));

        // Đổi màu Số tiền còn lại
        if (remaining < 0) {
            tvBudgetRemainingHome.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        } else {
            tvBudgetRemainingHome.setTextColor(getResources().getColor(android.R.color.white)); // Màu trắng trong header
        }

        // 3. Cập nhật biểu đồ
        updatePieChartData(budget, totalExpense);
    }

    private void setupPieChart() {
        pieChart.setDrawEntryLabels(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.animateY(1000);
    }

    private void updatePieChartData(Fixedcosts budget, double totalExpense) {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        // 1. Lấy dữ liệu chi tiêu theo danh mục
        List<ExpenseCategoryTotal> categoryTotals = expenseDAO.getCategoryTotalsByMonth(currentMonth);

        // 2. Thêm dữ liệu chi tiêu (sử dụng màu ngẫu nhiên)
        for (ExpenseCategoryTotal total : categoryTotals) {
            if (total.getTotalAmount() > 0) {
                entries.add(new PieEntry((float) total.getTotalAmount(), total.getCategoryName()));
                colors.add(generateRandomColor());
            }
        }

        // 3. Thêm phần "Số tiền còn lại"
        double remaining = budget.getSoTienConLai();
        double initialBudget = budget.getSoTienDuKien();

        if (initialBudget > 0 && remaining > 0) {
            // Chỉ thêm lát cắt "Còn lại" nếu có ngân sách và số dư còn dương
            entries.add(new PieEntry((float) remaining, "Còn lại"));
            colors.add(Color.parseColor("#4CAF50")); // Màu xanh lá cây cố định
        }

        if (entries.isEmpty()) {
            // Trường hợp không có dữ liệu chi tiêu và ngân sách
            entries.add(new PieEntry(1f, "Chưa có giao dịch"));
            colors.add(Color.GRAY);
        }

        PieDataSet dataSet = new PieDataSet(entries, "Chi tiêu theo Danh mục");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(2f); // Khoảng cách giữa các lát cắt

        // Định dạng giá trị hiển thị trên biểu đồ
        dataSet.setValueFormatter(new ValueFormatter() {
            private DecimalFormat formatter = new DecimalFormat("#,###");
            @Override
            public String getFormattedValue(float value) {
                return formatter.format(value) + " đ"; // Thêm đơn vị vào giá trị
            }
        });

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    private int generateRandomColor() {
        // Hàm tạo màu ngẫu nhiên nhưng đảm bảo không quá tối
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(200) + 55, rnd.nextInt(200) + 55, rnd.nextInt(200) + 55);
    }
}