package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminHomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private MaterialCardView btnMenu;
    private LineChart lineChart;

    private TextView txtTotalCourses;
    private TextView txtTotalStudents;
    private TextView txtTotalTeachers;
    private TextView txtPendingCourses;
    private TextView tabAdmin;

    private RequestQueue requestQueue;

    private int activeLessonCount = 0;
    private int pendingLessonCount = 0;
    private int studentCount = 0;
    private int teacherCount = 0;

    private static final String BASE_URL = "http://10.0.2.2:8080/NT118";

    private static final String PREF_NAME = "APP_PREFS";
    private static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String KEY_FULL_NAME = "FULL_NAME";
    private static final String KEY_USERNAME = "USERNAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_home);

        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenuCard);
        lineChart = findViewById(R.id.lineChart);

        txtTotalCourses = findViewById(R.id.txtTotalCourses);
        txtTotalStudents = findViewById(R.id.txtTotalStudents);
        txtTotalTeachers = findViewById(R.id.txtTotalTeachers);
        txtPendingCourses = findViewById(R.id.txtPendingCourses);
        tabAdmin = findViewById(R.id.tabAdmin);

        requestQueue = Volley.newRequestQueue(this);

        btnMenu.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.END));

        AdminSidebarNavigationHelper.setupSidebar(this, drawerLayout);

        showDefaultAdminName();
        loadAdminInfoFromApi();
        loadDefaultStats();
        loadDashboardData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAdminInfoFromApi();
    }

    private void showDefaultAdminName() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        String fullName = sharedPreferences.getString(KEY_FULL_NAME, "");
        String username = sharedPreferences.getString(KEY_USERNAME, "");

        String displayName = getDisplayAdminName(fullName, username);

        tabAdmin.setText(displayName);
    }

    private void loadAdminInfoFromApi() {
        String token = getToken();

        if (token == null || token.trim().isEmpty()) {
            tabAdmin.setText("Admin");
            return;
        }

        String url = BASE_URL + "/api/users/admin/info";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONObject result = response.optJSONObject("result");

                        if (result == null) {
                            tabAdmin.setText("Admin");
                            return;
                        }

                        String fullName = safe(result.optString("full_name", ""));
                        String username = safe(result.optString("username", ""));
                        String displayName = getDisplayAdminName(fullName, username);

                        tabAdmin.setText(displayName);

                        SharedPreferences sharedPreferences =
                                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

                        sharedPreferences.edit()
                                .putString(KEY_FULL_NAME, displayName)
                                .putString(KEY_USERNAME, username)
                                .apply();

                    } catch (Exception e) {
                        tabAdmin.setText("Admin");
                    }
                },
                error -> {
                    tabAdmin.setText("Admin");
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

    private String getDisplayAdminName(String fullName, String username) {
        fullName = safe(fullName);
        username = safe(username);

        if (!fullName.isEmpty()) {
            return fullName;
        }

        if (!username.isEmpty()) {
            return username;
        }

        return "Admin";
    }

    private void loadDefaultStats() {
        txtTotalCourses.setText("Đang tải...");
        txtTotalStudents.setText("Đang tải...");
        txtTotalTeachers.setText("Đang tải...");
        txtPendingCourses.setText("Đang tải...");

        setupLineChart();
    }

    private void loadDashboardData() {
        getAllTeachers();
        getAllStudents();
        getAllActiveLessons();
        getAllPendingLessons();
    }

    private String getToken() {
        SharedPreferences sharedPreferences =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        return sharedPreferences.getString(KEY_ACCESS_TOKEN, "");
    }

    private void getAllTeachers() {
        String token = getToken();

        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy token đăng nhập", Toast.LENGTH_SHORT).show();
            txtTotalTeachers.setText("0");
            return;
        }

        String url = BASE_URL + "/api/teachers/all";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    teacherCount = extractCount(response);
                    txtTotalTeachers.setText(String.valueOf(teacherCount));
                    setupLineChart();
                },
                error -> {
                    txtTotalTeachers.setText("0");
                    Toast.makeText(this, "Lỗi lấy danh sách giảng viên", Toast.LENGTH_LONG).show();
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

    private void getAllStudents() {
        String token = getToken();

        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy token đăng nhập", Toast.LENGTH_SHORT).show();
            txtTotalStudents.setText("0");
            return;
        }

        String url = BASE_URL + "/api/students/all";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    studentCount = extractCount(response);
                    txtTotalStudents.setText(String.valueOf(studentCount));
                    setupLineChart();
                },
                error -> {
                    txtTotalStudents.setText("0");
                    Toast.makeText(this, "Lỗi lấy danh sách học sinh", Toast.LENGTH_LONG).show();
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

    private void getAllActiveLessons() {
        String token = getToken();

        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy token đăng nhập", Toast.LENGTH_SHORT).show();
            txtTotalCourses.setText("0");
            return;
        }

        String url = BASE_URL + "/api/lessons/allActive";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    activeLessonCount = extractCount(response);
                    txtTotalCourses.setText(String.valueOf(activeLessonCount));
                    setupLineChart();
                },
                error -> {
                    txtTotalCourses.setText("0");
                    Toast.makeText(this, "Lỗi lấy khóa học đang hoạt động", Toast.LENGTH_LONG).show();
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

    private void getAllPendingLessons() {
        String token = getToken();

        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy token đăng nhập", Toast.LENGTH_SHORT).show();
            txtPendingCourses.setText("0");
            return;
        }

        String url = BASE_URL + "/api/lessons/allPending";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    pendingLessonCount = extractCount(response);
                    txtPendingCourses.setText(String.valueOf(pendingLessonCount));
                    setupLineChart();
                },
                error -> {
                    txtPendingCourses.setText("0");
                    Toast.makeText(this, "Lỗi lấy khóa học cần duyệt", Toast.LENGTH_LONG).show();
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

    private int extractCount(JSONObject response) {
        if (response == null) {
            return 0;
        }

        Object result = response.opt("result");

        if (result == null) {
            return 0;
        }

        if (result instanceof JSONArray) {
            return ((JSONArray) result).length();
        }

        if (result instanceof JSONObject) {
            JSONObject resultObject = (JSONObject) result;

            if (resultObject.has("totalElements")) {
                return resultObject.optInt("totalElements", 0);
            }

            if (resultObject.has("total")) {
                return resultObject.optInt("total", 0);
            }

            JSONArray contentArray = resultObject.optJSONArray("content");
            if (contentArray != null) {
                return contentArray.length();
            }

            JSONArray dataArray = resultObject.optJSONArray("data");
            if (dataArray != null) {
                return dataArray.length();
            }

            JSONArray lessonsArray = resultObject.optJSONArray("lessons");
            if (lessonsArray != null) {
                return lessonsArray.length();
            }

            if (resultObject.has("id")) {
                return 1;
            }
        }

        return 0;
    }

    private void setupLineChart() {
        ArrayList<Entry> studentEntries = new ArrayList<>();
        studentEntries.add(new Entry(1, 0));
        studentEntries.add(new Entry(2, Math.max(studentCount * 0.35f, 0)));
        studentEntries.add(new Entry(3, Math.max(studentCount * 0.60f, 0)));
        studentEntries.add(new Entry(4, Math.max(studentCount * 0.80f, 0)));
        studentEntries.add(new Entry(5, studentCount));

        ArrayList<Entry> teacherEntries = new ArrayList<>();
        teacherEntries.add(new Entry(1, 0));
        teacherEntries.add(new Entry(2, Math.max(teacherCount * 0.35f, 0)));
        teacherEntries.add(new Entry(3, Math.max(teacherCount * 0.60f, 0)));
        teacherEntries.add(new Entry(4, Math.max(teacherCount * 0.80f, 0)));
        teacherEntries.add(new Entry(5, teacherCount));

        LineDataSet studentDataSet = new LineDataSet(studentEntries, "Số lượng học viên");
        studentDataSet.setMode(LineDataSet.Mode.LINEAR);
        studentDataSet.setColor(Color.parseColor("#1976D2"));
        studentDataSet.setLineWidth(3f);
        studentDataSet.setCircleColor(Color.parseColor("#1976D2"));
        studentDataSet.setDrawCircleHole(true);
        studentDataSet.setCircleHoleColor(Color.WHITE);
        studentDataSet.setCircleRadius(4f);

        LineDataSet teacherDataSet = new LineDataSet(teacherEntries, "Số lượng giảng viên");
        teacherDataSet.setMode(LineDataSet.Mode.LINEAR);
        teacherDataSet.setColor(Color.parseColor("#FF5722"));
        teacherDataSet.setLineWidth(3f);
        teacherDataSet.setCircleColor(Color.parseColor("#FF5722"));
        teacherDataSet.setDrawCircleHole(true);
        teacherDataSet.setCircleHoleColor(Color.WHITE);
        teacherDataSet.setCircleRadius(4f);

        LineData lineData = new LineData(studentDataSet, teacherDataSet);
        lineData.setValueTextSize(10f);
        lineData.setValueTextColor(Color.BLACK);

        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setGridColor(Color.LTGRAY);

        Legend legend = lineChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        lineChart.animateY(1000);
        lineChart.invalidate();
    }

    private String safe(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) {
            return "";
        }

        return value.trim();
    }
}