package com.example.campusexpensemanager.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// Import cho Biểu đồ
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

// Import DAO và Model
import com.example.campusexpensemanager.R;
import com.example.campusexpensemanager.dao.BudgetDAO;
import com.example.campusexpensemanager.dao.ExpenseDAO;
import com.example.campusexpensemanager.model.ExpenseCategoryTotal;
import com.example.campusexpensemanager.model.Fixedcosts;

// Import cho PDF và Ghi File
import android.graphics.pdf.PdfDocument;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.Manifest; // Cần thiết cho Manifest.permission.POST_NOTIFICATIONS
import android.content.pm.PackageManager; // Cần thiết cho PackageManager.PERMISSION_GRANTED
import androidx.core.content.ContextCompat; // Cần thiết để kiểm tra quyền

public class HomeFragment extends Fragment {

    // Khai báo các thành phần UI
    private TextView tvWelcomeUser, tvTotalBudget, tvTotalExpense, tvBudgetRemainingHome;
    private PieChart pieChart;
    private Button btnExportReport;

    // Khai báo DAO và Công cụ
    private ExpenseDAO expenseDAO;
    private BudgetDAO budgetDAO;
    private DecimalFormat df = new DecimalFormat("#,### đ");
    private String currentMonth;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<String> requestNotificationPermissionLauncher; // <-- KHAI BÁO MỚI

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                createPdfReport();
            } else {
                Toast.makeText(requireContext(), "Không thể có quyền ghi công cộng.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Phương thức kiểm tra và yêu cầu quyền thông báo
    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Nếu chưa có quyền, yêu cầu quyền
                if (requestNotificationPermissionLauncher != null) {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
            }
        }
        // Với API < 33, quyền thông báo được cấp mặc định.
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // --- 1. ÁNH XẠ CÁC THÀNH PHẦN ---
        tvWelcomeUser = view.findViewById(R.id.tvWelcomeUser);
        tvTotalBudget = view.findViewById(R.id.tvTotalBudget);
        tvTotalExpense = view.findViewById(R.id.tvTotalExpense);
        tvBudgetRemainingHome = view.findViewById(R.id.tvBudgetRemainingHome);
        pieChart = view.findViewById(R.id.pieChart);
        btnExportReport = view.findViewById(R.id.btnExportReport);

        // --- 2. KHỞI TẠO DAO & DỮ LIỆU ---
        expenseDAO = new ExpenseDAO(requireContext());
        budgetDAO = new BudgetDAO(requireContext());
        currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());

        // --- 3. TẢI VÀ HIỂN THỊ ---
        loadUserData();
        loadFinancialSummary();
        setupPieChart();
        checkAndRequestNotificationPermission();

        // --- 4. THIẾT LẬP LISTENER ---
        btnExportReport.setOnClickListener(v -> checkPermissionAndExport());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFinancialSummary(); // Đảm bảo cập nhật khi quay lại
    }

    private void loadUserData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String email = prefs.getString("USERNAME", "Người dùng");
        tvWelcomeUser.setText("Xin chào, " + email);
    }

    // =======================================================
    // ======== PHƯƠNG THỨC SỬA ĐỔI: loadFinancialSummary ========
    // =======================================================
    private void loadFinancialSummary() {
        Fixedcosts budget = budgetDAO.getOrCreateCurrentBudget();

        // 1. Chi tiêu Biến đổi (thực tế đã chi từ bảng CHI_TIEU)
        double variableExpense = expenseDAO.getTotalByMonth(currentMonth);

        // 2. Chi phí Cố định (tổng các khoản phải chi)
        double totalFixedCosts = budgetDAO.getTotalFixedCosts();

        // 3. TÍNH TOÁN TỔNG CHI TIÊU THỰC TẾ (Đã chi + Phải chi)
        double totalExpenseActual = variableExpense + totalFixedCosts;

        // 4. TÍNH TOÁN LẠI SỐ DƯ CÒN LẠI (Sửa lỗi "chưa trừ" bằng cách tính lại ở client)
        // Số dư còn lại = Dự kiến - Tổng chi tiêu thực tế
        double remaining = budget.getSoTienDuKien() - totalExpenseActual;

        // 5. Cập nhật UI
        tvTotalBudget.setText(df.format(budget.getSoTienDuKien()));
        tvTotalExpense.setText(df.format(totalExpenseActual)); // Tổng chi phí bao gồm cả cố định
        tvBudgetRemainingHome.setText(df.format(remaining)); // Số tiền còn lại đã được trừ đúng

        if (remaining < 0) {
            tvBudgetRemainingHome.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        } else {
            tvBudgetRemainingHome.setTextColor(getResources().getColor(android.R.color.white));
        }

        // Cập nhật biểu đồ với Tổng Chi tiêu thực tế
        updatePieChartData(budget, remaining, variableExpense, totalFixedCosts);
    }
    // =======================================================
    // =======================================================


    private void setupPieChart() {
        pieChart.setDrawEntryLabels(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.animateY(1000);
    }

    // =======================================================
    // ======== PHƯƠNG THỨC SỬA ĐỔI: updatePieChartData ========
    // =======================================================
    private void updatePieChartData(Fixedcosts budget, double remaining, double variableExpense, double totalFixedCosts) {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        // 1. THÊM CHI PHÍ CỐ ĐỊNH (Theo yêu cầu)
        if (totalFixedCosts > 0) {
            entries.add(new PieEntry((float) totalFixedCosts, "Chi Phí Cố Định"));
            colors.add(Color.parseColor("#FF9800")); // Màu cam
        }

        // 2. THÊM CHI TIÊU BIẾN ĐỔI THEO DANH MỤC
        // Chi tiêu biến đổi là các khoản đã được ghi nhận trong CHI_TIEU
        List<ExpenseCategoryTotal> categoryTotals = expenseDAO.getCategoryTotalsByMonth(currentMonth);

        for (ExpenseCategoryTotal total : categoryTotals) {
            if (total.getTotalAmount() > 0) {
                entries.add(new PieEntry((float) total.getTotalAmount(), total.getCategoryName()));
                colors.add(generateRandomColor());
            }
        }

        // 3. THÊM SỐ TIỀN CÒN LẠI
        // Chỉ thêm nếu Ngân sách Dự kiến > 0 và số dư còn lại > 0
        if (budget.getSoTienDuKien() > 0 && remaining > 0) {
            entries.add(new PieEntry((float) remaining, "Còn lại"));
            colors.add(Color.parseColor("#4CAF50")); // Màu xanh lá
        }

        // Trường hợp không có giao dịch hoặc không có ngân sách
        if (entries.isEmpty()) {
            entries.add(new PieEntry(1f, "Chưa có giao dịch"));
            colors.add(Color.GRAY);
        }

        PieDataSet dataSet = new PieDataSet(entries, "Chi tiêu theo Danh mục");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(2f);

        dataSet.setValueFormatter(new ValueFormatter() {
            private DecimalFormat formatter = new DecimalFormat("#,###");
            @Override
            public String getFormattedValue(float value) {
                return formatter.format(value) + " đ";
            }
        });

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }
    // =======================================================
    // =======================================================

    private int generateRandomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(200) + 55, rnd.nextInt(200) + 55, rnd.nextInt(200) + 55);
    }

    private void checkPermissionAndExport() {
        createPdfReport();
    }


    // Trong HomeFragment.java

    private void createPdfReport() {

        // 1. Thu thập dữ liệu
        Fixedcosts budget = budgetDAO.getOrCreateCurrentBudget();
        List<ExpenseCategoryTotal> categoryTotals = expenseDAO.getCategoryTotalsByMonth(currentMonth);

        // Tính tổng chi tiêu thực tế (Đã chi + Cố định)
        double totalFixedCosts = budgetDAO.getTotalFixedCosts();
        double variableExpense = expenseDAO.getTotalByMonth(currentMonth);
        double totalExpenseActual = variableExpense + totalFixedCosts;
        double remaining = budget.getSoTienDuKien() - totalExpenseActual;

        // 2. Chuẩn bị Tài liệu PDF
        PdfDocument document = new PdfDocument();
        // Kích thước A4: 595pt x 842pt
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        int y = 50;
        int x = 50;
        final int LINE_HEIGHT = 25;
        final int HEADER_COLOR = Color.rgb(33, 150, 243); // Màu xanh dương
        final int DETAIL_COLOR = Color.DKGRAY;

        // 3. VẼ NỘI DUNG

        // --- TIÊU ĐỀ BÁO CÁO ---
        paint.setTextSize(24);
        paint.setFakeBoldText(true);
        paint.setColor(HEADER_COLOR);
        canvas.drawText("BÁO CÁO CHI TIÊU THÁNG " + currentMonth, x, y, paint);
        y += LINE_HEIGHT * 2;

        // --- TÓM TẮT NGÂN SÁCH ---
        paint.setTextSize(18);
        paint.setFakeBoldText(true);
        paint.setColor(Color.BLACK);
        canvas.drawText("I. TÓM TẮT TÀI CHÍNH", x, y, paint);
        y += LINE_HEIGHT;

        // Dòng Ngân sách Dự kiến
        paint.setTextSize(14);
        paint.setFakeBoldText(false);
        paint.setColor(DETAIL_COLOR);
        canvas.drawText("• Ngân sách Dự kiến:", x + 20, y, paint);
        canvas.drawText(df.format(budget.getSoTienDuKien()), 450, y, paint);
        y += LINE_HEIGHT;

        // Dòng Tổng Chi tiêu Thực tế
        paint.setFakeBoldText(false);
        canvas.drawText("• Tổng Chi tiêu Thực tế (Biến đổi + Cố định):", x + 20, y, paint);
        paint.setFakeBoldText(true);
        paint.setColor(Color.RED);
        canvas.drawText(df.format(totalExpenseActual), 450, y, paint);
        y += LINE_HEIGHT;

        // Dòng Số dư còn lại
        paint.setFakeBoldText(false);
        paint.setColor(DETAIL_COLOR);
        canvas.drawText("• Số dư còn lại:", x + 20, y, paint);
        paint.setFakeBoldText(true);
        paint.setColor(remaining >= 0 ? Color.rgb(76, 175, 80) : Color.RED); // Xanh nếu dương, Đỏ nếu âm
        canvas.drawText(df.format(remaining), 450, y, paint);
        y += LINE_HEIGHT * 2;


        // --- CHI TIẾT THEO DANH MỤC ---
        paint.setTextSize(18);
        paint.setFakeBoldText(true);
        paint.setColor(Color.BLACK);
        canvas.drawText("II. CHI TIẾT CHI TIÊU", x, y, paint);
        y += LINE_HEIGHT;

        // A. Chi phí Cố định (Thường là khoản lớn)
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        paint.setColor(Color.parseColor("#FF9800")); // Màu Cam
        canvas.drawText("A. Chi phí Cố định:", x + 10, y, paint);
        canvas.drawText(df.format(totalFixedCosts), 450, y, paint);
        y += LINE_HEIGHT;

        // B. Chi tiêu Biến đổi theo Danh mục
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        paint.setColor(HEADER_COLOR);
        canvas.drawText("B. Chi tiêu Biến đổi (Theo Danh mục):", x + 10, y, paint);
        y += LINE_HEIGHT;

        paint.setTextSize(14);
        paint.setFakeBoldText(false);
        paint.setColor(DETAIL_COLOR);

        if (categoryTotals.isEmpty()) {
            canvas.drawText("Không có chi tiêu biến đổi nào được ghi nhận.", x + 20, y, paint);
            y += LINE_HEIGHT;
        } else {
            // Vẽ tiêu đề cột
            paint.setFakeBoldText(true);
            canvas.drawText("Danh mục", x + 20, y, paint);
            canvas.drawText("Số tiền", 450, y, paint);
            y += LINE_HEIGHT;
            paint.setFakeBoldText(false);

            // Vẽ từng danh mục
            for (ExpenseCategoryTotal total : categoryTotals) {
                if (y > pageInfo.getPageHeight() - 50) { // Kiểm tra tràn trang
                    document.finishPage(page);
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 50; // Bắt đầu lại từ trên cùng
                }
                canvas.drawText("• " + total.getCategoryName(), x + 20, y, paint);
                canvas.drawText(df.format(total.getTotalAmount()), 450, y, paint);
                y += LINE_HEIGHT;
            }
        }


        // 4. Hoàn tất và Ghi File
        document.finishPage(page);

        try {
            // ... (Logic ghi file cũ, không thay đổi) ...
            File outputDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

            if (outputDir == null) {
                Toast.makeText(requireContext(), "Lỗi: Không thể truy cập bộ nhớ.", Toast.LENGTH_LONG).show();
                return;
            }

            File reportsFolder = new File(outputDir, "Reports");
            if (!reportsFolder.exists()) {
                reportsFolder.mkdirs();
            }

            String filename = "BaoCaoChiTieu_" + currentMonth + "_" + System.currentTimeMillis() + ".pdf";
            File file = new File(reportsFolder, filename);

            document.writeTo(new FileOutputStream(file));

            Toast.makeText(requireContext(), "Đã xuất báo cáo thành công! Lưu tại: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Toast.makeText(requireContext(), "Lỗi khi ghi file PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            document.close();
        }
    }
}