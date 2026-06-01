package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AdminUserDetailActivity extends AppCompatActivity {

    private ShapeableImageView imgDetailAvatar;
    private TextView txtDetailId, txtDetailEmail, txtDetailRole, txtDetailStatus;
    private TextView txtSecondFieldLabel;
    private EditText edtFullName, edtEmail, edtPhone, edtSecondField;
    private MaterialButton btnSave, btnDelete, btnBack;

    private int userId;
    private String userCode;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String role;
    private String status;
    private String dateOfBirth;
    private String department;

    private static final String BASE_URL = "http://10.0.2.2:8080/NT118";

    private static final String PREF_NAME = "APP_PREFS";
    private static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_user_detail);

        initViews();
        loadIntentData();
        bindDataToView();
        setupEvents();
    }

    private void initViews() {
        imgDetailAvatar = findViewById(R.id.imgDetailAvatar);

        txtDetailId = findViewById(R.id.txtDetailId);
        txtDetailEmail = findViewById(R.id.txtDetailEmail);
        txtDetailRole = findViewById(R.id.txtDetailRole);
        txtDetailStatus = findViewById(R.id.txtDetailStatus);
        txtSecondFieldLabel = findViewById(R.id.txtSecondFieldLabel);

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtSecondField = findViewById(R.id.edtSecondField);

        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadIntentData() {
        userId = getIntent().getIntExtra("user_id", -1);
        userCode = safe(getIntent().getStringExtra("user_code"));
        fullName = safe(getIntent().getStringExtra("full_name"));
        email = safe(getIntent().getStringExtra("email"));
        phone = safe(getIntent().getStringExtra("phone"));
        avatarUrl = safe(getIntent().getStringExtra("avatar_url"));
        role = safe(getIntent().getStringExtra("role"));
        status = safe(getIntent().getStringExtra("status"));
        dateOfBirth = safe(getIntent().getStringExtra("date_of_birth"));
        department = safe(getIntent().getStringExtra("department"));
    }

    private void bindDataToView() {
        txtDetailId.setText("Mã: " + userCode);
        txtDetailEmail.setText("Gmail: " + email);
        txtDetailRole.setText("Tài khoản: " + formatRole(role));
        txtDetailStatus.setText("Trạng thái: " + formatStatus(status));

        edtFullName.setText(fullName);
        edtEmail.setText(email);
        edtPhone.setText(phone);

        if ("TEACHER".equalsIgnoreCase(role)) {
            txtSecondFieldLabel.setText("Khoa");
            edtSecondField.setText(department);
            edtSecondField.setHint("Nhập khoa / bộ môn");
        } else {
            txtSecondFieldLabel.setText("Ngày sinh");
            edtSecondField.setText(dateOfBirth);
            edtSecondField.setHint("yyyy-MM-dd");
        }

        if (!avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(imgDetailAvatar);
        } else {
            imgDetailAvatar.setImageResource(R.drawable.ic_profile);
        }
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> updateUserByRole());

        btnDelete.setOnClickListener(v -> confirmDeleteUser());
    }

    private void updateUserByRole() {
        if (userId == -1) {
            Toast.makeText(this, "Không có userId, không thể cập nhật", Toast.LENGTH_LONG).show();
            return;
        }

        String newFullName = edtFullName.getText().toString().trim();
        String newEmail = edtEmail.getText().toString().trim();
        String newPhone = edtPhone.getText().toString().trim();
        String secondField = edtSecondField.getText().toString().trim();

        if (newFullName.isEmpty()) {
            edtFullName.setError("Vui lòng nhập họ tên");
            edtFullName.requestFocus();
            return;
        }

        if (newEmail.isEmpty()) {
            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return;
        }

        if ("TEACHER".equalsIgnoreCase(role)) {
            updateTeacher(newFullName, newEmail, newPhone, secondField);
        } else if ("STUDENT".equalsIgnoreCase(role)) {
            updateStudent(newFullName, newEmail, newPhone, secondField);
        } else {
            Toast.makeText(this, "Role không hợp lệ: " + role, Toast.LENGTH_LONG).show();
        }
    }

    private void updateTeacher(
            String newFullName,
            String newEmail,
            String newPhone,
            String newDepartment
    ) {
        try {
            JSONObject body = new JSONObject();

            body.put("full_name", newFullName);
            body.put("email", newEmail);
            body.put("phone", newPhone);
            body.put("department", newDepartment);

            String url = BASE_URL + "/api/teachers/update?userId=" + userId;

            sendPatchRequest(url, body, "Cập nhật giảng viên thành công");

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi dữ liệu teacher: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateStudent(
            String newFullName,
            String newEmail,
            String newPhone,
            String newDateOfBirth
    ) {
        try {
            JSONObject body = new JSONObject();

            body.put("full_name", newFullName);
            body.put("email", newEmail);
            body.put("phone", newPhone);

            if (!newDateOfBirth.isEmpty()) {
                body.put("date_of_birth", newDateOfBirth);
            }

            String url = BASE_URL + "/api/students/update?userId=" + userId;

            sendPatchRequest(url, body, "Cập nhật học sinh thành công");

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi dữ liệu student: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void sendPatchRequest(String url, JSONObject body, String successMessage) {
        String token = getToken();

        if (token.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy token đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Đang lưu...");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PATCH,
                url,
                body,
                response -> {
                    Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    btnSave.setText("Lưu");
                    finish();
                },
                error -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("Lưu");

                    String message = "Cập nhật thất bại";

                    if (error.networkResponse != null) {
                        message += " - HTTP " + error.networkResponse.statusCode;

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
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void confirmDeleteUser() {
        if (userId == -1) {
            Toast.makeText(this, "Không có userId, không thể xoá", Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc muốn xoá user này không?")
                .setPositiveButton("Xoá", (dialog, which) -> deleteUser())
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void deleteUser() {
        String token = getToken();

        if (token.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy token đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/api/users/delete?userId=" + userId;

        btnDelete.setEnabled(false);
        btnDelete.setText("Đang xoá...");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PATCH,
                url,
                null,
                response -> {
                    Toast.makeText(this, "Xoá user thành công", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    btnDelete.setEnabled(true);
                    btnDelete.setText("Xóa");

                    String message = "Xoá user thất bại";

                    if (error.networkResponse != null) {
                        message += " - HTTP " + error.networkResponse.statusCode;

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

    private String getToken() {
        SharedPreferences sharedPreferences =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        return sharedPreferences.getString(KEY_ACCESS_TOKEN, "");
    }

    private String formatRole(String role) {
        if ("TEACHER".equalsIgnoreCase(role)) {
            return "Giảng viên";
        }

        if ("STUDENT".equalsIgnoreCase(role)) {
            return "Học sinh";
        }

        if ("ADMIN".equalsIgnoreCase(role)) {
            return "Quản trị viên";
        }

        return role;
    }

    private String formatStatus(String status) {
        if ("ACTIVE".equalsIgnoreCase(status)) {
            return "Hoạt động";
        }

        if ("INACTIVE".equalsIgnoreCase(status)) {
            return "Không hoạt động";
        }

        return status;
    }

    private String safe(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) {
            return "";
        }

        return value.trim();
    }
}