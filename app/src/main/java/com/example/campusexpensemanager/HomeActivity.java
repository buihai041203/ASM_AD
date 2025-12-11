package com.example.campusexpensemanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import com.example.campusexpensemanager.Fragment.FixedcostsFragment;
import com.example.campusexpensemanager.Fragment.ExpenseFragment;
import com.example.campusexpensemanager.Fragment.HomeFragment;
import com.example.campusexpensemanager.Fragment.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNav = findViewById(R.id.bottomNav);

        // Load HomeFragment khi mở app
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            // --- QUAN TRỌNG: SỬA CÁC ID DƯỚI ĐÂY CHO KHỚP VỚI FILE MENU CỦA BẠN ---
            if (id == R.id.menu_home) { // Nếu menu của bạn là nav_home, hãy sửa thành R.id.nav_home
                selectedFragment = new HomeFragment();
            } else if (id == R.id.menu_expense) {
                selectedFragment = new ExpenseFragment();
            } else if (id == R.id.menu_fixedcosts) {
                selectedFragment = new FixedcostsFragment();
            } else if (id == R.id.menu_settings) {
                selectedFragment = new SettingsFragment();
            }
            // -----------------------------------------------------------------------

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.containerHome, fragment)
                .commit();
    }

    // Hàm cho phép Fragment con gọi để quay về Home
    public void navigateToHome() {
        if (bottomNav != null) {
            // SỬA ID NÀY CHO KHỚP VỚI ID MENU TRANG CHỦ CỦA BẠN
            bottomNav.setSelectedItemId(R.id.menu_home);
        }
    }

}