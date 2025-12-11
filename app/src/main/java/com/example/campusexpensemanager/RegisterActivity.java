package com.example.campusexpensemanager;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.campusexpensemanager.dao.UserDAO;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegUsername, etRegPassword;
    private Button btnRegister;
    private TextView tvBackLogin;

    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Ánh xạ view
        etRegUsername = findViewById(R.id.etRegUsername);
        etRegPassword = findViewById(R.id.etRegPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackLogin = findViewById(R.id.tvBackLogin);

        userDAO = new UserDAO(this);

        // Sự kiện đăng ký
        btnRegister.setOnClickListener(v -> registerUser());

        // Sự kiện quay lại login
        tvBackLogin.setOnClickListener(v ->
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class))
        );
    }

    private void registerUser() {
        String username = etRegUsername.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all the information!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi UserDAO để đăng ký
        boolean success = userDAO.register(username, username, password); // username dùng làm email tạm

        if (success) {
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
            // Chuyển về LoginActivity
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish(); // Kết thúc RegisterActivity
        } else {
            Toast.makeText(this, "Error! The username already exists.", Toast.LENGTH_SHORT).show();
        }
    }
}
