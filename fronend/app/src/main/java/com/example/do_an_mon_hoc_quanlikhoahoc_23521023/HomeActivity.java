package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvCourses;
    private TextView txtEmpty;
    private MaterialCardView btnMenuCard;

    private CourseAdapter adapter;
    private final ArrayList<LessonResponse> suggestedLessons = new ArrayList<>();

    /*
     * Chống trùng lessonId.
     */
    private final Set<Integer> addedSuggestedLessonIds = new HashSet<>();

    /*
     * Chống request cũ trả về sau request mới.
     */
    private int suggestedLoadVersion = 0;

    private RequestQueue requestQueue;

    private static final String BASE_URL = "http://10.0.2.2:8080/NT118";

    private static final String PREF_NAME = "APP_PREFS";
    private static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String KEY_USER_ID = "USER_ID";

    private int currentStudentId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        initViews();
        setupRecyclerView();
        setupEvents();

        requestQueue = Volley.newRequestQueue(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        currentStudentId = getCurrentStudentId();

        /*
         * Chỉ load ở onResume.
         * Không load thêm ở onCreate để tránh gọi API 2 lần.
         */
        loadSuggestedCourses();
    }

    private void initViews() {
        rvCourses = findViewById(R.id.rvCourses);
        txtEmpty = findViewById(R.id.txtEmpty);
        btnMenuCard = findViewById(R.id.btnMenuCard);
    }

    private void setupRecyclerView() {
        rvCourses.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new CourseAdapter(suggestedLessons);
        rvCourses.setAdapter(adapter);
    }

    private void setupEvents() {
        btnMenuCard.setOnClickListener(v -> showSidebarMenu());
    }

    private int getCurrentStudentId() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    private String getToken() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, "");
    }

    /*
     * API:
     * GET /api/students/{userId}/lessons_not_enroll
     *
     * Backend trả result là danh sách ID khóa học sinh viên chưa đăng ký.
     */
    private void loadSuggestedCourses() {
        if (currentStudentId == -1) {
            clearSuggestedList();
            showEmpty("Không tìm thấy ID học sinh");
            return;
        }

        String token = getToken();

        if (token == null || token.trim().isEmpty()) {
            clearSuggestedList();
            showEmpty("Không tìm thấy token đăng nhập");
            return;
        }

        /*
         * Mỗi lần load tăng version.
         * Request cũ nếu trả về sau sẽ bị bỏ qua.
         */
        final int requestVersion = ++suggestedLoadVersion;

        clearSuggestedList();

        String url = BASE_URL + "/api/students/" + currentStudentId + "/lessons_not_enroll";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    if (requestVersion != suggestedLoadVersion) {
                        return;
                    }

                    JSONArray ids = response.optJSONArray("result");

                    if (ids == null || ids.length() == 0) {
                        showEmpty("Bạn đã tham gia tất cả khóa học.");
                        return;
                    }

                    hideEmpty();

                    for (int i = 0; i < ids.length(); i++) {
                        int lessonId = ids.optInt(i, -1);

                        if (lessonId == -1) {
                            continue;
                        }

                        /*
                         * Nếu API trả trùng id thì bỏ qua ngay tại đây.
                         */
                        if (addedSuggestedLessonIds.contains(lessonId)) {
                            continue;
                        }

                        fetchLessonDetailAndAdd(lessonId, requestVersion);
                    }
                },
                error -> {
                    if (requestVersion == suggestedLoadVersion) {
                        clearSuggestedList();
                        showEmpty("Không lấy được danh sách khóa học gợi ý");
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void fetchLessonDetailAndAdd(int lessonId, int requestVersion) {
        String token = getToken();

        String url = BASE_URL + "/api/lessons/" + lessonId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    /*
                     * Nếu request này là request cũ, bỏ qua.
                     */
                    if (requestVersion != suggestedLoadVersion) {
                        return;
                    }

                    /*
                     * Chặn lần 2 trước khi add vào list.
                     */
                    if (addedSuggestedLessonIds.contains(lessonId)) {
                        return;
                    }

                    JSONObject result = response.optJSONObject("result");

                    if (result == null) {
                        return;
                    }

                    LessonResponse lesson = parseLessonResponse(result, lessonId);

                    if (lesson == null || lesson.getId() == null) {
                        return;
                    }

                    /*
                     * Chặn lần 3 theo id thực tế trong response.
                     */
                    Integer realLessonId = lesson.getId();

                    if (addedSuggestedLessonIds.contains(realLessonId)) {
                        return;
                    }

                    addedSuggestedLessonIds.add(realLessonId);
                    suggestedLessons.add(lesson);

                    adapter.notifyItemInserted(suggestedLessons.size() - 1);

                    if (!suggestedLessons.isEmpty()) {
                        hideEmpty();
                    }
                },
                error -> {
                    /*
                     * Bỏ qua lesson lỗi, không add gì.
                     */
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private LessonResponse parseLessonResponse(JSONObject result, int fallbackLessonId) {
        LessonResponse lesson = new LessonResponse();

        lesson.setId(result.optInt("id", fallbackLessonId));

        lesson.setTitle(
                safe(
                        firstNonEmpty(
                                result.optString("title", ""),
                                result.optString("lessonTitle", "")
                        )
                )
        );

        lesson.setDescription(
                safe(
                        firstNonEmpty(
                                result.optString("description", ""),
                                result.optString("desc", "")
                        )
                )
        );

        lesson.setWhatYouLearn(
                safe(
                        firstNonEmpty(
                                result.optString("what_you_learn", ""),
                                result.optString("whatYouLearn", "")
                        )
                )
        );

        lesson.setSkillLearned(
                safe(
                        firstNonEmpty(
                                result.optString("skill_learned", ""),
                                result.optString("skillLearned", "")
                        )
                )
        );

        lesson.setThumbnailUrl(
                safe(
                        firstNonEmpty(
                                result.optString("thumbnail_url", ""),
                                result.optString("thumbnailUrl", "")
                        )
                )
        );

        lesson.setTeacher_name(
                safe(
                        firstNonEmpty(
                                result.optString("teacher_name", ""),
                                result.optString("teacherName", "")
                        )
                )
        );

        lesson.setCreatedAt(
                safe(
                        firstNonEmpty(
                                result.optString("createdAt", ""),
                                result.optString("created_at", "")
                        )
                )
        );

        lesson.setStatus(
                safe(result.optString("status", ""))
        );

        return lesson;
    }

    private String firstNonEmpty(String a, String b) {
        if (a != null && !a.trim().isEmpty() && !"null".equalsIgnoreCase(a.trim())) {
            return a;
        }

        if (b != null && !b.trim().isEmpty() && !"null".equalsIgnoreCase(b.trim())) {
            return b;
        }

        return "";
    }

    private void clearSuggestedList() {
        suggestedLessons.clear();
        addedSuggestedLessonIds.clear();

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void showEmpty(String message) {
        if (txtEmpty != null) {
            txtEmpty.setText(message);
            txtEmpty.setVisibility(View.VISIBLE);
        }

        if (rvCourses != null) {
            rvCourses.setVisibility(View.GONE);
        }
    }

    private void hideEmpty() {
        if (txtEmpty != null) {
            txtEmpty.setVisibility(View.GONE);
        }

        if (rvCourses != null) {
            rvCourses.setVisibility(View.VISIBLE);
        }
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
            window.setWindowAnimations(android.R.style.Animation_Translucent);
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
            });
        }

        if (menuCourses != null) {
            menuCourses.setOnClickListener(v -> {
                dialog.dismiss();
                openPage(CourseListActivity.class);
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

                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
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