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
     * true  = mở từ trang/chế độ "Chưa duyệt"
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

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
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

                        if (lesson.getId() == null) {
                            showEmptyModuleText("Không tìm thấy lessonId.");
                            return;
                        }

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

        if (tvHeaderTitle != null) {
            tvHeaderTitle.setText(safe(lesson.getTitle()));
        }

        if (tvCourseInfo != null) {
            tvCourseInfo.setText(
                    "Giảng viên: " + safe(lesson.getTeacher_name()) + "\n"
                            + "Ngày đăng: " + safe(lesson.getCreatedAt())
            );
        }

        if (tvCourseDescription != null) {
            tvCourseDescription.setText(
                    "Mô tả: " + safe(lesson.getDescription())
            );
        }

        currentLessonThumbnail = safe(lesson.getThumbnailUrl());

        if (imgCourse != null) {
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
    }

    /*
     * Trang Admin - Chưa duyệt:
     * Chỉ lấy module PENDING / PENDING_DELETE của lesson.
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
            if (module != null && module.getId() != null) {
                moduleIds.add(module.getId());
            }
        }

        for (ModuleResponse module : displayModules) {
            if (module == null || module.getId() == null) {
                continue;
            }

            int realIndex = moduleIds.indexOf(module.getId());

            View moduleView = createPendingModuleView(
                    module,
                    moduleIds,
                    realIndex
            );

            if (layoutModules != null) {
                layoutModules.addView(moduleView);
            }
        }
    }

    private void renderNormalModuleList(List<ModuleResponse> modules) {
        clearModules();

        if (modules == null || modules.isEmpty()) {
            showEmptyModuleText("Không có bài giảng nào.");
            return;
        }

        ArrayList<ModuleResponse> displayModules = new ArrayList<>();
        ArrayList<Integer> moduleIds = new ArrayList<>();

        for (ModuleResponse module : modules) {
            if (module != null && module.getId() != null) {
                displayModules.add(module);
                moduleIds.add(module.getId());
            }
        }

        if (displayModules.isEmpty()) {
            showEmptyModuleText("Không có bài giảng nào.");
            return;
        }

        for (ModuleResponse module : displayModules) {
            int realIndex = moduleIds.indexOf(module.getId());

            View moduleView = createNormalModuleView(
                    module,
                    moduleIds,
                    realIndex
            );

            if (layoutModules != null) {
                layoutModules.addView(moduleView);
            }
        }
    }

    /*
     * Item pending có:
     * - Xem chi tiết
     * - Duyệt
     * - Duyệt xoá
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

        if (txtTitle != null) {
            txtTitle.setText(safe(module.getTitle()));
        }

        if (txtStatus != null) {
            txtStatus.setText("Trạng thái: " + status);
        }

        if (txtObjective != null) {
            txtObjective.setText("Mục tiêu: " + safe(module.getObjective()));
        }

        if (txtContent != null) {
            txtContent.setText("Nội dung: " + safe(module.getContent()));
        }

        if (txtExample != null) {
            txtExample.setText("Ví dụ: " + safe(module.getExample()));
        }

        if (btnView != null) {
            btnView.setText("Xem chi tiết");
            btnView.setOnClickListener(v ->
                    openModuleDetail(moduleIds, index, module.getId())
            );
        }

        if ("PENDING".equalsIgnoreCase(status)) {
            if (btnApprove != null) {
                btnApprove.setVisibility(View.VISIBLE);
                btnApprove.setText("Duyệt");
                btnApprove.setOnClickListener(v -> approvePendingModule(module.getId()));
            }

            if (btnApproveDelete != null) {
                btnApproveDelete.setVisibility(View.GONE);
            }

        } else if ("PENDING_DELETE".equalsIgnoreCase(status)) {
            if (btnApprove != null) {
                btnApprove.setVisibility(View.GONE);
            }

            if (btnApproveDelete != null) {
                btnApproveDelete.setVisibility(View.VISIBLE);
                btnApproveDelete.setText("Duyệt xoá");
                btnApproveDelete.setOnClickListener(v -> approveDeletePendingModule(module.getId()));
            }

        } else {
            if (btnApprove != null) {
                btnApprove.setVisibility(View.GONE);
            }

            if (btnApproveDelete != null) {
                btnApproveDelete.setVisibility(View.GONE);
            }
        }

        return view;
    }

    /*
     * Trang bình thường dùng item_existing_module.
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

        if (txtTitle != null) {
            txtTitle.setText(safe(module.getTitle()));
        }

        if (txtStatus != null) {
            txtStatus.setText("Trạng thái: " + safe(module.getStatus()));
        }

        if (txtObjective != null) {
            txtObjective.setText("Mục tiêu: " + safe(module.getObjective()));
        }

        if (txtContent != null) {
            txtContent.setText("Nội dung: " + safe(module.getContent()));
        }

        if (txtExample != null) {
            txtExample.setText("Ví dụ: " + safe(module.getExample()));
        }

        if (btnView != null) {
            btnView.setText("Xem chi tiết");
            btnView.setOnClickListener(v ->
                    openModuleDetail(moduleIds, index, module.getId())
            );
        }

        if (btnDelete != null) {
            btnDelete.setVisibility(View.GONE);
        }

        return view;
    }

    /*
     * Chỗ này là phần quan trọng.
     *
     * LessonActivity của bạn cần:
     * - MODULE_IDS
     * - CURRENT_INDEX
     * - PARENT_LESSON_THUMBNAIL
     *
     * Nếu chỉ start LessonActivity mà không truyền MODULE_IDS
     * thì nó sẽ hiện "Không có bài học".
     */
    private void openModuleDetail(
            ArrayList<Integer> moduleIds,
            int index,
            Integer selectedModuleId
    ) {
        ArrayList<Integer> safeModuleIds = new ArrayList<>();

        if (moduleIds != null) {
            for (Integer id : moduleIds) {
                if (id != null && !safeModuleIds.contains(id)) {
                    safeModuleIds.add(id);
                }
            }
        }

        /*
         * Fallback cực quan trọng:
         * Nếu vì lý do nào đó moduleIds rỗng,
         * vẫn mở được module đang bấm.
         */
        if (safeModuleIds.isEmpty() && selectedModuleId != null) {
            safeModuleIds.add(selectedModuleId);
        }

        if (safeModuleIds.isEmpty()) {
            Toast.makeText(this, "Không có bài giảng để mở", Toast.LENGTH_SHORT).show();
            return;
        }

        int realIndex = index;

        if (selectedModuleId != null) {
            int foundIndex = safeModuleIds.indexOf(selectedModuleId);

            if (foundIndex >= 0) {
                realIndex = foundIndex;
            }
        }

        if (realIndex < 0 || realIndex >= safeModuleIds.size()) {
            realIndex = 0;
        }

        Intent intent = new Intent(AdminCourseDetailActivity.this, LessonActivity.class);

        intent.putIntegerArrayListExtra("MODULE_IDS", safeModuleIds);
        intent.putExtra("CURRENT_INDEX", realIndex);
        intent.putExtra("PARENT_LESSON_THUMBNAIL", currentLessonThumbnail);

        /*
         * Truyền thêm MODULE_ID để dự phòng nếu sau này LessonActivity có hỗ trợ mở 1 module.
         */
        if (selectedModuleId != null) {
            intent.putExtra("MODULE_ID", selectedModuleId);
        }

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