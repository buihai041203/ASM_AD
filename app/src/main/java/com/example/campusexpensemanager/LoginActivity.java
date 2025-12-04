package com.example.campusexpensemanager;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    EditText edtUsername, edtPassword;
    Button btnLogin;
    TextView txtRegisterLink;

    DatabaseHelper_a db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper_a(this);

        // Khởi tạo các thành phần UI
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtRegisterLink = findViewById(R.id.txtRegisterLink);

        // Xử lý nút Đăng Nhập
        btnLogin.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            // Kiểm tra thông tin nhập vào
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all details!", Toast.LENGTH_SHORT).show();
            } else {
                // Xác thực người dùng
                if (db.validateUser(username, password)) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish(); // Ngăn không cho quay lại màn hình đăng nhập
                } else {
                    Toast.makeText(this, "Invalid username or password.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Xử lý liên kết Đăng Ký
        txtRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
}