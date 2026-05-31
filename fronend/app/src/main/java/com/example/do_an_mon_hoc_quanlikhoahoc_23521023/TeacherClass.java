package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherClass extends AppCompatActivity {
    private RecyclerView rvCourseList;
    private List<LessonResponse> filteredList;
    private TeacherCourseAdapter adapter;
    private MaterialCardView btnMenuCard;
    private View btnAddCourse;
    private EditText edtSearchCourse;

    private Uri selectedImageUri;
    private ImageView imgPreview;
    private ActivityResultLauncher<String> imagePickerLauncher;

    private final List<View> lessonViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_classes);

        btnMenuCard = findViewById(R.id.btnMenuCard);
        btnAddCourse = findViewById(R.id.btnAddCourse);
        edtSearchCourse = findViewById(R.id.edtSearch);
        rvCourseList = findViewById(R.id.rvCourseList);
        rvCourseList.setLayoutManager(new LinearLayoutManager(this));
        filteredList = new ArrayList<>();

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

                        if (imgPreview.getId() == R.id.imgCourseCover && lessonViews.isEmpty()) {
                            selectedImageUri = uri;
                        }
                    }
                }
        );

        btnMenuCard.setOnClickListener(v -> showSidebarMenu());
        btnAddCourse.setOnClickListener(v -> showAddDialog());
        fetchTeacherLessonsFromServer();

        if (edtSearchCourse != null) {
            edtSearchCourse.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

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
                public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    private void fetchTeacherLessonsFromServer() {
        LessonApiService apiService = RetrofitClient.getClient().create(LessonApiService.class);

        apiService.getMyLessons().enqueue(new Callback<ApiResponse<List<LessonResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<LessonResponse>>> call, Response<ApiResponse<List<LessonResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<LessonResponse>> apiResponse = response.body();

                    if (apiResponse.getCode() == 1000) {
                        List<LessonResponse> remoteLessons = apiResponse.getResult();

                        filteredList.clear();
                        if (remoteLessons != null && !remoteLessons.isEmpty()) {
                            filteredList.addAll(remoteLessons);
                        } else {
                            Toast.makeText(TeacherClass.this, "Bạn chưa đăng tải bài học nào!", Toast.LENGTH_SHORT).show();
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(TeacherClass.this, "Lỗi Server: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TeacherClass.this, "Lỗi kết nối, mã: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<LessonResponse>>> call, Throwable t) {
                Log.e("TEACHER_GET_LESSON", "Thất bại: " + t.getMessage());
                Toast.makeText(TeacherClass.this, "Không thể kết nối đến máy chủ!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchTeacherLessonsFromServer(String keyword) {
        LessonApiService apiService = RetrofitClient.getClient().create(LessonApiService.class);
        apiService.searchLessons(keyword).enqueue(new Callback<ApiResponse<List<LessonResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<LessonResponse>>> call, Response<ApiResponse<List<LessonResponse>>> response) {
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
            android.content.SharedPreferences sharedPreferences = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
            int currentUserId = sharedPreferences.getInt("USER_ID", 1);
            String teacherIdStr = String.valueOf(currentUserId);

            String title = edtTitle.getText().toString().trim();
            String description = edtDescription.getText().toString().trim();
            String whatYouLearn = edtWhatYouLearn.getText().toString().trim();
            String skills = edtSkills.getText().toString().trim();

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            MultipartBody.Part titlePart = MultipartBody.Part.createFormData("title", title);
            MultipartBody.Part descriptionPart = MultipartBody.Part.createFormData("description", description);
            MultipartBody.Part whatYouLearnPart = MultipartBody.Part.createFormData("what_you_learn", whatYouLearn);
            MultipartBody.Part skillLearnedPart = MultipartBody.Part.createFormData("skill_learned", skills);
            MultipartBody.Part teacherIdPart = MultipartBody.Part.createFormData("teacherId", teacherIdStr);
            java.io.File file = getFileFromUri(selectedImageUri);
            if (file == null) {
                Toast.makeText(this, "Vui lòng chọn ảnh bìa!", Toast.LENGTH_SHORT).show();
                return;
            }

            String mimeType = getContentResolver().getType(selectedImageUri);
            if (mimeType == null) {
                mimeType = "image/jpeg";
            }

            RequestBody requestFile = RequestBody.create(okhttp3.MediaType.parse(mimeType), file);
            MultipartBody.Part thumbnailPart = MultipartBody.Part.createFormData("thumbnail", file.getName(), requestFile);

            LessonApiService apiService = RetrofitClient.getClient().create(LessonApiService.class);

            apiService.createLesson(titlePart, descriptionPart, whatYouLearnPart, skillLearnedPart, teacherIdPart, thumbnailPart)
                    .enqueue(new Callback<ApiResponse<LessonResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<LessonResponse>> call, Response<ApiResponse<LessonResponse>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ApiResponse<LessonResponse> apiResponse = response.body();
                                if (apiResponse.getCode() == 1000) {
                                    Toast.makeText(TeacherClass.this, "Thêm khóa học thành công!", Toast.LENGTH_SHORT).show();
                                    fetchTeacherLessonsFromServer();
                                    int newLessonId = apiResponse.getResult().getId();
                                    uploadModules(newLessonId, dialog);
                                } else {
                                    Toast.makeText(TeacherClass.this, "Lỗi: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                try {
                                    String errLog = response.errorBody() != null ? response.errorBody().string() : "Mã lỗi trống";
                                    Log.e("SERVER_400_LOG", errLog);
                                } catch (Exception e) { e.printStackTrace(); }
                                Toast.makeText(TeacherClass.this, "Không thể lưu, mã phản hồi: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<LessonResponse>> call, Throwable t) {
                            Toast.makeText(TeacherClass.this, "Lỗi kết nối mạng!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        dialog.show();
    }

    private void showEditDialog(int position) {
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

        edtTitle.setText(oldCourse.getTitle());
        if (edtDescription != null) edtDescription.setText(oldCourse.getDescription());
        if (edtWhatYouLearn != null) edtWhatYouLearn.setText(oldCourse.getWhatYouLearn());
        if (edtSkills != null) edtSkills.setText(oldCourse.getSkillLearned());
        if (btnUploadImage != null) {
            btnUploadImage.setVisibility(View.GONE);
        }
        imgCourseCover.setImageResource(R.drawable.course_python);

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
        btnAddLesson.setOnClickListener(v -> addLesson(lessonContainer));

        btnSave.setOnClickListener(v -> {
            String newTitle = edtTitle.getText().toString().trim();
            String newDescription = edtDescription != null ? edtDescription.getText().toString().trim() : "";
            String newWhatYouLearn = edtWhatYouLearn != null ? edtWhatYouLearn.getText().toString().trim() : "";
            String newSkills = edtSkills != null ? edtSkills.getText().toString().trim() : "";

            if (newTitle.isEmpty()) {
                edtTitle.setError("Nhập tên khóa học");
                return;
            }

            LessonUpdateRequest updateRequest = new LessonUpdateRequest(newTitle, newDescription, newWhatYouLearn, newSkills);

            LessonApiService apiService = RetrofitClient.getClient().create(LessonApiService.class);
            apiService.updateLesson(oldCourse.getId(), updateRequest).enqueue(new Callback<ApiResponse<String>>() {
                @Override
                public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<String> apiResponse = response.body();
                        if (apiResponse.getCode() == 1000) {
                            Toast.makeText(TeacherClass.this, "Đã cập nhật thông tin khóa học thành công!", Toast.LENGTH_SHORT).show();
                            fetchTeacherLessonsFromServer();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(TeacherClass.this, "Lỗi cập nhật: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(TeacherClass.this, "Không thể cập nhật, mã phản hồi: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                    Log.e("UPDATE_LESSON_ERROR", "Thất bại: " + t.getMessage());
                    Toast.makeText(TeacherClass.this, "Lỗi mạng, vui lòng thử lại sau!", Toast.LENGTH_SHORT).show();
                }
            });
        })  ;


        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void setupTabs(TabLayout tabLayout, LinearLayout containerIntro, LinearLayout containerCurriculum) {
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText("Giới thiệu"));
        tabLayout.addTab(tabLayout.newTab().setText("Giáo trình"));

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TextView tabTextView = (TextView) LayoutInflater.from(this)
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
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void addLesson(LinearLayout lessonContainer) {
        View lessonView = LayoutInflater.from(this).inflate(R.layout.item_lesson, null);

        ImageView imgLesson = lessonView.findViewById(R.id.imgCourseCover);
        MaterialButton btnPickImage = lessonView.findViewById(R.id.btnUploadImage);

        btnPickImage.setOnClickListener(v -> {
            imgPreview = imgLesson;
            imagePickerLauncher.launch("image/*");
        });

        if (!lessonViews.isEmpty()) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.topMargin = 20;
            lessonView.setLayoutParams(params);
        }

        lessonContainer.addView(lessonView);
        lessonViews.add(lessonView);
    }

    private void showDeleteDialog(int position) {
        LessonResponse courseToDelete = filteredList.get(position);
        Integer lessonId = courseToDelete.getId();

        new AlertDialog.Builder(this)
                .setTitle("Xóa khóa học")
                .setMessage("Bạn có chắc muốn xóa khóa học này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {

                    LessonApiService apiService = RetrofitClient.getClient().create(LessonApiService.class);
                    apiService.deleteLesson(lessonId).enqueue(new Callback<ApiResponse<String>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ApiResponse<String> apiResponse = response.body();

                                if (apiResponse.getCode() == 1000) {
                                    filteredList.remove(position);
                                    adapter.notifyItemRemoved(position);
                                    adapter.notifyItemRangeChanged(position, filteredList.size());

                                    Toast.makeText(TeacherClass.this, "Đã xóa khóa học thành công!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(TeacherClass.this, "Lỗi Server: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(TeacherClass.this, "Xóa thất bại, mã phản hồi: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                            Log.e("DELETE_LESSON_ERROR", "Lỗi: " + t.getMessage());
                            Toast.makeText(TeacherClass.this, "Lỗi kết nối mạng!", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
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

        menuMyClasses.setOnClickListener(v -> {
            dialog.dismiss();
        });

        txtLogout.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherClass.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }

    private java.io.File getFileFromUri(Uri uri) {
        if (uri == null) return null;
        try {
            String extension = ".jpg";
            String mimeType = getContentResolver().getType(uri);
            if (mimeType != null) {
                if (mimeType.contains("png")) extension = ".png";
                else if (mimeType.contains("webp")) extension = ".webp";
            }

            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            java.io.File tempFile = java.io.File.createTempFile("upload_", extension, getCacheDir());
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile);

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

    private void uploadModules(int newLessonId, Dialog dialog) {
        if (lessonViews.isEmpty()) {
            Toast.makeText(TeacherClass.this, "Thêm khóa học thành công!", Toast.LENGTH_SHORT).show();
            fetchTeacherLessonsFromServer();
            dialog.dismiss();
            return;
        }

        ModuleApiService moduleApiService = RetrofitClient.getClient().create(ModuleApiService.class);
        int totalModules = lessonViews.size();
        int[] completed = {0};
        int[] failed = {0}; // Thêm biến đếm số module bị lỗi

        for (int i = 0; i < totalModules; i++) {
            View lessonView = lessonViews.get(i);

            EditText edtModuleTitle = lessonView.findViewById(R.id.txtModuleTitle);
            EditText edtObjective = lessonView.findViewById(R.id.txtObjective);
            EditText edtContent = lessonView.findViewById(R.id.txtContent);
            EditText edtExample = lessonView.findViewById(R.id.txtExample);
            ImageView imgLesson = lessonView.findViewById(R.id.imgCourseCover);

            String title = edtModuleTitle != null && !edtModuleTitle.getText().toString().isEmpty()
                    ? edtModuleTitle.getText().toString().trim() : "Module " + (i + 1);
            String objective = edtObjective != null ? edtObjective.getText().toString().trim() : "";
            String content = edtContent != null ? edtContent.getText().toString().trim() : "";
            String example = edtExample != null ? edtExample.getText().toString().trim() : "";

            Uri moduleImageUri = (Uri) imgLesson.getTag();

            RequestBody lessonIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(newLessonId));
            RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), title);
            RequestBody objectiveBody = RequestBody.create(MediaType.parse("text/plain"), objective);
            RequestBody contentBody = RequestBody.create(MediaType.parse("text/plain"), content);
            RequestBody exampleBody = RequestBody.create(MediaType.parse("text/plain"), example);
            RequestBody orderIndexBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(i + 1));

            MultipartBody.Part imagePart = null;
            if (moduleImageUri != null) {
                java.io.File file = getFileFromUri(moduleImageUri);
                if (file != null) {
                    String mimeType = getContentResolver().getType(moduleImageUri);
                    if (mimeType == null) mimeType = "image/jpeg";
                    RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);
                    imagePart = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
                }
            }

            moduleApiService.createModule(lessonIdBody, titleBody, objectiveBody, contentBody, exampleBody, orderIndexBody, imagePart)
                    .enqueue(new Callback<ApiResponse<ModuleResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<ModuleResponse>> call, Response<ApiResponse<ModuleResponse>> response) {
                            completed[0]++;
                            if (response.isSuccessful() && response.body() != null) {
                                // Thành công thật sự
                                Log.d("MODULE_UPLOAD", "Upload thành công module: " + response.body().getMessage());
                            } else {
                                // Server báo lỗi (Ví dụ 400 Bad Request)
                                failed[0]++;
                                try {
                                    String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                                    Log.e("SERVER_MODULE_ERROR", "Lỗi Server từ chối Module: " + errorBody);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            checkAllModulesUploaded(completed[0], failed[0], totalModules, dialog);
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<ModuleResponse>> call, Throwable t) {
                            completed[0]++;
                            failed[0]++;
                            Log.e("MODULE_UPLOAD", "Lỗi mạng khi upload module: " + t.getMessage());
                            checkAllModulesUploaded(completed[0], failed[0], totalModules, dialog);
                        }
                    });
        }
    }

    private void checkAllModulesUploaded(int completedCount, int failedCount, int totalModules, Dialog dialog) {
        if (completedCount == totalModules) {
            if (failedCount > 0) {
                Toast.makeText(TeacherClass.this, "Đã thêm khóa học, nhưng có " + failedCount + " bài học bị lỗi không lưu được!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(TeacherClass.this, "Thêm khóa học và giáo trình thành công!", Toast.LENGTH_SHORT).show();
            }
            fetchTeacherLessonsFromServer();
            dialog.dismiss();
        }
    }
}