package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TeacherHome extends AppCompatActivity {

    private MaterialCardView btnMenuCard;
    private TextView txtTotalStudents, txtTotalCourses, txtWelcome, txtTeacherName;
    private CombinedChart combinedChart;

    private RequestQueue requestQueue;

    private final ArrayList<Integer> lessonIds = new ArrayList<>();
    private final ArrayList<String> lessonTitles = new ArrayList<>();
    private final ArrayList<Integer> studentCountsByLesson = new ArrayList<>();
    private final Set<Integer> uniqueStudentIds = new HashSet<>();

    private int completedStudentRequests = 0;

    private static final String BASE_URL = "http://10.0.2.2:8080/NT118";

    private static final String PREF_NAME = "APP_PREFS";
    private static final String KEY_FULL_NAME = "FULL_NAME";
    private static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String KEY_ROLE = "ROLE";
    private static final String KEY_USERNAME = "USERNAME";
    private static final String KEY_IS_LOGGED_IN = "IS_LOGGED_IN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_home);

        btnMenuCard = findViewById(R.id.btnMenuCard);
        txtTotalStudents = findViewById(R.id.txtTotalStudents);
        txtTotalCourses = findViewById(R.id.txtTotalCourses);
        txtWelcome = findViewById(R.id.txtWelcome);
        txtTeacherName = findViewById(R.id.txtTeacherName);
        combinedChart = findViewById(R.id.combinedChart);

        requestQueue = Volley.newRequestQueue(this);

        btnMenuCard.setOnClickListener(v -> showSidebarMenu());

        loadTeacherName();
        loadDefaultStats();
        getMyLessonsFromApi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getMyLessonsFromApi();
    }

    private void loadTeacherName() {
        txtWelcome.setText("Chào giảng viên,");

        SharedPreferences sharedPreferences =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        String fullName = sharedPreferences.getString(KEY_FULL_NAME, "");

        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = "Giảng viên";
        }

        txtTeacherName.setText(fullName);
    }

    private void loadDefaultStats() {
        txtTotalStudents.setText("Đang tải...");
        txtTotalCourses.setText("Đang tải...");
        setupEmptyChart();
    }

    private void getMyLessonsFromApi() {
        String token = getToken();

        if (token.isEmpty()) {
            txtTotalStudents.setText("0");
            txtTotalCourses.setText("0");
            setupEmptyChart();
            Toast.makeText(this, "Không tìm thấy token đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        /*
         * Backend:
         * GET /NT118/api/lessons/my-lessons
         *
         * Backend tự lấy username từ JWT token:
         * SecurityContextHolder.getContext().getAuthentication().getName()
         */
        String url = BASE_URL + "/api/lessons/my-lessons";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    parseLessonsResponse(response);

                    txtTotalCourses.setText(String.valueOf(lessonIds.size()));

                    if (lessonIds.isEmpty()) {
                        txtTotalStudents.setText("0");
                        setupEmptyChart();
                    } else {
                        getStudentsForEachLesson(token);
                    }
                },
                error -> {
                    txtTotalStudents.setText("0");
                    txtTotalCourses.setText("0");
                    setupEmptyChart();

                    String message = "Lỗi lấy danh sách khóa học của giảng viên";

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

    private void parseLessonsResponse(JSONObject response) {
        lessonIds.clear();
        lessonTitles.clear();
        studentCountsByLesson.clear();
        uniqueStudentIds.clear();
        completedStudentRequests = 0;

        if (response == null) {
            return;
        }

        Object result = response.opt("result");

        if (result == null) {
            return;
        }

        if (result instanceof JSONArray) {
            parseLessonsArray((JSONArray) result);
            return;
        }

        if (result instanceof JSONObject) {
            JSONObject resultObject = (JSONObject) result;

            JSONArray contentArray = resultObject.optJSONArray("content");
            if (contentArray != null) {
                parseLessonsArray(contentArray);
                return;
            }

            JSONArray dataArray = resultObject.optJSONArray("data");
            if (dataArray != null) {
                parseLessonsArray(dataArray);
                return;
            }

            JSONArray lessonsArray = resultObject.optJSONArray("lessons");
            if (lessonsArray != null) {
                parseLessonsArray(lessonsArray);
                return;
            }

            if (resultObject.has("id")) {
                int lessonId = resultObject.optInt("id", -1);
                String title = resultObject.optString("title", "Khóa học " + lessonId);

                if (lessonId != -1) {
                    lessonIds.add(lessonId);
                    lessonTitles.add(title);
                    studentCountsByLesson.add(0);
                }
            }
        }
    }

    private void parseLessonsArray(JSONArray lessonsArray) {
        for (int i = 0; i < lessonsArray.length(); i++) {
            JSONObject lesson = lessonsArray.optJSONObject(i);

            if (lesson == null) {
                continue;
            }

            int lessonId = lesson.optInt("id", -1);
            String title = lesson.optString("title", "Khóa học " + lessonId);

            if (lessonId != -1) {
                lessonIds.add(lessonId);
                lessonTitles.add(title);
                studentCountsByLesson.add(0);
            }
        }
    }

    private void getStudentsForEachLesson(String token) {
        uniqueStudentIds.clear();
        completedStudentRequests = 0;

        for (int i = 0; i < lessonIds.size(); i++) {
            int index = i;
            int lessonId = lessonIds.get(i);

            getStudentsByLessonId(token, lessonId, index);
        }
    }

    private void getStudentsByLessonId(String token, int lessonId, int index) {
        /*
         * API:
         * GET /NT118/api/teachers/{lessonId}/students
         */
        String url = BASE_URL + "/api/teachers/" + lessonId + "/students";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    int count = parseStudentIdsResponse(response);

                    if (index >= 0 && index < studentCountsByLesson.size()) {
                        studentCountsByLesson.set(index, count);
                    }

                    completedStudentRequests++;

                    if (completedStudentRequests == lessonIds.size()) {
                        updateDashboard();
                    }
                },
                error -> {
                    if (index >= 0 && index < studentCountsByLesson.size()) {
                        studentCountsByLesson.set(index, 0);
                    }

                    completedStudentRequests++;

                    if (completedStudentRequests == lessonIds.size()) {
                        updateDashboard();
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

    private int parseStudentIdsResponse(JSONObject response) {
        if (response == null) {
            return 0;
        }

        Object result = response.opt("result");

        if (result == null) {
            return 0;
        }

        if (result instanceof JSONArray) {
            JSONArray studentsArray = (JSONArray) result;

            for (int i = 0; i < studentsArray.length(); i++) {
                Object item = studentsArray.opt(i);

                if (item instanceof Integer) {
                    uniqueStudentIds.add((Integer) item);
                } else if (item instanceof Number) {
                    uniqueStudentIds.add(((Number) item).intValue());
                } else if (item instanceof JSONObject) {
                    JSONObject studentObject = (JSONObject) item;

                    if (studentObject.has("id")) {
                        uniqueStudentIds.add(studentObject.optInt("id"));
                    } else if (studentObject.has("userId")) {
                        uniqueStudentIds.add(studentObject.optInt("userId"));
                    } else if (studentObject.has("user_id")) {
                        uniqueStudentIds.add(studentObject.optInt("user_id"));
                    }
                }
            }

            return studentsArray.length();
        }

        if (result instanceof JSONObject) {
            JSONObject resultObject = (JSONObject) result;

            if (resultObject.has("totalElements")) {
                JSONArray contentArray = resultObject.optJSONArray("content");

                if (contentArray != null) {
                    addStudentsFromArray(contentArray);
                }

                return resultObject.optInt("totalElements", 0);
            }

            JSONArray contentArray = resultObject.optJSONArray("content");
            if (contentArray != null) {
                addStudentsFromArray(contentArray);
                return contentArray.length();
            }

            JSONArray dataArray = resultObject.optJSONArray("data");
            if (dataArray != null) {
                addStudentsFromArray(dataArray);
                return dataArray.length();
            }
        }

        return 0;
    }

    private void addStudentsFromArray(JSONArray studentsArray) {
        for (int i = 0; i < studentsArray.length(); i++) {
            JSONObject studentObject = studentsArray.optJSONObject(i);

            if (studentObject == null) {
                continue;
            }

            if (studentObject.has("id")) {
                uniqueStudentIds.add(studentObject.optInt("id"));
            } else if (studentObject.has("userId")) {
                uniqueStudentIds.add(studentObject.optInt("userId"));
            } else if (studentObject.has("user_id")) {
                uniqueStudentIds.add(studentObject.optInt("user_id"));
            }
        }
    }

    private void updateDashboard() {
        txtTotalCourses.setText(String.valueOf(lessonIds.size()));
        txtTotalStudents.setText(String.valueOf(uniqueStudentIds.size()));

        setupChartWithApiData();
    }

    private void setupEmptyChart() {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<Entry> lineEntries = new ArrayList<>();

        BarDataSet barDataSet = new BarDataSet(barEntries, "Khóa học");
        barDataSet.setColor(Color.parseColor("#112D4E"));

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.4f);

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Sinh viên");
        lineDataSet.setColor(Color.parseColor("#3F72AF"));
        lineDataSet.setCircleColor(Color.parseColor("#3F72AF"));
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(4f);
        lineDataSet.setValueTextSize(10f);

        LineData lineData = new LineData(lineDataSet);

        CombinedData combinedData = new CombinedData();
        combinedData.setData(barData);
        combinedData.setData(lineData);

        combinedChart.setData(combinedData);

        combinedChart.getDescription().setEnabled(false);
        combinedChart.setDrawGridBackground(false);

        XAxis xAxis = combinedChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        combinedChart.invalidate();
    }

    private void setupChartWithApiData() {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<Entry> lineEntries = new ArrayList<>();

        for (int i = 0; i < lessonIds.size(); i++) {
            float x = i + 1;

            barEntries.add(new BarEntry(x, 1));

            int studentCount = 0;

            if (i < studentCountsByLesson.size()) {
                studentCount = studentCountsByLesson.get(i);
            }

            lineEntries.add(new Entry(x, studentCount));
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Khóa học");
        barDataSet.setColor(Color.parseColor("#112D4E"));
        barDataSet.setValueTextSize(9f);

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.4f);

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Sinh viên");
        lineDataSet.setColor(Color.parseColor("#3F72AF"));
        lineDataSet.setCircleColor(Color.parseColor("#3F72AF"));
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(4f);
        lineDataSet.setValueTextSize(10f);

        LineData lineData = new LineData(lineDataSet);

        CombinedData combinedData = new CombinedData();
        combinedData.setData(barData);
        combinedData.setData(lineData);

        combinedChart.setData(combinedData);

        combinedChart.getDescription().setEnabled(false);
        combinedChart.setDrawGridBackground(false);
        combinedChart.animateY(1000);

        XAxis xAxis = combinedChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(lessonIds.size() + 1f);

        combinedChart.invalidate();
    }

    private String getToken() {
        SharedPreferences sharedPreferences =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        return sharedPreferences.getString(KEY_ACCESS_TOKEN, "");
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
        LinearLayout menuMyClasses = dialog.findViewById(R.id.menuMyClasses);
        LinearLayout menuProfile = dialog.findViewById(R.id.menuProfile);

        TextView txtLogout = dialog.findViewById(R.id.txtLogout);

        btnCloseMenu.setOnClickListener(v -> dialog.dismiss());

        menuHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, TeacherHome.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            dialog.dismiss();
        });

        menuMyClasses.setOnClickListener(v -> {
            startActivity(new Intent(this, TeacherClass.class));
            dialog.dismiss();
        });

        menuProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, TeacherProfileActivity.class));
            dialog.dismiss();
        });

        txtLogout.setOnClickListener(v -> {
            logout();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void logout() {
        SharedPreferences sharedPreferences =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_ACCESS_TOKEN);
        editor.remove(KEY_ROLE);
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_FULL_NAME);
        editor.remove(KEY_IS_LOGGED_IN);
        editor.apply();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}