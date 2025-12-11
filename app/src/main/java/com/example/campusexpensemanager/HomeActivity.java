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

        // Hiển thị HomeFragment đầu tiên khi app mới mở
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // Xử lý sự kiện Bottom Navigation
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.menu_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.menu_expense) {
                selectedFragment = new ExpenseFragment();
            } else if (id == R.id.menu_fixedcosts) {
                selectedFragment = new FixedcostsFragment();
            } else if (id == R.id.menu_settings) {
                selectedFragment = new SettingsFragment();
            }

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

    // ==================================================================
    // HÀM ĐIỀU HƯỚNG VỀ HOME (Được gọi từ các Fragment con)
    // ==================================================================
    public void navigateToHome() {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.menu_home);
        }
    }
}