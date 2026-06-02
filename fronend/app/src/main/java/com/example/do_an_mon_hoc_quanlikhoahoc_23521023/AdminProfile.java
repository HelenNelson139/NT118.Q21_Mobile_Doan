package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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

public class AdminProfile extends AppCompatActivity {

    private ShapeableImageView imgAvatar;

    private EditText edtId;
    private EditText edtUsername;
    private EditText edtFullName;
    private EditText edtGmail;
    private EditText edtPhone;
    private EditText edtRole;
    private EditText edtStatus;

    private MaterialButton btnUpdate;
    private MaterialButton btnBack;

    private RequestQueue requestQueue;

    private static final String BASE_URL = "http://10.0.2.2:8080/NT118";

    private static final String PREF_NAME = "APP_PREFS";
    private static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String KEY_FULL_NAME = "FULL_NAME";
    private static final String KEY_USERNAME = "USERNAME";

    private Integer currentAdminId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_profile);

        requestQueue = Volley.newRequestQueue(this);

        initViews();
        setupReadonlyFields();
        setupClickListeners();

        loadAdminInfo();
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgDetailAvatar);

        edtId = findViewById(R.id.edtId);
        edtUsername = findViewById(R.id.edtUsername);
        edtFullName = findViewById(R.id.edtFullName);
        edtGmail = findViewById(R.id.edtGmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtRole = findViewById(R.id.edtRole);
        edtStatus = findViewById(R.id.edtStatus);

        btnUpdate = findViewById(R.id.btnUpdate);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupReadonlyFields() {
        setEditTextReadonly(edtId);
        setEditTextReadonly(edtUsername);
        setEditTextReadonly(edtRole);
        setEditTextReadonly(edtStatus);
    }

    private void setEditTextReadonly(EditText editText) {
        if (editText == null) {
            return;
        }

        editText.setEnabled(false);
        editText.setFocusable(false);
        editText.setCursorVisible(false);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnUpdate.setOnClickListener(v -> updateAdminProfile());
    }

    private void loadAdminInfo() {
        String token = getToken();

        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy token đăng nhập", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(this, "Không có dữ liệu admin", Toast.LENGTH_SHORT).show();
                            fillDefaultAdminInfo();
                            return;
                        }

                        bindAdminInfo(result);

                    } catch (Exception e) {
                        Toast.makeText(this, "Lỗi đọc dữ liệu admin", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Không lấy được thông tin admin", Toast.LENGTH_LONG).show();
                    fillDefaultAdminInfo();
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

    private void bindAdminInfo(JSONObject result) {
        currentAdminId = result.optInt("id", -1);

        String id = String.valueOf(result.optInt("id", 0));
        String username = safe(result.optString("username", ""));
        String fullName = safe(result.optString("full_name", ""));
        String email = safe(result.optString("email", ""));
        String phone = safe(result.optString("phone", ""));
        String role = safe(result.optString("role", ""));
        String status = safe(result.optString("status", ""));
        String avatarUrl = safe(result.optString("avatar_url", ""));

        if (fullName.isEmpty()) {
            fullName = "Admin";
        }

        if (username.isEmpty()) {
            username = "admin";
        }

        if (role.isEmpty()) {
            role = "ADMIN";
        }

        edtId.setText(id);
        edtUsername.setText(username);
        edtFullName.setText(fullName);
        edtGmail.setText(email);
        edtPhone.setText(phone);
        edtRole.setText(role);
        edtStatus.setText(status);

        if (!avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.ic_profile);
        }

        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        sharedPreferences.edit()
                .putString(KEY_FULL_NAME, fullName)
                .putString(KEY_USERNAME, username)
                .apply();
    }

    private void fillDefaultAdminInfo() {
        edtId.setText("");
        edtUsername.setText("admin");
        edtFullName.setText("Admin");
        edtGmail.setText("");
        edtPhone.setText("");
        edtRole.setText("ADMIN");
        edtStatus.setText("");
        imgAvatar.setImageResource(R.drawable.ic_profile);
    }

    private void updateAdminProfile() {
        String token = getToken();

        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy token đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String inputFullName = safe(edtFullName.getText().toString());
        String inputEmail = safe(edtGmail.getText().toString());
        String inputPhone = safe(edtPhone.getText().toString());

        final String finalFullName;

        if (inputFullName.isEmpty()) {
            finalFullName = "Admin";
        } else {
            finalFullName = inputFullName;
        }

        try {
            JSONObject body = new JSONObject();

            body.put("full_name", finalFullName);

            if (!inputEmail.isEmpty()) {
                body.put("email", inputEmail);
            } else {
                body.put("email", JSONObject.NULL);
            }

            if (!inputPhone.isEmpty()) {
                body.put("phone", inputPhone);
            } else {
                body.put("phone", JSONObject.NULL);
            }

            String url = BASE_URL + "/api/users/admin/update";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PATCH,
                    url,
                    body,
                    response -> {
                        Toast.makeText(
                                AdminProfile.this,
                                "Cập nhật thông tin admin thành công",
                                Toast.LENGTH_SHORT
                        ).show();

                        SharedPreferences sharedPreferences =
                                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

                        sharedPreferences.edit()
                                .putString(KEY_FULL_NAME, finalFullName)
                                .apply();

                        loadAdminInfo();
                    },
                    error -> {
                        Toast.makeText(
                                AdminProfile.this,
                                "Cập nhật thất bại",
                                Toast.LENGTH_LONG
                        ).show();
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

            requestQueue.add(request);

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tạo dữ liệu cập nhật", Toast.LENGTH_SHORT).show();
        }
    }

    private String getToken() {
        SharedPreferences sharedPreferences =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        return sharedPreferences.getString(KEY_ACCESS_TOKEN, "");
    }

    private String safe(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) {
            return "";
        }

        return value.trim();
    }
}