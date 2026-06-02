package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LessonActivity extends AppCompatActivity {

    private TextView txtLessonTitle;
    private TextView txtObjective;
    private TextView txtContent;
    private TextView txtExample;

    private ImageView imgExample;
    private ImageView imgCourse;

    private ImageButton btnBack;
    private ImageButton btnNext;

    private PlayerView videoModule;
    private ExoPlayer lessonPlayer;

    private LinearLayout layoutModuleFiles;

    private int currentIndex = 0;

    private List<LessonResponse> lessonList = new ArrayList<>();

    private ArrayList<Integer> moduleIds = new ArrayList<>();
    private boolean isModuleMode = false;
    private String parentLessonThumbnail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        initViews();

        btnBack.setVisibility(View.INVISIBLE);
        btnNext.setVisibility(View.INVISIBLE);

        handleIntentData();
        setupButtonEvents();
    }

    private void initViews() {
        txtLessonTitle = findViewById(R.id.txtLessonTitle);
        txtObjective = findViewById(R.id.txtObjective);
        txtContent = findViewById(R.id.txtContent);
        txtExample = findViewById(R.id.txtExample);

        imgExample = findViewById(R.id.imgExample);
        imgCourse = findViewById(R.id.imgCourse);

        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);

        videoModule = findViewById(R.id.videoModule);
        layoutModuleFiles = findViewById(R.id.layoutModuleFiles);
    }

    private void handleIntentData() {
        if (getIntent() != null && getIntent().hasExtra("MODULE_IDS")) {
            isModuleMode = true;

            moduleIds = getIntent().getIntegerArrayListExtra("MODULE_IDS");
            currentIndex = getIntent().getIntExtra("CURRENT_INDEX", 0);
            parentLessonThumbnail = getIntent().getStringExtra("PARENT_LESSON_THUMBNAIL");

            if (parentLessonThumbnail != null && !parentLessonThumbnail.trim().isEmpty()) {
                Glide.with(this)
                        .load(parentLessonThumbnail)
                        .placeholder(R.drawable.course_python)
                        .error(R.drawable.course_python)
                        .into(imgCourse);
            }

            fetchModuleFromServer();

        } else {
            isModuleMode = false;
            currentIndex = getIntent().getIntExtra("index", 0);
            fetchTeacherLessonsFromServer();
        }
    }

    private void setupButtonEvents() {
        btnNext.setOnClickListener(v -> {
            releaseLessonPlayer();

            if (isModuleMode) {
                if (moduleIds != null && currentIndex < moduleIds.size() - 1) {
                    currentIndex++;
                    fetchModuleFromServer();
                }
            } else {
                if (lessonList != null && currentIndex < lessonList.size() - 1) {
                    currentIndex++;
                    loadLesson();
                }
            }
        });

        btnBack.setOnClickListener(v -> {
            releaseLessonPlayer();

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

    private void fetchTeacherLessonsFromServer() {
        LessonApiService apiService =
                RetrofitClient.getClient().create(LessonApiService.class);

        apiService.getMyLessons().enqueue(new Callback<ApiResponse<List<LessonResponse>>>() {
            @Override
            public void onResponse(
                    Call<ApiResponse<List<LessonResponse>>> call,
                    Response<ApiResponse<List<LessonResponse>>> response
            ) {
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
                            showEmptyLesson();
                        }
                    } else {
                        showEmptyLesson();
                    }

                } else {
                    showEmptyLesson();
                }
            }

            @Override
            public void onFailure(
                    Call<ApiResponse<List<LessonResponse>>> call,
                    Throwable t
            ) {
                Log.e("TEACHER_LESSON_API", "Thất bại: " + t.getMessage());
                showEmptyLesson();
            }
        });
    }

    private void loadLesson() {
        releaseLessonPlayer();

        if (lessonList == null || lessonList.isEmpty()) {
            showEmptyLesson();
            return;
        }

        LessonResponse lesson = lessonList.get(currentIndex);

        txtLessonTitle.setText(
                (currentIndex + 1) + "/" + lessonList.size() + " - " + safe(lesson.getTitle())
        );

        txtObjective.setText(safe(lesson.getWhatYouLearn()));
        txtContent.setText(safe(lesson.getDescription()));
        txtExample.setText(safe(lesson.getSkillLearned()));

        if (videoModule != null) {
            videoModule.setVisibility(View.GONE);
        }

        if (layoutModuleFiles != null) {
            layoutModuleFiles.removeAllViews();
        }

        String thumbnailUrl = lesson.getThumbnailUrl();

        if (thumbnailUrl != null && !thumbnailUrl.trim().isEmpty()) {
            Glide.with(this)
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.course_python)
                    .error(R.drawable.course_python)
                    .into(imgCourse);
        } else {
            imgCourse.setImageResource(R.drawable.course_python);
        }

        if (imgExample != null) {
            imgExample.setImageResource(R.drawable.course_python);
        }

        updateNavigationButtonsForLessons();
    }

    private void fetchModuleFromServer() {
        releaseLessonPlayer();

        if (moduleIds == null || moduleIds.isEmpty()) {
            showEmptyModule();
            return;
        }

        if (currentIndex < 0 || currentIndex >= moduleIds.size()) {
            currentIndex = 0;
        }

        int currentModuleId = moduleIds.get(currentIndex);

        ModuleApiService apiService =
                RetrofitClient.getClient().create(ModuleApiService.class);

        apiService.getModuleById(currentModuleId).enqueue(new Callback<ApiResponse<ModuleResponse>>() {
            @Override
            public void onResponse(
                    Call<ApiResponse<ModuleResponse>> call,
                    Response<ApiResponse<ModuleResponse>> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ModuleResponse> apiResponse = response.body();

                    if (apiResponse.getCode() == 1000 && apiResponse.getResult() != null) {
                        loadModule(apiResponse.getResult());
                    } else {
                        showEmptyModule();
                    }
                } else {
                    showEmptyModule();
                }
            }

            @Override
            public void onFailure(
                    Call<ApiResponse<ModuleResponse>> call,
                    Throwable t
            ) {
                Log.e("API_MODULE_LOG", "Thất bại: " + t.getMessage());
                showEmptyModule();
            }
        });
    }

    private void loadModule(ModuleResponse module) {
        releaseLessonPlayer();

        if (module == null) {
            showEmptyModule();
            return;
        }

        txtLessonTitle.setText(
                (currentIndex + 1) + "/" + moduleIds.size() + " - " + safe(module.getTitle())
        );

        txtObjective.setText(safe(module.getObjective()));
        txtContent.setText(safe(module.getContent()));
        txtExample.setText(safe(module.getExample()));

        if (module.getImageExampleUrl() != null && !module.getImageExampleUrl().trim().isEmpty()) {
            Glide.with(this)
                    .load(module.getImageExampleUrl())
                    .placeholder(R.drawable.course_python)
                    .error(R.drawable.course_python)
                    .into(imgExample);
        } else {
            imgExample.setImageResource(R.drawable.course_python);
        }

        if (videoModule != null) {
            videoModule.setVisibility(View.GONE);
        }

        if (layoutModuleFiles != null) {
            layoutModuleFiles.removeAllViews();
        }

        fetchFilesOfModule(module.getId());
        updateNavigationButtonsForModules();
    }

    private void fetchFilesOfModule(Integer moduleId) {
        if (moduleId == null) {
            return;
        }

        FileApiService fileApiService =
                RetrofitClient.getClient().create(FileApiService.class);

        fileApiService.getFilesByModule(moduleId).enqueue(new Callback<List<FileResponse>>() {
            @Override
            public void onResponse(
                    Call<List<FileResponse>> call,
                    Response<List<FileResponse>> response
            ) {
                if (layoutModuleFiles != null) {
                    layoutModuleFiles.removeAllViews();
                }

                releaseLessonPlayer();

                if (videoModule != null) {
                    videoModule.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<FileResponse> files = response.body();

                    boolean hasVideo = false;

                    for (FileResponse file : files) {
                        if (file == null || file.getFileUrl() == null) {
                            continue;
                        }

                        String url = file.getFileUrl();
                        String name = file.getFileName();

                        if (isVideoFile(url, name) && !hasVideo) {
                            hasVideo = true;
                            showVideo(url);
                        } else {
                            addDownloadButton(name, url);
                        }
                    }

                    if (!hasVideo) {
                        Log.d("MODULE_VIDEO", "Module này chưa có video");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<FileResponse>> call, Throwable t) {
                Log.e("GET_MODULE_FILES", "Lỗi lấy file module: " + t.getMessage());
            }
        });
    }

    private void showVideo(String videoUrl) {
        if (videoModule == null || videoUrl == null || videoUrl.trim().isEmpty()) {
            return;
        }

        try {
            videoModule.setVisibility(View.VISIBLE);

            releaseLessonPlayer();

            lessonPlayer = new ExoPlayer.Builder(this).build();
            videoModule.setPlayer(lessonPlayer);

            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));

            lessonPlayer.setMediaItem(mediaItem);
            lessonPlayer.prepare();
            lessonPlayer.setPlayWhenReady(false);

            Log.d("LESSON_VIDEO_URL", videoUrl);

        } catch (Exception e) {
            Log.e("SHOW_VIDEO_ERROR", String.valueOf(e.getMessage()));

            Toast.makeText(
                    this,
                    "Không thể khởi tạo video: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void addDownloadButton(String fileName, String fileUrl) {
        if (layoutModuleFiles == null || fileUrl == null) {
            return;
        }

        Button button = new Button(this);

        String displayName = fileName == null || fileName.trim().isEmpty()
                ? "Tải tài liệu"
                : "Tải: " + fileName;

        button.setText(displayName);
        button.setAllCaps(false);

        button.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(fileUrl));
                startActivity(intent);
            } catch (Exception e) {
                downloadFile(fileName, fileUrl);
            }
        });

        layoutModuleFiles.addView(button);
    }

    private void downloadFile(String fileName, String fileUrl) {
        try {
            String name = fileName == null || fileName.trim().isEmpty()
                    ? "module_file"
                    : fileName;

            DownloadManager.Request request =
                    new DownloadManager.Request(Uri.parse(fileUrl));

            request.setTitle(name);
            request.setDescription("Đang tải tài liệu bài học");
            request.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            );

            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    name
            );

            DownloadManager downloadManager =
                    (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

            if (downloadManager != null) {
                downloadManager.enqueue(request);

                Toast.makeText(
                        this,
                        "Đang tải file...",
                        Toast.LENGTH_SHORT
                ).show();
            }

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Không thể tải file: " + e.getMessage(),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private boolean isVideoFile(String url, String name) {
        String value = "";

        if (url != null) {
            value += url.toLowerCase();
        }

        if (name != null) {
            value += " " + name.toLowerCase();
        }

        return value.contains(".mp4")
                || value.contains(".mov")
                || value.contains(".mkv")
                || value.contains(".webm")
                || value.contains(".m4v")
                || value.contains("video");
    }

    private void showEmptyLesson() {
        releaseLessonPlayer();

        txtLessonTitle.setText("Không có khóa học");
        txtObjective.setText("");
        txtContent.setText("");
        txtExample.setText("");

        imgCourse.setImageResource(R.drawable.course_python);
        imgExample.setImageResource(R.drawable.course_python);

        if (videoModule != null) {
            videoModule.setVisibility(View.GONE);
        }

        if (layoutModuleFiles != null) {
            layoutModuleFiles.removeAllViews();
        }

        btnBack.setVisibility(View.INVISIBLE);
        btnNext.setVisibility(View.INVISIBLE);
    }

    private void showEmptyModule() {
        releaseLessonPlayer();

        txtLessonTitle.setText("Không có bài học");
        txtObjective.setText("");
        txtContent.setText("");
        txtExample.setText("");

        imgExample.setImageResource(R.drawable.course_python);

        if (videoModule != null) {
            videoModule.setVisibility(View.GONE);
        }

        if (layoutModuleFiles != null) {
            layoutModuleFiles.removeAllViews();
        }

        btnBack.setVisibility(View.INVISIBLE);
        btnNext.setVisibility(View.INVISIBLE);
    }

    private void updateNavigationButtonsForLessons() {
        if (lessonList == null || lessonList.isEmpty()) {
            btnBack.setVisibility(View.INVISIBLE);
            btnNext.setVisibility(View.INVISIBLE);
            return;
        }

        btnBack.setVisibility(currentIndex == 0 ? View.INVISIBLE : View.VISIBLE);
        btnNext.setVisibility(currentIndex == lessonList.size() - 1 ? View.INVISIBLE : View.VISIBLE);
    }

    private void updateNavigationButtonsForModules() {
        if (moduleIds == null || moduleIds.isEmpty()) {
            btnBack.setVisibility(View.INVISIBLE);
            btnNext.setVisibility(View.INVISIBLE);
            return;
        }

        btnBack.setVisibility(currentIndex == 0 ? View.INVISIBLE : View.VISIBLE);
        btnNext.setVisibility(currentIndex == moduleIds.size() - 1 ? View.INVISIBLE : View.VISIBLE);
    }

    private void releaseLessonPlayer() {
        if (lessonPlayer != null) {
            lessonPlayer.release();
            lessonPlayer = null;
        }

        if (videoModule != null) {
            videoModule.setPlayer(null);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (lessonPlayer != null) {
            lessonPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseLessonPlayer();
    }

    private String safe(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) {
            return "";
        }

        return value.trim();
    }
}