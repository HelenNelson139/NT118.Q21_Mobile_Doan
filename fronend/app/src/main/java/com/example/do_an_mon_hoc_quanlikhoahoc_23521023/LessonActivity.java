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
    ImageView imgExample;
    ImageButton btnBack, btnNext;
    int currentIndex;
    List<LessonResponse> lessonList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        txtLessonTitle = findViewById(R.id.txtLessonTitle);
        txtObjective = findViewById(R.id.txtObjective);
        txtContent = findViewById(R.id.txtContent);
        txtExample = findViewById(R.id.txtExample);
        imgExample = findViewById(R.id.imgExample);

        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);

        currentIndex = getIntent().getIntExtra("index", 0);

        btnBack.setVisibility(View.INVISIBLE);
        btnNext.setVisibility(View.INVISIBLE);

        fetchLessonsFromServer();

        btnNext.setOnClickListener(v -> {
            if (currentIndex < lessonList.size() - 1) {
                currentIndex++;
                loadLesson();
            }
        });

        btnBack.setOnClickListener(v -> {
            if (currentIndex == 0) {
                finish();
            } else {
                currentIndex--;
                loadLesson();
            }
        });
    }

    void loadLesson() {
        if (lessonList == null || lessonList.isEmpty()) return;

        LessonResponse lesson = lessonList.get(currentIndex);
        txtLessonTitle.setText((currentIndex + 1) + "/" + lessonList.size() + " - " + lesson.getTitle());
        txtObjective.setText(lesson.getWhatYouLearn());
        txtContent.setText(lesson.getDescription());
        txtExample.setText(lesson.getSkillLearned());

        if (lesson.getThumbnailUrl() != null && !lesson.getThumbnailUrl().isEmpty()) {
            Glide.with(this)
                    .load(lesson.getThumbnailUrl())
                    .into(imgExample);
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
                            Toast.makeText(LessonActivity.this, "Không có bài học nào trên hệ thống!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LessonActivity.this, "Lỗi hệ thống: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LessonActivity.this, "Lỗi kết nối mạng, mã: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<LessonResponse>>> call, Throwable t) {
                Log.e("API_LOG", "Thất bại: " + t.getMessage());
                Toast.makeText(LessonActivity.this, "Không thể kết nối đến máy chủ!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}