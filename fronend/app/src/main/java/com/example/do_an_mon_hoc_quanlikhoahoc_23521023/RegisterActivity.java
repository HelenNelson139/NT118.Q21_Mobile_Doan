package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText editUsername, editName, editEmail, editPhone, editPassword, editExtraInfo;
    private TextView textExtraLabel;
    private RadioGroup radioRoleGroup;
    private RadioButton radioTeacher, radioStudent;
    private ImageView imgAvatarPreview;
    private Button buttonPickAvatar, buttonSubmit;

    private Uri selectedAvatarUri = null;

    private static final String BASE_URL = "http://10.0.2.2:8080/NT118";
    private static final String TEACHER_REGISTER_URL = BASE_URL + "/api/teachers/register";
    private static final String STUDENT_REGISTER_URL = BASE_URL + "/api/students/register";

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editUsername = findViewById(R.id.editUsername);
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        editPassword = findViewById(R.id.editPassword);
        editExtraInfo = findViewById(R.id.editExtraInfo);

        textExtraLabel = findViewById(R.id.textExtraLabel);

        radioRoleGroup = findViewById(R.id.radioRoleGroup);
        radioTeacher = findViewById(R.id.radioTeacher);
        radioStudent = findViewById(R.id.radioStudent);

        imgAvatarPreview = findViewById(R.id.imgAvatarPreview);
        buttonPickAvatar = findViewById(R.id.buttonPickAvatar);
        buttonSubmit = findViewById(R.id.buttonSubmitRegister);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedAvatarUri = uri;
                        imgAvatarPreview.setImageURI(uri);
                    }
                }
        );

        buttonPickAvatar.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        radioRoleGroup.setOnCheckedChangeListener((group, checkedId) -> updateExtraFieldByRole());

        buttonSubmit.setOnClickListener(v -> handleRegister());

        updateExtraFieldByRole();
    }

    private void updateExtraFieldByRole() {
        if (radioTeacher.isChecked()) {
            textExtraLabel.setText("KHOA");
            editExtraInfo.setHint("Nhập khoa");
            editExtraInfo.setText("");
            editExtraInfo.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        } else {
            textExtraLabel.setText("NGÀY SINH");
            editExtraInfo.setHint("Nhập ngày sinh, ví dụ: 2003-05-20");
            editExtraInfo.setText("");
            editExtraInfo.setInputType(android.text.InputType.TYPE_CLASS_DATETIME);
        }
    }

    private void handleRegister() {
        String username = editUsername.getText().toString().trim();
        String fullName = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String extraInfo = editExtraInfo.getText().toString().trim();

        boolean isTeacher = radioTeacher.isChecked();

        if (username.isEmpty()
                || fullName.isEmpty()
                || email.isEmpty()
                || phone.isEmpty()
                || password.isEmpty()
                || extraInfo.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải từ 6 ký tự trở lên", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isTeacher && !isValidDateFormat(extraInfo)) {
            Toast.makeText(this, "Ngày sinh phải có dạng yyyy-MM-dd, ví dụ 2003-05-20", Toast.LENGTH_LONG).show();
            return;
        }

        sendRegisterRequest(
                username,
                fullName,
                email,
                phone,
                password,
                extraInfo,
                isTeacher
        );
    }

    private void sendRegisterRequest(
            String username,
            String fullName,
            String email,
            String phone,
            String password,
            String extraInfo,
            boolean isTeacher
    ) {
        buttonSubmit.setEnabled(false);
        buttonSubmit.setText("Đang đăng ký...");

        String url = isTeacher ? TEACHER_REGISTER_URL : STUDENT_REGISTER_URL;

        VolleyMultipartRequest request = new VolleyMultipartRequest(
                Request.Method.POST,
                url,
                response -> {
                    Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    buttonSubmit.setEnabled(true);
                    buttonSubmit.setText("Đăng ký");

                    String message = "Đăng ký thất bại";

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
                Map<String, String> params = new HashMap<>();

                params.put("username", username);
                params.put("phone", phone);
                params.put("email", email);
                params.put("password", password);
                params.put("full_name", fullName);
                params.put("status", "ACTIVE");

                if (isTeacher) {
                    params.put("role", "TEACHER");
                    params.put("department", extraInfo);
                } else {
                    params.put("role", "STUDENT");
                    params.put("date_of_birth", extraInfo);
                }

                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();

                if (selectedAvatarUri != null) {
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
                }

                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private boolean isValidDateFormat(String date) {
        return date.matches("^\\d{4}-\\d{2}-\\d{2}$");
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
}