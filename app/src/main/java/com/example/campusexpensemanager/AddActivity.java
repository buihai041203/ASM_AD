package com.example.campusexpensemanager;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddActivity extends AppCompatActivity {

    // KHAI BÁO BIẾN (Đã sửa tvAddTransaction thành tvAddTitle cho khớp XML)
    private TextView tvAddTitle;
    private Button btnDoneAdd;
    private EditText edtNote, edtCost, edtReason;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        // 1. Ánh xạ (Đã sửa ID cho khớp)
        tvAddTitle = findViewById(R.id.tvAddTitle);
        btnDoneAdd = findViewById(R.id.btnDoneAdd);
        edtNote = findViewById(R.id.edtNote);
        edtCost = findViewById(R.id.edtCost);
        edtReason = findViewById(R.id.edtReason);


        // 2. Logic đổi màu tiền (Đỏ/Xanh)
        edtCost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (input.contains("-")) {
                    edtCost.setTextColor(Color.parseColor("#E53935")); // Đỏ
                } else {
                    edtCost.setTextColor(Color.parseColor("#43A047")); // Xanh
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 3. Sự kiện bấm nút SAVE
        btnDoneAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSaveTransaction();
            }
        });
    }

    private void handleSaveTransaction() {
        String reason = edtReason.getText().toString().trim();
        String costString = edtCost.getText().toString().trim();
        String note = edtNote.getText().toString().trim();

        if (reason.isEmpty() || costString.isEmpty()) {
            Toast.makeText(this, "Please enter Amount and Reason!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(costString);
            String currentTime = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault()).format(new Date());

            // Tạo giao dịch mới
            Transaction newTransaction = new Transaction(reason, currentTime, amount);

            // Lưu vào AppData
            AppData.getInstance().addTransaction(newTransaction);

            Toast.makeText(this, "Transaction added successfully!", Toast.LENGTH_SHORT).show();
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount format!", Toast.LENGTH_SHORT).show();
        }
    }
}