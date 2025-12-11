package com.example.campusexpensemanager.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.pdf.PdfDocument;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.os.Environment;
import androidx.core.content.ContextCompat;
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

    // Khai báo Launcher (Giữ lại cấu trúc hiện đại cho mục đích chung)
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo ActivityResultLauncher (Sử dụng requireContext() an toàn hơn)
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                // Quyền được cấp (Dù không cần cho thư mục riêng, vẫn là logic mẫu)
                Toast.makeText(requireContext(), "Quyền được cấp (Nếu cần).", Toast.LENGTH_SHORT).show();
                createPdfReport();
            } else {
                // Quyền bị từ chối
                Toast.makeText(requireContext(), "Không thể có quyền ghi công cộng.", Toast.LENGTH_SHORT).show();
            }
        });
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
        // Sử dụng requireContext() thay vì getContext() khi Fragment đã được attach
        expenseDAO = new ExpenseDAO(requireContext());
        budgetDAO = new BudgetDAO(requireContext());
        currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());

        // --- 3. TẢI VÀ HIỂN THỊ ---
        loadUserData();
        loadFinancialSummary();
        setupPieChart();

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
        // 1. Khởi tạo SharedPreferences
        // "UserPrefs" là tên file SharedPreferences (phải khớp với tên được dùng khi lưu dữ liệu)
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // 2. Lấy tên người dùng
        // Lấy giá trị từ key "USERNAME". Nếu không tìm thấy, sử dụng "Người dùng" làm giá trị mặc định.
        String email = prefs.getString("USERNAME", "Người dùng");

        // 3. Hiển thị lên TextView
        tvWelcomeUser.setText("Xin chào, " + email);
    }

    private void loadFinancialSummary() {
        Fixedcosts budget = budgetDAO.getOrCreateCurrentBudget();
        double totalExpense = expenseDAO.getTotalByMonth(currentMonth);

        double remaining = budget.getSoTienConLai();

        tvTotalBudget.setText(df.format(budget.getSoTienDuKien()));
        tvTotalExpense.setText(df.format(totalExpense));
        tvBudgetRemainingHome.setText(df.format(remaining));

        if (remaining < 0) {
            tvBudgetRemainingHome.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        } else {
            tvBudgetRemainingHome.setTextColor(getResources().getColor(android.R.color.white));
        }

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

        List<ExpenseCategoryTotal> categoryTotals = expenseDAO.getCategoryTotalsByMonth(currentMonth);

        for (ExpenseCategoryTotal total : categoryTotals) {
            if (total.getTotalAmount() > 0) {
                entries.add(new PieEntry((float) total.getTotalAmount(), total.getCategoryName()));
                colors.add(generateRandomColor());
            }
        }

        double remaining = budget.getSoTienConLai();
        double initialBudget = budget.getSoTienDuKien();

        if (initialBudget > 0 && remaining > 0) {
            entries.add(new PieEntry((float) remaining, "Còn lại"));
            colors.add(Color.parseColor("#4CAF50"));
        }

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

    private int generateRandomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(200) + 55, rnd.nextInt(200) + 55, rnd.nextInt(200) + 55);
    }

    // --- A. KIỂM TRA QUYỀN TRUY CẬP (Hiện tại chỉ gọi hàm tạo PDF) ---
    private void checkPermissionAndExport() {
        // Nếu bạn muốn yêu cầu quyền trong tương lai:
        // if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        //     createPdfReport();
        // } else {
        //     requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // }

        // Hiện tại, gọi trực tiếp vì đường dẫn lưu file không cần quyền
        createPdfReport();
    }


    // --- B. LOGIC TẠO VÀ GHI PDF (Sử dụng đường dẫn riêng của ứng dụng AN TOÀN) ---
    private void createPdfReport() {

        // 1. Thu thập dữ liệu
        Fixedcosts budget = budgetDAO.getOrCreateCurrentBudget();
        List<ExpenseCategoryTotal> categoryTotals = expenseDAO.getCategoryTotalsByMonth(currentMonth);

        // 2. Chuẩn bị Tài liệu PDF
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        int y = 50;
        int x = 50;

        // 3. VẼ NỘI DUNG (Bạn có thể thêm logic vẽ của mình ở đây)
        paint.setTextSize(20);
        paint.setFakeBoldText(true);
        canvas.drawText("BÁO CÁO CHI TIÊU THÁNG " + currentMonth, x, y, paint);
        // ... (Tiếp tục logic vẽ) ...

        // 4. Hoàn tất và Ghi File
        document.finishPage(page);

        try {
            // Lấy thư mục Documents riêng của ứng dụng
            File outputDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

            if (outputDir == null) {
                Toast.makeText(requireContext(), "Lỗi: Không thể truy cập bộ nhớ ngoài.", Toast.LENGTH_LONG).show();
                return;
            }

            // Tạo thư mục con 'Reports'
            File reportsFolder = new File(outputDir, "Reports");
            if (!reportsFolder.exists()) {
                reportsFolder.mkdirs();
            }

            // Tạo file trong thư mục riêng của ứng dụng
            String filename = "BaoCaoChiTieu_" + currentMonth + "_" + System.currentTimeMillis() + ".pdf";
            File file = new File(reportsFolder, filename);

            // Ghi nội dung vào file
            document.writeTo(new FileOutputStream(file));

            // Thông báo và đường dẫn lưu file
            Toast.makeText(requireContext(), "Đã xuất báo cáo thành công! Lưu tại: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Toast.makeText(requireContext(), "Lỗi khi ghi file PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            document.close();
        }
    }
}