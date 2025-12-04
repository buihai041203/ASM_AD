package com.example.campusexpensemanager;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    private Spinner spinnerMonth, spinnerYear;
    private Button btnGenerate;
    private TextView txtTotalExpense, txtBalance;
    private RecyclerView recyclerCategory, recyclerTransaction;
    private DatabaseHelper db;
    private DecimalFormat moneyFormat = new DecimalFormat("#,### đ");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        db = new DatabaseHelper(this);

        // 1. Ánh xạ View
        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        btnGenerate = findViewById(R.id.btnGenerateReport);
        txtTotalExpense = findViewById(R.id.txtTotalExpense);
        txtBalance = findViewById(R.id.txtBalance);
        recyclerCategory = findViewById(R.id.recyclerCategoryReport);
        recyclerTransaction = findViewById(R.id.recyclerTransactionReport);

        // 2. Setup RecyclerView
        recyclerCategory.setLayoutManager(new LinearLayoutManager(this));
        recyclerTransaction.setLayoutManager(new LinearLayoutManager(this));

        // 3. Setup Spinner & Load Data
        setupSpinners();
        loadReport(); // Load lần đầu khi mở màn hình

        // 4. Sự kiện nút Xem
        btnGenerate.setOnClickListener(v -> loadReport());
    }

    private void setupSpinners() {
        Integer[] months = new Integer[12];
        for (int i = 0; i < 12; i++) months[i] = i + 1;

        // Tạo danh sách năm động (từ năm hiện tại - 2 đến + 2)
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<Integer> years = new ArrayList<>();
        for (int i = currentYear - 2; i <= currentYear + 2; i++) {
            years.add(i);
        }

        spinnerMonth.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, months));
        spinnerYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years));

        // Mặc định chọn tháng/năm hiện tại
        Calendar c = Calendar.getInstance();
        // Calendar.MONTH trả về 0-11, trùng khớp với index của mảng months
        spinnerMonth.setSelection(c.get(Calendar.MONTH));
        // Giả sử năm hiện tại nằm ở vị trí index 2
        spinnerYear.setSelection(2);
    }

    private void loadReport() {
        int selectedMonth = Integer.parseInt(spinnerMonth.getSelectedItem().toString());
        int selectedYear = Integer.parseInt(spinnerYear.getSelectedItem().toString());

        // Key lọc: YYYY-MM (Ví dụ: 2025-12)
        String filterKey = String.format("%d-%02d", selectedYear, selectedMonth);

        // --- BƯỚC 1: Lấy dữ liệu từ DatabaseHelper (Tối ưu hóa) ---

        // 1.1. Lấy danh sách giao dịch chi tiết theo tháng
        // (Sử dụng hàm getTransactionsByMonth mới)
        List<Transaction> filteredList = db.getTransactionsByMonth(filterKey);

        // 1.2. Lấy tổng chi tiêu theo danh mục (Đã gom nhóm)
        // (Sử dụng hàm getMonthlyExpensesByCategory đã có/được thêm)
        Map<String, Double> categoryMap = db.getMonthlyExpensesByCategory(filterKey);

        // 1.3. Lấy ngân sách và tính tổng chi tiêu
        double budget = db.getBudgetByMonth(filterKey);
        // Tính tổng chi tiêu từ categoryMap (tối ưu hơn là gọi hàm DB khác)
        double totalExpense = 0;
        for (Double amount : categoryMap.values()) {
            totalExpense += amount;
        }

        // --- BƯỚC 2: Hiển thị Tổng chi & Số dư ---
        double balance = budget - totalExpense;

        txtTotalExpense.setText("Tổng chi tiêu: " + moneyFormat.format(totalExpense));
        txtBalance.setText("Số dư: " + moneyFormat.format(balance));

        // --- BƯỚC 3: Hiển thị List Giao dịch (Chi tiết) ---
        // Sử dụng dữ liệu filteredList đã truy vấn trực tiếp từ DB
        TransactionAdapter tranAdapter = new TransactionAdapter(filteredList, null);
        recyclerTransaction.setAdapter(tranAdapter);

        // --- BƯỚC 4: Hiển thị List Danh mục (Tổng hợp) ---
        // Chuyển categoryMap sang list CategoryReport
        List<CategoryReport> categoryReports = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            // Giả định CategoryReport có constructor(String categoryName, double totalAmount)
            categoryReports.add(new CategoryReport(entry.getKey(), entry.getValue()));
        }

        // Giả định CategoryReportAdapter tồn tại
        CategoryReportAdapter catAdapter = new CategoryReportAdapter(categoryReports);
        recyclerCategory.setAdapter(catAdapter);
    }
}