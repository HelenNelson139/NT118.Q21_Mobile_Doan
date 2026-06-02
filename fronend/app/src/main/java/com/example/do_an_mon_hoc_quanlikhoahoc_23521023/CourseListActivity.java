package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseListActivity extends AppCompatActivity {

    private MaterialCardView btnMenuCard;
    private RecyclerView rvStudentCourses;
    private EditText edtSearch;
    private TextView txtListTitle;

    private StudentLessonAdapter studentAdapter;

    private final List<LessonResponse> enrolledLessons = new ArrayList<>();
    private final List<LessonResponse> originalLessons = new ArrayList<>();

    /*
     * Chặn trùng lessonId.
     */
    private final Set<Integer> addedEnrolledLessonIds = new HashSet<>();

    /*
     * Chặn request cũ trả về sau request mới.
     */
    private int enrolledLoadVersion = 0;

    private LessonApiService lessonApiService;
    private StudentCourseApiService studentCourseApiService;

    private static final String PREF_NAME = "APP_PREFS";
    private static final String KEY_USER_ID = "USER_ID";

    private int currentStudentId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.courses);

        initViews();
        setupRecyclerView();
        setupServices();
        setupEvents();

        /*
         * Không gọi fetchEnrolledLessonIds() ở đây.
         * Chỉ gọi trong onResume() để tránh load 2 lần.
         */
    }

    @Override
    protected void onResume() {
        super.onResume();

        currentStudentId = getCurrentStudentId();
        fetchEnrolledLessonIds();
    }

    private void initViews() {
        btnMenuCard = findViewById(R.id.btnMenuCard);
        rvStudentCourses = findViewById(R.id.rvCourseList);
        edtSearch = findViewById(R.id.edtSearch);
        txtListTitle = findViewById(R.id.txtListTitle);

        if (txtListTitle != null) {
            txtListTitle.setText("Danh sách khóa học đã đăng ký");
        }
    }

    private void setupRecyclerView() {
        rvStudentCourses.setLayoutManager(new LinearLayoutManager(this));

        studentAdapter = new StudentLessonAdapter(this, enrolledLessons);
        rvStudentCourses.setAdapter(studentAdapter);
    }

    private void setupServices() {
        lessonApiService = RetrofitClient.getClient().create(LessonApiService.class);
        studentCourseApiService = RetrofitClient.getClient().create(StudentCourseApiService.class);
    }

    private void setupEvents() {
        btnMenuCard.setOnClickListener(view -> showSidebarMenu());

        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterLocalLessons(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        View icFilter = findViewById(R.id.icFilter);

        if (icFilter != null) {
            icFilter.setOnClickListener(v -> {
                String query = "";

                if (edtSearch != null) {
                    query = edtSearch.getText().toString().trim();
                }

                filterLocalLessons(query);
            });
        }
    }

    private int getCurrentStudentId() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    /*
     * API:
     * GET /api/students/{userId}/lessons
     *
     * Backend trả result là danh sách lessonId sinh viên đã đăng ký.
     */
    private void fetchEnrolledLessonIds() {
        if (currentStudentId == -1) {
            clearCourseList();
            Toast.makeText(this, "Không tìm thấy ID học sinh", Toast.LENGTH_SHORT).show();
            return;
        }

        final int requestVersion = ++enrolledLoadVersion;

        clearCourseList();

        studentCourseApiService.getStudentLessonIds(currentStudentId)
                .enqueue(new Callback<ApiResponse<List<Integer>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<Integer>>> call,
                            Response<ApiResponse<List<Integer>>> response
                    ) {
                        /*
                         * Nếu request này là request cũ, bỏ qua.
                         */
                        if (requestVersion != enrolledLoadVersion) {
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<Integer>> apiResponse = response.body();

                            if (apiResponse.getCode() == 1000) {
                                List<Integer> lessonIds = apiResponse.getResult();

                                if (lessonIds == null || lessonIds.isEmpty()) {
                                    Toast.makeText(
                                            CourseListActivity.this,
                                            "Bạn chưa đăng ký khóa học nào",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    return;
                                }

                                /*
                                 * Dùng LinkedHashSet để:
                                 * - Loại trùng ID từ backend nếu backend trả trùng
                                 * - Giữ nguyên thứ tự
                                 */
                                Set<Integer> uniqueLessonIds = new LinkedHashSet<>(lessonIds);

                                for (Integer lessonId : uniqueLessonIds) {
                                    if (lessonId == null) {
                                        continue;
                                    }

                                    if (addedEnrolledLessonIds.contains(lessonId)) {
                                        continue;
                                    }

                                    fetchLessonDetailAndAdd(lessonId, requestVersion);
                                }

                            } else {
                                Toast.makeText(
                                        CourseListActivity.this,
                                        "Lỗi: " + apiResponse.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                        } else {
                            Toast.makeText(
                                    CourseListActivity.this,
                                    "Không lấy được danh sách khóa học đã đăng ký",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<List<Integer>>> call,
                            Throwable t
                    ) {
                        if (requestVersion == enrolledLoadVersion) {
                            Toast.makeText(
                                    CourseListActivity.this,
                                    "Lỗi kết nối khi lấy khóa học đã đăng ký",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                });
    }

    private void fetchLessonDetailAndAdd(Integer lessonId, int requestVersion) {
        lessonApiService.getLessonById(lessonId)
                .enqueue(new Callback<ApiResponse<LessonResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<LessonResponse>> call,
                            Response<ApiResponse<LessonResponse>> response
                    ) {
                        /*
                         * Nếu request cũ trả về sau, bỏ qua.
                         */
                        if (requestVersion != enrolledLoadVersion) {
                            return;
                        }

                        /*
                         * Chặn trùng trước khi add.
                         */
                        if (addedEnrolledLessonIds.contains(lessonId)) {
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<LessonResponse> apiResponse = response.body();

                            if (apiResponse.getCode() == 1000 && apiResponse.getResult() != null) {
                                LessonResponse lesson = apiResponse.getResult();

                                if (lesson == null || lesson.getId() == null) {
                                    return;
                                }

                                Integer realLessonId = lesson.getId();

                                /*
                                 * Chặn trùng theo id thật từ API lesson detail.
                                 */
                                if (addedEnrolledLessonIds.contains(realLessonId)) {
                                    return;
                                }

                                addedEnrolledLessonIds.add(realLessonId);

                                originalLessons.add(lesson);
                                enrolledLessons.add(lesson);

                                studentAdapter.notifyItemInserted(enrolledLessons.size() - 1);
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<LessonResponse>> call,
                            Throwable t
                    ) {
                        /*
                         * Bỏ qua lesson lỗi.
                         * Không add gì để tránh dữ liệu sai.
                         */
                    }
                });
    }

    private void clearCourseList() {
        enrolledLessons.clear();
        originalLessons.clear();
        addedEnrolledLessonIds.clear();

        if (studentAdapter != null) {
            studentAdapter.notifyDataSetChanged();
        }
    }

    private void filterLocalLessons(String keyword) {
        String query = safe(keyword).toLowerCase();

        enrolledLessons.clear();

        if (query.isEmpty()) {
            enrolledLessons.addAll(originalLessons);
        } else {
            for (LessonResponse lesson : originalLessons) {
                if (lesson == null) {
                    continue;
                }

                String title = safe(lesson.getTitle()).toLowerCase();
                String description = safe(lesson.getDescription()).toLowerCase();

                if (title.contains(query) || description.contains(query)) {
                    enrolledLessons.add(lesson);
                }
            }
        }

        studentAdapter.notifyDataSetChanged();
    }

    private void showSidebarMenu() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_sidebar);

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
        LinearLayout menuCourses = dialog.findViewById(R.id.menuCourses);
        LinearLayout menuProfile = dialog.findViewById(R.id.menuProfile);
        TextView txtLogout = dialog.findViewById(R.id.txtLogout);
        TextView tvUserName = dialog.findViewById(R.id.tvUserName);

        if (tvUserName != null) {
            tvUserName.setText("Học viên");
        }

        if (btnCloseMenu != null) {
            btnCloseMenu.setOnClickListener(v -> dialog.dismiss());
        }

        if (menuHome != null) {
            menuHome.setOnClickListener(v -> {
                dialog.dismiss();
                openPage(HomeActivity.class);
            });
        }

        if (menuCourses != null) {
            menuCourses.setOnClickListener(v -> {
                dialog.dismiss();
            });
        }

        if (menuProfile != null) {
            menuProfile.setOnClickListener(v -> {
                dialog.dismiss();
                openPage(ProfileActivity.class);
            });
        }

        if (txtLogout != null) {
            txtLogout.setOnClickListener(v -> {
                SharedPreferences sharedPreferences =
                        getSharedPreferences(PREF_NAME, MODE_PRIVATE);

                sharedPreferences.edit().clear().apply();

                Intent intent = new Intent(CourseListActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                dialog.dismiss();
                finish();
            });
        }

        dialog.show();
    }
    private void openPage(Class<?> targetActivity) {
        if (this.getClass().equals(targetActivity)) {
            return;
        }

        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(intent);
    }

    private String safe(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) {
            return "";
        }

        return value.trim();
    }
}