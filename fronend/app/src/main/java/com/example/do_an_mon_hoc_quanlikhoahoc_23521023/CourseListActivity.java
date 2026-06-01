package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
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

    // Y XÌ FILE ADMIN: Khai báo đúng tên biến edtSearch
    private EditText edtSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.courses);

        // Ánh xạ các thành phần cũ của bạn
        btnMenuCard = findViewById(R.id.btnMenuCard);
        rvStudentCourses = findViewById(R.id.rvCourseList);

        // Y XÌ FILE ADMIN: Ánh xạ và gán TextWatcher lắng nghe sự kiện
        edtSearch = findViewById(R.id.edtSearch);
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().trim();
                    if (query.isEmpty()) {
                        // Nếu trống ô nhập -> Gọi lại hàm tải danh sách ban đầu giống hệt Admin
                        fetchActiveLessons();
                    } else {
                        // Nếu có chữ -> Thực hiện hàm tìm kiếm kết nối Server
                        searchLessonsFromServer(query);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        btnMenuCard.setOnClickListener(view -> showSidebarMenu());

        // Cài đặt RecyclerView hiển thị danh sách
        rvStudentCourses.setLayoutManager(new LinearLayoutManager(this));
        studentAdapter = new StudentLessonAdapter(this, activeLessonsList);
        rvStudentCourses.setAdapter(studentAdapter);

        // Khởi tạo Api Service qua Retrofit Client
        lessonApiService = RetrofitClient.getClient().create(LessonApiService.class);
        // Tải danh sách mặc định ban đầu khi vào ứng dụng
        fetchActiveLessons();
        setupSearch();
    }

    // Hàm lấy danh sách bài học ban đầu
    private void fetchActiveLessons() {
        lessonApiService.getAllLessonsActive().enqueue(new Callback<ApiResponse<List<LessonResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<LessonResponse>>> call, Response<ApiResponse<List<LessonResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<LessonResponse>> apiResponse = response.body();
                    if (apiResponse.getCode() == 1000 && apiResponse.getResult() != null) {
                        activeLessonsList.clear();
                        activeLessonsList.addAll(apiResponse.getResult());
                        studentAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<LessonResponse>>> call, Throwable t) {
                Log.e("API_ERROR", "Lỗi tải danh sách bài học: " + t.getMessage());
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
                    searchLessonsFromServer(query);
                } else {
                    Toast.makeText(this, "Vui lòng nhập từ khóa tìm kiếm!", Toast.LENGTH_SHORT).show();
                    fetchActiveLessons();
                }
            });
        }
    }


    // Y XÌ FILE ADMIN: Hàm xử lý gọi API kết nối đến Server và cập nhật giao diện
    private void searchLessonsFromServer(String keyword) {
        lessonApiService.searchLessons(keyword).enqueue(new Callback<ApiResponse<List<LessonResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<LessonResponse>>> call, Response<ApiResponse<List<LessonResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<LessonResponse>> apiResponse = response.body();
                    if (apiResponse.getCode() == 1000 && apiResponse.getResult() != null) {

                        activeLessonsList.clear();
                        if (!apiResponse.getResult().isEmpty()) {
                            // Đổ dữ liệu tìm kiếm tìm thấy được
                            activeLessonsList.addAll(apiResponse.getResult());
                        } else {
                            // Hiển thị thông báo Toast nếu không tìm thấy giống file Admin mẫu của bạn
                            Toast.makeText(CourseListActivity.this, "Không tìm thấy bài học nào phù hợp!", Toast.LENGTH_SHORT).show();
                        }
                        studentAdapter.notifyDataSetChanged();

                    } else {
                        Toast.makeText(CourseListActivity.this, "Lỗi hệ thống: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CourseListActivity.this, "Lỗi kết nối mạng, mã: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<LessonResponse>>> call, Throwable t) {
                Log.e("API_STUDENT_SEARCH", "Thất bại: " + t.getMessage());
                Toast.makeText(CourseListActivity.this, "Không thể kết nối đến máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // GIỮ NGUYÊN HOÀN TOÀN: Sidebar Menu gốc của bạn không đổi một chữ
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