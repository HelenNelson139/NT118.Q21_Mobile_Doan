package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class AdminCourseSummaryActivity extends AppCompatActivity {

    private TextView txtTitle;
    private MaterialButton btnDetails, btnBack;
    private String courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_course_summary);

        // 1. Ánh xạ View
        txtTitle = findViewById(R.id.txtCourseTitle);
        btnDetails = findViewById(R.id.btnDetails);
//        btnBack = findViewById(R.id.btnBack);

        // 2. Nhận dữ liệu từ Adapter gửi sang
        if (getIntent() != null) {
            courseId = getIntent().getStringExtra("COURSE_ID");
            String title = getIntent().getStringExtra("COURSE_TITLE");
            txtTitle.setText(title);
        }

        // 3. Sự kiện nút "Xem chi tiết" -> Chuyển sang AdminCourseDetailActivity
        btnDetails.setOnClickListener(v -> {
            Intent intent = new Intent(AdminCourseSummaryActivity.this, AdminCourseDetailActivity.class);
            // Tiếp tục truyền ID để trang chi tiết cuối cùng lấy dữ liệu từ Database/API
            intent.putExtra("COURSE_ID", courseId);
            startActivity(intent);
        });

        // 4. Nút quay lại
//        btnBack.setOnClickListener(v -> finish());
    }
}
