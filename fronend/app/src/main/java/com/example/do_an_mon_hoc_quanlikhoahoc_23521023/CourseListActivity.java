package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseListActivity extends AppCompatActivity {

    private MaterialCardView btnMenuCard;
    private RecyclerView rvStudentCourses;
    private StudentLessonAdapter studentAdapter;
    private List<LessonResponse> activeLessonsList = new ArrayList<>();
    private LessonApiService lessonApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.courses);

        // 1. Ánh xạ Sidebar Menu cũ của bạn
        btnMenuCard = findViewById(R.id.btnMenuCard);
        btnMenuCard.setOnClickListener(view -> showSidebarMenu());

        // 2. Ánh xạ và cấu hình RecyclerView hiển thị danh sách bài học
        rvStudentCourses = findViewById(R.id.rvCourseList);
        rvStudentCourses.setLayoutManager(new LinearLayoutManager(this));

        studentAdapter = new StudentLessonAdapter(this, activeLessonsList);
        rvStudentCourses.setAdapter(studentAdapter);

        // 3. Khởi tạo Retrofit API Service
        // (Thay RetrofitClient bằng class cấu hình khởi tạo Retrofit thực tế trong dự án của bạn)
        lessonApiService = RetrofitClient.getClient().create(LessonApiService.class);

        // 4. Gọi hàm tải danh sách bài học Active từ server về
        fetchActiveLessonsFromServer();
    }

    private void fetchActiveLessonsFromServer() {
        Call<ApiResponse<List<LessonResponse>>> call = lessonApiService.getAllLessonsActive();

        call.enqueue(new Callback<ApiResponse<List<LessonResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<LessonResponse>>> call, Response<ApiResponse<List<LessonResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<LessonResponse>> apiResponse = response.body();
                    if (apiResponse.getCode() == 1000) {
                        activeLessonsList.clear();
                        if (apiResponse.getResult() != null) {
                            activeLessonsList.addAll(apiResponse.getResult());
                        }
                        studentAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(CourseListActivity.this, "Lỗi: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CourseListActivity.this, "Không thể lấy danh sách bài học!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<LessonResponse>>> call, Throwable t) {
                Log.e("STUDENT_API_ERROR", "Lỗi kết nối mạng: " + t.getMessage());
                Toast.makeText(CourseListActivity.this, "Lỗi mạng, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showSidebarMenu() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_sidebar);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.END);
        }

        MaterialCardView btnCloseMenu = dialog.findViewById(R.id.btnCloseMenu);
        LinearLayout menuProfile = dialog.findViewById(R.id.menuProfile);
        LinearLayout menuCourses = dialog.findViewById(R.id.menuCourses);
        LinearLayout menuMyCourses = dialog.findViewById(R.id.menuLearning);
        TextView txtLogout = dialog.findViewById(R.id.txtLogout);

        btnCloseMenu.setOnClickListener(v -> dialog.dismiss());

        menuProfile.setOnClickListener(v -> {
            startActivity(new Intent(CourseListActivity.this, ProfileActivity.class));
            dialog.dismiss();
        });

        menuCourses.setOnClickListener(v -> {
            startActivity(new Intent(CourseListActivity.this, CourseListActivity.class));
            dialog.dismiss();
        });

        menuMyCourses.setOnClickListener(v -> {
            startActivity(new Intent(CourseListActivity.this, mycourseactivity.class));
            dialog.dismiss();
        });

        txtLogout.setOnClickListener(v -> {
            Intent intent = new Intent(CourseListActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }
}