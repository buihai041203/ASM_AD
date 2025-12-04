package com.example.campusexpensemanager;

import android.os.Bundle;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    DatabaseHelper db;

    // Header
    TextView tvUsername, tvInitialBudget, tvTotalExpense, tvBalance;

    // Pie Chart
    Spinner spMonthPie, spYearPie;
    PieChart pieChart;

    // Daily Filter
    Spinner spDay, spMonth, spYear;
    RecyclerView rvDailyExpenses;
    TransactionAdapter adapter;

    List<Transaction> allTransactions = new ArrayList<>();

    DecimalFormat formatter = new DecimalFormat("#,### đ");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        db = new DatabaseHelper(this);

        // ---------------- ÁNH XẠ VIEW ----------------
        tvUsername = findViewById(R.id.tvUsername);
        tvInitialBudget = findViewById(R.id.tvInitialBudget);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvBalance = findViewById(R.id.tvBalance);

        spMonthPie = findViewById(R.id.spMonthPie);
        spYearPie = findViewById(R.id.spYearPie);

        spDay = findViewById(R.id.spDay);
        spMonth = findViewById(R.id.spMonth);
        spYear = findViewById(R.id.spYear);

        pieChart = findViewById(R.id.pieChart);

        rvDailyExpenses = findViewById(R.id.rvDailyExpenses);
        rvDailyExpenses.setLayoutManager(new LinearLayoutManager(this));

        // ---------------- LOAD DATA ----------------
        allTransactions = db.getAllTransactions();
        loadHeaderData();
        loadMonthYearForPieChart();
        loadDayMonthYearFilter();
        loadPieChart();
        loadDailyList();
    }

    // ---------------- HEADER ----------------
    private void loadHeaderData() {
        String username = "User"; // tùy bạn xử lý login
        tvUsername.setText("Hello, " + username);

        double init = db.getTotalBudget();
        double total = db.getTotalExpense();
        double balance = init - total;

        tvInitialBudget.setText("Chi phí ban đầu: " + formatter.format(init));
        tvTotalExpense.setText("Tổng chi tiêu: " + formatter.format(total));
        tvBalance.setText("Số dư: " + formatter.format(balance));
    }

    // ---------------- PIECHART FILTER (MONTH/YEAR) ----------------
    private void loadMonthYearForPieChart() {
        Integer[] months = new Integer[12];
        for (int i = 0; i < 12; i++) months[i] = i + 1;

        spMonthPie.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, months));

        Integer[] years = new Integer[6];
        int current = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 0; i < 6; i++) years[i] = current - 3 + i;

        spYearPie.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, years));

        spMonthPie.setSelection(Calendar.getInstance().get(Calendar.MONTH));
        spYearPie.setSelection(3);

        // reload piechart khi user thay đổi
        spMonthPie.setOnItemSelectedListener(new SimpleSelector(() -> loadPieChart()));
        spYearPie.setOnItemSelectedListener(new SimpleSelector(() -> loadPieChart()));
    }

    // ---------------- PIECHART LOAD DATA ----------------
    private void loadPieChart() {
        int month = (int) spMonthPie.getSelectedItem();
        int year = (int) spYearPie.getSelectedItem();

        List<PieEntry> entries = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        List<Float> amounts = new ArrayList<>();

        for (Transaction t : allTransactions) {
            if (matchMonthYear(t.getDate(), month, year)) {
                int index = categories.indexOf(t.getCategory());
                if (index >= 0) {
                    amounts.set(index, amounts.get(index) + (float) t.getAmount());
                } else {
                    categories.add(t.getCategory());
                    amounts.add((float) t.getAmount());
                }
            }
        }

        for (int i = 0; i < categories.size(); i++) {
            entries.add(new PieEntry(amounts.get(i), categories.get(i)));
        }

        PieDataSet set = new PieDataSet(entries, "Thống kê theo danh mục");
        PieData data = new PieData(set);

        pieChart.setData(data);
        pieChart.invalidate();
    }

    private boolean matchMonthYear(String date, int m, int y) {
        String[] parts = date.split("/");
        int mm = Integer.parseInt(parts[1]);
        int yy = Integer.parseInt(parts[2]);
        return (mm == m && yy == y);
    }

    // ---------------- DAILY LIST FILTER ----------------
    private void loadDayMonthYearFilter() {
        Integer[] days = new Integer[31];
        for (int i = 0; i < 31; i++) days[i] = i + 1;
        spDay.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, days));

        Integer[] months = new Integer[12];
        for (int i = 0; i < 12; i++) months[i] = i + 1;
        spMonth.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, months));

        Integer[] years = new Integer[6];
        int current = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 0; i < 6; i++) years[i] = current - 3 + i;
        spYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, years));

        spDay.setSelection(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - 1);
        spMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH));
        spYear.setSelection(3);

        // reload daily list khi user thay đổi
        spDay.setOnItemSelectedListener(new SimpleSelector(this::loadDailyList));
        spMonth.setOnItemSelectedListener(new SimpleSelector(this::loadDailyList));
        spYear.setOnItemSelectedListener(new SimpleSelector(this::loadDailyList));
    }

    // ---------------- LOAD DAILY LIST ----------------
    private void loadDailyList() {
        int d = (int) spDay.getSelectedItem();
        int m = (int) spMonth.getSelectedItem();
        int y = (int) spYear.getSelectedItem();

        String dateStr = String.format("%02d/%02d/%04d", d, m, y);

        List<Transaction> filtered = new ArrayList<>();
        for (Transaction t : allTransactions) {
            if (t.getDate().equals(dateStr)) filtered.add(t);
        }

        adapter = new TransactionAdapter(filtered, transaction -> {
            // callback nếu bạn muốn mở EditActivity
        });

        rvDailyExpenses.setAdapter(adapter);
    }

    // ---------------- SIMPLE LISTENER ----------------
    private static class SimpleSelector implements android.widget.AdapterView.OnItemSelectedListener {
        Runnable callback;

        SimpleSelector(Runnable cb) {
            this.callback = cb;
        }

        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
            callback.run();
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) { }
    }
}
