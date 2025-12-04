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

        // 1. Ánh xạ View
        edtAmount = findViewById(R.id.edtAmount);
        edtNote = findViewById(R.id.edtNote);
        edtDate = findViewById(R.id.edtDate);
        edtDesc = findViewById(R.id.edtDescription);
        spinner = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSaveTransaction);
        btnCancel = findViewById(R.id.btnCancel);

        // 2. Setup Spinner (BUG FIX: Chỉ hiện từ "Ăn uống" trở đi, bỏ "Chi phí cố định")
        // LƯU Ý: Tên ở đây phải GIỐNG HỆT tên trong DatabaseHelper thì mới tìm ra ID được
        String[] cats = {
                "Ăn uống",
                "Xăng xe / Đi lại",
                "Mua sắm / Shopping",
                "Giải trí",
                "Y tế / Thuốc men",
                "Khác"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cats);
        spinner.setAdapter(adapter);

        // Mặc định ngày hiện tại (Format chuẩn YYYY-MM-DD để DB sắp xếp đúng)
        Calendar c = Calendar.getInstance();
        String today = String.format("%d-%02d-%02d", c.get(Calendar.YEAR), (c.get(Calendar.MONTH) + 1), c.get(Calendar.DAY_OF_MONTH));
        edtDate.setText(today);

        // 3. Chọn ngày
        edtDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                // BUG FIX: Format ngày tháng có số 0 đằng trước (VD: 2025-05-01 thay vì 2025-5-1)
                String dateStr = String.format("%d-%02d-%02d", year, (month + 1), dayOfMonth);
                edtDate.setText(dateStr);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // 4. Lưu
        btnSave.setOnClickListener(v -> {
            String amountStr = edtAmount.getText().toString().trim();
            String note = edtNote.getText().toString().trim();
            String date = edtDate.getText().toString().trim();
            String desc = edtDesc.getText().toString().trim();

            if (amountStr.isEmpty() || note.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên khoản chi và số tiền!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                String categoryName = spinner.getSelectedItem().toString();

                Transaction t = new Transaction(
                        0, // ID tự tăng
                        note,
                        amount,
                        date,
                        categoryName, // Gửi tên, DatabaseHelper sẽ tự tìm ID
                        desc
                );

                db.addTransaction(t);
                Toast.makeText(this, "Đã thêm thành công!", Toast.LENGTH_SHORT).show();
                finish();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> finish());

    }
}