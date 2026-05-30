package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
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
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TeacherEditProfileActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtUsername, edtPhone, edtAddress;
    private ImageView imgAvatarEdit;
    private MaterialButton btnSave;

    private Uri selectedAvatarUri = null;

    private String oldName = "";
    private String oldEmail = "";
    private String oldUsername = "";
    private String oldPhone = "";
    private String oldAddress = "";
    private String oldAvatarUrl = "";

    private static final String PREF_NAME = "APP_PREFS";
    private static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String KEY_USER_ID = "USER_ID";

    private static final String BASE_URL = "http://10.0.2.2:8080/NT118";

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        imgAvatarEdit = findViewById(R.id.imgAvatarEdit);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtUsername = findViewById(R.id.edtUsername);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        btnSave = findViewById(R.id.btnSave);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedAvatarUri = uri;
                        imgAvatarEdit.setImageURI(uri);
                    }
                }
        );

        loadDataFromIntent();

        imgAvatarEdit.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnSave.setOnClickListener(v -> saveChanges());
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
        oldAddress = safeString(intent.getStringExtra("address"));
        oldAvatarUrl = safeString(intent.getStringExtra("avatar_url"));

        edtName.setText(oldName);
        edtEmail.setText(oldEmail);
        edtUsername.setText(oldUsername);
        edtPhone.setText(oldPhone);
        edtAddress.setText(oldAddress);

        if (!oldAvatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(oldAvatarUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(imgAvatarEdit);
        } else {
            imgAvatarEdit.setImageResource(R.drawable.ic_profile);
        }
    }

    private void saveChanges() {
        JSONObject body = buildChangedJsonBody();
        boolean hasTextChanged = body.length() > 0;
        boolean hasAvatarChanged = selectedAvatarUri != null;

        if (!hasTextChanged && !hasAvatarChanged) {
            Toast.makeText(this, "Bạn chưa thay đổi thông tin nào", Toast.LENGTH_SHORT).show();
            return;
        }

        if (hasTextChanged) {
            updateTeacherInfo(body, hasAvatarChanged);
        } else {
            updateAvatarOnly();
        }
    }

    private JSONObject buildChangedJsonBody() {
        JSONObject body = new JSONObject();

        String newName = edtName.getText().toString().trim();
        String newEmail = edtEmail.getText().toString().trim();
        String newUsername = edtUsername.getText().toString().trim();
        String newPhone = edtPhone.getText().toString().trim();
        String newAddress = edtAddress.getText().toString().trim();

        try {
            if (!newName.equals(oldName)) {
                body.put("full_name", newName);
            }

            if (!newEmail.equals(oldEmail)) {
                body.put("email", newEmail);
            }

            if (!newUsername.equals(oldUsername)) {
                body.put("username", newUsername);
            }

            if (!newPhone.equals(oldPhone)) {
                body.put("phone", newPhone);
            }

            if (!newAddress.equals(oldAddress)) {
                body.put("department", newAddress);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return body;
    }

    private void updateTeacherInfo(JSONObject body, boolean uploadAvatarAfterTextUpdate) {
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

        String url = BASE_URL + "/api/teachers/update?userId=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PATCH,
                url,
                body,
                response -> {
                    if (uploadAvatarAfterTextUpdate) {
                        updateAvatarOnly();
                    } else {
                        Toast.makeText(this, "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show();
                        finishWithSuccess();
                    }
                },
                error -> {
                    String message = "Lỗi cập nhật thông tin";

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

    private void updateAvatarOnly() {
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

        if (selectedAvatarUri == null) {
            finishWithSuccess();
            return;
        }

        String url = BASE_URL + "/api/users/" + userId + "/avatar";

        VolleyMultipartRequest request = new VolleyMultipartRequest(
                Request.Method.PATCH,
                url,
                response -> {
                    Toast.makeText(this, "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show();
                    finishWithSuccess();
                },
                error -> {
                    String message = "Lỗi cập nhật ảnh đại diện";

                    if (error.networkResponse != null) {
                        message += ": HTTP " + error.networkResponse.statusCode;
                    } else {
                        message += ": " + error.toString();
                    }

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                return new HashMap<>();
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();

                try {
                    byte[] imageBytes = getBytesFromUri(selectedAvatarUri);
                    String fileName = getFileNameFromUri(selectedAvatarUri);
                    String mimeType = getContentResolver().getType(selectedAvatarUri);

                    if (mimeType == null || mimeType.trim().isEmpty()) {
                        mimeType = "image/jpeg";
                    }

                    params.put(
                            "avatar",
                            new DataPart(fileName, imageBytes, mimeType)
                    );

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return params;
            }

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

    private void finishWithSuccess() {
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private byte[] getBytesFromUri(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;

        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        if (inputStream != null) {
            inputStream.close();
        }

        return byteBuffer.toByteArray();
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = "avatar_" + System.currentTimeMillis() + ".jpg";

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

            if (nameIndex >= 0 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex);
            }

            cursor.close();
        }

        return fileName;
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }
}