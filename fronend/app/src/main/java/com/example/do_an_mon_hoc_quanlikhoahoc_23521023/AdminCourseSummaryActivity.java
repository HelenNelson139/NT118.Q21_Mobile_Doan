package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class AdminCourseSummaryActivity extends AppCompatActivity {

    private TextView txtTitle;
    private MaterialButton btnDetails;

    private int courseId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_course_summary);

        txtTitle = findViewById(R.id.txtCourseTitle);
        btnDetails = findViewById(R.id.btnDetails);

        if (getIntent() != null) {
            courseId = getIntent().getIntExtra("COURSE_ID", -1);
            String title = getIntent().getStringExtra("COURSE_TITLE");
            txtTitle.setText(title);
        }

        btnDetails.setOnClickListener(v -> {
            if (courseId == -1) {
                return;
            }

            Intent intent = new Intent(
                    AdminCourseSummaryActivity.this,
                    AdminCourseDetailActivity.class
            );

            intent.putExtra("COURSE_ID", courseId);
            startActivity(intent);
        });
    }
}