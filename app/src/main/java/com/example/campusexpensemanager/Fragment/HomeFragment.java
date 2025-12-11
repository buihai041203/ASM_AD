package com.example.campusexpensemanager.Fragment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.campusexpensemanager.DatabaseHelper;
import com.example.campusexpensemanager.R;
import com.example.campusexpensemanager.dao.ExpenseDAO;
import com.example.campusexpensemanager.model.Expense;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvRemaining, tvMonthTitle;
    private ListView listViewExpenses;
    private DatabaseHelper dbHelper;
    private ExpenseDAO expenseDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvRemaining = view.findViewById(R.id.tvRemainingMoney);
        tvMonthTitle = view.findViewById(R.id.tvMonthTitle);
        listViewExpenses = view.findViewById(R.id.listViewExpenses);

        dbHelper = new DatabaseHelper(getContext());
        expenseDAO = new ExpenseDAO(getContext());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        try {
            // 1. Lấy tháng hiện tại
            String currentMonthKey = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
            tvMonthTitle.setText("Tháng " + new SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(new Date()));

            // 2. Tính toán số dư (Ngân sách - Đã chi)
            double totalSpent = expenseDAO.getTotalByMonth(currentMonthKey);
            double totalBudget = 0;

            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(DatabaseHelper.TABLE_NGAN_SACH,
                    new String[]{DatabaseHelper.NS_SO_TIEN_DU_KIEN},
                    DatabaseHelper.NS_THANG_NAM + "=?",
                    new String[]{currentMonthKey}, null, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    totalBudget = cursor.getDouble(0);
                }
                cursor.close();
            }

            // Hiển thị số dư
            DecimalFormat df = new DecimalFormat("#,###");
            tvRemaining.setText(df.format(totalBudget - totalSpent) + " đ");

            // 3. Hiển thị danh sách chi tiêu
            List<Expense> list = expenseDAO.getByMonth(currentMonthKey); // Biến list được khai báo ở đây

            List<String> displayList = new ArrayList<>();
            for (Expense e : list) {
                String name = (e.getGhiChu() != null && !e.getGhiChu().isEmpty()) ? e.getGhiChu() : e.getTenLoai();
                displayList.add(name + "\n" + df.format(e.getSoTien()) + " đ");
            }

            // Đổ dữ liệu vào ListView
            listViewExpenses.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, displayList));

            // --- QUAN TRỌNG: Sự kiện click phải nằm TRONG khối try để thấy biến 'list' ---
            listViewExpenses.setOnItemClickListener((parent, view, position, id) -> {
                // Lấy đối tượng Expense tương ứng với dòng được bấm
                Expense selectedExpense = list.get(position);

                // Mở màn hình EditExpenseFragment
                EditExpenseFragment fragment = new EditExpenseFragment();

                // Gửi ID sang màn hình Edit
                Bundle args = new Bundle();
                args.putInt("expense_id", selectedExpense.getId());
                fragment.setArguments(args);

                // Chuyển màn hình
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.containerHome, fragment)
                        .addToBackStack(null) // Cho phép ấn nút Back để quay lại
                        .commit();
            });
            // -----------------------------------------------------------------------------

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}