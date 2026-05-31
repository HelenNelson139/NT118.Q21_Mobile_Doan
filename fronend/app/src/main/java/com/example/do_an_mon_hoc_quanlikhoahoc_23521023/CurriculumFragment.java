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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_curriculum_fragment, container, false);

        if (getActivity() != null && getActivity().getIntent() != null) {
            courseId = getActivity().getIntent().getIntExtra("course_id", -1);
        }

        rvChapters = view.findViewById(R.id.rvChapters);
        rvChapters.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new LessonAdapter(moduleList);
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
                            adapter.updateData(serverList);
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
}