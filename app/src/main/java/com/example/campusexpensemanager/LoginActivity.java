package com.example.campusexpensemanager;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent; // Cần import Intent
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

// Đổi từ 'class' thành 'public class' để đảm bảo Activity được truy cập và khai báo đúng
public class LoginActivity extends AppCompatActivity {

    // Khai báo các đối tượng
    EditText edtUsername, edtPassword, edtEmail, edtPhone;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đảm bảo R.layout.activity_login đã được giải quyết (không còn lỗi)
        setContentView(R.layout.activity_login);

        // 1. Ánh xạ (tìm) các thành phần từ XML bằng ID
        // Các ID này phải khớp với activity_login.xml
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        btnLogin = findViewById(R.id.btnLogin);

        // 2. Thiết lập lắng nghe sự kiện khi bấm nút LOGIN
        // Dùng Lambda expression (tối ưu hơn View.OnClickListener)
        btnLogin.setOnClickListener(v -> {
            // Lấy dữ liệu người dùng nhập và cắt khoảng trắng thừa
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            // Kiểm tra điều kiện đăng nhập
            if (!username.isEmpty() && !password.isEmpty()) {

                // --- PHẦN CODE CHUYỂN MÀN HÌNH ---
                Toast.makeText(LoginActivity.this,
                        "Đăng nhập thành công! Chuyển đến trang chính.",
                        Toast.LENGTH_SHORT).show();

                // Tạo Intent để chuyển sang MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);

                // Kết thúc màn hình Login để không thể bấm nút Back quay lại
                finish();
                // ---------------------------------
            } else {
                Toast.makeText(LoginActivity.this,
                        "Vui lòng nhập đầy đủ Tên người dùng và Mật khẩu.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}