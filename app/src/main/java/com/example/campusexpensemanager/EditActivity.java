package com.example.campusexpensemanager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class EditActivity extends AppCompatActivity {

    private EditText edtReason, edtCost, edtDate, edtNote; // Lưu ý ID ánh xạ bên dưới
    private Spinner spinnerCategory;
    private Button btnUpdate, btnDelete, btnCancel;
    private Transaction currentTransaction;
    private DatabaseHelper db;
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit); // File layout activity_edit.xml

        db = new DatabaseHelper(this);

        // 1. Ánh xạ (Phải khớp ID trong XML activity_edit)
        // Nếu bạn dùng lại layout cũ thì sửa ID cho khớp code hoặc sửa code khớp ID
        edtReason = findViewById(R.id.edtReasonEdit);
        edtCost = findViewById(R.id.edtCostEdit);
        edtNote = findViewById(R.id.edtNoteEdit);
        edtDate = findViewById(R.id.edtDate); // ID ngày
        spinnerCategory = findViewById(R.id.spinnerCategory);

        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnCancel = findViewById(R.id.btnCancel);

        // 2. Setup Spinner
        String[] categories = {"Tiền thuê nhà", "Ăn uống", "Đi lại", "Giải trí", "Giáo dục", "Y tế", "Khác"};
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(spinnerAdapter);

        // 3. Nhận dữ liệu
        if (getIntent().hasExtra("transaction_data")) {
            currentTransaction = (Transaction) getIntent().getSerializableExtra("transaction_data");
            loadDataToViews();
        }

        // 4. Sự kiện
        edtDate.setOnClickListener(v -> showDatePicker());
        btnUpdate.setOnClickListener(v -> updateData());
        btnDelete.setOnClickListener(v -> deleteData());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadDataToViews() {
        edtReason.setText(currentTransaction.getNote());
        edtCost.setText(String.valueOf((long)currentTransaction.getAmount()));
        edtDate.setText(currentTransaction.getDate());
        if(currentTransaction.getDescription() != null) {
            edtNote.setText(currentTransaction.getDescription());
        }

        // Chọn đúng danh mục cũ
        int pos = spinnerAdapter.getPosition(currentTransaction.getCategory());
        spinnerCategory.setSelection(pos);
    }

    private void updateData() {
        String note = edtReason.getText().toString();
        String amountStr = edtCost.getText().toString();

        if (note.isEmpty() || amountStr.isEmpty()) return;

        // Tạo object mới nhưng GIỮ NGUYÊN ID CŨ
        Transaction t = new Transaction(
                currentTransaction.getId(), // Quan trọng: ID cũ
                note,
                Double.parseDouble(amountStr),
                edtDate.getText().toString(),
                spinnerCategory.getSelectedItem().toString(),
                edtNote.getText().toString()
        );

        db.updateTransaction(t);
        Toast.makeText(this, "Đã cập nhật!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void deleteData() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa giao dịch")
                .setMessage("Bạn có chắc chắn muốn xóa?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.deleteTransaction(currentTransaction.getId());
                    Toast.makeText(this, "Đã xóa!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            edtDate.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }
}