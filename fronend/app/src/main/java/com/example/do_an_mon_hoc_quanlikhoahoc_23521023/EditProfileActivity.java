package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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

public class EditProfileActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtUsername, edtPhone, edtAddress;
    private MaterialButton btnSave;

    private ImageView imgAvatar;
    private MaterialCardView avatarCard;

    private String oldName = "";
    private String oldEmail = "";
    private String oldUsername = "";
    private String oldPhone = "";
    private String oldDateOfBirth = "";
    private String currentAvatarUrl = "";

    private static final String PREF_NAME = "APP_PREFS";
    private static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String KEY_USER_ID = "USER_ID";

    private static final String BASE_URL = "http://10.0.2.2:8080/NT118";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Đúng layout chỉnh sửa student
        setContentView(R.layout.activity_edit_student_profile);

        imgAvatar = findViewById(R.id.imgAvatar);
        avatarCard = findViewById(R.id.avatarCard);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtUsername = findViewById(R.id.edtUsername);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);

        btnSave = findViewById(R.id.btnSave);

        loadDataFromIntent();

        avatarCard.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng đổi ảnh sẽ xử lý sau", Toast.LENGTH_SHORT).show();
        });

        btnSave.setOnClickListener(v -> updateChangedStudentFields());
    }

    private void loadDataFromIntent() {
        Intent intent = getIntent();

        if (intent == null) {
            return;
        }

        oldName = safeString(intent.getStringExtra("name"));
        oldEmail = safeString(intent.getStringExtra("email"));
        oldUsername = safeString(intent.getStringExtra("username"));
        oldPhone = safeString(intent.getStringExtra("phone"));
        oldDateOfBirth = safeString(intent.getStringExtra("date_of_birth"));
        currentAvatarUrl = safeString(intent.getStringExtra("avatar_url"));

        edtName.setText(oldName);
        edtEmail.setText(oldEmail);
        edtUsername.setText(oldUsername);
        edtPhone.setText(oldPhone);
        edtAddress.setText(oldDateOfBirth);

        edtUsername.setEnabled(false);

        loadAvatar(currentAvatarUrl);
    }

    private void loadAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            imgAvatar.setImageResource(R.drawable.ic_profile);
            return;
        }

        Glide.with(this)
                .load(avatarUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(imgAvatar);
    }

    private void updateChangedStudentFields() {
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

        String newName = edtName.getText().toString().trim();
        String newEmail = edtEmail.getText().toString().trim();
        String newPhone = edtPhone.getText().toString().trim();
        String newDateOfBirth = edtAddress.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newEmail.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPhone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newDateOfBirth.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập ngày sinh", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidDateFormat(newDateOfBirth)) {
            Toast.makeText(
                    this,
                    "Ngày sinh phải có dạng yyyy-MM-dd, ví dụ: 2005-05-20",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        JSONObject body = new JSONObject();

        try {
            if (!newName.equals(oldName)) {
                body.put("full_name", newName);
            }

            if (!newEmail.equals(oldEmail)) {
                body.put("email", newEmail);
            }

            if (!newPhone.equals(oldPhone)) {
                body.put("phone", newPhone);
            }

            if (!newDateOfBirth.equals(oldDateOfBirth)) {
                body.put("date_of_birth", newDateOfBirth);
            }

            if (body.length() == 0) {
                Toast.makeText(this, "Bạn chưa thay đổi thông tin nào", Toast.LENGTH_SHORT).show();
                return;
            }

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tạo dữ liệu cập nhật", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/api/students/update?userId=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PATCH,
                url,
                body,
                response -> {
                    Toast.makeText(this, "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show();

                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                    finish();
                },
                error -> {
                    String message = "Lỗi cập nhật hồ sơ";

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
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private boolean isValidDateFormat(String date) {
        return date.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }

    private String safeString(String value) {
        if (value == null || value.equalsIgnoreCase("null")) {
            return "";
        }

        if (value.contains("T")) {
            return value.substring(0, value.indexOf("T"));
        }

        return value;
    }
}