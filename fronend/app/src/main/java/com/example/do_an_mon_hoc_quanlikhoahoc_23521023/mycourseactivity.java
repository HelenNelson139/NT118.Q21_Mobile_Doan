package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class mycourseactivity extends AppCompatActivity {

    private MaterialCardView btnMenuCard;

    private MaterialCardView cardLearningCourse;
    private MaterialCardView cardCompletedCourse;

    private static final String PREF_NAME = "APP_PREFS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_course);

        btnMenuCard = findViewById(R.id.btnMenuCard);
        cardLearningCourse = findViewById(R.id.cardLearningCourse);
        cardCompletedCourse = findViewById(R.id.cardCompletedCourse);

        btnMenuCard.setOnClickListener(v -> showSidebarMenu());

        /*
         * Trang này hiện tại bạn không dùng nữa.
         * Nếu vẫn bấm vào card thì tạm mở CourseListActivity cho đúng luồng khóa học đã đăng ký.
         * Không nên mở LessonActivity trực tiếp khi chưa có MODULE_IDS.
         */
        if (cardLearningCourse != null) {
            cardLearningCourse.setOnClickListener(v -> openPage(CourseListActivity.class));
        }

        if (cardCompletedCourse != null) {
            cardCompletedCourse.setOnClickListener(v -> openPage(CourseListActivity.class));
        }
    }

    private void showSidebarMenu() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_sidebar);

        Window window = dialog.getWindow();

        if (window != null) {
            window.setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.END);
        }

        MaterialCardView btnCloseMenu = dialog.findViewById(R.id.btnCloseMenu);

        LinearLayout menuHome = dialog.findViewById(R.id.menuHome);
        LinearLayout menuCourses = dialog.findViewById(R.id.menuCourses);
        LinearLayout menuProfile = dialog.findViewById(R.id.menuProfile);

        TextView txtLogout = dialog.findViewById(R.id.txtLogout);
        TextView tvUserName = dialog.findViewById(R.id.tvUserName);

        if (tvUserName != null) {
            tvUserName.setText("Học viên");
        }

        if (btnCloseMenu != null) {
            btnCloseMenu.setOnClickListener(v -> dialog.dismiss());
        }

        if (menuHome != null) {
            menuHome.setOnClickListener(v -> {
                dialog.dismiss();
                openPage(HomeActivity.class);
            });
        }

        if (menuCourses != null) {
            menuCourses.setOnClickListener(v -> {
                dialog.dismiss();
                openPage(CourseListActivity.class);
            });
        }

        if (menuProfile != null) {
            menuProfile.setOnClickListener(v -> {
                dialog.dismiss();
                openPage(ProfileActivity.class);
            });
        }

        if (txtLogout != null) {
            txtLogout.setOnClickListener(v -> {
                SharedPreferences sharedPreferences =
                        getSharedPreferences(PREF_NAME, MODE_PRIVATE);

                sharedPreferences.edit().clear().apply();

                Intent intent = new Intent(mycourseactivity.this, MainActivity.class);
                intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                );

                startActivity(intent);
                dialog.dismiss();
                finish();
            });
        }

        dialog.show();
    }

    private void openPage(Class<?> targetActivity) {
        if (this.getClass().equals(targetActivity)) {
            return;
        }

        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}