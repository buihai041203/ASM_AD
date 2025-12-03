package com.example.campusexpensemanager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.example.campusexpensemanager.R;
import com.example.campusexpensemanager.DatabaseHelper;
import java.text.DecimalFormat;

public class BudgetFragment extends Fragment {
    private DatabaseHelper db;
    private TextView txtTotalBudget;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);
        db = new DatabaseHelper(getContext());

        txtTotalBudget = view.findViewById(R.id.txtTotalBudgetValue);

        view.findViewById(R.id.btnAddBudget).setOnClickListener(v -> showDialog());
        loadData();
        return view;
    }

    private void loadData() {
        double total = db.getTotalBudget();
        DecimalFormat df = new DecimalFormat("#,### đ");
        txtTotalBudget.setText(df.format(total));
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dView = getLayoutInflater().inflate(R.layout.dialog_add_budget, null);
        EditText edt = dView.findViewById(R.id.edtBudgetAmount);

        builder.setView(dView);
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String s = edt.getText().toString();
            if(!s.isEmpty()) {
                db.setBudget(Double.parseDouble(s));
                loadData();
                Toast.makeText(getContext(), "Đã cập nhật!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
}