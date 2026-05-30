package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.bumptech.glide.Glide;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCourseDetailActivity extends AppCompatActivity {

    private TextView tvHeaderTitle;
    private MaterialButton btnBack;
    private ImageView imgCourse;
    private TextView tvInstructor, tvDate;

    private int lessonId = -1;
    private TextView tvCourseInfo, tvCourseDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_course_detail);

        initViews();

        lessonId = getIntent().getIntExtra("COURSE_ID", -1);

        if (lessonId != -1) {
            fetchLessonDetails(lessonId);
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID khóa học!", Toast.LENGTH_SHORT).show();
        }

        setupClickListeners();
    }

    private void initViews() {
        tvHeaderTitle = findViewById(R.id.tvFixedHeader);
        btnBack = findViewById(R.id.btnBack);
        imgCourse = findViewById(R.id.imgCourse);

        // Nhớ ánh xạ thêm các TextView chứa Tên giảng viên, ngày đăng (nếu có trong XML)
        // tvInstructor = findViewById(R.id.tvInstructor);
        // tvDate = findViewById(R.id.tvDate);

        tvCourseInfo = findViewById(R.id.tvCourseInfo);
        tvCourseDescription = findViewById(R.id.tvCourseDescription);

        MaterialButton btnApprove = findViewById(R.id.btnApprove);
        MaterialButton btnReject = findViewById(R.id.btnReject);
        if (btnApprove != null) btnApprove.setVisibility(View.GONE);
        if (btnReject != null) btnReject.setVisibility(View.GONE);
    }

    private void fetchLessonDetails(int id) {
        LessonApiService apiService = RetrofitClient.getClient().create(LessonApiService.class);

        apiService.getLessonById(id).enqueue(new Callback<ApiResponse<LessonResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LessonResponse>> call, Response<ApiResponse<LessonResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<LessonResponse> apiResponse = response.body();

                    if (apiResponse.getCode() == 1000) { // Giả sử 1000 là code thành công của bạn
                        LessonResponse lesson = apiResponse.getResult();
                        displayCourseData(lesson);
                    } else {
                        Toast.makeText(AdminCourseDetailActivity.this, "Lỗi: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminCourseDetailActivity.this, "Không lấy được dữ liệu từ Server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LessonResponse>> call, Throwable t) {
                Toast.makeText(AdminCourseDetailActivity.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayCourseData(LessonResponse lesson) {
        // Cập nhật Text
        tvHeaderTitle.setText(lesson.getTitle());
        tvCourseDescription.setText("Mô tả: " + lesson.getDescription());

        // Cập nhật các thông tin khác
        // if (tvInstructor != null) tvInstructor.setText("Giảng viên: " + lesson.getTeacherName());
        // if (tvDate != null) tvDate.setText("Ngày đăng: " + lesson.getCreatedAt());

        String info = "Giảng viên: " + lesson.getTeacher_name() + "\n" // Nếu bạn có API lấy tên thì thay getTeacherName vào
                + "Ngày đăng: " + lesson.getCreatedAt();
        tvCourseInfo.setText(info);

        // Load ảnh từ URL trả về bằng thư viện Glide
        if (lesson.getThumbnailUrl() != null && !lesson.getThumbnailUrl().isEmpty()) {
            Glide.with(this)
                    .load(lesson.getThumbnailUrl())
                    .into(imgCourse);
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
}