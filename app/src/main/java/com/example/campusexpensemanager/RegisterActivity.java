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

    EditText etRegUsername, etRegPassword;
    Button btnRegister;
    TextView tvBackLogin;

    UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegUsername = findViewById(R.id.etRegUsername);
        etRegPassword = findViewById(R.id.etRegPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackLogin = findViewById(R.id.tvBackLogin);

        userDAO = new UserDAO(this);

        btnRegister.setOnClickListener(v -> {
            String user = etRegUsername.getText().toString();
            String pass = etRegPassword.getText().toString();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ!", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = userDAO.register(user, user, pass);

            if (success) {
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            } else {
                Toast.makeText(this, "Lỗi! Tên đăng nhập đã tồn tại", Toast.LENGTH_SHORT).show();
            }
        });

        tvBackLogin.setOnClickListener(v ->
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class))
        );
    }
}
