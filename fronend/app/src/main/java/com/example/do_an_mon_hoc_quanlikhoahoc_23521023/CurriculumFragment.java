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
    List<LessonResponse> lessonList = new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_curriculum_fragment, container, false);

        rvChapters = view.findViewById(R.id.rvChapters);
        rvChapters.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new LessonAdapter(getActivity(), lessonList);
        rvChapters.setAdapter(adapter);
        fetchLessonsFromServer();
        return view;
    }

    private void fetchLessonsFromServer() {
        LessonApiService apiService = RetrofitClient.getClient().create(LessonApiService.class);
        apiService.getAllLessons().enqueue(new Callback<ApiResponse<List<LessonResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<LessonResponse>>> call, Response<ApiResponse<List<LessonResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<LessonResponse>> apiResponse = response.body();

                    if (apiResponse.getCode() == 1000) {
                        List<LessonResponse> serverList = apiResponse.getResult();
                        if (serverList != null && !serverList.isEmpty()) {
                            adapter.updateData(serverList);
                        } else {
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Không có bài học nào!", Toast.LENGTH_SHORT).show();
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
            public void onFailure(Call<ApiResponse<List<LessonResponse>>> call, Throwable t) {
                Log.e("API_FRAG_LOG", "Thất bại: " + t.getMessage());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Không thể kết nối đến máy chủ!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}