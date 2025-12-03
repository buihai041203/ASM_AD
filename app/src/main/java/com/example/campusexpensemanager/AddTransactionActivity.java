package com.example.campusexpensemanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class AddTransactionActivity extends AppCompatActivity {

    private EditText edtAmount, edtNote, edtDate, edtDesc;
    private Spinner spinner;
    private Button btnSave, btnCancel;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        db = new DatabaseHelper(this);

        // 1. Ánh xạ (Phải khớp ID trong XML activity_add_transaction)
        edtAmount = findViewById(R.id.edtAmount);
        edtNote = findViewById(R.id.edtNote);
        edtDate = findViewById(R.id.edtDate);
        edtDesc = findViewById(R.id.edtDescription);
        spinner = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSaveTransaction);
        btnCancel = findViewById(R.id.btnCancel);

        // 2. Setup Spinner
        String[] cats = {"Tiền thuê nhà", "Ăn uống", "Đi lại", "Giải trí", "Giáo dục", "Y tế", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cats);
        spinner.setAdapter(adapter);

        // 3. Chọn ngày
        edtDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                edtDate.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // 4. Lưu
        btnSave.setOnClickListener(v -> {
            String amountStr = edtAmount.getText().toString();
            String note = edtNote.getText().toString();

            if (amountStr.isEmpty() || note.isEmpty()) {
                Toast.makeText(this, "Nhập thiếu thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            Transaction t = new Transaction(
                    0, // ID tự tăng
                    note,
                    Double.parseDouble(amountStr),
                    edtDate.getText().toString(),
                    spinner.getSelectedItem().toString(),
                    edtDesc.getText().toString()
            );

            db.addTransaction(t); // Gọi hàm thêm
            Toast.makeText(this, "Đã thêm!", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnCancel.setOnClickListener(v -> finish());
    }
}