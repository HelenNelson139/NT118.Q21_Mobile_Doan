package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtName, txtEmail, txtUsername, txtPhone, txtAddress;
    private TextView txtRole, txtCourses, txtStatus;
    private MaterialButton btnEdit;

    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Ánh xạ các view
        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtUsername = findViewById(R.id.txtUsername);
        txtPhone = findViewById(R.id.txtPhone);
        txtAddress = findViewById(R.id.txtAddress);

        txtRole = findViewById(R.id.txtRole);
        txtCourses = findViewById(R.id.txtCourses);
        txtStatus = findViewById(R.id.txtStatus);

        btnEdit = findViewById(R.id.btnEdit);

        showProfileData();

        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();

                        txtName.setText(data.getStringExtra("name"));
                        txtEmail.setText(data.getStringExtra("email"));
                        txtUsername.setText(data.getStringExtra("username"));
                        txtPhone.setText(data.getStringExtra("phone"));
                        txtAddress.setText(data.getStringExtra("address"));
                    }
                }
        );

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);

            intent.putExtra("name", txtName.getText().toString());
            intent.putExtra("email", txtEmail.getText().toString());
            intent.putExtra("username", txtUsername.getText().toString());
            intent.putExtra("phone", txtPhone.getText().toString());
            intent.putExtra("address", txtAddress.getText().toString());

            editProfileLauncher.launch(intent);
        });
    }

    private void showProfileData() {
        txtName.setText("Nguyễn Văn A");
        txtEmail.setText("email@example.com");
        txtUsername.setText("student01");
        txtPhone.setText("0123456789");
        txtAddress.setText("TP.HCM");

        txtRole.setText("Học viên");
        txtCourses.setText("5");
        txtStatus.setText("Đang hoạt động");
    }
}