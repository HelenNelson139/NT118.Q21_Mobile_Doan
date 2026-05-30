package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

public class TeacherEditProfileActivity extends AppCompatActivity {

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
        String address = edtAddress.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("name", name);
        resultIntent.putExtra("email", email);
        resultIntent.putExtra("username", username);
        resultIntent.putExtra("phone", phone);
        resultIntent.putExtra("address", address);

        if (selectedImageUri != null) {
            resultIntent.putExtra("selected_avatar_uri", selectedImageUri.toString());
        }

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}