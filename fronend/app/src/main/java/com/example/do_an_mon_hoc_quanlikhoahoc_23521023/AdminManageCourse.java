package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminManageCourse extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private MaterialCardView btnMenu;
    private RecyclerView rvCourseList;
    private EditText edtSearch;
    private AdminCourseSummaryAdapter adapter;
    private List<LessonResponse> lessonList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_course_manage);
        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenuCard);
        rvCourseList = findViewById(R.id.rvCourseList);
        edtSearch = findViewById(R.id.edtSearch);
        AdminSidebarNavigationHelper.setupSidebar(this, drawerLayout);
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));
        setupRecyclerView();
        fetchCoursesFromServer();
        setupSearch();
        setupBackPressed();
    }

    private void setupRecyclerView() {
        lessonList = new ArrayList<>();
        rvCourseList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminCourseSummaryAdapter(this, lessonList);
        rvCourseList.setAdapter(adapter);
    }

    private void fetchCoursesFromServer() {
        LessonApiService apiService = RetrofitClient.getClient().create(LessonApiService.class);
        apiService.getAllLessons().enqueue(new Callback<ApiResponse<List<LessonResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<LessonResponse>>> call, Response<ApiResponse<List<LessonResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<LessonResponse>> apiResponse = response.body();

                    if (apiResponse.getCode() == 1000) {
                        List<LessonResponse> remoteLessons = apiResponse.getResult();
                        if (remoteLessons != null && !remoteLessons.isEmpty()) {
                            lessonList.clear();
                            lessonList.addAll(remoteLessons);
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(AdminManageCourse.this, "Không có bài học nào trên hệ thống!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AdminManageCourse.this, "Lỗi hệ thống: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (response.code() == 403) {
                        Toast.makeText(AdminManageCourse.this, "Tài khoản không có quyền truy cập!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(AdminManageCourse.this, "Lỗi kết nối mạng, mã: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<LessonResponse>>> call, Throwable t) {
                Log.e("API_ADMIN_LESSON", "Thất bại: " + t.getMessage());
                Toast.makeText(AdminManageCourse.this, "Không thể kết nối đến máy chủ Backend!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupSearch() {
        View icFilter = findViewById(R.id.icFilter);
        if (icFilter != null) {
            icFilter.setOnClickListener(v -> {
                String query = edtSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    Toast.makeText(this, "Đang tìm kiếm online: " + query, Toast.LENGTH_SHORT).show();
                } else {
                    fetchCoursesFromServer();
                }
            });
        }
    }
    private void setupBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    setEnabled(false);
                    onBackPressed();
                }
            }
        });
    }
}