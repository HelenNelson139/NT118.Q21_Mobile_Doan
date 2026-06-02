package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherClass extends AppCompatActivity {

    private RecyclerView rvCourseList;
    private List<LessonResponse> filteredList;
    private TeacherCourseAdapter adapter;
    private final Map<View, ExoPlayer> previewPlayerMap = new HashMap<>();
    private MaterialCardView btnMenuCard;
    private View btnAddCourse;
    private EditText edtSearchCourse;

    private Uri selectedImageUri;
    private ImageView imgPreview;
    private ActivityResultLauncher<String> imagePickerLauncher;

    private ActivityResultLauncher<String> videoPickerLauncher;
    private ActivityResultLauncher<String> documentPickerLauncher;

    private View currentVideoModuleView;
    private View currentDocumentModuleView;

    private final List<View> lessonViews = new ArrayList<>();
    private final Map<View, Uri> moduleVideoMap = new HashMap<>();
    private final Map<View, Uri> moduleDocumentMap = new HashMap<>();

    private static final String PREF_NAME = "APP_PREFS";
    private static final String KEY_USER_ID = "USER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_classes);

        btnMenuCard = findViewById(R.id.btnMenuCard);
        btnAddCourse = findViewById(R.id.btnAddCourse);
        edtSearchCourse = findViewById(R.id.edtSearch);
        rvCourseList = findViewById(R.id.rvCourseList);

        filteredList = new ArrayList<>();

        rvCourseList.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TeacherCourseAdapter(filteredList, new TeacherCourseAdapter.OnCourseActionListener() {
            @Override
            public void onEditClick(int position) {
                showEditDialog(position);
            }

            @Override
            public void onDeleteClick(int position) {
                showDeleteDialog(position);
            }
        });

        rvCourseList.setAdapter(adapter);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && imgPreview != null) {
                        imgPreview.setImageURI(uri);
                        imgPreview.setTag(uri);

                        if (imgPreview.getId() == R.id.imgCourseCover) {
                            selectedImageUri = uri;
                        }
                    }
                }
        );

        videoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null || currentVideoModuleView == null) {
                        return;
                    }

                    moduleVideoMap.put(currentVideoModuleView, uri);

                    TextView txtSelectedVideo =
                            currentVideoModuleView.findViewById(R.id.txtSelectedVideo);

                    PlayerView videoPreview =
                            currentVideoModuleView.findViewById(R.id.videoPreview);

                    String fileName = getFileNameFromUri(uri);

                    if (txtSelectedVideo != null) {
                        txtSelectedVideo.setText("Đã chọn video: " + fileName);
                    }

                    if (videoPreview != null) {
                        videoPreview.setVisibility(View.VISIBLE);

                        ExoPlayer oldPlayer = previewPlayerMap.get(currentVideoModuleView);

                        if (oldPlayer != null) {
                            oldPlayer.release();
                        }

                        ExoPlayer player = new ExoPlayer.Builder(this).build();

                        videoPreview.setPlayer(player);

                        MediaItem mediaItem = MediaItem.fromUri(uri);
                        player.setMediaItem(mediaItem);

                        player.prepare();

                        /*
                         * Không auto play khi đang thêm giáo trình.
                         * Chỉ render preview, người dùng bấm play nếu muốn xem thử.
                         */
                        player.setPlayWhenReady(false);

                        previewPlayerMap.put(currentVideoModuleView, player);
                    }
                }
        );

        documentPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null || currentDocumentModuleView == null) {
                        return;
                    }

                    moduleDocumentMap.put(currentDocumentModuleView, uri);

                    TextView txtSelectedDocument =
                            currentDocumentModuleView.findViewById(R.id.txtSelectedDocument);

                    String fileName = getFileNameFromUri(uri);

                    if (txtSelectedDocument != null) {
                        txtSelectedDocument.setText("Đã chọn file: " + fileName);
                    }
                }
        );

        btnMenuCard.setOnClickListener(v -> showSidebarMenu());
        btnAddCourse.setOnClickListener(v -> showAddDialog());

        fetchTeacherLessonsFromServer();
        setupSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchTeacherLessonsFromServer();
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
                        List<LessonResponse> remoteLessons = apiResponse.getResult();

                        filteredList.clear();

                        if (remoteLessons != null && !remoteLessons.isEmpty()) {
                            filteredList.addAll(remoteLessons);
                        } else {
                            Toast.makeText(
                                    TeacherClass.this,
                                    "Bạn chưa đăng tải khóa học nào!",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        adapter.notifyDataSetChanged();

                    } else {
                        Toast.makeText(
                                TeacherClass.this,
                                "Lỗi Server: " + apiResponse.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                } else {
                    Toast.makeText(
                            TeacherClass.this,
                            "Lỗi kết nối, mã: " + response.code(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<LessonResponse>>> call, Throwable t) {
                Log.e("TEACHER_GET_LESSON", "Thất bại: " + t.getMessage());

                Toast.makeText(
                        TeacherClass.this,
                        "Không thể kết nối đến máy chủ!",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void setupSearch() {
        if (edtSearchCourse != null) {
            edtSearchCourse.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().trim();

                    if (!query.isEmpty()) {
                        searchTeacherLessonsFromServer(query);
                    } else {
                        fetchTeacherLessonsFromServer();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        View icFilter = findViewById(R.id.icFilter);

        if (icFilter != null) {
            icFilter.setOnClickListener(v -> {
                String query = edtSearchCourse.getText().toString().trim();

                if (!query.isEmpty()) {
                    Toast.makeText(this, "Đang tìm kiếm: " + query, Toast.LENGTH_SHORT).show();
                    searchTeacherLessonsFromServer(query);
                } else {
                    fetchTeacherLessonsFromServer();
                }
            });
        }
    }

    private void searchTeacherLessonsFromServer(String keyword) {
        LessonApiService apiService =
                RetrofitClient.getClient().create(LessonApiService.class);

        apiService.searchLessons(keyword).enqueue(new Callback<ApiResponse<List<LessonResponse>>>() {
            @Override
            public void onResponse(
                    Call<ApiResponse<List<LessonResponse>>> call,
                    Response<ApiResponse<List<LessonResponse>>> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<LessonResponse>> apiResponse = response.body();

                    if (apiResponse.getCode() == 1000) {
                        List<LessonResponse> searchResults = apiResponse.getResult();

                        filteredList.clear();

                        if (searchResults != null && !searchResults.isEmpty()) {
                            filteredList.addAll(searchResults);
                        }

                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<LessonResponse>>> call, Throwable t) {
                Log.e("SEARCH_LESSON_ERROR", "Thất bại: " + t.getMessage());
            }
        });
    }

    private void showAddDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_course, null);

        EditText edtTitle = view.findViewById(R.id.edtCourseTitle);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);

        LinearLayout containerIntro = view.findViewById(R.id.containerIntro);
        LinearLayout containerCurriculum = view.findViewById(R.id.containerCurriculum);

        EditText edtDescription = view.findViewById(R.id.edtDescription);
        EditText edtWhatYouLearn = view.findViewById(R.id.edtWhatYouLearn);
        EditText edtSkills = view.findViewById(R.id.edtSkills);

        ImageView imgCourseCover = view.findViewById(R.id.imgCourseCover);
        MaterialButton btnUploadImage = view.findViewById(R.id.btnUploadImage);

        LinearLayout lessonContainer = view.findViewById(R.id.lessonContainer);
        MaterialButton btnAddLesson = view.findViewById(R.id.btnAddLesson);
        MaterialButton btnSave = view.findViewById(R.id.btnSaveCourse);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);

        selectedImageUri = null;
        lessonViews.clear();
        moduleVideoMap.clear();
        moduleDocumentMap.clear();

        imgPreview = imgCourseCover;

        Dialog dialog = new Dialog(this, R.style.CustomDialogTheme);
        dialog.setContentView(view);

        Window window = dialog.getWindow();

        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
            window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }

        setupTabs(tabLayout, containerIntro, containerCurriculum);

        btnUploadImage.setOnClickListener(v -> {
            imgPreview = imgCourseCover;
            imagePickerLauncher.launch("image/*");
        });

        btnAddLesson.setOnClickListener(v -> addLesson(lessonContainer));
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            SharedPreferences sharedPreferences =
                    getSharedPreferences(PREF_NAME, MODE_PRIVATE);

            int currentUserId = sharedPreferences.getInt(KEY_USER_ID, -1);

            if (currentUserId == -1) {
                Toast.makeText(this, "Không tìm thấy teacherId", Toast.LENGTH_SHORT).show();
                return;
            }

            String teacherIdStr = String.valueOf(currentUserId);

            String title = edtTitle.getText().toString().trim();
            String description = edtDescription.getText().toString().trim();
            String whatYouLearn = edtWhatYouLearn.getText().toString().trim();
            String skills = edtSkills.getText().toString().trim();

            if (title.isEmpty()) {
                edtTitle.setError("Nhập tên khóa học");
                edtTitle.requestFocus();
                return;
            }

            if (description.isEmpty()) {
                edtDescription.setError("Nhập mô tả khóa học");
                edtDescription.requestFocus();
                return;
            }

            if (selectedImageUri == null) {
                Toast.makeText(this, "Vui lòng chọn ảnh bìa khóa học!", Toast.LENGTH_SHORT).show();
                return;
            }

            File file = getFileFromUri(selectedImageUri);

            if (file == null) {
                Toast.makeText(this, "Không đọc được ảnh bìa!", Toast.LENGTH_SHORT).show();
                return;
            }

            MultipartBody.Part titlePart =
                    MultipartBody.Part.createFormData("title", title);

            MultipartBody.Part descriptionPart =
                    MultipartBody.Part.createFormData("description", description);

            MultipartBody.Part whatYouLearnPart =
                    MultipartBody.Part.createFormData("what_you_learn", whatYouLearn);

            MultipartBody.Part skillLearnedPart =
                    MultipartBody.Part.createFormData("skill_learned", skills);

            MultipartBody.Part teacherIdPart =
                    MultipartBody.Part.createFormData("teacherId", teacherIdStr);

            String mimeType = getContentResolver().getType(selectedImageUri);

            if (mimeType == null) {
                mimeType = "image/jpeg";
            }

            RequestBody requestFile =
                    RequestBody.create(MediaType.parse(mimeType), file);

            MultipartBody.Part thumbnailPart =
                    MultipartBody.Part.createFormData(
                            "thumbnail",
                            file.getName(),
                            requestFile
                    );

            LessonApiService apiService =
                    RetrofitClient.getClient().create(LessonApiService.class);

            btnSave.setEnabled(false);
            btnSave.setText("Đang lưu...");

            apiService.createLesson(
                    titlePart,
                    descriptionPart,
                    whatYouLearnPart,
                    skillLearnedPart,
                    teacherIdPart,
                    thumbnailPart
            ).enqueue(new Callback<ApiResponse<LessonResponse>>() {
                @Override
                public void onResponse(
                        Call<ApiResponse<LessonResponse>> call,
                        Response<ApiResponse<LessonResponse>> response
                ) {
                    btnSave.setEnabled(true);
                    btnSave.setText("Thêm khóa học");

                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<LessonResponse> apiResponse = response.body();

                        if (apiResponse.getCode() == 1000 && apiResponse.getResult() != null) {
                            int newLessonId = apiResponse.getResult().getId();

                            if (!lessonViews.isEmpty()) {
                                uploadModules(newLessonId, dialog);
                            } else {
                                Toast.makeText(
                                        TeacherClass.this,
                                        "Thêm khóa học thành công!",
                                        Toast.LENGTH_SHORT
                                ).show();

                                fetchTeacherLessonsFromServer();
                                dialog.dismiss();
                            }

                        } else {
                            Toast.makeText(
                                    TeacherClass.this,
                                    "Lỗi: " + apiResponse.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                    } else {
                        try {
                            String errLog = response.errorBody() != null
                                    ? response.errorBody().string()
                                    : "Mã lỗi trống";

                            Log.e("CREATE_LESSON_ERROR", errLog);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Toast.makeText(
                                TeacherClass.this,
                                "Không thể lưu, mã phản hồi: " + response.code(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<LessonResponse>> call, Throwable t) {
                    btnSave.setEnabled(true);
                    btnSave.setText("Thêm khóa học");

                    Log.e("CREATE_LESSON_FAIL", String.valueOf(t.getMessage()));

                    Toast.makeText(
                            TeacherClass.this,
                            "Lỗi kết nối mạng!",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        });

        dialog.show();
    }

    private void showEditDialog(int position) {
        if (position < 0 || position >= filteredList.size()) {
            Toast.makeText(this, "Vị trí khóa học không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        LessonResponse oldCourse = filteredList.get(position);

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_course, null);

        EditText edtTitle = view.findViewById(R.id.edtCourseTitle);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);

        LinearLayout containerIntro = view.findViewById(R.id.containerIntro);
        LinearLayout containerCurriculum = view.findViewById(R.id.containerCurriculum);

        EditText edtDescription = view.findViewById(R.id.edtDescription);
        EditText edtWhatYouLearn = view.findViewById(R.id.edtWhatYouLearn);
        EditText edtSkills = view.findViewById(R.id.edtSkills);

        ImageView imgCourseCover = view.findViewById(R.id.imgCourseCover);
        MaterialButton btnUploadImage = view.findViewById(R.id.btnUploadImage);

        LinearLayout lessonContainer = view.findViewById(R.id.lessonContainer);
        MaterialButton btnAddLesson = view.findViewById(R.id.btnAddLesson);
        MaterialButton btnSave = view.findViewById(R.id.btnSaveCourse);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);

        lessonViews.clear();
        moduleVideoMap.clear();
        moduleDocumentMap.clear();
        selectedImageUri = null;

        edtTitle.setText(safe(oldCourse.getTitle()));

        if (edtDescription != null) {
            edtDescription.setText(safe(oldCourse.getDescription()));
        }

        if (edtWhatYouLearn != null) {
            edtWhatYouLearn.setText(safe(oldCourse.getWhatYouLearn()));
        }

        if (edtSkills != null) {
            edtSkills.setText(safe(oldCourse.getSkillLearned()));
        }

        com.bumptech.glide.Glide.with(this)
                .load(oldCourse.getThumbnailUrl())
                .placeholder(R.drawable.course_python)
                .error(R.drawable.course_python)
                .into(imgCourseCover);

        if (btnUploadImage != null) {
            btnUploadImage.setVisibility(View.GONE);
        }

        btnSave.setText("Cập nhật");

        Dialog dialog = new Dialog(this, R.style.CustomDialogTheme);
        dialog.setContentView(view);

        Window window = dialog.getWindow();

        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
            window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }

        setupTabs(tabLayout, containerIntro, containerCurriculum);

        fetchExistingModulesForLesson(
                oldCourse.getId(),
                oldCourse.getThumbnailUrl(),
                lessonContainer
        );

        btnAddLesson.setOnClickListener(v -> addLesson(lessonContainer));
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newTitle = edtTitle.getText().toString().trim();

            String newDescription = edtDescription != null
                    ? edtDescription.getText().toString().trim()
                    : "";

            String newWhatYouLearn = edtWhatYouLearn != null
                    ? edtWhatYouLearn.getText().toString().trim()
                    : "";

            String newSkills = edtSkills != null
                    ? edtSkills.getText().toString().trim()
                    : "";

            if (newTitle.isEmpty()) {
                edtTitle.setError("Nhập tên khóa học");
                edtTitle.requestFocus();
                return;
            }

            LessonUpdateRequest updateRequest =
                    new LessonUpdateRequest(
                            newTitle,
                            newDescription,
                            newWhatYouLearn,
                            newSkills
                    );

            LessonApiService apiService =
                    RetrofitClient.getClient().create(LessonApiService.class);

            btnSave.setEnabled(false);
            btnSave.setText("Đang cập nhật...");

            apiService.updateLesson(oldCourse.getId(), updateRequest)
                    .enqueue(new Callback<ApiResponse<String>>() {
                        @Override
                        public void onResponse(
                                Call<ApiResponse<String>> call,
                                Response<ApiResponse<String>> response
                        ) {
                            btnSave.setEnabled(true);
                            btnSave.setText("Cập nhật");

                            if (response.isSuccessful() && response.body() != null) {
                                ApiResponse<String> apiResponse = response.body();

                                if (apiResponse.getCode() == 1000) {
                                    int currentLessonId = oldCourse.getId();

                                    if (!lessonViews.isEmpty()) {
                                        uploadModules(currentLessonId, dialog);
                                    } else {
                                        Toast.makeText(
                                                TeacherClass.this,
                                                "Cập nhật khóa học thành công!",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        fetchTeacherLessonsFromServer();
                                        dialog.dismiss();
                                    }

                                } else {
                                    Toast.makeText(
                                            TeacherClass.this,
                                            "Lỗi cập nhật: " + apiResponse.getMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }

                            } else {
                                try {
                                    String errorBody = response.errorBody() != null
                                            ? response.errorBody().string()
                                            : "Không có error body";

                                    Log.e("UPDATE_LESSON_ERROR", errorBody);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                Toast.makeText(
                                        TeacherClass.this,
                                        "Không thể cập nhật, mã phản hồi: " + response.code(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                            btnSave.setEnabled(true);
                            btnSave.setText("Cập nhật");

                            Log.e("UPDATE_LESSON_ERROR", "Thất bại: " + t.getMessage());

                            Toast.makeText(
                                    TeacherClass.this,
                                    "Lỗi mạng, vui lòng thử lại sau!",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });

        dialog.show();
    }

    private void setupTabs(
            TabLayout tabLayout,
            LinearLayout containerIntro,
            LinearLayout containerCurriculum
    ) {
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText("Giới thiệu"));
        tabLayout.addTab(tabLayout.newTab().setText("Giáo trình"));

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TextView tabTextView =
                    (TextView) LayoutInflater.from(this)
                            .inflate(R.layout.tab_item_layout, null);

            if (tabLayout.getTabAt(i) != null) {
                tabTextView.setText(tabLayout.getTabAt(i).getText());
                tabTextView.setTextSize(14);
                tabLayout.getTabAt(i).setCustomView(tabTextView);
            }
        }

        containerIntro.setVisibility(View.VISIBLE);
        containerCurriculum.setVisibility(View.GONE);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    containerIntro.setVisibility(View.VISIBLE);
                    containerCurriculum.setVisibility(View.GONE);
                } else {
                    containerIntro.setVisibility(View.GONE);
                    containerCurriculum.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void addLesson(LinearLayout lessonContainer) {
        View lessonView = LayoutInflater.from(this).inflate(R.layout.item_lesson, null);

        MaterialButton btnUploadVideo = lessonView.findViewById(R.id.btnUploadVideo);
        MaterialButton btnUploadDocument = lessonView.findViewById(R.id.btnUploadDocument);

        if (btnUploadVideo != null) {
            btnUploadVideo.setOnClickListener(v -> {
                currentVideoModuleView = lessonView;
                videoPickerLauncher.launch("video/*");
            });
        }

        if (btnUploadDocument != null) {
            btnUploadDocument.setOnClickListener(v -> {
                currentDocumentModuleView = lessonView;
                documentPickerLauncher.launch("*/*");
            });
        }

        lessonContainer.addView(lessonView);
        lessonViews.add(lessonView);
    }

    private void uploadModules(int lessonId, Dialog dialog) {
        if (lessonViews.isEmpty()) {
            Toast.makeText(
                    TeacherClass.this,
                    "Lưu khóa học thành công!",
                    Toast.LENGTH_SHORT
            ).show();

            fetchTeacherLessonsFromServer();
            dialog.dismiss();
            return;
        }

        ModuleApiService moduleApiService =
                RetrofitClient.getClient().create(ModuleApiService.class);

        int totalModules = lessonViews.size();
        int[] completed = {0};
        int[] failed = {0};

        for (int i = 0; i < totalModules; i++) {
            final int orderIndex = i + 1;
            View lessonView = lessonViews.get(i);

            EditText edtModuleTitle = lessonView.findViewById(R.id.txtModuleTitle);
            EditText edtObjective = lessonView.findViewById(R.id.txtObjective);
            EditText edtContent = lessonView.findViewById(R.id.txtContent);
            EditText edtExample = lessonView.findViewById(R.id.txtExample);

            String title =
                    edtModuleTitle != null
                            && !edtModuleTitle.getText().toString().trim().isEmpty()
                            ? edtModuleTitle.getText().toString().trim()
                            : "Module " + orderIndex;

            String objective =
                    edtObjective != null
                            ? edtObjective.getText().toString().trim()
                            : "";

            String content =
                    edtContent != null
                            ? edtContent.getText().toString().trim()
                            : "";

            String example =
                    edtExample != null
                            ? edtExample.getText().toString().trim()
                            : "";

            Uri videoUri = moduleVideoMap.get(lessonView);
            Uri documentUri = moduleDocumentMap.get(lessonView);

            RequestBody lessonIdBody =
                    RequestBody.create(
                            MediaType.parse("text/plain"),
                            String.valueOf(lessonId)
                    );

            RequestBody titleBody =
                    RequestBody.create(
                            MediaType.parse("text/plain"),
                            title
                    );

            RequestBody objectiveBody =
                    RequestBody.create(
                            MediaType.parse("text/plain"),
                            objective
                    );

            RequestBody contentBody =
                    RequestBody.create(
                            MediaType.parse("text/plain"),
                            content
                    );

            RequestBody exampleBody =
                    RequestBody.create(
                            MediaType.parse("text/plain"),
                            example
                    );

            RequestBody orderIndexBody =
                    RequestBody.create(
                            MediaType.parse("text/plain"),
                            String.valueOf(orderIndex)
                    );

            moduleApiService.createModule(
                    lessonIdBody,
                    titleBody,
                    objectiveBody,
                    contentBody,
                    exampleBody,
                    orderIndexBody
            ).enqueue(new Callback<ApiResponse<ModuleResponse>>() {
                @Override
                public void onResponse(
                        Call<ApiResponse<ModuleResponse>> call,
                        Response<ApiResponse<ModuleResponse>> response
                ) {
                    if (response.isSuccessful()
                            && response.body() != null
                            && response.body().getCode() == 1000
                            && response.body().getResult() != null) {

                        Integer moduleId = response.body().getResult().getId();

                        uploadModuleAttachments(
                                moduleId,
                                videoUri,
                                documentUri,
                                () -> {
                                    completed[0]++;
                                    checkAllModulesUploaded(
                                            completed[0],
                                            failed[0],
                                            totalModules,
                                            dialog
                                    );
                                },
                                () -> {
                                    completed[0]++;
                                    failed[0]++;
                                    checkAllModulesUploaded(
                                            completed[0],
                                            failed[0],
                                            totalModules,
                                            dialog
                                    );
                                }
                        );

                    } else {
                        failed[0]++;
                        completed[0]++;

                        try {
                            String errorBody = response.errorBody() != null
                                    ? response.errorBody().string()
                                    : "Không có error body";

                            Log.e("CREATE_MODULE_ERROR", errorBody);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        checkAllModulesUploaded(
                                completed[0],
                                failed[0],
                                totalModules,
                                dialog
                        );
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<ModuleResponse>> call, Throwable t) {
                    failed[0]++;
                    completed[0]++;

                    Log.e("CREATE_MODULE_FAIL", String.valueOf(t.getMessage()));

                    checkAllModulesUploaded(
                            completed[0],
                            failed[0],
                            totalModules,
                            dialog
                    );
                }
            });
        }
    }

    private void uploadModuleAttachments(
            Integer moduleId,
            Uri videoUri,
            Uri documentUri,
            Runnable onSuccess,
            Runnable onError
    ) {
        if (moduleId == null) {
            onError.run();
            return;
        }

        if (videoUri != null) {
            uploadMultipartToServer(
                    moduleId,
                    videoUri,
                    true,
                    () -> {
                        if (documentUri != null) {
                            uploadMultipartToServer(
                                    moduleId,
                                    documentUri,
                                    false,
                                    onSuccess,
                                    onError
                            );
                        } else {
                            onSuccess.run();
                        }
                    },
                    onError
            );
        } else {
            if (documentUri != null) {
                uploadMultipartToServer(
                        moduleId,
                        documentUri,
                        false,
                        onSuccess,
                        onError
                );
            } else {
                onSuccess.run();
            }
        }
    }

    private void uploadMultipartToServer(
            Integer moduleId,
            Uri fileUri,
            boolean isVideo,
            Runnable onSuccess,
            Runnable onError
    ) {
        if (moduleId == null || fileUri == null) {
            onError.run();
            return;
        }

        try {
            File file = getFileFromUri(fileUri);

            if (file == null) {
                Toast.makeText(this, "Không đọc được file", Toast.LENGTH_LONG).show();
                onError.run();
                return;
            }

            String fileName = getFileNameFromUri(fileUri);

            String mimeType = getContentResolver().getType(fileUri);

            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            RequestBody moduleIdBody =
                    RequestBody.create(
                            MediaType.parse("text/plain"),
                            String.valueOf(moduleId)
                    );

            RequestBody fileNameBody =
                    RequestBody.create(
                            MediaType.parse("text/plain"),
                            fileName
                    );

            RequestBody requestFile =
                    RequestBody.create(
                            MediaType.parse(mimeType),
                            file
                    );

            MultipartBody.Part filePart =
                    MultipartBody.Part.createFormData(
                            "file",
                            fileName,
                            requestFile
                    );

            FileApiService fileApiService =
                    RetrofitClient.getClient().create(FileApiService.class);

            Call<ApiResponse<String>> call;

            if (isVideo) {
                call = fileApiService.uploadVideo(
                        moduleIdBody,
                        fileNameBody,
                        filePart
                );
            } else {
                call = fileApiService.uploadFile(
                        moduleIdBody,
                        fileNameBody,
                        filePart
                );
            }

            call.enqueue(new Callback<ApiResponse<String>>() {
                @Override
                public void onResponse(
                        Call<ApiResponse<String>> call,
                        Response<ApiResponse<String>> response
                ) {
                    if (response.isSuccessful() && response.body() != null) {
                        onSuccess.run();
                    } else {
                        try {
                            String errorBody = response.errorBody() != null
                                    ? response.errorBody().string()
                                    : "Không có error body";

                            Log.e("UPLOAD_ATTACHMENT_ERROR", "HTTP " + response.code());
                            Log.e("UPLOAD_ATTACHMENT_ERROR", errorBody);

                            Toast.makeText(
                                    TeacherClass.this,
                                    "Upload " + (isVideo ? "video" : "file")
                                            + " lỗi HTTP " + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();

                        } catch (Exception e) {
                            Log.e("UPLOAD_ATTACHMENT_PARSE", String.valueOf(e.getMessage()));
                        }

                        onError.run();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                    Log.e("UPLOAD_ATTACHMENT_FAIL", String.valueOf(t.getMessage()));

                    Toast.makeText(
                            TeacherClass.this,
                            "Lỗi upload " + (isVideo ? "video" : "file")
                                    + ": " + t.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                    onError.run();
                }
            });

        } catch (Exception e) {
            Log.e("UPLOAD_ATTACHMENT_EXCEPTION", String.valueOf(e.getMessage()));
            Toast.makeText(this, "Lỗi upload: " + e.getMessage(), Toast.LENGTH_LONG).show();
            onError.run();
        }
    }

    private void checkAllModulesUploaded(
            int completedCount,
            int failedCount,
            int totalModules,
            Dialog dialog
    ) {
        if (completedCount == totalModules) {
            if (failedCount > 0) {
                Toast.makeText(
                        TeacherClass.this,
                        "Đã lưu khóa học, nhưng có " + failedCount + " bài học/file bị lỗi!",
                        Toast.LENGTH_LONG
                ).show();
            } else {
                Toast.makeText(
                        TeacherClass.this,
                        "Lưu khóa học, giáo trình, video và file thành công!",
                        Toast.LENGTH_SHORT
                ).show();
            }

            fetchTeacherLessonsFromServer();
            dialog.dismiss();
        }
    }

    private void showDeleteDialog(int position) {
        if (position < 0 || position >= filteredList.size()) {
            Toast.makeText(this, "Vị trí khóa học không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        LessonResponse courseToDelete = filteredList.get(position);
        Integer lessonId = courseToDelete.getId();

        if (lessonId == null) {
            Toast.makeText(this, "Không tìm thấy lessonId", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xóa khóa học")
                .setMessage("Bạn có chắc muốn xóa khóa học này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    LessonApiService apiService =
                            RetrofitClient.getClient().create(LessonApiService.class);

                    apiService.deleteLesson(lessonId).enqueue(new Callback<ApiResponse<String>>() {
                        @Override
                        public void onResponse(
                                Call<ApiResponse<String>> call,
                                Response<ApiResponse<String>> response
                        ) {
                            if (response.isSuccessful() && response.body() != null) {
                                ApiResponse<String> apiResponse = response.body();

                                if (apiResponse.getCode() == 1000) {
                                    filteredList.remove(position);
                                    adapter.notifyItemRemoved(position);
                                    adapter.notifyItemRangeChanged(position, filteredList.size());

                                    Toast.makeText(
                                            TeacherClass.this,
                                            "Đã xóa khóa học thành công!",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                } else {
                                    Toast.makeText(
                                            TeacherClass.this,
                                            "Lỗi Server: " + apiResponse.getMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }

                            } else {
                                Toast.makeText(
                                        TeacherClass.this,
                                        "Xóa thất bại, mã phản hồi: " + response.code(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                            Log.e("DELETE_LESSON_ERROR", "Lỗi: " + t.getMessage());

                            Toast.makeText(
                                    TeacherClass.this,
                                    "Lỗi kết nối mạng!",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private File getFileFromUri(Uri uri) {
        if (uri == null) {
            return null;
        }

        try {
            String extension = ".tmp";
            String mimeType = getContentResolver().getType(uri);

            if (mimeType != null) {
                if (mimeType.contains("jpeg") || mimeType.contains("jpg")) {
                    extension = ".jpg";
                } else if (mimeType.contains("png")) {
                    extension = ".png";
                } else if (mimeType.contains("webp")) {
                    extension = ".webp";
                } else if (mimeType.contains("mp4")) {
                    extension = ".mp4";
                } else if (mimeType.contains("pdf")) {
                    extension = ".pdf";
                } else if (mimeType.contains("word")) {
                    extension = ".docx";
                } else if (mimeType.contains("presentation")) {
                    extension = ".pptx";
                } else if (mimeType.contains("spreadsheet") || mimeType.contains("excel")) {
                    extension = ".xlsx";
                } else if (mimeType.contains("zip")) {
                    extension = ".zip";
                } else if (mimeType.contains("text")) {
                    extension = ".txt";
                }
            }

            InputStream inputStream =
                    getContentResolver().openInputStream(uri);

            if (inputStream == null) {
                return null;
            }

            File tempFile =
                    File.createTempFile(
                            "upload_",
                            extension,
                            getCacheDir()
                    );

            FileOutputStream outputStream =
                    new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return tempFile;

        } catch (Exception e) {
            Log.e("URI_CONVERT_ERROR", "Không thể chuyển đổi URI sang File: " + e.getMessage());
            return null;
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = "module_file";

        if (uri == null) {
            return result;
        }

        try {
            Cursor cursor =
                    getContentResolver().query(uri, null, null, null, null);

            if (cursor != null) {
                int nameIndex =
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    result = cursor.getString(nameIndex);
                }

                cursor.close();
            }
        } catch (Exception e) {
            Log.e("GET_FILE_NAME", String.valueOf(e.getMessage()));
        }

        return result;
    }

    private void showSidebarMenu() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.teacher_layout_sidebar);

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
        LinearLayout menuProfile = dialog.findViewById(R.id.menuProfile);
        LinearLayout menuMyClasses = dialog.findViewById(R.id.menuMyClasses);

        TextView txtLogout = dialog.findViewById(R.id.txtLogout);

        btnCloseMenu.setOnClickListener(v -> dialog.dismiss());

        menuHome.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherClass.this, TeacherHome.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            dialog.dismiss();
            finish();
        });

        menuProfile.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherClass.this, TeacherProfileActivity.class);
            startActivity(intent);
            dialog.dismiss();
        });

        menuMyClasses.setOnClickListener(v -> dialog.dismiss());

        txtLogout.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherClass.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }

    private void fetchExistingModulesForLesson(
            Integer lessonId,
            String lessonThumbnail,
            LinearLayout lessonContainer
    ) {
        if (lessonId == null || lessonContainer == null) {
            return;
        }

        ModuleApiService moduleApiService =
                RetrofitClient.getClient().create(ModuleApiService.class);

        moduleApiService.getModulesByLessonId(lessonId)
                .enqueue(new Callback<ApiResponse<List<ModuleResponse>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<ModuleResponse>>> call,
                            Response<ApiResponse<List<ModuleResponse>>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<ModuleResponse>> apiResponse = response.body();

                            if (apiResponse.getCode() == 1000) {
                                List<ModuleResponse> modules = apiResponse.getResult();

                                if (modules == null || modules.isEmpty()) {
                                    addEmptyModuleText(lessonContainer);
                                    return;
                                }

                                ArrayList<Integer> moduleIds = new ArrayList<>();

                                for (ModuleResponse module : modules) {
                                    if (module != null && module.getId() != null) {
                                        moduleIds.add(module.getId());
                                    }
                                }

                                for (int i = 0; i < modules.size(); i++) {
                                    ModuleResponse module = modules.get(i);

                                    if (module == null) {
                                        continue;
                                    }

                                    View moduleView = createExistingModuleView(
                                            module,
                                            moduleIds,
                                            i,
                                            lessonThumbnail
                                    );

                                    lessonContainer.addView(moduleView);
                                }

                            } else {
                                Toast.makeText(
                                        TeacherClass.this,
                                        "Không lấy được giáo trình: " + apiResponse.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                        } else {
                            Toast.makeText(
                                    TeacherClass.this,
                                    "Lỗi lấy giáo trình, mã: " + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<List<ModuleResponse>>> call,
                            Throwable t
                    ) {
                        Log.e("GET_MODULES_BY_LESSON", String.valueOf(t.getMessage()));

                        Toast.makeText(
                                TeacherClass.this,
                                "Không thể kết nối để lấy giáo trình",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private View createExistingModuleView(
            ModuleResponse module,
            ArrayList<Integer> moduleIds,
            int index,
            String lessonThumbnail
    ) {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.item_existing_module, null);

        TextView txtTitle = view.findViewById(R.id.txtExistingModuleTitle);
        TextView txtStatus = view.findViewById(R.id.txtExistingModuleStatus);
        TextView txtObjective = view.findViewById(R.id.txtExistingModuleObjective);
        TextView txtContent = view.findViewById(R.id.txtExistingModuleContent);
        TextView txtExample = view.findViewById(R.id.txtExistingModuleExample);

        MaterialButton btnView = view.findViewById(R.id.btnViewExistingModule);
        MaterialButton btnDelete = view.findViewById(R.id.btnDeleteExistingModule);

        txtTitle.setText(safe(module.getTitle()));

        txtStatus.setText("Trạng thái: " + String.valueOf(module.getStatus()));

        txtObjective.setText("Mục tiêu: " + safe(module.getObjective()));
        txtContent.setText("Nội dung: " + safe(module.getContent()));
        txtExample.setText("Ví dụ: " + safe(module.getExample()));

        String status = String.valueOf(module.getStatus());

        if ("PENDING_DELETE".equalsIgnoreCase(status)) {
            btnDelete.setEnabled(false);
            btnDelete.setText("Đang chờ xoá");
        }

        btnView.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherClass.this, LessonActivity.class);
            intent.putIntegerArrayListExtra("MODULE_IDS", moduleIds);
            intent.putExtra("CURRENT_INDEX", index);
            intent.putExtra("PARENT_LESSON_THUMBNAIL", lessonThumbnail);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> {
            Integer moduleId = module.getId();

            if (moduleId == null) {
                Toast.makeText(
                        TeacherClass.this,
                        "Không tìm thấy moduleId",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            confirmDeleteModule(moduleId, txtStatus, btnDelete);
        });

        return view;
    }

    private void confirmDeleteModule(
            Integer moduleId,
            TextView txtStatus,
            MaterialButton btnDelete
    ) {
        new AlertDialog.Builder(this)
                .setTitle("Yêu cầu xoá bài học")
                .setMessage("Bạn có chắc muốn gửi yêu cầu xoá bài học này không?")
                .setPositiveButton("Gửi yêu cầu", (dialog, which) -> {
                    requestDeleteModule(moduleId, txtStatus, btnDelete);
                })
                .setNegativeButton("Huỷ", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void requestDeleteModule(
            Integer moduleId,
            TextView txtStatus,
            MaterialButton btnDelete
    ) {
        ModuleApiService moduleApiService =
                RetrofitClient.getClient().create(ModuleApiService.class);

        btnDelete.setEnabled(false);
        btnDelete.setText("Đang gửi...");

        moduleApiService.deleteModule(moduleId)
                .enqueue(new Callback<ApiResponse<String>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<String>> call,
                            Response<ApiResponse<String>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<String> apiResponse = response.body();

                            if (apiResponse.getCode() == 1000) {
                                Toast.makeText(
                                        TeacherClass.this,
                                        "Đã gửi yêu cầu xoá bài học",
                                        Toast.LENGTH_SHORT
                                ).show();

                                txtStatus.setText("Trạng thái: PENDING_DELETE");
                                btnDelete.setText("Đang chờ xoá");
                                btnDelete.setEnabled(false);

                            } else {
                                btnDelete.setText("Yêu cầu xoá");
                                btnDelete.setEnabled(true);

                                Toast.makeText(
                                        TeacherClass.this,
                                        "Lỗi: " + apiResponse.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                        } else {
                            btnDelete.setText("Yêu cầu xoá");
                            btnDelete.setEnabled(true);

                            Toast.makeText(
                                    TeacherClass.this,
                                    "Gửi yêu cầu xoá thất bại, mã: " + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                        btnDelete.setText("Yêu cầu xoá");
                        btnDelete.setEnabled(true);

                        Log.e("DELETE_MODULE_ERROR", String.valueOf(t.getMessage()));

                        Toast.makeText(
                                TeacherClass.this,
                                "Lỗi kết nối khi xoá bài học",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void addEmptyModuleText(LinearLayout lessonContainer) {
        TextView textView = new TextView(this);
        textView.setText("Khóa học này chưa có bài học nào.");
        textView.setTextColor(Color.GRAY);
        textView.setTextSize(14);
        textView.setPadding(12, 16, 12, 8);

        lessonContainer.addView(textView);
    }
    private String safe(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) {
            return "";
        }

        return value.trim();
    }

}