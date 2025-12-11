package com.example.campusexpensemanager.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.campusexpensemanager.model.Expense;
import java.text.DecimalFormat;
import java.util.List;

public class ExpenseAdapter extends ArrayAdapter<Expense> {

    public ExpenseAdapter(@NonNull Context context, List<Expense> expenses) {
        // Dùng layout có sẵn của Android để tránh lỗi
        super(context, android.R.layout.simple_list_item_1, expenses);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            // Inflate layout 2 dòng
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        Expense expense = getItem(position);
        TextView tvName = convertView.findViewById(android.R.id.text1);
        TextView tvDetail = convertView.findViewById(android.R.id.text2);

        if (expense != null) {
            // Hiển thị tên: "Ghi chú - Loại"
            String hienThi = (expense.getGhiChu() != null && !expense.getGhiChu().isEmpty())
                    ? expense.getGhiChu() + " - " + expense.getTenLoai()
                    : expense.getTenLoai();

            tvName.setText(hienThi);
            tvName.setTextColor(Color.BLACK);
            tvName.setTextSize(16);

            // Hiển thị: "Tiền (Ngày)"
            DecimalFormat formatter = new DecimalFormat("#,###");
            String soTien = formatter.format(expense.getSoTien()) + " đ";
            tvDetail.setText(soTien + "  (" + expense.getNgay() + ")");
            tvDetail.setTextColor(Color.parseColor("#E53935"));
        }
        return convertView;
    }
}