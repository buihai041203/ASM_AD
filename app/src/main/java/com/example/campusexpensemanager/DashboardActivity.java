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
    private List<Transaction> allTransactions;

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
                updateDashboardStats(); // Tính toán lại số liệu
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
                updateDailyList(); // Load lại list
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
        // Load lại dữ liệu mỗi khi quay lại màn hình này (VD: vừa thêm mới xong)
        allTransactions = db.getAllTransactions();
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
        spMonth.setSelection(c.get(Calendar.MONTH));
        spYear.setSelection(2); // Giả sử năm hiện tại nằm ở vị trí thứ 2 (index 2)

        spMonthPie.setSelection(c.get(Calendar.MONTH));
        spYearPie.setSelection(2);
    }

    // --- LOGIC 1: CẬP NHẬT THỐNG KÊ & PIE CHART ---
    private void updateDashboardStats() {
        if (allTransactions == null) return;

        int selectedMonth = Integer.parseInt(spMonthPie.getSelectedItem().toString());
        int selectedYear = Integer.parseInt(spYearPie.getSelectedItem().toString());
        String monthKey = String.format("%d-%02d", selectedYear, selectedMonth); // Format: YYYY-MM

        double totalExpense = 0;
        List<PieEntry> entries = new ArrayList<>();

        // 1. Tính tổng chi tiêu của tháng đang chọn & gom nhóm cho PieChart
        // Logic: Duyệt qua tất cả transaction, cái nào khớp tháng/năm thì cộng vào
        // (Đây là cách đơn giản nhất, với dữ liệu nhỏ < 1000 dòng thì không lo chậm)

        // Dùng Map tạm để gom nhóm theo Category
        java.util.HashMap<String, Double> categoryMap = new java.util.HashMap<>();

        for (Transaction t : allTransactions) {
            // Cắt chuỗi ngày YYYY-MM-DD lấy YYYY-MM
            if (t.getDate().startsWith(monthKey)) {
                totalExpense += t.getAmount();

                // Cộng dồn theo danh mục
                double currentVal = categoryMap.getOrDefault(t.getCategory(), 0.0);
                categoryMap.put(t.getCategory(), currentVal + t.getAmount());
            }
        }

        // 2. Tạo dữ liệu cho PieChart
        for (String cat : categoryMap.keySet()) {
            entries.add(new PieEntry(categoryMap.get(cat).floatValue(), cat));
        }

        // 3. Lấy ngân sách từ DB
        double budget = db.getBudgetByMonth(monthKey);
        double balance = budget - totalExpense;

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
            pieChart.clear(); // Xóa biểu đồ nếu không có dữ liệu
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

        // Cấu hình giao diện biểu đồ (Màu trắng cho Dark Mode)
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setCenterText("Chi Tiêu");
        pieChart.setCenterTextColor(Color.BLACK); // Tâm biểu đồ màu đen cho dễ đọc trên nền trắng
        pieChart.setHoleColor(Color.WHITE);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setTextColor(Color.WHITE);

        pieChart.animateY(1000);
        pieChart.invalidate(); // Refresh
    }

    // --- LOGIC 2: CẬP NHẬT LIST THEO NGÀY ---
    private void updateDailyList() {
        if (allTransactions == null) return;

        int d = Integer.parseInt(spDay.getSelectedItem().toString());
        int m = Integer.parseInt(spMonth.getSelectedItem().toString());
        int y = Integer.parseInt(spYear.getSelectedItem().toString());

        // Tạo chuỗi ngày đúng định dạng DB: YYYY-MM-DD
        String targetDate = String.format("%d-%02d-%02d", y, m, d);

        List<Transaction> filteredList = new ArrayList<>();
        for (Transaction t : allTransactions) {
            if (t.getDate().equals(targetDate)) {
                filteredList.add(t);
            }
        }

        // Đổ vào RecyclerView
        adapter = new TransactionAdapter(filteredList, transaction -> {
            // Khi click vào item -> Mở EditActivity
            Intent intent = new Intent(DashboardActivity.this, EditActivity.class);
            intent.putExtra("transaction_data", transaction);
            startActivity(intent);
        });
        rvDailyExpenses.setAdapter(adapter);
    }
}