package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurriculumFragment extends Fragment {
    RecyclerView rvChapters;
    LessonAdapter adapter;
    List<ModuleResponse> moduleList = new ArrayList<>();
    private int courseId = -1;

    // --- THÊM MỚI: 2 biến lưu thông tin Lesson nhận từ Intent của Activity ---
    private String lessonDescription = "";
    private String lessonThumbnailUrl = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_curriculum_fragment, container, false);

        // Hứng dữ liệu được truyền từ Activity cha vào đây
        if (getActivity() != null && getActivity().getIntent() != null) {
            courseId = getActivity().getIntent().getIntExtra("course_id", -1);
            // --- THÊM MỚI: Nhận thêm mô tả và ảnh bìa ---
            lessonDescription = getActivity().getIntent().getStringExtra("course_description");
            lessonThumbnailUrl = getActivity().getIntent().getStringExtra("course_thumbnail");
        }

        rvChapters = view.findViewById(R.id.rvChapters);
        rvChapters.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new LessonAdapter(moduleList, new LessonAdapter.OnModuleActionListener() {
            @Override
            public void onDeleteClick(int position, ModuleResponse module) {
                // Gọi hàm hiển thị Dialog xác nhận trước khi gọi API xóa
                showConfirmDeleteModuleDialog(position, module);
            }
        });
        rvChapters.setAdapter(adapter);

        if (courseId != -1) {
            fetchModulesFromServer(courseId);
        } else {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Không tìm thấy ID khóa học hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        }

        return view;
    }

    private void fetchModulesFromServer(int id) {
        ModuleApiService apiService = RetrofitClient.getClient().create(ModuleApiService.class);

        apiService.getModulesByLessonId(id).enqueue(new Callback<ApiResponse<List<ModuleResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ModuleResponse>>> call, Response<ApiResponse<List<ModuleResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<ModuleResponse>> apiResponse = response.body();

                    if (apiResponse.getCode() == 1000) {
                        List<ModuleResponse> serverList = apiResponse.getResult();

                        if (serverList != null && !serverList.isEmpty()) {
                            // --- CẬP NHẬT: Truyền thêm mô tả và ảnh bìa vào hàm updateData của adapter ---
                            adapter.updateData(serverList, lessonDescription, lessonThumbnailUrl);
                        } else {
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Khóa học này chưa có giáo trình!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi hệ thống: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi kết nối mạng, mã: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ModuleResponse>>> call, Throwable t) {
                Log.e("API_FRAG_LOG", "Thất bại: " + t.getMessage());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Không thể kết nối đến máy chủ!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void showConfirmDeleteModuleDialog(int position, ModuleResponse module) {
        if (getContext() == null) return;
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Xóa bài học nhỏ")
                .setMessage("Bạn có chắc chắn muốn xóa bài học '" + module.getTitle() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    ModuleApiService apiService = RetrofitClient.getClient().create(ModuleApiService.class);
                    apiService.deleteModule(module.getId()).enqueue(new retrofit2.Callback<ApiResponse<String>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ApiResponse<String> apiResponse = response.body();
                                if (apiResponse.getCode() == 1000) {
                                    Toast.makeText(getContext(), "Xóa module thành công!", Toast.LENGTH_SHORT).show();
                                    if (moduleList != null && position < moduleList.size()) {
                                        moduleList.remove(position);
                                        adapter.notifyItemRemoved(position);
                                        adapter.notifyItemRangeChanged(position, moduleList.size());
                                    }
                                } else {
                                    Toast.makeText(getContext(), "Lỗi: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), "Lỗi từ máy chủ, mã: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                            Toast.makeText(getContext(), "Lỗi kết nối mạng khi xóa!", Toast.LENGTH_SHORT).show();
                        }
                    });

                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }
}