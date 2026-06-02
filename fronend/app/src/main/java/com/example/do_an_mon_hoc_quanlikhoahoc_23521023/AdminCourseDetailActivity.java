package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCourseDetailActivity extends AppCompatActivity {

    private TextView tvHeaderTitle;
    private MaterialButton btnBack;
    private ImageView imgCourse;

    private TextView tvCourseInfo;
    private TextView tvCourseDescription;
    private TextView tvModuleListTitle;

    private LinearLayout layoutModules;

    private int lessonId = -1;
    private String currentLessonThumbnail = "";

    /*
     * true  = mở từ tab/trang "Chưa duyệt"
     * false = mở từ trang bình thường
     */
    private boolean adminPendingMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_course_detail);

        initViews();

        lessonId = getIntent().getIntExtra("COURSE_ID", -1);
        adminPendingMode = getIntent().getBooleanExtra("ADMIN_PENDING_MODE", false);

        if (lessonId != -1) {
            fetchLessonDetails(lessonId);
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID khóa học!", Toast.LENGTH_SHORT).show();
        }

        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvHeaderTitle = findViewById(R.id.tvFixedHeader);
        btnBack = findViewById(R.id.btnBack);
        imgCourse = findViewById(R.id.imgCourse);

        tvCourseInfo = findViewById(R.id.tvCourseInfo);
        tvCourseDescription = findViewById(R.id.tvCourseDescription);
        tvModuleListTitle = findViewById(R.id.tvModuleListTitle);

        layoutModules = findViewById(R.id.layoutModules);
    }

    private void fetchLessonDetails(int id) {
        LessonApiService apiService =
                RetrofitClient.getClient().create(LessonApiService.class);

        apiService.getLessonById(id).enqueue(new Callback<ApiResponse<LessonResponse>>() {
            @Override
            public void onResponse(
                    Call<ApiResponse<LessonResponse>> call,
                    Response<ApiResponse<LessonResponse>> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<LessonResponse> apiResponse = response.body();

                    if (apiResponse.getCode() == 1000 && apiResponse.getResult() != null) {
                        LessonResponse lesson = apiResponse.getResult();

                        displayLessonInfo(lesson);

                        if (adminPendingMode) {
                            loadPendingModulesByLessonId(lesson.getId());
                        } else {
                            loadNormalModulesByLessonId(lesson.getId());
                        }

                    } else {
                        Toast.makeText(
                                AdminCourseDetailActivity.this,
                                "Lỗi: " + apiResponse.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                } else {
                    Toast.makeText(
                            AdminCourseDetailActivity.this,
                            "Không lấy được dữ liệu khóa học, mã: " + response.code(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<ApiResponse<LessonResponse>> call,
                    Throwable t
            ) {
                Toast.makeText(
                        AdminCourseDetailActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void displayLessonInfo(LessonResponse lesson) {
        if (lesson == null) {
            return;
        }

        tvHeaderTitle.setText(safe(lesson.getTitle()));

        tvCourseInfo.setText(
                "Giảng viên: " + safe(lesson.getTeacher_name()) + "\n"
                        + "Ngày đăng: " + safe(lesson.getCreatedAt())
        );

        tvCourseDescription.setText(
                "Mô tả: " + safe(lesson.getDescription())
        );

        currentLessonThumbnail = safe(lesson.getThumbnailUrl());

        if (!currentLessonThumbnail.isEmpty()) {
            Glide.with(this)
                    .load(currentLessonThumbnail)
                    .placeholder(R.drawable.course_python)
                    .error(R.drawable.course_python)
                    .into(imgCourse);
        } else {
            imgCourse.setImageResource(R.drawable.course_python);
        }
    }

    /*
     * Trang Admin - Chưa duyệt:
     * Chỉ lấy module PENDING / PENDING_DELETE của lesson.
     * Không gọi /api/modules/lesson/{lessonId}.
     */
    private void loadPendingModulesByLessonId(Integer lessonId) {
        if (lessonId == null) {
            showEmptyModuleText("Không tìm thấy lessonId.");
            return;
        }

        clearModules();

        if (tvModuleListTitle != null) {
            tvModuleListTitle.setText("Danh sách bài giảng chờ xử lý");
        }

        ModuleApiService moduleApiService =
                RetrofitClient.getClient().create(ModuleApiService.class);

        moduleApiService.getPendingModulesByLessonId(lessonId)
                .enqueue(new Callback<ApiResponse<List<ModuleResponse>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<ModuleResponse>>> call,
                            Response<ApiResponse<List<ModuleResponse>>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<ModuleResponse>> apiResponse = response.body();

                            if (apiResponse.getCode() == 1000) {
                                List<ModuleResponse> modules = apiResponse.getResult();

                                if (modules == null || modules.isEmpty()) {
                                    showEmptyModuleText("Không có bài giảng nào đang chờ xử lý.");
                                    return;
                                }

                                renderPendingModuleList(modules);

                            } else {
                                showEmptyModuleText("Không lấy được danh sách bài giảng chờ xử lý.");
                            }

                        } else {
                            showEmptyModuleText("Không lấy được danh sách bài giảng chờ xử lý.");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<List<ModuleResponse>>> call,
                            Throwable t
                    ) {
                        showEmptyModuleText("Lỗi kết nối khi lấy bài giảng chờ xử lý.");
                    }
                });
    }

    private void loadNormalModulesByLessonId(Integer lessonId) {
        if (lessonId == null) {
            showEmptyModuleText("Không tìm thấy lessonId.");
            return;
        }

        clearModules();

        if (tvModuleListTitle != null) {
            tvModuleListTitle.setText("Danh sách bài giảng");
        }

        ModuleApiService moduleApiService =
                RetrofitClient.getClient().create(ModuleApiService.class);

        moduleApiService.getModulesByLessonId(lessonId)
                .enqueue(new Callback<ApiResponse<List<ModuleResponse>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<ModuleResponse>>> call,
                            Response<ApiResponse<List<ModuleResponse>>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<ModuleResponse>> apiResponse = response.body();

                            if (apiResponse.getCode() == 1000) {
                                List<ModuleResponse> modules = apiResponse.getResult();

                                if (modules == null || modules.isEmpty()) {
                                    showEmptyModuleText("Khóa học này chưa có bài giảng.");
                                    return;
                                }

                                renderNormalModuleList(modules);

                            } else {
                                showEmptyModuleText("Không lấy được danh sách bài giảng.");
                            }

                        } else {
                            showEmptyModuleText("Không lấy được danh sách bài giảng.");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<List<ModuleResponse>>> call,
                            Throwable t
                    ) {
                        showEmptyModuleText("Lỗi kết nối khi lấy danh sách bài giảng.");
                    }
                });
    }

    private void renderPendingModuleList(List<ModuleResponse> modules) {
        clearModules();

        ArrayList<ModuleResponse> displayModules = new ArrayList<>();

        for (ModuleResponse module : modules) {
            if (module == null || module.getId() == null) {
                continue;
            }

            String status = safe(module.getStatus());

            /*
             * Trang chưa duyệt chỉ hiện module cần xử lý.
             * Không hiện ACTIVE.
             */
            if ("PENDING".equalsIgnoreCase(status)
                    || "PENDING_DELETE".equalsIgnoreCase(status)) {
                displayModules.add(module);
            }
        }

        if (displayModules.isEmpty()) {
            showEmptyModuleText("Không có bài giảng nào đang chờ xử lý.");
            return;
        }

        ArrayList<Integer> moduleIds = new ArrayList<>();

        for (ModuleResponse module : displayModules) {
            moduleIds.add(module.getId());
        }

        for (ModuleResponse module : displayModules) {
            int realIndex = moduleIds.indexOf(module.getId());

            View moduleView = createPendingModuleView(
                    module,
                    moduleIds,
                    realIndex
            );

            layoutModules.addView(moduleView);
        }
    }

    private void renderNormalModuleList(List<ModuleResponse> modules) {
        clearModules();

        if (modules == null || modules.isEmpty()) {
            showEmptyModuleText("Không có bài giảng nào.");
            return;
        }

        ArrayList<Integer> moduleIds = new ArrayList<>();

        for (ModuleResponse module : modules) {
            if (module != null && module.getId() != null) {
                moduleIds.add(module.getId());
            }
        }

        for (ModuleResponse module : modules) {
            if (module == null || module.getId() == null) {
                continue;
            }

            int realIndex = moduleIds.indexOf(module.getId());

            View moduleView = createNormalModuleView(
                    module,
                    moduleIds,
                    realIndex
            );

            layoutModules.addView(moduleView);
        }
    }

    /*
     * Layout riêng có 3 nút: Xem chi tiết / Duyệt / Duyệt xoá
     */
    private View createPendingModuleView(
            ModuleResponse module,
            ArrayList<Integer> moduleIds,
            int index
    ) {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.item_admin_pending_module, layoutModules, false);

        TextView txtTitle = view.findViewById(R.id.txtPendingModuleTitle);
        TextView txtStatus = view.findViewById(R.id.txtPendingModuleStatus);
        TextView txtObjective = view.findViewById(R.id.txtPendingModuleObjective);
        TextView txtContent = view.findViewById(R.id.txtPendingModuleContent);
        TextView txtExample = view.findViewById(R.id.txtPendingModuleExample);

        MaterialButton btnView = view.findViewById(R.id.btnViewPendingModule);
        MaterialButton btnApprove = view.findViewById(R.id.btnApprovePendingModule);
        MaterialButton btnApproveDelete = view.findViewById(R.id.btnApproveDeletePendingModule);

        String status = safe(module.getStatus());

        txtTitle.setText(safe(module.getTitle()));
        txtStatus.setText("Trạng thái: " + status);
        txtObjective.setText("Mục tiêu: " + safe(module.getObjective()));
        txtContent.setText("Nội dung: " + safe(module.getContent()));
        txtExample.setText("Ví dụ: " + safe(module.getExample()));

        btnView.setOnClickListener(v -> openModuleDetail(moduleIds, index));

        if ("PENDING".equalsIgnoreCase(status)) {
            btnApprove.setVisibility(View.VISIBLE);
            btnApproveDelete.setVisibility(View.GONE);

            btnApprove.setText("Duyệt");
            btnApprove.setOnClickListener(v -> approvePendingModule(module.getId()));

        } else if ("PENDING_DELETE".equalsIgnoreCase(status)) {
            btnApprove.setVisibility(View.GONE);
            btnApproveDelete.setVisibility(View.VISIBLE);

            btnApproveDelete.setText("Duyệt xoá");
            btnApproveDelete.setOnClickListener(v -> approveDeletePendingModule(module.getId()));

        } else {
            btnApprove.setVisibility(View.GONE);
            btnApproveDelete.setVisibility(View.GONE);
        }

        return view;
    }

    /*
     * Trang bình thường dùng lại item_existing_module.
     */
    private View createNormalModuleView(
            ModuleResponse module,
            ArrayList<Integer> moduleIds,
            int index
    ) {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.item_existing_module, layoutModules, false);

        TextView txtTitle = view.findViewById(R.id.txtExistingModuleTitle);
        TextView txtStatus = view.findViewById(R.id.txtExistingModuleStatus);
        TextView txtObjective = view.findViewById(R.id.txtExistingModuleObjective);
        TextView txtContent = view.findViewById(R.id.txtExistingModuleContent);
        TextView txtExample = view.findViewById(R.id.txtExistingModuleExample);

        MaterialButton btnView = view.findViewById(R.id.btnViewExistingModule);
        MaterialButton btnDelete = view.findViewById(R.id.btnDeleteExistingModule);

        txtTitle.setText(safe(module.getTitle()));
        txtStatus.setText("Trạng thái: " + safe(module.getStatus()));
        txtObjective.setText("Mục tiêu: " + safe(module.getObjective()));
        txtContent.setText("Nội dung: " + safe(module.getContent()));
        txtExample.setText("Ví dụ: " + safe(module.getExample()));

        btnView.setText("Xem chi tiết");
        btnView.setOnClickListener(v -> openModuleDetail(moduleIds, index));

        if (btnDelete != null) {
            btnDelete.setVisibility(View.GONE);
        }

        return view;
    }

    private void openModuleDetail(ArrayList<Integer> moduleIds, int index) {
        if (moduleIds == null || moduleIds.isEmpty()) {
            Toast.makeText(this, "Không có bài giảng để mở", Toast.LENGTH_SHORT).show();
            return;
        }

        if (index < 0 || index >= moduleIds.size()) {
            Toast.makeText(this, "Vị trí bài giảng không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(AdminCourseDetailActivity.this, LessonActivity.class);
        intent.putIntegerArrayListExtra("MODULE_IDS", moduleIds);
        intent.putExtra("CURRENT_INDEX", index);
        intent.putExtra("PARENT_LESSON_THUMBNAIL", currentLessonThumbnail);
        startActivity(intent);
    }

    private void approvePendingModule(Integer moduleId) {
        if (moduleId == null) {
            Toast.makeText(this, "Không tìm thấy moduleId", Toast.LENGTH_SHORT).show();
            return;
        }

        ModuleApiService moduleApiService =
                RetrofitClient.getClient().create(ModuleApiService.class);

        moduleApiService.approveModule(moduleId)
                .enqueue(new Callback<ApiResponse<ModuleResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<ModuleResponse>> call,
                            Response<ApiResponse<ModuleResponse>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<ModuleResponse> apiResponse = response.body();

                            if (apiResponse.getCode() == 1000) {
                                Toast.makeText(
                                        AdminCourseDetailActivity.this,
                                        "Đã duyệt bài giảng",
                                        Toast.LENGTH_SHORT
                                ).show();

                                loadPendingModulesByLessonId(lessonId);

                            } else {
                                Toast.makeText(
                                        AdminCourseDetailActivity.this,
                                        "Lỗi: " + apiResponse.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                        } else {
                            Toast.makeText(
                                    AdminCourseDetailActivity.this,
                                    "Duyệt bài giảng thất bại, HTTP " + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<ModuleResponse>> call,
                            Throwable t
                    ) {
                        Toast.makeText(
                                AdminCourseDetailActivity.this,
                                "Lỗi kết nối khi duyệt bài giảng",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void approveDeletePendingModule(Integer moduleId) {
        if (moduleId == null) {
            Toast.makeText(this, "Không tìm thấy moduleId", Toast.LENGTH_SHORT).show();
            return;
        }

        ModuleApiService moduleApiService =
                RetrofitClient.getClient().create(ModuleApiService.class);

        moduleApiService.approveDeleteModule(moduleId)
                .enqueue(new Callback<ApiResponse<ModuleResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<ModuleResponse>> call,
                            Response<ApiResponse<ModuleResponse>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<ModuleResponse> apiResponse = response.body();

                            if (apiResponse.getCode() == 1000) {
                                Toast.makeText(
                                        AdminCourseDetailActivity.this,
                                        "Đã duyệt xoá bài giảng",
                                        Toast.LENGTH_SHORT
                                ).show();

                                loadPendingModulesByLessonId(lessonId);

                            } else {
                                Toast.makeText(
                                        AdminCourseDetailActivity.this,
                                        "Lỗi: " + apiResponse.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                        } else {
                            Toast.makeText(
                                    AdminCourseDetailActivity.this,
                                    "Duyệt xoá bài giảng thất bại, HTTP " + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<ModuleResponse>> call,
                            Throwable t
                    ) {
                        Toast.makeText(
                                AdminCourseDetailActivity.this,
                                "Lỗi kết nối khi duyệt xoá bài giảng",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void showEmptyModuleText(String message) {
        clearModules();

        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextColor(android.graphics.Color.GRAY);
        textView.setTextSize(14);
        textView.setPadding(12, 16, 12, 16);

        if (layoutModules != null) {
            layoutModules.addView(textView);
        }
    }

    private void clearModules() {
        if (layoutModules != null) {
            layoutModules.removeAllViews();
        }
    }

    private String safe(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) {
            return "";
        }

        return value.trim();
    }
}