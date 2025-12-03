package com.example.campusexpensemanager;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.campusexpensemanager.DatabaseHelper;
import com.example.campusexpensemanager.Transaction;

public class AddTransactionActivity extends AppCompatActivity {

    private EditText edtAmount, edtNote, edtDate, edtDesc;
    private Spinner spinner;
    private Transaction currentTransaction;
    private boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        edtAmount = findViewById(R.id.edtAmount);
        edtNote = findViewById(R.id.edtNote);
        edtDate = findViewById(R.id.edtDate);
        edtDesc = findViewById(R.id.edtDescription);
        spinner = findViewById(R.id.spinnerCategory);
        Button btnSave = findViewById(R.id.btnSaveTransaction);
        Button btnCancel = findViewById(R.id.btnCancel);

        // Setup Spinner
        String[] cats = {"Tiền thuê nhà", "Ăn uống", "Đi lại", "Giải trí", "Giáo dục", "Y tế", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cats);
        spinner.setAdapter(adapter);

        // KIỂM TRA: CÓ PHẢI ĐANG SỬA KHÔNG?
        if (getIntent().hasExtra("transaction_data")) {
            isEdit = true;
            currentTransaction = (Transaction) getIntent().getSerializableExtra("transaction_data");

            // Điền dữ liệu cũ vào form
            edtAmount.setText(String.valueOf((long)currentTransaction.getAmount()));
            edtNote.setText(currentTransaction.getNote());
            edtDate.setText(currentTransaction.getDate());
            edtDesc.setText(currentTransaction.getDescription());

            // Chọn đúng danh mục cũ
            int pos = adapter.getPosition(currentTransaction.getCategory());
            spinner.setSelection(pos);

            btnSave.setText("Lưu thay đổi");
        }

        btnSave.setOnClickListener(v -> {
            String amountStr = edtAmount.getText().toString();
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Nhập số tiền!", Toast.LENGTH_SHORT).show();
                return;
            }
            double amount = Double.parseDouble(amountStr);
            String note = edtNote.getText().toString();
            String date = edtDate.getText().toString();
            String cat = spinner.getSelectedItem().toString();
            String desc = edtDesc.getText().toString();

            DatabaseHelper db = new DatabaseHelper(this);

            if (isEdit) {
                // Sửa: Giữ nguyên ID cũ
                Transaction t = new Transaction(currentTransaction.getId(), note, amount, date, cat, desc);
                db.updateTransaction(t);
            } else {
                // Thêm: ID = 0 (DB tự tăng)
                Transaction t = new Transaction(0, note, amount, date, cat, desc);
                db.addTransaction(t);
            }
            finish();
        });

        btnCancel.setOnClickListener(v -> finish());
    }
}