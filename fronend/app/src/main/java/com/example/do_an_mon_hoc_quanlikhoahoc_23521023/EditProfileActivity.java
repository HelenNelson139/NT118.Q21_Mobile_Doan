package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class EditProfileActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtUsername, edtPhone, edtAddress;
    private MaterialButton btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtUsername = findViewById(R.id.edtUsername);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        btnSave = findViewById(R.id.btnSave);

        Intent intent = getIntent();
        if (intent != null) {
            edtName.setText(intent.getStringExtra("name"));
            edtEmail.setText(intent.getStringExtra("email"));
            edtUsername.setText(intent.getStringExtra("username"));
            edtPhone.setText(intent.getStringExtra("phone"));
            edtAddress.setText(intent.getStringExtra("address"));
        }

        btnSave.setOnClickListener(v -> {
            Intent resultIntent = new Intent();

            resultIntent.putExtra("name", edtName.getText().toString().trim());
            resultIntent.putExtra("email", edtEmail.getText().toString().trim());
            resultIntent.putExtra("username", edtUsername.getText().toString().trim());
            resultIntent.putExtra("phone", edtPhone.getText().toString().trim());
            resultIntent.putExtra("address", edtAddress.getText().toString().trim());

            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}