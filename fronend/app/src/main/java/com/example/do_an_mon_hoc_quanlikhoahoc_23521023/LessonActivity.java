package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LessonActivity extends AppCompatActivity {
    TextView txtLessonTitle, txtObjective, txtContent, txtExample;
    ImageView imgExample, imgCourse; // <--- THÊM MỚI: Khai báo thêm imgCourse ở trên đầu XML
    ImageButton btnBack, btnNext;

    int currentIndex;
    List<LessonResponse> lessonList = new ArrayList<>();

    // Các biến phục vụ cho phần Module độc lập
    ArrayList<Integer> moduleIds = new ArrayList<>();
    boolean isModuleMode = false;
    String parentLessonThumbnail = ""; // Lưu ảnh bìa của Lesson truyền từ ngoài vào

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        // Ánh xạ toàn bộ UI từ XML
        txtLessonTitle = findViewById(R.id.txtLessonTitle);
        txtObjective = findViewById(R.id.txtObjective);
        txtContent = findViewById(R.id.txtContent);
        txtExample = findViewById(R.id.txtExample);
        imgExample = findViewById(R.id.imgExample);
        imgCourse = findViewById(R.id.imgCourse); // <--- THÊM MỚI: Ánh xạ ảnh lớn ở trên cùng

        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);

        btnBack.setVisibility(View.INVISIBLE);
        btnNext.setVisibility(View.INVISIBLE);

        // Kiểm tra chế độ hiển thị dựa trên Intent truyền vào
        if (getIntent() != null && getIntent().hasExtra("MODULE_IDS")) {
            isModuleMode = true;
            moduleIds = getIntent().getIntegerArrayListExtra("MODULE_IDS");
            currentIndex = getIntent().getIntExtra("CURRENT_INDEX", 0);

            // Nhận thêm đường dẫn ảnh mô tả/thumbnail của Lesson lớn từ danh sách truyền sang
            parentLessonThumbnail = getIntent().getStringExtra("PARENT_LESSON_THUMBNAIL");

            // Hiển thị ngay ảnh bìa của Lesson lên đầu giao diện trước
            if (parentLessonThumbnail != null && !parentLessonThumbnail.isEmpty()) {
                Glide.with(this).load(parentLessonThumbnail).into(imgCourse);
            }

            fetchModuleFromServer();
        } else {
            isModuleMode = false;
            currentIndex = getIntent().getIntExtra("index", 0);
            fetchLessonsFromServer();
        }

        // Sự kiện nút chuyển bài tiếp theo (Giữ nguyên logic cũ)
        btnNext.setOnClickListener(v -> {
            if (isModuleMode) {
                if (moduleIds != null && currentIndex < moduleIds.size() - 1) {
                    currentIndex++;
                    fetchModuleFromServer();
                }
            } else {
                if (currentIndex < lessonList.size() - 1) {
                    currentIndex++;
                    loadLesson();
                }
            }
        });

        // Sự kiện nút quay lại (Giữ nguyên logic cũ)
        btnBack.setOnClickListener(v -> {
            if (isModuleMode) {
                if (currentIndex == 0) {
                    finish();
                } else {
                    currentIndex--;
                    fetchModuleFromServer();
                }
            } else {
                if (currentIndex == 0) {
                    finish();
                } else {
                    currentIndex--;
                    loadLesson();
                }
            }
        });
    }

    // --- ĐÃ CẬP NHẬT: Đưa ảnh Thumbnail của Lesson lên đúng vị trí imgCourse ---
    void loadLesson() {
        if (lessonList == null || lessonList.isEmpty()) return;

        LessonResponse lesson = lessonList.get(currentIndex);
        txtLessonTitle.setText((currentIndex + 1) + "/" + lessonList.size() + " - " + lesson.getTitle());
        txtObjective.setText(lesson.getWhatYouLearn());
        txtContent.setText(lesson.getDescription());
        txtExample.setText(lesson.getSkillLearned());

        // Đổ ảnh mô tả/thumbnail của Lesson vào imgCourse (Ảnh lớn trên cùng)
        if (lesson.getThumbnailUrl() != null && !lesson.getThumbnailUrl().isEmpty()) {
            Glide.with(this)
                    .load(lesson.getThumbnailUrl())
                    .into(imgCourse); // Đưa lên banner đầu trang thay vì đè vào imgExample
        }

        imgExample.setAlpha(0f);
        imgExample.animate().alpha(1f).setDuration(300);

        if (currentIndex == 0) {
            btnBack.setVisibility(View.INVISIBLE);
        } else {
            btnBack.setVisibility(View.VISIBLE);
        }
        if (currentIndex == lessonList.size() - 1) {
            btnNext.setVisibility(View.INVISIBLE);
        } else {
            btnNext.setVisibility(View.VISIBLE);
        }
    }

    // --- GIỮ NGUYÊN HOÀN TOÀN: Hàm gọi danh sách Lesson cũ ---
    private void fetchLessonsFromServer() {
        LessonApiService apiService = RetrofitClient.getClient().create(LessonApiService.class);
        apiService.getAllLessons().enqueue(new Callback<ApiResponse<List<LessonResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<LessonResponse>>> call, Response<ApiResponse<List<LessonResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<LessonResponse>> apiResponse = response.body();
                    if (apiResponse.getCode() == 1000) {
                        lessonList = apiResponse.getResult();
                        if (lessonList != null && !lessonList.isEmpty()) {
                            if (currentIndex < 0 || currentIndex >= lessonList.size()) {
                                currentIndex = 0;
                            }
                            loadLesson();
                        } else {
                            Toast.makeText(LessonActivity.this, "Không có bài học nào!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<LessonResponse>>> call, Throwable t) {
                Log.e("API_LOG", "Thất bại: " + t.getMessage());
            }
        });
    }

    // --- GIỮ NGUYÊN HOÀN TOÀN: Hàm lấy Module chi tiết theo ID ---
    private void fetchModuleFromServer() {
        if (moduleIds == null || moduleIds.isEmpty() || currentIndex < 0 || currentIndex >= moduleIds.size()) return;

        int currentModuleId = moduleIds.get(currentIndex);
        ModuleApiService apiService = RetrofitClient.getClient().create(ModuleApiService.class);

        apiService.getModuleById(currentModuleId).enqueue(new Callback<ApiResponse<ModuleResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ModuleResponse>> call, Response<ApiResponse<ModuleResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ModuleResponse> apiResponse = response.body();
                    if (apiResponse.getCode() == 1000 && apiResponse.getResult() != null) {
                        loadModule(apiResponse.getResult());
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<ModuleResponse>> call, Throwable t) {
                Log.e("API_MODULE_LOG", "Thất bại: " + t.getMessage());
            }
        });
    }

    // --- GIỮ NGUYÊN HOÀN TOÀN: Hàm hiển thị dữ liệu Module ---
    void loadModule(ModuleResponse module) {
        if (module == null) return;

        txtLessonTitle.setText((currentIndex + 1) + "/" + moduleIds.size() + " - " + module.getTitle());
        txtObjective.setText(module.getObjective());
        txtContent.setText(module.getContent());
        txtExample.setText(module.getExample());

        // Hiển thị ảnh minh họa cụ thể của riêng Module này (nếu có) vào imgExample phía dưới
        if (module.getImageExampleUrl() != null && !module.getImageExampleUrl().isEmpty()) {
            Glide.with(this)
                    .load(module.getImageExampleUrl())
                    .into(imgExample);
        }

        imgExample.setAlpha(0f);
        imgExample.animate().alpha(1f).setDuration(300);

        if (currentIndex == 0) {
            btnBack.setVisibility(View.INVISIBLE);
        } else {
            btnBack.setVisibility(View.VISIBLE);
        }
        if (currentIndex == moduleIds.size() - 1) {
            btnNext.setVisibility(View.INVISIBLE);
        } else {
            btnNext.setVisibility(View.VISIBLE);
        }
    }
}