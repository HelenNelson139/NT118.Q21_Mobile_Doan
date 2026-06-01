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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtName, txtEmail, txtPhone, txtUsername, txtAddress;
    private TextView txtRole, txtCourses, txtStatus;
    private ImageView imgAvatar;
    private MaterialButton btnEdit;
    private MaterialCardView btnMenuCard;

    private ActivityResultLauncher<Intent> editProfileLauncher;

    private String currentAvatarUrl = "";
    private String currentUsername = "";
    private String currentDateOfBirth = "";

    private static final String PREF_NAME = "APP_PREFS";
    private static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String KEY_USER_ID = "USER_ID";

    private static final String BASE_URL = "http://10.0.2.2:8080/NT118";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        btnMenuCard = findViewById(R.id.btnMenuCard);
        imgAvatar = findViewById(R.id.imgAvatar);

        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);
        txtUsername = findViewById(R.id.txtUsername);
        txtAddress = findViewById(R.id.txtAddress);

        txtRole = findViewById(R.id.txtRole);
        txtCourses = findViewById(R.id.txtCourses);
        txtStatus = findViewById(R.id.txtStatus);

        btnEdit = findViewById(R.id.btnEdit);

        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        getStudentProfileFromApi();
                        getStudentLessonCountFromApi();
                    }
                }
        );

        btnMenuCard.setOnClickListener(v -> showSidebarMenu());

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);

            intent.putExtra("name", txtName.getText().toString());
            intent.putExtra("email", txtEmail.getText().toString());
            intent.putExtra("phone", txtPhone.getText().toString());
            intent.putExtra("username", currentUsername);
            intent.putExtra("date_of_birth", currentDateOfBirth);
            intent.putExtra("avatar_url", currentAvatarUrl);

            editProfileLauncher.launch(intent);
        });

        loadDefaultProfileData();
        getStudentProfileFromApi();
        getStudentLessonCountFromApi();
    }

    private void loadDefaultProfileData() {
        txtName.setText("Đang tải...");
        txtEmail.setText("Đang tải...");

        txtPhone.setText("Đang tải...");
        txtUsername.setText("Đang tải...");
        txtAddress.setText("Đang tải...");

        txtRole.setText("Học viên");
        txtCourses.setText("Đang tải...");
        txtStatus.setText("Đang hoạt động");

        imgAvatar.setImageResource(R.drawable.ic_profile);
    }

    private void getStudentProfileFromApi() {
        SharedPreferences sharedPreferences =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        String token = sharedPreferences.getString(KEY_ACCESS_TOKEN, "");
        int userId = sharedPreferences.getInt(KEY_USER_ID, -1);

        if (token.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy token đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy userId", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/api/students/get?userId=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> mapStudentProfileToView(response),
                error -> {
                    String message = "Lỗi lấy thông tin học viên";

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

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void getStudentLessonCountFromApi() {
        SharedPreferences sharedPreferences =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        String token = sharedPreferences.getString(KEY_ACCESS_TOKEN, "");
        int userId = sharedPreferences.getInt(KEY_USER_ID, -1);

        if (token.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy token đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy userId", Toast.LENGTH_SHORT).show();
            return;
        }

        /*
         * API lấy danh sách lesson mà học viên đã đăng ký:
         * GET /NT118/api/students/{userId}/lessons
         *
         * Response ví dụ:
         * {
         *   "code": 1000,
         *   "message": "Get student lesson ids successful",
         *   "result": [1]
         * }
         */
        String url = BASE_URL + "/api/students/" + userId + "/lessons";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    int lessonCount = extractStudentLessonCount(response);
                    txtCourses.setText(String.valueOf(lessonCount));
                },
                error -> {
                    txtCourses.setText("0");

                    String message = "Lỗi lấy số khóa học đã đăng ký";

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

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private int extractStudentLessonCount(JSONObject response) {
        if (response == null) {
            return 0;
        }

        Object result = response.opt("result");

        if (result == null) {
            return 0;
        }

        /*
         * Trường hợp API trả:
         * "result": [1, 2, 3]
         */
        if (result instanceof JSONArray) {
            return ((JSONArray) result).length();
        }

        /*
         * Trường hợp API trả dạng object phân trang:
         * "result": {
         *   "content": [...]
         * }
         */
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
        }

        return 0;
    }

    private void mapStudentProfileToView(JSONObject response) {
        String avatarUrl = response.optString("avatar_url", "");
        String dateOfBirth = response.optString("date_of_birth", "");
        String email = response.optString("email", "Chưa cập nhật");
        String fullName = response.optString("full_name", "Chưa cập nhật");
        String phone = response.optString("phone", "Chưa cập nhật");
        String role = response.optString("role", "STUDENT");
        String status = response.optString("status", "ACTIVE");
        String username = response.optString("username", "Chưa cập nhật");

        currentAvatarUrl = avatarUrl;
        currentUsername = username;
        currentDateOfBirth = formatDate(dateOfBirth);

        txtName.setText(fullName);
        txtEmail.setText(email);

        txtPhone.setText(phone);
        txtUsername.setText(email);
        txtAddress.setText(fullName);

        txtRole.setText(formatRole(role));
        txtStatus.setText(formatStatus(status));

        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            imgAvatar.setImageResource(R.drawable.ic_profile);
        } else {
            Glide.with(this)
                    .load(avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(imgAvatar);
        }
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

    private String formatRole(String role) {
        if (role == null) {
            return "Học viên";
        }

        if ("STUDENT".equalsIgnoreCase(role)) {
            return "Học viên";
        }

        if ("TEACHER".equalsIgnoreCase(role)) {
            return "Giảng viên";
        }

        if ("ADMIN".equalsIgnoreCase(role)) {
            return "Quản trị viên";
        }

        return role;
    }

    private String formatStatus(String status) {
        if (status == null) {
            return "Đang hoạt động";
        }

        if ("ACTIVE".equalsIgnoreCase(status)) {
            return "Đang hoạt động";
        }

        if ("INACTIVE".equalsIgnoreCase(status)) {
            return "Không hoạt động";
        }

        return status;
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
        LinearLayout menuLearning = dialog.findViewById(R.id.menuLearning);
        LinearLayout menuProfile = dialog.findViewById(R.id.menuProfile);

        TextView txtLogout = dialog.findViewById(R.id.txtLogout);
        TextView tvUserName = dialog.findViewById(R.id.tvUserName);

        if (tvUserName != null) {
            tvUserName.setText("Học viên");
        }

        btnCloseMenu.setOnClickListener(v -> dialog.dismiss());

        menuHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            dialog.dismiss();
            finish();
        });

        menuCourses.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            dialog.dismiss();
            finish();
        });

        menuLearning.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng học tập sẽ làm sau", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        menuProfile.setOnClickListener(v -> {
            dialog.dismiss();
        });

        txtLogout.setOnClickListener(v -> {
            SharedPreferences sharedPreferences =
                    getSharedPreferences(PREF_NAME, MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            dialog.dismiss();
            finish();
        });

        dialog.show();
    }
}