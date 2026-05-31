package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.google.android.flexbox.FlexboxLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IntroFragment extends Fragment {

    Button btnJoin;
    LinearLayout layoutChecklist;
    FlexboxLayout layoutSkills;
    TextView txtDescription;
    private int courseId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_intro_fragment, container, false);

        // 1. Ánh xạ các View
        layoutChecklist = view.findViewById(R.id.layoutChecklist);
        layoutSkills = view.findViewById(R.id.layoutSkills);
        btnJoin = view.findViewById(R.id.btnJoin);
        txtDescription = view.findViewById(R.id.txtDescription);

        // 2. Lấy course_id được truyền từ Activity cha (CourseActivity)
        if (getActivity() != null && getActivity().getIntent() != null) {
            courseId = getActivity().getIntent().getIntExtra("course_id", -1);
        }

        // 3. Nếu tìm thấy ID, gọi API lấy dữ liệu thực tế từ Server
        if (courseId != -1) {
            fetchCourseIntro(courseId);
        } else {
            txtDescription.setText("Không tìm thấy thông tin khóa học.");
        }

        // Nút Tham gia khóa học chuyển sang LessonActivity
        btnJoin.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LessonActivity.class);
            // Bạn có thể gửi kèm courseId sang LessonActivity nếu cần thiết
            intent.putExtra("COURSE_ID", courseId);
            startActivity(intent);
        });

        return view;
    }

    // 4. Hàm gọi API từ Server để xóa bỏ hoàn toàn dữ liệu tĩnh mẫu cũ
    private void fetchCourseIntro(int id) {
        // Khởi tạo ApiService (Sử dụng đúng Interface chứa API lấy chi tiết bài học/khóa học của bạn)
        // Lưu ý: Thay đổi LessonApiService bằng đúng tên Interface của bạn (Ví dụ: LessonApiService hoặc CourseApiService)
        LessonApiService apiService = RetrofitClient.getClient().create(LessonApiService.class);

        apiService.getLessonById(id).enqueue(new Callback<ApiResponse<LessonResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LessonResponse>> call, Response<ApiResponse<LessonResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<LessonResponse> apiResponse = response.body();
                    if (apiResponse.getCode() == 1000 && apiResponse.getResult() != null) {

                        LessonResponse lesson = apiResponse.getResult();

                        // Cập nhật mô tả động từ server
                        if (lesson.getDescription() != null && !lesson.getDescription().isEmpty()) {
                            txtDescription.setText(lesson.getDescription());
                        } else {
                            txtDescription.setText("Khóa học này chưa có mô tả.");
                        }

                        // Xóa sạch các view mẫu cũ trong Layout trước khi thêm mới
                        layoutChecklist.removeAllViews();
                        layoutSkills.removeAllViews();

                        // Tách chuỗi "Bạn sẽ học được gì" (what_you_learn) thành danh sách dòng (ngăn cách bởi dấu xuống dòng hoặc dấu phẩy)
                        if (lesson.getWhatYouLearn() != null && !lesson.getWhatYouLearn().isEmpty()) {
                            String[] checklistItems = lesson.getWhatYouLearn().split("\n");
                            for (String item : checklistItems) {
                                if (item.trim().isEmpty()) continue;

                                TextView tv = new TextView(getContext());
                                SpannableString spannable = new SpannableString("✓  " + item.trim());
                                spannable.setSpan(
                                        new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.bright_blue)),
                                        0, 2, 0
                                );
                                tv.setText(spannable);
                                tv.setTextSize(17f);
                                tv.setPadding(0, 6, 0, 6);
                                layoutChecklist.addView(tv);
                            }
                        }

                        // Tách chuỗi "Kỹ năng đạt được" (skill_learned) thành các thẻ Tag riêng biệt
                        if (lesson.getSkillLearned() != null && !lesson.getSkillLearned().isEmpty()) {
                            String[] skills = lesson.getSkillLearned().split(",");
                            for (String skill : skills) {
                                if (skill.trim().isEmpty()) continue;

                                TextView tv = new TextView(getContext());
                                tv.setText(skill.trim());
                                tv.setTextSize(14f);
                                tv.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_green));
                                tv.setBackgroundResource(R.drawable.bg_skill_tag);
                                tv.setPadding(20, 10, 20, 10);

                                FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(
                                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                                        FlexboxLayout.LayoutParams.WRAP_CONTENT
                                );
                                lp.setMargins(0, 0, 16, 16);
                                tv.setLayoutParams(lp);

                                layoutSkills.addView(tv);
                            }
                        }

                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LessonResponse>> call, Throwable t) {
                Log.e("API_INTRO", "Thất bại: " + t.getMessage());
            }
        });
    }
}