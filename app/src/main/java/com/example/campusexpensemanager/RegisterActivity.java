package com.example.campusexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    EditText edtUsername, edtPassword, edtEmail, edtPhone;
    Button btnRegister;
    TextView textLoginLink;

    DatabaseHelper_a db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper_a(this);

        // Map UI components
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        btnRegister = findViewById(R.id.btnRegister);
        textLoginLink = findViewById(R.id.textLoginLink);  // Kiểm tra lại ID này

        // Set up listener for the Register button
        btnRegister.setOnClickListener(v -> registerUser());

        // Set up listener for the Login link
        textLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });
    }

    // Phương thức để xử lý đăng ký người dùng
    private void registerUser() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        // Input validation
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please enter all details!", Toast.LENGTH_SHORT).show();
        } else if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show();
        } else if (db.userExists(username)) {
            Toast.makeText(this, "Username already exists, please choose another!", Toast.LENGTH_SHORT).show();
        } else {
            // Tạo và thêm người dùng mới
            UserActivity newUser = new UserActivity(username, password, email, phone);
            boolean isAdded = db.addUser(newUser);  // Gọi phương thức addUser

            if (isAdded) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class)); // Chuyển hướng đến trang đăng nhập
            } else {
                Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}