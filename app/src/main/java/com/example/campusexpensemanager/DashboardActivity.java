package com.example.campusexpensemanager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private TextView tvInitialBudget, tvTotalExpense, tvBalance, tvUsername;
    private Spinner spMonthPie, spYearPie, spDay, spMonth, spYear;
    private PieChart pieChart;
    private RecyclerView rvDailyExpenses;
    private TransactionAdapter adapter;
    // allTransactions không cần thiết nữa, có thể bỏ, nhưng tôi giữ lại ở trạng thái null để không gây lỗi logic
    // private List<Transaction> allTransactions; // BỎ DÒNG NÀY ĐI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        db = new DatabaseHelper(this);

        initViews();
        setupSpinners();

        // Sự kiện khi thay đổi bộ lọc biểu đồ (Tháng/Năm)
        AdapterView.OnItemSelectedListener pieFilterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Chỉ cần gọi updateDashboardStats() vì hàm này đã truy vấn DB trực tiếp
                updateDashboardStats();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        spMonthPie.setOnItemSelectedListener(pieFilterListener);
        spYearPie.setOnItemSelectedListener(pieFilterListener);

        // Sự kiện khi thay đổi bộ lọc danh sách (Ngày/Tháng/Năm)
        AdapterView.OnItemSelectedListener listFilterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Chỉ cần gọi updateDailyList() vì hàm này đã truy vấn DB trực tiếp
                updateDailyList();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        spDay.setOnItemSelectedListener(listFilterListener);
        spMonth.setOnItemSelectedListener(listFilterListener);
        spYear.setOnItemSelectedListener(listFilterListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Không cần load allTransactions nữa
        updateDashboardStats();
        updateDailyList();
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tvUsername);
        tvInitialBudget = findViewById(R.id.tvInitialBudget);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvBalance = findViewById(R.id.tvBalance);

        spMonthPie = findViewById(R.id.spMonthPie);
        spYearPie = findViewById(R.id.spYearPie);
        pieChart = findViewById(R.id.pieChart);

        spDay = findViewById(R.id.spDay);
        spMonth = findViewById(R.id.spMonth);
        spYear = findViewById(R.id.spYear);

        rvDailyExpenses = findViewById(R.id.rvDailyExpenses);
        rvDailyExpenses.setLayoutManager(new LinearLayoutManager(this));

        // Set tên user mặc định
        tvUsername.setText("Xin chào, Admin");
    }

    private void setupSpinners() {
        // Setup dữ liệu cho các Spinner (Ngày 1-31, Tháng 1-12, Năm 2023-2030)
        List<Integer> days = new ArrayList<>();
        for (int i = 1; i <= 31; i++) days.add(i);

        List<Integer> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) months.add(i);

        List<Integer> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        // Năm hiện tại nằm ở vị trí index 2 (currentYear - 2, currentYear - 1, currentYear, ...)
        for (int i = currentYear - 2; i <= currentYear + 5; i++) years.add(i);

        ArrayAdapter<Integer> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<Integer> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<Integer> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spDay.setAdapter(dayAdapter);
        spMonth.setAdapter(monthAdapter);
        spYear.setAdapter(yearAdapter);

        spMonthPie.setAdapter(monthAdapter);
        spYearPie.setAdapter(yearAdapter);

        // Mặc định chọn ngày hôm nay
        Calendar c = Calendar.getInstance();
        spDay.setSelection(c.get(Calendar.DAY_OF_MONTH) - 1);
        spMonth.setSelection(c.get(Calendar.MONTH)); // Calendar.MONTH trả về 0-11
        spYear.setSelection(2);

        spMonthPie.setSelection(c.get(Calendar.MONTH));
        spYearPie.setSelection(2);
    }

    // --- LOGIC 1: CẬP NHẬT THỐNG KÊ & PIE CHART (TỐI ƯU HÓA TRUY VẤN DB) ---
    private void updateDashboardStats() {
        // 1. Lấy khóa tháng/năm từ Spinner Pie Chart
        int selectedMonth = Integer.parseInt(spMonthPie.getSelectedItem().toString());
        int selectedYear = Integer.parseInt(spYearPie.getSelectedItem().toString());
        String monthKey = String.format("%d-%02d", selectedYear, selectedMonth); // Format: YYYY-MM

        // 2. Truy vấn DB để lấy tổng chi tiêu và ngân sách
        double totalExpense = db.getTotalExpenseByMonth(monthKey);
        double budget = db.getBudgetByMonth(monthKey);
        double balance = budget - totalExpense;

        // 3. Truy vấn DB để lấy dữ liệu gom nhóm cho PieChart
        java.util.HashMap<String, Double> categoryMap = db.getMonthlyExpensesByCategory(monthKey);

        List<PieEntry> entries = new ArrayList<>();
        for (String cat : categoryMap.keySet()) {
            entries.add(new PieEntry(categoryMap.get(cat).floatValue(), cat));
        }

        // 4. Hiển thị lên Header
        DecimalFormat df = new DecimalFormat("#,### đ");
        tvInitialBudget.setText("Ngân sách: " + df.format(budget));
        tvTotalExpense.setText(df.format(totalExpense));
        tvBalance.setText(df.format(balance));

        // Đổi màu số dư: Đỏ nếu âm, Xanh nếu dương
        tvBalance.setTextColor(balance < 0 ? Color.parseColor("#FF5252") : Color.parseColor("#69F0AE"));

        // 5. Vẽ PieChart
        drawPieChart(entries);
    }

    private void drawPieChart(List<PieEntry> entries) {
        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("Chưa có chi tiêu trong tháng này");
            pieChart.setNoDataTextColor(Color.WHITE);
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        // Cấu hình giao diện biểu đồ
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setCenterText("Chi Tiêu");
        pieChart.setCenterTextColor(Color.BLACK);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setTextColor(Color.WHITE);

        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    // --- LOGIC 2: CẬP NHẬT LIST THEO NGÀY (TỐI ƯU HÓA TRUY VẤN DB) ---
    private void updateDailyList() {
        // 1. Lấy ngày/tháng/năm từ Spinner List Filter
        int d = Integer.parseInt(spDay.getSelectedItem().toString());
        int m = Integer.parseInt(spMonth.getSelectedItem().toString());
        int y = Integer.parseInt(spYear.getSelectedItem().toString());

        // Tạo chuỗi ngày đúng định dạng DB: YYYY-MM-DD
        String targetDate = String.format("%d-%02d-%02d", y, m, d);

        // 2. Truy vấn DB để lấy danh sách giao dịch của ngày chỉ định
        List<Transaction> filteredList = db.getTransactionsByDate(targetDate);

        // 3. Đổ vào RecyclerView
        adapter = new TransactionAdapter(filteredList, transaction -> {
            // Khi click vào item -> Mở EditActivity
            Intent intent = new Intent(DashboardActivity.this, EditActivity.class);
            intent.putExtra("transaction_data", transaction);
            startActivity(intent);
        });
        rvDailyExpenses.setAdapter(adapter);
    }
}