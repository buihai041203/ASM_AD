package com.example.campusexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private TextView textName, textBalance;
    private ImageView ibNotifi;
    private FloatingActionButton btnAdd;
    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ View
        textName = findViewById(R.id.textName);
        textBalance = findViewById(R.id.textBalance);
        ibNotifi = findViewById(R.id.ibNotifi);
        btnAdd = findViewById(R.id.btnAdd);
        rvTransactions = findViewById(R.id.rvTransactions);

        textName.setText("Hello: Datbells");

        // Cài đặt RecyclerView
        adapter = new TransactionAdapter(AppData.getInstance().getTransactionList());
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);

        // --- PHẦN KẾT NỐI VỚI EDIT ACTIVITY ---
        adapter.setOnItemClickListener(new TransactionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Khi bấm vào 1 dòng -> Mở EditActivity
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                // Gửi vị trí (position) sang để bên kia biết đang sửa dòng nào
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
        // ---------------------------------------

        updateBalanceUI();

        // Nút Thêm mới
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                startActivity(intent);
            }
        });

        // Nút Thông báo (Demo)
        ibNotifi.setOnClickListener(v ->
                Toast.makeText(MainActivity.this, "No new notifications", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật lại danh sách và tiền khi quay lại từ Add hoặc Edit
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateBalanceUI();
    }

    private void updateBalanceUI() {
        double total = AppData.getInstance().getTotalBalance();
        DecimalFormat formatter = new DecimalFormat("#,###");
        textBalance.setText(formatter.format(total) + " đ");
    }
}