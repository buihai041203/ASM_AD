package com.example.campusexpensemanager;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditActivity extends AppCompatActivity {

    private EditText edtReason, edtCost, edtNote;
    private Button btnUpdate, btnDelete, btnCancel;
    private int position; // Vị trí dòng cần sửa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // 1. Ánh xạ (Đã khớp với ID trong activity_edit.xml)
        edtReason = findViewById(R.id.edtReasonEdit);
        edtCost = findViewById(R.id.edtCostEdit);
        edtNote = findViewById(R.id.edtNoteEdit);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnCancel = findViewById(R.id.btnCancel);

        // 2. Nhận dữ liệu từ Main gửi sang
        position = getIntent().getIntExtra("position", -1);

        if (position != -1) {
            // Lấy dữ liệu cũ từ kho
            Transaction oldTransaction = AppData.getInstance().getTransactionList().get(position);

            // Điền vào ô trống
            edtReason.setText       (oldTransaction.getTitle());
            // Ép kiểu int để hiển thị đẹp (bỏ số .0)
            edtCost.setText(String.valueOf((int) oldTransaction.getAmount()));

            // Nếu bạn có lưu Note thì set vào đây (Ví dụ: edtNote.setText(oldTransaction.getNote());)
        }

        // 3. Logic đổi màu tiền (Đỏ/Xanh)
        edtCost.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains("-")) {
                    edtCost.setTextColor(Color.RED);
                } else {
                    edtCost.setTextColor(Color.parseColor("#43A047")); // Green
                }
            }
            public void afterTextChanged(Editable s) {}
        });

        // 4. Sự kiện nút SAVE CHANGES (Lưu sửa đổi)
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges();
            }
        });

        // 5. Sự kiện nút DELETE (Xóa)
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });

        // 6. Sự kiện nút CANCEL (Hủy)
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveChanges() {
        String reason = edtReason.getText().toString().trim();
        String costString = edtCost.getText().toString().trim();
        // String note = edtNote.getText().toString().trim();

        if (reason.isEmpty() || costString.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(costString);

            // Cập nhật thời gian sửa đổi
            String updateTime = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault()).format(new Date());

            // Tạo giao dịch mới đè lên cái cũ
            Transaction updatedTransaction = new Transaction(reason, updateTime, amount);

            // GỌI APPDATA ĐỂ CẬP NHẬT
            AppData.getInstance().updateTransaction(position, updatedTransaction);

            Toast.makeText(this, "Update successful!", Toast.LENGTH_SHORT).show();
            finish(); // Quay về Main

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount format!", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // GỌI APPDATA ĐỂ XÓA
                        AppData.getInstance().removeTransaction(position);
                        Toast.makeText(EditActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}