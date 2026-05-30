package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private Button buttonLogin;

    private static final String LOGIN_URL =
            "http://10.0.2.2:8080/NT118/api/auth/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLoginSubmit);

        buttonLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String username = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                    this,
                    "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        sendLoginRequest(username, password);
    }

    private void sendLoginRequest(String username, String password) {
        try {
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("password", password);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    LOGIN_URL,
                    body,
                    response -> {
                        try {
                            int code = response.getInt("code");

                            if (code == 0 || code == 1000) {
                                JSONObject result = response.getJSONObject("result");

                                boolean authenticated = result.getBoolean("authenticated");
                                String role = result.getString("role");
                                String accessToken = result.getString("accessToken");
                                String fullName = result.getString("full_name");
                                int userId = result.getInt("id");

                                if (authenticated) {
                                    saveLoginData(accessToken, role, username, fullName, userId);
                                    Toast.makeText(
                                            this,
                                            "Đăng nhập thành công",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    navigateByRole(role);
                                } else {
                                    Toast.makeText(
                                            this,
                                            "Sai tài khoản hoặc mật khẩu",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            } else {
                                Toast.makeText(
                                        this,
                                        "Đăng nhập thất bại",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                        } catch (JSONException e) {
                            Toast.makeText(
                                    this,
                                    "Lỗi đọc dữ liệu phản hồi từ server",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    },
                    error -> {
                        Toast.makeText(
                                this,
                                "Lỗi kết nối server: " + error.toString(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
            );

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request);

        } catch (JSONException e) {
            Toast.makeText(
                    this,
                    "Lỗi tạo dữ liệu đăng nhập",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void saveLoginData(String accessToken, String role, String username, String fullName, int userId) {
        SharedPreferences sharedPreferences =
                getSharedPreferences("APP_PREFS", MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ACCESS_TOKEN", accessToken);
        editor.putString("ROLE", role);
        editor.putString("USERNAME", username);
        editor.putString("FULL_NAME", fullName);
        editor.putInt("USER_ID", userId);
        editor.putBoolean("IS_LOGGED_IN", true);
        editor.apply();
    }

    private void navigateByRole(String role) {
        Intent intent;

        if ("ADMIN".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
        } else if ("TEACHER".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, TeacherHome.class);
        } else if ("STUDENT".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, HomeActivity.class);
        } else {
            Toast.makeText(
                    this,
                    "Role không hợp lệ: " + role,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        startActivity(intent);
        finish();
    }
}