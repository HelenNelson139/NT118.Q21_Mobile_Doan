package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TeacherEditProfileActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:8080/NT118";

    private static final String PREF_NAME = "APP_PREFS";
    private static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String KEY_USER_ID = "USER_ID";

    private EditText edtName, edtEmail, edtUsername, edtPhone, edtAddress;
    private Button btnSave;
    private ImageView imgAvatar;
    private MaterialCardView avatarCard;

    private String avatarUrl = "";
    private Uri selectedImageUri = null;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        imgAvatar = findViewById(R.id.imgAvatar);
        avatarCard = findViewById(R.id.avatarCard);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtUsername = findViewById(R.id.edtUsername);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);

        btnSave = findViewById(R.id.btnSave);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;

                        Glide.with(this)
                                .load(uri)
                                .circleCrop()
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .into(imgAvatar);
                    }
                }
        );

        avatarCard.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        loadDataFromIntent();

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadDataFromIntent() {
        Intent intent = getIntent();
        if (intent == null) return;

        String name = intent.getStringExtra("name");
        String email = intent.getStringExtra("email");
        String username = intent.getStringExtra("username");
        String phone = intent.getStringExtra("phone");
        String address = intent.getStringExtra("address");
        avatarUrl = intent.getStringExtra("avatar_url");

        edtName.setText(name != null ? name : "");
        edtEmail.setText(email != null ? email : "");
        edtUsername.setText(username != null ? username : "");
        edtPhone.setText(phone != null ? phone : "");
        edtAddress.setText(address != null ? address : "");

        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.ic_profile);
        }
    }

    private void saveProfile() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String department = edtAddress.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String token = prefs.getString(KEY_ACCESS_TOKEN, "");
        int userId = prefs.getInt(KEY_USER_ID, -1);

        if (token.isEmpty() || userId == -1) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Đang lưu...");

        try {
            JSONObject body = new JSONObject();

            body.put("full_name", name);
            body.put("email", email);
            body.put("username", username);
            body.put("phone", phone);
            body.put("department", department);

            String url = BASE_URL + "/api/teachers/update?userId=" + userId;

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PATCH,
                    url,
                    body,
                    response -> {
                        if (selectedImageUri != null) {
                            uploadAvatar(userId, token);
                        } else {
                            finishSuccess(name, email, username, phone, department);
                        }
                    },
                    error -> {
                        btnSave.setEnabled(true);
                        btnSave.setText("Lưu thay đổi");

                        String message = "Cập nhật thông tin thất bại";

                        if (error.networkResponse != null) {
                            message += " - Code: " + error.networkResponse.statusCode;
                            try {
                                String responseBody = new String(error.networkResponse.data);
                                message += "\n" + responseBody;
                            } catch (Exception ignored) {
                            }
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

        } catch (Exception e) {
            btnSave.setEnabled(true);
            btnSave.setText("Lưu thay đổi");
            Toast.makeText(this, "Lỗi dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void uploadAvatar(int userId, String token) {
        String url = BASE_URL + "/api/users/" + userId + "/avatar";

        VolleyMultipartRequest request = new VolleyMultipartRequest(
                Request.Method.PATCH,
                url,
                response -> {
                    String name = edtName.getText().toString().trim();
                    String email = edtEmail.getText().toString().trim();
                    String username = edtUsername.getText().toString().trim();
                    String phone = edtPhone.getText().toString().trim();
                    String department = edtAddress.getText().toString().trim();

                    finishSuccess(name, email, username, phone, department);
                },
                error -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("Lưu thay đổi");

                    String message = "Thông tin đã lưu nhưng upload avatar thất bại";

                    if (error.networkResponse != null) {
                        message += " - Code: " + error.networkResponse.statusCode;
                        try {
                            String responseBody = new String(error.networkResponse.data);
                            message += "\n" + responseBody;
                        } catch (Exception ignored) {
                        }
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

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();

                try {
                    byte[] fileBytes = getBytesFromUri(selectedImageUri);
                    String fileName = getFileNameFromUri(selectedImageUri);

                    params.put("avatar", new DataPart(
                            fileName,
                            fileBytes,
                            "image/jpeg"
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void finishSuccess(
            String name,
            String email,
            String username,
            String phone,
            String department
    ) {
        Intent resultIntent = new Intent();

        resultIntent.putExtra("name", name);
        resultIntent.putExtra("email", email);
        resultIntent.putExtra("username", username);
        resultIntent.putExtra("phone", phone);
        resultIntent.putExtra("address", department);

        if (selectedImageUri != null) {
            resultIntent.putExtra("selected_avatar_uri", selectedImageUri.toString());
        }

        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Cập nhật profile thành công", Toast.LENGTH_SHORT).show();

        btnSave.setEnabled(true);
        btnSave.setText("Lưu thay đổi");

        finish();
    }

    private byte[] getBytesFromUri(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);

        if (inputStream == null) {
            throw new RuntimeException("Không đọc được file ảnh");
        }

        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;

        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        inputStream.close();

        return byteBuffer.toByteArray();
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = "avatar.jpg";

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

            if (cursor.moveToFirst() && nameIndex >= 0) {
                fileName = cursor.getString(nameIndex);
            }

            cursor.close();
        }

        return fileName;
    }
}