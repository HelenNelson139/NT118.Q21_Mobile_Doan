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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TeacherProfileActivity extends AppCompatActivity {

    private TextView txtName, txtEmail, txtPhone, txtUsername, txtAddress;
    private TextView txtRole, txtCourses, txtStatus;
    private ImageView imgAvatar;
    private MaterialButton btnEdit;
    private MaterialCardView btnMenuCard;

    private ActivityResultLauncher<Intent> editProfileLauncher;

    private String currentAvatarUrl = "";

    private static final String PREF_NAME = "APP_PREFS";
    private static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String KEY_USER_ID = "USER_ID";

    private static final String BASE_URL = "http://10.0.2.2:8080/NT118";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

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

        btnMenuCard.setOnClickListener(v -> showSidebarMenu());

        loadDefaultProfileData();
        getTeacherProfileFromApi();

        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        getTeacherProfileFromApi();
                    }
                }
        );

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(
                    TeacherProfileActivity.this,
                    TeacherEditProfileActivity.class
            );

            intent.putExtra("name", txtName.getText().toString());
            intent.putExtra("email", txtEmail.getText().toString());
            intent.putExtra("phone", txtPhone.getText().toString());
            intent.putExtra("username", txtUsername.getText().toString());
            intent.putExtra("address", txtAddress.getText().toString());
            intent.putExtra("avatar_url", currentAvatarUrl);

            editProfileLauncher.launch(intent);
        });
    }

    private void loadDefaultProfileData() {
        txtName.setText("Đang tải...");
        txtEmail.setText("Đang tải...");
        txtPhone.setText("Đang tải...");
        txtUsername.setText("Đang tải...");
        txtAddress.setText("Đang tải...");

        txtRole.setText("Đang tải...");
        txtCourses.setText("0");
        txtStatus.setText("Đang tải...");

        imgAvatar.setImageResource(R.drawable.ic_profile);
    }

    private void getTeacherProfileFromApi() {
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

        String url = BASE_URL + "/api/teachers/get?userId=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> mapTeacherProfileToView(response),
                error -> {
                    String message = "Lỗi lấy thông tin giảng viên";

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

    private void mapTeacherProfileToView(JSONObject response) {
        String fullName = response.optString("full_name", "Chưa cập nhật");
        String email = response.optString("email", "Chưa cập nhật");
        String phone = response.optString("phone", "Chưa cập nhật");
        String username = response.optString("username", "Chưa cập nhật");
        String department = response.optString("department", "Chưa cập nhật");
        String role = response.optString("role", "TEACHER");
        String status = response.optString("status", "Chưa cập nhật");
        String avatarUrl = response.optString("avatar_url", "");

        currentAvatarUrl = avatarUrl;

        txtName.setText(fullName);
        txtEmail.setText(email);
        txtPhone.setText(phone);
        txtUsername.setText(username);
        txtAddress.setText(department);

        txtRole.setText(formatRole(role));
        txtCourses.setText("0");
        txtStatus.setText(formatStatus(status));

        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            imgAvatar.setImageResource(R.drawable.ic_profile);
        } else {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(imgAvatar);
        }
    }

    private String formatRole(String role) {
        if (role == null) {
            return "Giảng viên";
        }

        if ("TEACHER".equalsIgnoreCase(role)) {
            return "Giảng viên";
        }

        if ("ADMIN".equalsIgnoreCase(role)) {
            return "Quản trị viên";
        }

        if ("STUDENT".equalsIgnoreCase(role)) {
            return "Sinh viên";
        }

        return role;
    }

    private String formatStatus(String status) {
        if (status == null) {
            return "Chưa cập nhật";
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
            Intent intent = new Intent(TeacherProfileActivity.this, TeacherHome.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            dialog.dismiss();
            finish();
        });

        menuMyClasses.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherProfileActivity.this, TeacherClass.class);
            startActivity(intent);
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

            Intent intent = new Intent(TeacherProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            dialog.dismiss();
            finish();
        });

        dialog.show();
    }
}