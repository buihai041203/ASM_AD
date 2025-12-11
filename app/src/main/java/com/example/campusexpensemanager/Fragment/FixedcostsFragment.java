package com.example.campusexpensemanager.Fragment;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.campusexpensemanager.DatabaseHelper;
import com.example.campusexpensemanager.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FixedcostsFragment extends Fragment {

    private ListView lvFixedCosts;
    private FloatingActionButton fabAdd;
    private DatabaseHelper dbHelper;
    private List<FixedCostItem> listData;
    private FixedCostAdapter adapter;

    // Class mô hình dữ liệu nội bộ
    private static class FixedCostItem {
        int id;
        String name;
        double amount;

        public FixedCostItem(int id, String name, double amount) {
            this.id = id;
            this.name = name;
            this.amount = amount;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fixedcosts, container, false);

        lvFixedCosts = view.findViewById(R.id.lvFixedCosts);
        fabAdd = view.findViewById(R.id.fabAddFixed);
        dbHelper = new DatabaseHelper(getContext());
        listData = new ArrayList<>();

        // Cài đặt Adapter
        adapter = new FixedCostAdapter(getContext(), listData);
        lvFixedCosts.setAdapter(adapter);

        // Load dữ liệu
        loadFixedCosts();

        // Sự kiện thêm mới
        fabAdd.setOnClickListener(v -> showAddDialog());

        // Sự kiện nhấn giữ để xóa (Tính năng tặng kèm cho tiện)
        lvFixedCosts.setOnItemLongClickListener((parent, view1, position, id) -> {
            showDeleteConfirm(listData.get(position));
            return true;
        });

        return view;
    }

    private void loadFixedCosts() {
        listData.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CHI_PHI_CO_DINH, null, null, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.CD_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CD_TEN));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.CD_SO_TIEN));
                listData.add(new FixedCostItem(id, name, amount));
            }
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    // Hiển thị hộp thoại nhập liệu
    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thêm Chi Phí Cố Định");

        // Tạo giao diện nhập liệu trong Dialog bằng code (đỡ phải tạo file xml mới)
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText edtName = new EditText(getContext());
        edtName.setHint("Tên (Ví dụ: Tiền nhà)");
        layout.addView(edtName);

        final EditText edtAmount = new EditText(getContext());
        edtAmount.setHint("Số tiền");
        edtAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(edtAmount);

        builder.setView(layout);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String name = edtName.getText().toString().trim();
            String amountStr = edtAmount.getText().toString().trim();

            if (!name.isEmpty() && !amountStr.isEmpty()) {
                saveFixedCost(name, Double.parseDouble(amountStr));
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveFixedCost(String name, double amount) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.CD_TEN, name);
        values.put(DatabaseHelper.CD_SO_TIEN, amount);

        long result = db.insert(DatabaseHelper.TABLE_CHI_PHI_CO_DINH, null, values);
        if (result != -1) {
            Toast.makeText(getContext(), "Đã thêm!", Toast.LENGTH_SHORT).show();
            loadFixedCosts(); // Load lại danh sách
        } else {
            Toast.makeText(getContext(), "Lỗi khi lưu!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirm(FixedCostItem item) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa mục này?")
                .setMessage("Bạn muốn xóa: " + item.name + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.delete(DatabaseHelper.TABLE_CHI_PHI_CO_DINH, DatabaseHelper.CD_ID + "=?", new String[]{String.valueOf(item.id)});
                    loadFixedCosts();
                    Toast.makeText(getContext(), "Đã xóa!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // --- Adapter nội bộ để hiển thị danh sách ---
    private class FixedCostAdapter extends ArrayAdapter<FixedCostItem> {
        public FixedCostAdapter(Context context, List<FixedCostItem> items) {
            super(context, 0, items);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                // Dùng layout có sẵn của Android (simple_list_item_2) để hiện 2 dòng
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
                convertView.setBackgroundColor(Color.WHITE); // Nền trắng
            }

            FixedCostItem item = getItem(position);
            TextView tvName = convertView.findViewById(android.R.id.text1);
            TextView tvAmount = convertView.findViewById(android.R.id.text2);

            if (item != null) {
                tvName.setText(item.name);
                tvName.setTextSize(16);
                tvName.setTextColor(Color.BLACK);

                DecimalFormat df = new DecimalFormat("#,###");
                tvAmount.setText(df.format(item.amount) + " đ");
                tvAmount.setTextColor(Color.parseColor("#009688")); // Màu xanh Teal
            }
            return convertView;
        }
    }
}