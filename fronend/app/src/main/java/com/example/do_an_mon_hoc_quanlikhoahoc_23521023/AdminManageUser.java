package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminManageUser extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private MaterialCardView btnMenu;
    private RecyclerView rvUserList;
    private EditText edtSearch;
    private MaterialButton btnLecturerTab, btnStudentTab;

    private AdminUserSummaryAdapter adapter;

    private final List<AdminUserSummary> lecturerList = new ArrayList<>();
    private final List<AdminUserSummary> studentList = new ArrayList<>();
    private final List<AdminUserSummary> currentDisplayList = new ArrayList<>();

    private RequestQueue requestQueue;

    private boolean isLecturerSelected = true;

    private static final String BASE_URL = "http://10.0.2.2:8080/NT118";

    private static final String PREF_NAME = "APP_PREFS";
    private static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_user_manage);

        initViews();

        requestQueue = Volley.newRequestQueue(this);

        AdminSidebarNavigationHelper.setupSidebar(this, drawerLayout);

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        setupRecyclerView();
        setupTabEvents();
        setupSearch();
        setupBackPressed();

        loadTeachersFromApi();
        loadStudentsFromApi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTeachersFromApi();
        loadStudentsFromApi();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenuCard);
        rvUserList = findViewById(R.id.rvUserList);
        edtSearch = findViewById(R.id.edtSearch);
        btnLecturerTab = findViewById(R.id.btnLecturerTab);
        btnStudentTab = findViewById(R.id.btnStudentTab);
    }

    private void setupRecyclerView() {
        rvUserList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminUserSummaryAdapter(this, currentDisplayList);
        rvUserList.setAdapter(adapter);
    }

    private void setupTabEvents() {
        btnLecturerTab.setOnClickListener(v -> {
            isLecturerSelected = true;
            updateTabUI(true);
            showLecturers();
        });

        btnStudentTab.setOnClickListener(v -> {
            isLecturerSelected = false;
            updateTabUI(false);
            showStudents();
        });

        updateTabUI(true);
    }

    private void updateTabUI(boolean lecturerSelected) {
        int activeColor = Color.parseColor("#3F72AF");
        int inactiveColor = Color.parseColor("#112D4E");

        if (lecturerSelected) {
            btnLecturerTab.setTextColor(activeColor);
            btnLecturerTab.setAlpha(1.0f);

            btnStudentTab.setTextColor(inactiveColor);
            btnStudentTab.setAlpha(0.45f);
        } else {
            btnStudentTab.setTextColor(activeColor);
            btnStudentTab.setAlpha(1.0f);

            btnLecturerTab.setTextColor(inactiveColor);
            btnLecturerTab.setAlpha(0.45f);
        }
    }

    private void loadTeachersFromApi() {
        String token = getToken();

        if (token.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy token đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/api/teachers/all";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        lecturerList.clear();

                        JSONArray result = response.optJSONArray("result");

                        if (result == null) {
                            Toast.makeText(this, "API teachers/all không có result", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (int i = 0; i < result.length(); i++) {
                            JSONObject item = result.optJSONObject(i);
                            if (item == null) continue;

                            lecturerList.add(parseUser(item, "TEACHER"));
                        }

                        if (isLecturerSelected) {
                            showLecturers();
                        }

                    } catch (Exception e) {
                        Log.e("ADMIN_TEACHER_PARSE", e.toString());
                        Toast.makeText(this, "Lỗi đọc danh sách giảng viên", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    String message = "Lỗi lấy danh sách giảng viên";

                    if (error.networkResponse != null) {
                        message += ": HTTP " + error.networkResponse.statusCode;
                    } else {
                        message += ": " + error.toString();
                    }

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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

    private void loadStudentsFromApi() {
        String token = getToken();

        if (token.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy token đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/api/students/all";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        studentList.clear();

                        JSONArray result = response.optJSONArray("result");

                        if (result == null) {
                            Toast.makeText(this, "API students/all không có result", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (int i = 0; i < result.length(); i++) {
                            JSONObject item = result.optJSONObject(i);
                            if (item == null) continue;

                            studentList.add(parseUser(item, "STUDENT"));
                        }

                        if (!isLecturerSelected) {
                            showStudents();
                        }

                    } catch (Exception e) {
                        Log.e("ADMIN_STUDENT_PARSE", e.toString());
                        Toast.makeText(this, "Lỗi đọc danh sách học sinh", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    String message = "Lỗi lấy danh sách học sinh";

                    if (error.networkResponse != null) {
                        message += ": HTTP " + error.networkResponse.statusCode;
                    } else {
                        message += ": " + error.toString();
                    }

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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

    private AdminUserSummary parseUser(JSONObject item, String defaultRole) {
        int userId = item.optInt("id", -1);

        if (userId == -1) {
            userId = item.optInt("user_id", -1);
        }

        if (userId == -1) {
            userId = item.optInt("userId", -1);
        }

        String role = item.optString("role", defaultRole);

        String userCode;
        if ("TEACHER".equalsIgnoreCase(role)) {
            userCode = item.optString("teacher_code", "");
        } else {
            userCode = item.optString("student_code", "");
        }

        if (userCode == null || userCode.trim().isEmpty()) {
            userCode = String.valueOf(userId);
        }

        String username = item.optString("username", "");
        String fullName = item.optString("full_name", "");
        String email = item.optString("email", "");
        String phone = item.optString("phone", "");
        String avatarUrl = item.optString("avatar_url", "");
        String status = item.optString("status", "ACTIVE");

        String dateOfBirth = item.optString("date_of_birth", "");
        dateOfBirth = formatDate(dateOfBirth);

        String department = item.optString("department", "");

        return new AdminUserSummary(
                userId,
                userCode,
                username,
                fullName,
                email,
                phone,
                avatarUrl,
                role,
                status,
                dateOfBirth,
                department
        );
    }

    private void showLecturers() {
        currentDisplayList.clear();
        currentDisplayList.addAll(lecturerList);
        adapter.updateList(currentDisplayList);
    }

    private void showStudents() {
        currentDisplayList.clear();
        currentDisplayList.addAll(studentList);
        adapter.updateList(currentDisplayList);
    }

    private void setupSearch() {
        View filterIcon = findViewById(R.id.icFilter);

        if (filterIcon != null) {
            filterIcon.setOnClickListener(v -> {
                String query = edtSearch.getText().toString().trim().toLowerCase();

                if (query.isEmpty()) {
                    if (isLecturerSelected) {
                        showLecturers();
                    } else {
                        showStudents();
                    }
                    return;
                }

                List<AdminUserSummary> source =
                        isLecturerSelected ? lecturerList : studentList;

                List<AdminUserSummary> filtered = new ArrayList<>();

                for (AdminUserSummary user : source) {
                    String name = safe(user.getFullName()).toLowerCase();
                    String email = safe(user.getEmail()).toLowerCase();
                    String username = safe(user.getUsername()).toLowerCase();
                    String code = safe(user.getUserCode()).toLowerCase();

                    if (name.contains(query)
                            || email.contains(query)
                            || username.contains(query)
                            || code.contains(query)) {
                        filtered.add(user);
                    }
                }

                currentDisplayList.clear();
                currentDisplayList.addAll(filtered);
                adapter.updateList(currentDisplayList);
            });
        }
    }

    private String getToken() {
        SharedPreferences sharedPreferences =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        return sharedPreferences.getString(KEY_ACCESS_TOKEN, "");
    }

    private String formatDate(String date) {
        if (date == null || date.trim().isEmpty() || "null".equalsIgnoreCase(date)) {
            return "";
        }

        if (date.contains("T")) {
            return date.substring(0, date.indexOf("T"));
        }

        return date;
    }

    private String safe(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) {
            return "";
        }

        return value.trim();
    }

    private void setupBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    setEnabled(false);
                    onBackPressed();
                }
            }
        });
    }
}