package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseActivity extends AppCompatActivity {

    private ImageView imgCourseCover;
    private TextView txtCourseTitle;

    private TextView tabIntro;
    private TextView tabCurriculum;

    private LinearLayout containerIntro;
    private LinearLayout containerCurriculum;

    private TextView txtDescription;
    private TextView txtWhatYouLearn;
    private TextView txtSkillLearned;
    private TextView txtTeacher;

    private MaterialButton btnJoin;

    private Integer lessonId = -1;
    private boolean isEnrolled = false;
    private boolean modulesLoaded = false;

    private String thumbnailUrl = "";

    private final ArrayList<Integer> moduleIds = new ArrayList<>();

    private static final String PREF_NAME = "APP_PREFS";
    private static final String KEY_USER_ID = "USER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_course_detail);

        initViews();
        readIntentData();
        bindIntentData();
        setupState();
        setupEvents();

        fetchLessonDetailFromServer();
    }

    private void initViews() {
        imgCourseCover = findViewById(R.id.imgCourseCover);
        txtCourseTitle = findViewById(R.id.txtCourseTitle);

        tabIntro = findViewById(R.id.tabIntro);
        tabCurriculum = findViewById(R.id.tabCurriculum);

        containerIntro = findViewById(R.id.containerIntro);
        containerCurriculum = findViewById(R.id.containerCurriculum);

        txtDescription = findViewById(R.id.txtDescription);
        txtWhatYouLearn = findViewById(R.id.txtWhatYouLearn);
        txtSkillLearned = findViewById(R.id.txtSkillLearned);
        txtTeacher = findViewById(R.id.txtTeacher);

        btnJoin = findViewById(R.id.btnJoin);
    }

    private void readIntentData() {
        Intent intent = getIntent();

        /*
         * Key mới.
         */
        lessonId = intent.getIntExtra("LESSON_ID", -1);

        /*
         * Hỗ trợ key cũ từ StudentLessonAdapter cũ:
         * intent.putExtra("course_id", lesson.getId())
         */
        if (lessonId == -1) {
            lessonId = intent.getIntExtra("course_id", -1);
        }

        /*
         * Gợi ý khóa học: IS_ENROLLED = false
         * Khóa học đã đăng ký: IS_ENROLLED = true
         */
        isEnrolled = intent.getBooleanExtra("IS_ENROLLED", false);

        thumbnailUrl = safe(intent.getStringExtra("LESSON_THUMBNAIL"));

        if (thumbnailUrl.isEmpty()) {
            thumbnailUrl = safe(intent.getStringExtra("course_thumbnail"));
        }
    }

    private void bindIntentData() {
        Intent intent = getIntent();

        String title = safe(intent.getStringExtra("LESSON_TITLE"));
        String description = safe(intent.getStringExtra("LESSON_DESCRIPTION"));
        String whatYouLearn = safe(intent.getStringExtra("LESSON_WHAT_YOU_LEARN"));
        String skillLearned = safe(intent.getStringExtra("LESSON_SKILL_LEARNED"));
        String teacher = safe(intent.getStringExtra("LESSON_TEACHER"));

        /*
         * Hỗ trợ key cũ.
         */
        if (title.isEmpty()) {
            title = safe(intent.getStringExtra("course_name"));
        }

        if (txtCourseTitle != null) {
            txtCourseTitle.setText(title);
        }

        if (txtDescription != null) {
            txtDescription.setText(description);
        }

        if (txtWhatYouLearn != null) {
            if (whatYouLearn.isEmpty()) {
                txtWhatYouLearn.setText("Chưa cập nhật");
            } else {
                txtWhatYouLearn.setText("✓ " + whatYouLearn);
            }
        }

        if (txtSkillLearned != null) {
            if (skillLearned.isEmpty()) {
                txtSkillLearned.setText("Chưa cập nhật");
            } else {
                txtSkillLearned.setText(skillLearned);
            }
        }

        if (txtTeacher != null) {
            if (teacher.isEmpty()) {
                txtTeacher.setText("Giảng viên: Chưa cập nhật");
            } else {
                txtTeacher.setText("Giảng viên: " + teacher);
            }
        }

        loadThumbnail(thumbnailUrl);
    }

    private void setupState() {
        selectIntroTab();

        if (isEnrolled) {
            if (btnJoin != null) {
                btnJoin.setVisibility(View.GONE);
            }

            if (tabCurriculum != null) {
                tabCurriculum.setVisibility(View.VISIBLE);
            }

        } else {
            if (btnJoin != null) {
                btnJoin.setVisibility(View.VISIBLE);
            }

            if (tabCurriculum != null) {
                tabCurriculum.setVisibility(View.GONE);
            }
        }
    }

    private void setupEvents() {
        if (tabIntro != null) {
            tabIntro.setOnClickListener(v -> selectIntroTab());
        }

        if (tabCurriculum != null) {
            tabCurriculum.setOnClickListener(v -> {
                if (!isEnrolled) {
                    Toast.makeText(
                            CourseActivity.this,
                            "Bạn cần tham gia khóa học trước",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                selectCurriculumTab();

                if (!modulesLoaded) {
                    loadModulesByLessonId();
                }
            });
        }

        if (btnJoin != null) {
            btnJoin.setOnClickListener(v -> enrollCourse());
        }
    }

    private void fetchLessonDetailFromServer() {
        if (lessonId == null || lessonId == -1) {
            return;
        }

        LessonApiService lessonApiService =
                RetrofitClient.getClient().create(LessonApiService.class);

        lessonApiService.getLessonById(lessonId)
                .enqueue(new Callback<ApiResponse<LessonResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<LessonResponse>> call,
                            Response<ApiResponse<LessonResponse>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<LessonResponse> apiResponse = response.body();

                            if (apiResponse.getCode() == 1000 && apiResponse.getResult() != null) {
                                bindLessonFromApi(apiResponse.getResult());
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<LessonResponse>> call,
                            Throwable t
                    ) {
                        /*
                         * Không toast ở đây để tránh làm phiền.
                         * Vì intent đã có dữ liệu cơ bản rồi.
                         */
                    }
                });
    }

    private void bindLessonFromApi(LessonResponse lesson) {
        if (lesson == null) {
            return;
        }

        if (txtCourseTitle != null) {
            txtCourseTitle.setText(safe(lesson.getTitle()));
        }

        if (txtDescription != null) {
            txtDescription.setText(safe(lesson.getDescription()));
        }

        if (txtWhatYouLearn != null) {
            String whatYouLearn = safe(lesson.getWhatYouLearn());

            if (whatYouLearn.isEmpty()) {
                txtWhatYouLearn.setText("Chưa cập nhật");
            } else {
                txtWhatYouLearn.setText("✓ " + whatYouLearn);
            }
        }

        if (txtSkillLearned != null) {
            String skillLearned = safe(lesson.getSkillLearned());

            if (skillLearned.isEmpty()) {
                txtSkillLearned.setText("Chưa cập nhật");
            } else {
                txtSkillLearned.setText(skillLearned);
            }
        }

        if (txtTeacher != null) {
            String teacher = safe(lesson.getTeacher_name());

            if (teacher.isEmpty()) {
                txtTeacher.setText("Giảng viên: Chưa cập nhật");
            } else {
                txtTeacher.setText("Giảng viên: " + teacher);
            }
        }

        thumbnailUrl = safe(lesson.getThumbnailUrl());
        loadThumbnail(thumbnailUrl);
    }

    private void loadThumbnail(String url) {
        if (imgCourseCover == null) {
            return;
        }

        if (!safe(url).isEmpty()) {
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.course_python)
                    .error(R.drawable.course_python)
                    .into(imgCourseCover);
        } else {
            imgCourseCover.setImageResource(R.drawable.course_python);
        }
    }

    private void selectIntroTab() {
        if (containerIntro != null) {
            containerIntro.setVisibility(View.VISIBLE);
        }

        if (containerCurriculum != null) {
            containerCurriculum.setVisibility(View.GONE);
        }

        if (tabIntro != null) {
            tabIntro.setTextColor(Color.parseColor("#2196F3"));
            tabIntro.setTypeface(null, Typeface.BOLD);
        }

        if (tabCurriculum != null) {
            tabCurriculum.setTextColor(Color.GRAY);
            tabCurriculum.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void selectCurriculumTab() {
        if (containerIntro != null) {
            containerIntro.setVisibility(View.GONE);
        }

        if (containerCurriculum != null) {
            containerCurriculum.setVisibility(View.VISIBLE);
        }

        if (tabCurriculum != null) {
            tabCurriculum.setTextColor(Color.parseColor("#2196F3"));
            tabCurriculum.setTypeface(null, Typeface.BOLD);
        }

        if (tabIntro != null) {
            tabIntro.setTextColor(Color.GRAY);
            tabIntro.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void enrollCourse() {
        if (lessonId == null || lessonId == -1) {
            Toast.makeText(this, "Không tìm thấy khóa học", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = getCurrentUserId();

        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy học sinh đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        StudentCourseApiService apiService =
                RetrofitClient.getClient().create(StudentCourseApiService.class);

        StudentCourseRequest request = new StudentCourseRequest(userId, lessonId);

        apiService.enrollCourse(request)
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
                                        CourseActivity.this,
                                        "Đăng ký khóa học thành công",
                                        Toast.LENGTH_SHORT
                                ).show();

                                /*
                                 * Sau khi tham gia:
                                 * - Ẩn nút THAM GIA
                                 * - Hiện tab Giáo trình
                                 * - Không tự bắt buộc chuyển tab, nhưng load sẵn giáo trình.
                                 */
                                isEnrolled = true;

                                if (btnJoin != null) {
                                    btnJoin.setVisibility(View.GONE);
                                }

                                if (tabCurriculum != null) {
                                    tabCurriculum.setVisibility(View.VISIBLE);
                                }

                                loadModulesByLessonId();

                            } else {
                                Toast.makeText(
                                        CourseActivity.this,
                                        "Lỗi: " + apiResponse.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                        } else {
                            Toast.makeText(
                                    CourseActivity.this,
                                    "Đăng ký thất bại, HTTP " + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<String>> call,
                            Throwable t
                    ) {
                        Toast.makeText(
                                CourseActivity.this,
                                "Lỗi kết nối khi đăng ký khóa học",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void loadModulesByLessonId() {
        if (lessonId == null || lessonId == -1) {
            Toast.makeText(this, "Không tìm thấy khóa học", Toast.LENGTH_SHORT).show();
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
                                renderModules(apiResponse.getResult());
                            } else {
                                showEmptyCurriculum("Không lấy được giáo trình");
                            }

                        } else {
                            showEmptyCurriculum("Không lấy được giáo trình, HTTP " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<List<ModuleResponse>>> call,
                            Throwable t
                    ) {
                        showEmptyCurriculum("Lỗi kết nối khi lấy giáo trình");
                    }
                });
    }

    private void renderModules(List<ModuleResponse> modules) {
        modulesLoaded = true;

        if (containerCurriculum == null) {
            return;
        }

        containerCurriculum.removeAllViews();
        moduleIds.clear();

        if (modules == null || modules.isEmpty()) {
            showEmptyCurriculum("Khóa học chưa có giáo trình");
            return;
        }

        ArrayList<ModuleResponse> activeModules = new ArrayList<>();

        for (ModuleResponse module : modules) {
            if (module == null || module.getId() == null) {
                continue;
            }

            String status = safe(module.getStatus());

            if (status.isEmpty() || "ACTIVE".equalsIgnoreCase(status)) {
                activeModules.add(module);
                moduleIds.add(module.getId());
            }
        }

        if (activeModules.isEmpty()) {
            showEmptyCurriculum("Khóa học chưa có giáo trình");
            return;
        }

        for (ModuleResponse module : activeModules) {
            int index = moduleIds.indexOf(module.getId());

            View moduleView = createModuleCard(module, index);
            containerCurriculum.addView(moduleView);
        }
    }

    private View createModuleCard(ModuleResponse module, int index) {
        MaterialCardView card = new MaterialCardView(this);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 10, 0, 10);
        card.setLayoutParams(cardParams);

        card.setRadius(18f);
        card.setCardElevation(4f);
        card.setCardBackgroundColor(Color.parseColor("#A7D8F5"));
        card.setStrokeWidth(2);
        card.setStrokeColor(Color.parseColor("#2196F3"));
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(18, 16, 18, 16);

        TextView txtTitle = new TextView(this);
        txtTitle.setText(safe(module.getTitle()));
        txtTitle.setTextSize(17);
        txtTitle.setTextColor(Color.parseColor("#112D4E"));
        txtTitle.setTypeface(null, Typeface.BOLD);

        TextView txtContent = new TextView(this);
        txtContent.setText("Nội dung: " + safe(module.getContent()));
        txtContent.setTextSize(14);
        txtContent.setTextColor(Color.parseColor("#112D4E"));
        txtContent.setPadding(0, 8, 0, 0);

        MaterialButton btnOpen = new MaterialButton(this);
        btnOpen.setText("Xem bài học");
        btnOpen.setTextColor(Color.parseColor("#2196F3"));
        btnOpen.setTextSize(14);
        btnOpen.setAllCaps(false);
        btnOpen.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(Color.parseColor("#BBDEFB"))
        );

        LinearLayout.LayoutParams btnParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        48
                );

        btnParams.setMargins(0, 14, 0, 0);
        btnOpen.setLayoutParams(btnParams);

        btnOpen.setOnClickListener(v -> openModule(index));
        card.setOnClickListener(v -> openModule(index));

        root.addView(txtTitle);
        root.addView(txtContent);
        root.addView(btnOpen);

        card.addView(root);

        return card;
    }

    private void openModule(int index) {
        if (moduleIds.isEmpty()) {
            Toast.makeText(this, "Không có bài giảng để mở", Toast.LENGTH_SHORT).show();
            return;
        }

        if (index < 0 || index >= moduleIds.size()) {
            Toast.makeText(this, "Vị trí bài giảng không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(CourseActivity.this, LessonActivity.class);
        intent.putIntegerArrayListExtra("MODULE_IDS", moduleIds);
        intent.putExtra("CURRENT_INDEX", index);
        intent.putExtra("PARENT_LESSON_THUMBNAIL", thumbnailUrl);

        startActivity(intent);
    }

    private void showEmptyCurriculum(String message) {
        if (containerCurriculum == null) {
            return;
        }

        containerCurriculum.removeAllViews();

        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextColor(Color.GRAY);
        textView.setTextSize(15);
        textView.setPadding(12, 16, 12, 16);

        containerCurriculum.addView(textView);
    }

    private int getCurrentUserId() {
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