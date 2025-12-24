package com.example.myapplicationuithread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    // Tham chiếu tới các view
    private MaterialButton btnBlockUi;
    private MaterialButton btnUseBackgroundThread;
    private ProgressBar progressBar;
    private TextView tvResult;

    // ExecutorService để quản lý background thread hiệu quả
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    // Handler để cập nhật UI từ background thread
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Gây ra lỗi nếu dependency không chính xác
        setContentView(R.layout.activity_main);

        // Setup insets cho view gốc (id = main)
        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ view
        btnBlockUi = findViewById(R.id.btnBlockUi);
        btnUseBackgroundThread = findViewById(R.id.btnUseBackgroundThread);
        progressBar = findViewById(R.id.progressBar);
        tvResult = findViewById(R.id.tvResult);

        // Nút ví dụ XẤU: Tác vụ nặng block UI Thread
        btnBlockUi.setOnClickListener(v -> demoBlockUiThread());

        // Nút ví dụ ĐÚNG: Tác vụ nặng chạy bằng ExecutorService
        btnUseBackgroundThread.setOnClickListener(v -> demoBackgroundThread());
    }

    /**
     * Ví dụ XẤU: Tính Fibonacci lớn trực tiếp trên UI Thread (gây đơ app)
     */
    private void demoBlockUiThread() {
        tvResult.setText("Đang chạy tác vụ nặng trên UI Thread...");
        progressBar.setVisibility(View.VISIBLE);

        int n = 45;
        long start = System.currentTimeMillis();

        // Chạy tác vụ nặng trên UI thread (XẤU)
        long result = fibonacciRecursive(n);

        long end = System.currentTimeMillis();
        progressBar.setVisibility(View.GONE);
        tvResult.setText(
                String.format("[XẤU] Tính fibonacci(%d) TRÊN UI THREAD\nKết quả: %d\nThời gian: %d ms", n, result, (end - start))
        );
    }

    /**
     * Ví dụ ĐÚNG (TỐI ƯU): Dùng ExecutorService để chạy tác vụ nền
     */
    private void demoBackgroundThread() {
        tvResult.setText("Đang tính fibonacci(45) trên background thread...");
        progressBar.setVisibility(View.VISIBLE);

        final int n = 45;
        final long start = System.currentTimeMillis();

        // Chạy tác vụ nặng trên background thread bằng ExecutorService
        executorService.execute(() -> {
            long result = fibonacciRecursive(n);
            long end = System.currentTimeMillis();

            // Trả kết quả về UI Thread (dùng Handler)
            mainHandler.post(() -> {
                progressBar.setVisibility(View.GONE);
                tvResult.setText(
                        String.format("[ĐÚNG] Tính fibonacci(%d) trên background thread\nKết quả: %d\nThời gian: %d ms", n, result, (end - start))
                );
            });
        });
    }

    /**
     * Hàm Fibonacci đệ quy (siêu nặng) để demo block thread
     */
    private long fibonacciRecursive(int n) {
        if (n <= 1) return n;
        return fibonacciRecursive(n - 1) + fibonacciRecursive(n - 2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Luôn shutdown ExecutorService để tránh rò rỉ tài nguyên
        executorService.shutdown();
    }
}
