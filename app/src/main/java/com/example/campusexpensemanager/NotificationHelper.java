package com.example.campusexpensemanager;
// Đảm bảo package này là chính xác. Nếu bạn dùng utils, hãy sửa thành com.example.campusexpensemanager.utils

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.example.campusexpensemanager.R; // Đảm bảo R.java được import đúng

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NotificationHelper {

    private static final String CHANNEL_ID = "budget_alert_channel";
    private static final String CHANNEL_NAME = "Cảnh báo Ngân sách";
    private static final int NOTIFICATION_ID = 101;

    private Context context;

    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo khi ngân sách còn lại thấp.");

            // Lấy NotificationManager Service
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Hiển thị thông báo cảnh báo ngân sách.
     * @param remaining Số tiền còn lại.
     */
    public void showBudgetAlert(double remaining) {

        // Dùng DecimalFormat để định dạng số tiền cho dễ đọc (ví dụ: 10,000 đ)
        // Sử dụng Locale.VIETNAM cho dấu phân cách
        DecimalFormat df = new DecimalFormat("#,###", new DecimalFormatSymbols(new Locale("vi", "VN")));
        String formattedRemaining = df.format(remaining) + " đ";

        String title = "⚠️ Cảnh báo Ngân sách Thấp!";
        String content = String.format(
                "Ngân sách còn lại của bạn chỉ còn %s. Hãy cân nhắc chi tiêu!",
                formattedRemaining
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_budget_alert) // Đảm bảo icon này tồn tại
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Lấy NotificationManager Service
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Dòng này đã an toàn và chính xác, không gây lỗi nếu các import khác đúng
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}