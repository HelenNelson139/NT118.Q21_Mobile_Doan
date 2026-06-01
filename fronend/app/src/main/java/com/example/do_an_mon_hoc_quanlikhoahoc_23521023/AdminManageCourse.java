package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class AdminManageCourse extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private MaterialCardView btnMenu;
    private RecyclerView rvCourseList;
    private EditText edtSearch;
    private TextView txtEmpty;
    private TextView txtListTitle;

    private MaterialButton btnApprovedCourses;
    private MaterialButton btnPendingCourses;

    private AdminCourseSummaryAdapter adapter;
    private List<LessonResponse> lessonList;

    private RequestQueue requestQueue;

    private static final String BASE_URL = "http://10.0.2.2:8080/NT118";

    private static final String PREF_NAME = "APP_PREFS";
    private static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";

    private static final int FILTER_APPROVED = 1;
    private static final int FILTER_PENDING = 2;

    private int currentFilter = FILTER_APPROVED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_course_manage);

        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenuCard);
        rvCourseList = findViewById(R.id.rvCourseList);
        edtSearch = findViewById(R.id.edtSearch);
        txtEmpty = findViewById(R.id.txtEmpty);
        txtListTitle = findViewById(R.id.txtListTitle);

        btnApprovedCourses = findViewById(R.id.btnApprovedCourses);
        btnPendingCourses = findViewById(R.id.btnPendingCourses);

        requestQueue = Volley.newRequestQueue(this);

        AdminSidebarNavigationHelper.setupSidebar(this, drawerLayout);

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        setupRecyclerView();
        setupFilterButtons();
        setupSearch();
        setupBackPressed();

        fetchApprovedCoursesFromServer();
    }

    private void setupRecyclerView() {
        lessonList = new ArrayList<>();
        rvCourseList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminCourseSummaryAdapter(this, lessonList);
        rvCourseList.setAdapter(adapter);
    }

    private void setupFilterButtons() {
        btnApprovedCourses.setOnClickListener(v -> fetchApprovedCoursesFromServer());
        btnPendingCourses.setOnClickListener(v -> fetchPendingCoursesFromServer());

        updateFilterButtonStyle(FILTER_APPROVED);
    }

    private void updateFilterButtonStyle(int filter) {
        currentFilter = filter;

        if (filter == FILTER_APPROVED) {
            btnApprovedCourses.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#3F72AF"))
            );
            btnPendingCourses.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#112D4E"))
            );

            txtListTitle.setText("Danh sách khóa học đã duyệt");
        } else {
            btnApprovedCourses.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#112D4E"))
            );
            btnPendingCourses.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#3F72AF"))
            );

            txtListTitle.setText("Danh sách khóa học chưa duyệt");
        }

        btnApprovedCourses.setTextColor(Color.WHITE);
        btnPendingCourses.setTextColor(Color.WHITE);
    }

    private void fetchApprovedCoursesFromServer() {
        updateFilterButtonStyle(FILTER_APPROVED);

        String url = BASE_URL + "/api/lessons/allActive";

        fetchCoursesByUrl(
                url,
                "Không có khóa học đã duyệt nào",
                "Lỗi lấy danh sách khóa học đã duyệt"
        );
    }

    private void fetchPendingCoursesFromServer() {
        updateFilterButtonStyle(FILTER_PENDING);

        String url = BASE_URL + "/api/lessons/allPending";

        fetchCoursesByUrl(
                url,
                "Không có khóa học chưa duyệt nào",
                "Lỗi lấy danh sách khóa học chưa duyệt"
        );
    }

    private void fetchCoursesByUrl(
            String url,
            String emptyMessage,
            String errorMessage
    ) {
        String token = getToken();

        if (token == null || token.trim().isEmpty()) {
            showEmptyState("Không tìm thấy token đăng nhập");
            Toast.makeText(this, "Không tìm thấy token đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> handleLessonResponse(response, emptyMessage),
                error -> {
                    String message = errorMessage;

                    if (error.networkResponse != null) {
                        message += ": HTTP " + error.networkResponse.statusCode;

                        try {
                            String responseBody = new String(error.networkResponse.data);
                            message += "\n" + responseBody;
                        } catch (Exception ignored) {
                        }
                    } else {
                        message += ": " + error.toString();
                    }

                    Log.e("ADMIN_COURSE_API", message);
                    showEmptyState(message);
                    Toast.makeText(AdminManageCourse.this, message, Toast.LENGTH_LONG).show();
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

    private void handleLessonResponse(JSONObject response, String emptyMessage) {
        try {
            Gson gson = new Gson();

            Type type = new TypeToken<ApiResponse<List<LessonResponse>>>() {
            }.getType();

            ApiResponse<List<LessonResponse>> apiResponse =
                    gson.fromJson(response.toString(), type);

            if (apiResponse == null) {
                showEmptyState("Phản hồi từ server rỗng");
                return;
            }

            if (apiResponse.getCode() != 1000) {
                showEmptyState("Lỗi hệ thống: " + apiResponse.getMessage());
                Toast.makeText(
                        this,
                        "Lỗi hệ thống: " + apiResponse.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            List<LessonResponse> remoteLessons = apiResponse.getResult();

            lessonList.clear();

            if (remoteLessons != null && !remoteLessons.isEmpty()) {
                lessonList.addAll(remoteLessons);
                hideEmptyState();
            } else {
                showEmptyState(emptyMessage);
            }

            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            Log.e("ADMIN_COURSE_PARSE", "Lỗi parse: " + e.getMessage());
            showEmptyState("Lỗi đọc dữ liệu khóa học");
            Toast.makeText(this, "Lỗi đọc dữ liệu khóa học", Toast.LENGTH_LONG).show();
        }
    }

    private void setupSearch() {
        View icFilter = findViewById(R.id.icFilter);

        if (icFilter != null) {
            icFilter.setOnClickListener(v -> {
                String query = edtSearch.getText().toString().trim();

                if (!query.isEmpty()) {
                    searchLocalCourses(query);
                } else {
                    if (currentFilter == FILTER_APPROVED) {
                        fetchApprovedCoursesFromServer();
                    } else {
                        fetchPendingCoursesFromServer();
                    }
                }
            });
        }
    }

    private void searchLocalCourses(String query) {
        String lowerQuery = query.toLowerCase();

        List<LessonResponse> filteredList = new ArrayList<>();

        for (LessonResponse lesson : lessonList) {
            if (lesson == null) continue;

            String title = "";

            try {
                if (lesson.getTitle() != null) {
                    title = lesson.getTitle().toLowerCase();
                }
            } catch (Exception ignored) {
            }

            if (title.contains(lowerQuery)) {
                filteredList.add(lesson);
            }
        }

        lessonList.clear();
        lessonList.addAll(filteredList);
        adapter.notifyDataSetChanged();

        if (lessonList.isEmpty()) {
            showEmptyState("Không tìm thấy khóa học phù hợp");
        } else {
            hideEmptyState();
        }
    }

    private String getToken() {
        SharedPreferences sharedPreferences =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        return sharedPreferences.getString(KEY_ACCESS_TOKEN, "");
    }

    private void showEmptyState(String message) {
        lessonList.clear();

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (txtEmpty != null) {
            txtEmpty.setText(message);
            txtEmpty.setVisibility(View.VISIBLE);
        }

        if (rvCourseList != null) {
            rvCourseList.setVisibility(View.GONE);
        }
    }

    private void hideEmptyState() {
        if (txtEmpty != null) {
            txtEmpty.setVisibility(View.GONE);
        }

        if (rvCourseList != null) {
            rvCourseList.setVisibility(View.VISIBLE);
        }
    }

    private void setupBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    setEnabled(false);
                    onBackPressed();
                }
            }
        });
    }
}