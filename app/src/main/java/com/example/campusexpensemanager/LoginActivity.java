package com.example.campusexpensemanager;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.campusexpensemanager.dao.UserDAO;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.campusexpensemanager.model.User; // Cần import model User

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin;
    TextView tvGoRegister;

    UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);

        userDAO = new UserDAO(this);

        btnLogin.setOnClickListener(v -> {
            String user = etUsername.getText().toString();
            String pass = etPassword.getText().toString();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Do not leave it blank!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi login và nhận về đối tượng User
            User loggedInUser = userDAO.login(user, pass);

            if (loggedInUser != null) { // Đăng nhập thành công

                // --- BỔ SUNG: LƯU TÊN ĐĂNG NHẬP VÀO SHARED PREFERENCES ---

                // Tên đăng nhập là Email
                String emailToSave = loggedInUser.getEmail();

                SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                // LƯU VỚI KEY: "USERNAME" (Key này được HomeFragment đọc)
                editor.putString("USERNAME", emailToSave);

                editor.apply();

                // --- KẾT THÚC BỔ SUNG ---

                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Incorrect login information!", Toast.LENGTH_SHORT).show();
            }
        });

        tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }
}
