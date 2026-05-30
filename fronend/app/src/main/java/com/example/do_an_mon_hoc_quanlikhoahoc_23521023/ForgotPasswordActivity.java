package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText editUsername, editOldPassword, editNewPassword, editConfirmNewPassword;
    private Button buttonChangePassword;

    private static final String BASE_URL = "http://10.0.2.2:8080/NT118";

    private static final String LOGIN_URL =
            BASE_URL + "/api/auth/login";

    private static final String CHANGE_PASSWORD_URL =
            BASE_URL + "/api/users/password?userId=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        editUsername = findViewById(R.id.editUsername);
        editOldPassword = findViewById(R.id.editOldPassword);
        editNewPassword = findViewById(R.id.editNewPassword);
        editConfirmNewPassword = findViewById(R.id.editConfirmNewPassword);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);

        buttonChangePassword.setOnClickListener(v -> handleChangePassword());
    }

    private void handleChangePassword() {
        String username = editUsername.getText().toString().trim();
        String oldPassword = editOldPassword.getText().toString().trim();
        String newPassword = editNewPassword.getText().toString().trim();
        String confirmNewPassword = editConfirmNewPassword.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        if (oldPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu cũ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(this, "Mật khẩu mới nhập lại không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        if (oldPassword.equals(newPassword)) {
            Toast.makeText(this, "Mật khẩu mới không được trùng mật khẩu cũ", Toast.LENGTH_SHORT).show();
            return;
        }

        loginThenChangePassword(username, oldPassword, newPassword);
    }

    private void loginThenChangePassword(String username, String oldPassword, String newPassword) {
        try {
            JSONObject loginBody = new JSONObject();
            loginBody.put("username", username);
            loginBody.put("password", oldPassword);

            JsonObjectRequest loginRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    LOGIN_URL,
                    loginBody,
                    response -> {
                        try {
                            int code = response.getInt("code");

                            if (code == 0 || code == 1000) {
                                JSONObject result = response.getJSONObject("result");

                                boolean authenticated = result.getBoolean("authenticated");

                                if (!authenticated) {
                                    Toast.makeText(this, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                String accessToken = result.getString("accessToken");
                                int userId = result.getInt("id");

                                changePassword(userId, accessToken, oldPassword, newPassword);

                            } else {
                                Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            Toast.makeText(
                                    this,
                                    "Lỗi đọc dữ liệu login: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    },
                    error -> {
                        Toast.makeText(
                                this,
                                "Đăng nhập ngầm thất bại. Kiểm tra username hoặc mật khẩu cũ",
                                Toast.LENGTH_LONG
                        ).show();
                    }
            );

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(loginRequest);

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tạo dữ liệu đăng nhập", Toast.LENGTH_SHORT).show();
        }
    }

    private void changePassword(int userId, String accessToken, String oldPassword, String newPassword) {
        try {
            String url = CHANGE_PASSWORD_URL + userId;

            JSONObject body = new JSONObject();
            body.put("oldPassword", oldPassword);
            body.put("newPassword", newPassword);

            JsonObjectRequest changePasswordRequest = new JsonObjectRequest(
                    Request.Method.PATCH,
                    url,
                    body,
                    response -> {
                        Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    error -> {
                        String message = "Lỗi đổi mật khẩu";

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
                    headers.put("Authorization", "Bearer " + accessToken);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(changePasswordRequest);

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tạo dữ liệu đổi mật khẩu", Toast.LENGTH_SHORT).show();
        }
    }
}