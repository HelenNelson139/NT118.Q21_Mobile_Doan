package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminModuleDetailActivity extends AppCompatActivity {

    private TextView tvModuleDetailTitle;
    private TextView tvModuleStatus;
    private TextView tvModuleContent;
    private TextView tvModuleGoal;
    private TextView tvModuleExample;

    private MaterialButton btnBack;

    private int moduleId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_module_detail);

        initViews();

        moduleId = getIntent().getIntExtra("MODULE_ID", -1);

        if (moduleId != -1) {
            fetchModuleDetail(moduleId);
        } else {
            Toast.makeText(this, "Không tìm thấy ID bài giảng", Toast.LENGTH_SHORT).show();
        }

        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvModuleDetailTitle = findViewById(R.id.tvModuleDetailTitle);
        tvModuleStatus = findViewById(R.id.tvModuleStatus);
        tvModuleContent = findViewById(R.id.tvModuleContent);
        tvModuleGoal = findViewById(R.id.tvModuleGoal);
        tvModuleExample = findViewById(R.id.tvModuleExample);
        btnBack = findViewById(R.id.btnBack);
    }

    private void fetchModuleDetail(int moduleId) {
        LessonApiService apiService = RetrofitClient.getClient().create(LessonApiService.class);

        apiService.getModuleById(moduleId).enqueue(new Callback<ApiResponse<ModuleResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ModuleResponse>> call,
                                   Response<ApiResponse<ModuleResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ModuleResponse> apiResponse = response.body();

                    if (apiResponse.getCode() == 1000) {
                        displayModule(apiResponse.getResult());
                    } else {
                        Toast.makeText(AdminModuleDetailActivity.this, "Lỗi: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminModuleDetailActivity.this, "Không lấy được chi tiết bài giảng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ModuleResponse>> call, Throwable t) {
                Toast.makeText(AdminModuleDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayModule(ModuleResponse module) {
        if (module == null) return;

        tvModuleDetailTitle.setText(safe(module.getTitle()));
        tvModuleStatus.setText("Trạng thái: " + safe(module.getStatus()));
        tvModuleContent.setText(safe(module.getContent()));

        /*
         * Nếu ModuleResponse của bạn chưa có getObjective/getExample
         * thì xóa 2 dòng dưới hoặc đổi theo đúng tên field DTO của bạn.
         */
        tvModuleGoal.setText(safe(module.getObjective()));
        tvModuleExample.setText(safe(module.getExample()));
    }

    private String safe(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) {
            return "";
        }
        return value.trim();
    }
}