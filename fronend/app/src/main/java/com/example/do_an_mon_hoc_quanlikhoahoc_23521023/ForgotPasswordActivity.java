package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

public class ForgotPasswordActivity extends AppCompatActivity {

    private LinearLayout layoutPhoneStep, layoutOtpStep, layoutPasswordStep;
    private TextView textDescription;

    private EditText editPhone, editOtp;
    private TextInputEditText editNewPassword, editConfirmPassword;

    private Button buttonSendOtp, buttonVerifyOtp, buttonResetPassword;

    private RequestQueue requestQueue;

    private String verifiedPhone = "";
    private String verifiedOtp = "";

    private static final String BASE_URL = "http://10.0.2.2:8080/NT118";

    private static final String FORGOT_PASSWORD_URL =
            BASE_URL + "/api/auth/forgot-password";

    private static final String VERIFY_OTP_URL =
            BASE_URL + "/api/auth/verify-otp";

    private static final String RESET_PASSWORD_URL =
            BASE_URL + "/api/auth/reset-password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        requestQueue = Volley.newRequestQueue(this);

        mappingView();

        showPhoneStep();

        buttonSendOtp.setOnClickListener(v -> sendOtp());
        buttonVerifyOtp.setOnClickListener(v -> verifyOtp());
        buttonResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void mappingView() {
        layoutPhoneStep = findViewById(R.id.layoutPhoneStep);
        layoutOtpStep = findViewById(R.id.layoutOtpStep);
        layoutPasswordStep = findViewById(R.id.layoutPasswordStep);

        textDescription = findViewById(R.id.textDescription);

        editPhone = findViewById(R.id.editPhone);
        editOtp = findViewById(R.id.editOtp);
        editNewPassword = findViewById(R.id.editNewPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);

        buttonSendOtp = findViewById(R.id.buttonSendOtp);
        buttonVerifyOtp = findViewById(R.id.buttonVerifyOtp);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);
    }

    private void showPhoneStep() {
        layoutPhoneStep.setVisibility(View.VISIBLE);
        layoutOtpStep.setVisibility(View.GONE);
        layoutPasswordStep.setVisibility(View.GONE);

        textDescription.setText("Nhập số điện thoại để nhận mã xác thực OTP.");
    }

    private void showOtpStep() {
        layoutPhoneStep.setVisibility(View.GONE);
        layoutOtpStep.setVisibility(View.VISIBLE);
        layoutPasswordStep.setVisibility(View.GONE);

        textDescription.setText("Mã OTP đã được gửi qua SMS. Vui lòng nhập mã để xác nhận.");
    }

    private void showPasswordStep() {
        layoutPhoneStep.setVisibility(View.GONE);
        layoutOtpStep.setVisibility(View.GONE);
        layoutPasswordStep.setVisibility(View.VISIBLE);

        textDescription.setText("OTP hợp lệ. Vui lòng nhập mật khẩu mới.");
    }

    private void sendOtp() {
        String phone = editPhone.getText().toString().trim();

        if (phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.length() < 9) {
            Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("phone", phone);

            buttonSendOtp.setEnabled(false);
            buttonSendOtp.setText("Đang gửi...");

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    FORGOT_PASSWORD_URL,
                    body,
                    response -> {
                        buttonSendOtp.setEnabled(true);
                        buttonSendOtp.setText("Gửi mã OTP");

                        verifiedPhone = phone;

                        Toast.makeText(
                                this,
                                "Đã gửi OTP. Hãy kiểm tra SMS trên emulator",
                                Toast.LENGTH_SHORT
                        ).show();

                        showOtpStep();
                    },
                    error -> {
                        buttonSendOtp.setEnabled(true);
                        buttonSendOtp.setText("Gửi mã OTP");

                        Toast.makeText(
                                this,
                                getVolleyErrorMessage(error, "Gửi OTP thất bại"),
                                Toast.LENGTH_LONG
                        ).show();
                    }
            );

            requestQueue.add(request);

        } catch (Exception e) {
            buttonSendOtp.setEnabled(true);
            buttonSendOtp.setText("Gửi mã OTP");
            Toast.makeText(this, "Lỗi tạo dữ liệu gửi OTP", Toast.LENGTH_SHORT).show();
        }
    }

    private void verifyOtp() {
        String otp = editOtp.getText().toString().trim();

        if (verifiedPhone.isEmpty()) {
            Toast.makeText(this, "Vui lòng gửi OTP trước", Toast.LENGTH_SHORT).show();
            showPhoneStep();
            return;
        }

        if (otp.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        if (otp.length() != 6) {
            Toast.makeText(this, "Mã OTP phải gồm 6 số", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("phone", verifiedPhone);
            body.put("otp", otp);

            buttonVerifyOtp.setEnabled(false);
            buttonVerifyOtp.setText("Đang xác nhận...");

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    VERIFY_OTP_URL,
                    body,
                    response -> {
                        buttonVerifyOtp.setEnabled(true);
                        buttonVerifyOtp.setText("Xác nhận OTP");

                        boolean verified = true;

                        try {
                            if (response.has("result")) {
                                verified = response.getBoolean("result");
                            }
                        } catch (Exception ignored) {
                            verified = true;
                        }

                        if (verified) {
                            verifiedOtp = otp;
                            Toast.makeText(this, "Xác nhận OTP thành công", Toast.LENGTH_SHORT).show();
                            showPasswordStep();
                        } else {
                            Toast.makeText(this, "OTP không đúng hoặc đã hết hạn", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        buttonVerifyOtp.setEnabled(true);
                        buttonVerifyOtp.setText("Xác nhận OTP");

                        Toast.makeText(
                                this,
                                getVolleyErrorMessage(error, "OTP không đúng hoặc đã hết hạn"),
                                Toast.LENGTH_LONG
                        ).show();
                    }
            );

            requestQueue.add(request);

        } catch (Exception e) {
            buttonVerifyOtp.setEnabled(true);
            buttonVerifyOtp.setText("Xác nhận OTP");
            Toast.makeText(this, "Lỗi tạo dữ liệu xác nhận OTP", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetPassword() {
        String newPassword = editNewPassword.getText() == null
                ? ""
                : editNewPassword.getText().toString().trim();

        String confirmPassword = editConfirmPassword.getText() == null
                ? ""
                : editConfirmPassword.getText().toString().trim();

        if (verifiedPhone.isEmpty() || verifiedOtp.isEmpty()) {
            Toast.makeText(this, "Vui lòng xác nhận OTP trước", Toast.LENGTH_SHORT).show();
            showOtpStep();
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

        if (confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng xác nhận mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("phone", verifiedPhone);
            body.put("otp", verifiedOtp);
            body.put("newPassword", newPassword);

            buttonResetPassword.setEnabled(false);
            buttonResetPassword.setText("Đang đổi...");

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    RESET_PASSWORD_URL,
                    body,
                    response -> {
                        buttonResetPassword.setEnabled(true);
                        buttonResetPassword.setText("Đổi mật khẩu");

                        Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    error -> {
                        buttonResetPassword.setEnabled(true);
                        buttonResetPassword.setText("Đổi mật khẩu");

                        Toast.makeText(
                                this,
                                getVolleyErrorMessage(error, "Đổi mật khẩu thất bại"),
                                Toast.LENGTH_LONG
                        ).show();
                    }
            );

            requestQueue.add(request);

        } catch (Exception e) {
            buttonResetPassword.setEnabled(true);
            buttonResetPassword.setText("Đổi mật khẩu");
            Toast.makeText(this, "Lỗi tạo dữ liệu đổi mật khẩu", Toast.LENGTH_SHORT).show();
        }
    }

    private String getVolleyErrorMessage(com.android.volley.VolleyError error, String defaultMessage) {
        if (error == null) {
            return defaultMessage;
        }

        if (error.networkResponse != null) {
            return defaultMessage + ": HTTP " + error.networkResponse.statusCode;
        }

        if (error.getMessage() != null) {
            return defaultMessage + ": " + error.getMessage();
        }

        return defaultMessage;
    }
}