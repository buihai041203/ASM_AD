package com.example.campusexpensemanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import com.example.campusexpensemanager.Fragment.BudgetFragment;
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

        // Hiển thị fragment mặc định
        loadFragment(new HomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected;

            switch (item.getItemId()) {
                case R.id.menu_expense:
                    selected = new ExpenseFragment();
                    break;

                case R.id.menu_budget:
                    selected = new BudgetFragment();
                    break;

                case R.id.menu_settings:
                    selected = new SettingsFragment();
                    break;

                default:
                    selected = new HomeFragment();
            }

            loadFragment(selected);
            return true;
        });
    }

    private void loadFragment(Fragment f) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.containerHome, f)
                .commit();
    }
}
