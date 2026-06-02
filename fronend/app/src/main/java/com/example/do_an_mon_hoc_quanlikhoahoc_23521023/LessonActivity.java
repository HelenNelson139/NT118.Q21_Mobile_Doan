package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.material.button.MaterialButton;

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

    private MaterialButton btnCompleteModule;

    private static final String PREF_NAME = "APP_PREFS";
    private static final String KEY_USER_ID = "USER_ID";

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

        btnCompleteModule = findViewById(R.id.btnCompleteModule);
    }

    private void handleIntentData() {
        Intent intent = getIntent();

        if (intent == null) {
            isModuleMode = false;
            showEmptyModule();
            return;
        }

        /*
         * CÁCH 1:
         * Mở từ Teacher / Student / Admin bằng danh sách module.
         */
        if (intent.hasExtra("MODULE_IDS")) {
            isModuleMode = true;

            moduleIds = intent.getIntegerArrayListExtra("MODULE_IDS");
            currentIndex = intent.getIntExtra("CURRENT_INDEX", 0);
            parentLessonThumbnail = intent.getStringExtra("PARENT_LESSON_THUMBNAIL");

            if (moduleIds == null) {
                moduleIds = new ArrayList<>();
            }

            /*
             * Nếu vì lý do nào đó MODULE_IDS rỗng,
             * lấy MODULE_ID dự phòng.
             */
            int singleModuleId = intent.getIntExtra("MODULE_ID", -1);

            if (moduleIds.isEmpty() && singleModuleId != -1) {
                moduleIds.add(singleModuleId);
                currentIndex = 0;
            }

            if (currentIndex < 0 || currentIndex >= moduleIds.size()) {
                currentIndex = 0;
            }

            loadParentThumbnail();
            fetchModuleFromServer();
            return;
        }

        /*
         * CÁCH 2:
         * Mở trực tiếp bằng 1 MODULE_ID.
         * Đây là fallback cho Admin.
         */
        int singleModuleId = intent.getIntExtra("MODULE_ID", -1);

        if (singleModuleId != -1) {
            isModuleMode = true;

            moduleIds = new ArrayList<>();
            moduleIds.add(singleModuleId);

            currentIndex = 0;
            parentLessonThumbnail = intent.getStringExtra("PARENT_LESSON_THUMBNAIL");

            loadParentThumbnail();
            fetchModuleFromServer();
            return;
        }

        /*
         * CÁCH 3:
         * Luồng cũ của Teacher.
         */
        isModuleMode = false;
        currentIndex = intent.getIntExtra("index", 0);
        fetchTeacherLessonsFromServer();
    }

    private void loadParentThumbnail() {
        if (imgCourse == null) {
            return;
        }

        if (parentLessonThumbnail != null && !parentLessonThumbnail.trim().isEmpty()) {
            Glide.with(this)
                    .load(parentLessonThumbnail)
                    .placeholder(R.drawable.course_python)
                    .error(R.drawable.course_python)
                    .into(imgCourse);
        } else {
            imgCourse.setImageResource(R.drawable.course_python);
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

        if (btnCompleteModule != null) {
            btnCompleteModule.setOnClickListener(view -> completeCurrentModule());
        }
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

        if (btnCompleteModule != null) {
            btnCompleteModule.setVisibility(View.GONE);
        }

        updateNavigationButtonsForLessons();
    }

    private void fetchModuleFromServer() {
        releaseLessonPlayer();
        //gitcommit
        if (moduleIds == null || moduleIds.isEmpty()) {
            Toast.makeText(this, "Không nhận được moduleId từ màn hình trước", Toast.LENGTH_SHORT).show();
            showEmptyModule();
            return;
        }

        if (currentIndex < 0 || currentIndex >= moduleIds.size()) {
            currentIndex = 0;
        }

        int currentModuleId = moduleIds.get(currentIndex);

        Toast.makeText(
                this,
                "Đang mở bài giảng ID: " + currentModuleId,
                Toast.LENGTH_SHORT
        ).show();

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
                        Toast.makeText(
                                LessonActivity.this,
                                "Không lấy được chi tiết bài giảng: " + apiResponse.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                        showEmptyModule();
                    }
                } else {
                    Toast.makeText(
                            LessonActivity.this,
                            "Không lấy được chi tiết bài giảng, HTTP " + response.code(),
                            Toast.LENGTH_SHORT
                    ).show();
                    showEmptyModule();
                }
            }

            @Override
            public void onFailure(
                    Call<ApiResponse<ModuleResponse>> call,
                    Throwable t
            ) {
                Toast.makeText(
                        LessonActivity.this,
                        "Lỗi kết nối khi lấy bài giảng: " + t.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
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

        resetCompleteButton();

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

        if (btnCompleteModule != null) {
            btnCompleteModule.setVisibility(View.GONE);
        }
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

        if (btnCompleteModule != null) {
            btnCompleteModule.setVisibility(View.GONE);
        }
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

    private void completeCurrentModule() {
        Integer currentModuleId = getCurrentModuleId();

        if (currentModuleId == null) {
            Toast.makeText(this, "Không tìm thấy bài học hiện tại", Toast.LENGTH_SHORT).show();
            return;
        }

        int studentId = getCurrentStudentId();

        if (studentId == -1) {
            Toast.makeText(this, "Không tìm thấy học sinh đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(
                this,
                "Đang lưu tiến độ bài học...",
                Toast.LENGTH_SHORT
        ).show();

        if (btnCompleteModule != null) {
            btnCompleteModule.setEnabled(false);
            btnCompleteModule.setText("Đang lưu tiến độ...");
        }

        ProgressApiService progressApiService =
                RetrofitClient.getClient().create(ProgressApiService.class);

        progressApiService.completeModule(currentModuleId, studentId)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<Void>> call,
                            Response<ApiResponse<Void>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Void> apiResponse = response.body();

                            if (apiResponse.getCode() == 1000 || apiResponse.getCode() == 0) {
                                Toast.makeText(
                                        LessonActivity.this,
                                        "Đã đánh dấu hoàn thành bài học",
                                        Toast.LENGTH_SHORT
                                ).show();

                                if (btnCompleteModule != null) {
                                    btnCompleteModule.setText("Đã học xong");
                                    btnCompleteModule.setEnabled(false);
                                }
                            } else {
                                Toast.makeText(
                                        LessonActivity.this,
                                        "Lỗi: " + apiResponse.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();

                                resetCompleteButton();
                            }

                        } else {
                            Toast.makeText(
                                    LessonActivity.this,
                                    "Lưu tiến độ thất bại, HTTP " + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();

                            resetCompleteButton();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<Void>> call,
                            Throwable t
                    ) {
                        Toast.makeText(
                                LessonActivity.this,
                                "Lỗi kết nối khi lưu tiến độ: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();

                        resetCompleteButton();
                    }
                });
    }

    private void resetCompleteButton() {
        if (btnCompleteModule == null) {
            return;
        }

        if (isModuleMode && getCurrentStudentId() != -1) {
            btnCompleteModule.setVisibility(View.VISIBLE);
            btnCompleteModule.setEnabled(true);
            btnCompleteModule.setText("Đánh dấu đã học xong");
        } else {
            btnCompleteModule.setVisibility(View.GONE);
        }
    }

    private Integer getCurrentModuleId() {
        if (moduleIds == null || moduleIds.isEmpty()) {
            return null;
        }

        if (currentIndex < 0 || currentIndex >= moduleIds.size()) {
            return null;
        }

        return moduleIds.get(currentIndex);
    }

    private int getCurrentStudentId() {
        SharedPreferences sharedPreferences =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    private String safe(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) {
            return "";
        }

        return value.trim();
    }
}