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

public class EditProfileActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtUsername, edtPhone, edtAddress;
    private Button btnSave;
    private ImageView imgAvatar;
    private MaterialCardView avatarCard;

    private Uri selectedAvatarUri = null;

    private String oldName = "";
    private String oldEmail = "";
    private String oldUsername = "";
    private String oldPhone = "";
    private String oldDateOfBirth = "";
    private String oldAvatarUrl = "";

    private static final String PREF_NAME = "APP_PREFS";
    private static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String KEY_USER_ID = "USER_ID";

    private static final String BASE_URL = "http://10.0.2.2:8080/NT118";

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student_profile);

        imgAvatar = findViewById(R.id.imgAvatar);
        avatarCard = findViewById(R.id.avatarCard);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtUsername = findViewById(R.id.edtUsername);
        edtPhone = findViewById(R.id.edtPhone);

        // Với student, edtAddress dùng để nhập ngày sinh yyyy-MM-dd
        edtAddress = findViewById(R.id.edtAddress);

        btnSave = findViewById(R.id.btnSave);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedAvatarUri = uri;

                        Glide.with(this)
                                .load(uri)
                                .circleCrop()
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .into(imgAvatar);
                    }
                }
        );

        avatarCard.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        loadDataFromIntent();

        btnSave.setOnClickListener(v -> saveProfile());
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
        oldAvatarUrl = safeString(intent.getStringExtra("avatar_url"));

        edtName.setText(oldName);
        edtEmail.setText(oldEmail);
        edtUsername.setText(oldUsername);
        edtPhone.setText(oldPhone);
        edtAddress.setText(oldDateOfBirth);

        edtUsername.setEnabled(false);

        if (!oldAvatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(oldAvatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.ic_profile);
        }
    }

    private void saveProfile() {
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
            edtName.setError("Vui lòng nhập họ tên");
            edtName.requestFocus();
            return;
        }

        if (newEmail.isEmpty()) {
            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return;
        }

        if (!newDateOfBirth.isEmpty() && !isValidDateFormat(newDateOfBirth)) {
            edtAddress.setError("Ngày sinh phải có dạng yyyy-MM-dd");
            edtAddress.requestFocus();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Đang lưu...");

        boolean profileChanged =
                !newName.equals(oldName)
                        || !newEmail.equals(oldEmail)
                        || !newPhone.equals(oldPhone)
                        || !newDateOfBirth.equals(oldDateOfBirth);

        boolean avatarChanged = selectedAvatarUri != null;

        if (!profileChanged && !avatarChanged) {
            Toast.makeText(this, "Không có thay đổi nào", Toast.LENGTH_SHORT).show();
            btnSave.setEnabled(true);
            btnSave.setText("LƯU");
            return;
        }

        if (profileChanged) {
            updateStudentProfile(
                    userId,
                    token,
                    newName,
                    newEmail,
                    newPhone,
                    newDateOfBirth,
                    avatarChanged
            );
        } else {
            uploadAvatar(userId, token);
        }
    }

    private void updateStudentProfile(
            int userId,
            String token,
            String name,
            String email,
            String phone,
            String dateOfBirth,
            boolean uploadAvatarAfterProfile
    ) {
        try {
            JSONObject body = new JSONObject();

            body.put("full_name", name);
            body.put("email", email);
            body.put("phone", phone);

            if (!dateOfBirth.isEmpty()) {
                body.put("date_of_birth", dateOfBirth);
            }

            String url = BASE_URL + "/api/students/update?userId=" + userId;

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PATCH,
                    url,
                    body,
                    response -> {
                        if (uploadAvatarAfterProfile) {
                            uploadAvatar(userId, token);
                        } else {
                            finishSuccess();
                        }
                    },
                    error -> {
                        btnSave.setEnabled(true);
                        btnSave.setText("LƯU");

                        String message = "Cập nhật thông tin thất bại";

                        if (error.networkResponse != null) {
                            message += " - Code: " + error.networkResponse.statusCode;
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

        } catch (Exception e) {
            btnSave.setEnabled(true);
            btnSave.setText("LƯU");
            Toast.makeText(this, "Lỗi dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void uploadAvatar(int userId, String token) {
        String url = BASE_URL + "/api/users/" + userId + "/avatar";

        VolleyMultipartRequest request = new VolleyMultipartRequest(
                Request.Method.PATCH,
                url,
                response -> finishSuccess(),
                error -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("LƯU");

                    String message = "Thông tin đã lưu nhưng upload avatar thất bại";

                    if (error.networkResponse != null) {
                        message += " - Code: " + error.networkResponse.statusCode;
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

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();

                try {
                    byte[] fileBytes = getBytesFromUri(selectedAvatarUri);
                    String fileName = getFileNameFromUri(selectedAvatarUri);
                    String mimeType = getContentResolver().getType(selectedAvatarUri);

                    if (mimeType == null || mimeType.trim().isEmpty()) {
                        mimeType = "image/jpeg";
                    }

                    params.put(
                            "avatar",
                            new DataPart(fileName, fileBytes, mimeType)
                    );

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void finishSuccess() {
        Toast.makeText(this, "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show();

        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);

        btnSave.setEnabled(true);
        btnSave.setText("LƯU");

        finish();
    }

    private byte[] getBytesFromUri(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);

        if (inputStream == null) {
            throw new RuntimeException("Không đọc được ảnh");
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
        String fileName = "avatar_" + System.currentTimeMillis() + ".jpg";

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

    private boolean isValidDateFormat(String date) {
        return date.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }

    private String safeString(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) {
            return "";
        }

        return value.trim();
    }
}