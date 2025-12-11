package com.example.campusexpensemanager.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.campusexpensemanager.R;
import com.example.campusexpensemanager.adapter.ExpenseAdapter;
import com.example.campusexpensemanager.dao.ExpenseDAO;
import com.example.campusexpensemanager.model.Expense;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseFragment extends Fragment {

    private ListView listViewHistory;
    private FloatingActionButton fabAdd;
    private ExpenseDAO expenseDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense, container, false);

        listViewHistory = view.findViewById(R.id.listViewHistory);
        fabAdd = view.findViewById(R.id.fabAddExpense);
        expenseDAO = new ExpenseDAO(getContext());

        // Bấm nút thêm -> Mở AddExpenseFragment
        fabAdd.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.containerHome, new AddExpenseFragment()) // Lưu ý ID containerHome của Activity chính
                    .addToBackStack(null)
                    .commit();
        });

        // Sự kiện click vào item để sửa (dùng lại EditExpenseFragment bài trước)
        listViewHistory.setOnItemClickListener((parent, v, position, id) -> {
            Expense selected = (Expense) parent.getItemAtPosition(position);
            EditExpenseFragment editFrag = new EditExpenseFragment();
            Bundle args = new Bundle();
            args.putInt("expense_id", selected.getId());
            editFrag.setArguments(args);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.containerHome, editFrag)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        List<Expense> list = expenseDAO.getByMonth(currentMonth);

        // Dùng lại ExpenseAdapter bạn đã tạo ở các bước trước
        if (list != null) {
            ExpenseAdapter adapter = new ExpenseAdapter(getContext(), list);
            listViewHistory.setAdapter(adapter);
        }
    }
}