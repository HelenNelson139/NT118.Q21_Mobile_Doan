package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentLessonAdapter extends RecyclerView.Adapter<StudentLessonAdapter.StudentViewHolder> {

    private final Context context;
    private final List<LessonResponse> lessonList;

    private static final String PREF_NAME = "APP_PREFS";
    private static final String KEY_USER_ID = "USER_ID";

    public StudentLessonAdapter(Context context, List<LessonResponse> lessonList) {
        this.context = context;
        this.lessonList = lessonList;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_course_list, parent, false);

        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull StudentViewHolder holder,
            int position
    ) {
        LessonResponse lesson = lessonList.get(position);

        String title = safe(lesson.getTitle());
        String description = safe(lesson.getDescription());

        holder.tvDescription.setText(
                "Khóa học: " + title + "\nMô tả: " + description
        );

        String thumbnail = safe(lesson.getThumbnailUrl());

        if (!thumbnail.isEmpty()) {
            Glide.with(context)
                    .load(thumbnail)
                    .placeholder(R.drawable.course_python)
                    .error(R.drawable.course_python)
                    .into(holder.imgThumbnail);
        } else {
            holder.imgThumbnail.setImageResource(R.drawable.course_python);
        }

        holder.progressLesson.setProgress(0);
        holder.txtProgressPercent.setText("0%");
        holder.txtProgressDetail.setText("Đang tải tiến độ...");

        loadLessonProgress(holder, lesson.getId());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CourseActivity.class);

            intent.putExtra("LESSON_ID", lesson.getId());
            intent.putExtra("IS_ENROLLED", true);

            intent.putExtra("LESSON_TITLE", safe(lesson.getTitle()));
            intent.putExtra("LESSON_DESCRIPTION", safe(lesson.getDescription()));
            intent.putExtra("LESSON_WHAT_YOU_LEARN", safe(lesson.getWhatYouLearn()));
            intent.putExtra("LESSON_SKILL_LEARNED", safe(lesson.getSkillLearned()));
            intent.putExtra("LESSON_THUMBNAIL", safe(lesson.getThumbnailUrl()));
            intent.putExtra("LESSON_TEACHER", safe(lesson.getTeacher_name()));

            context.startActivity(intent);
        });
    }

    private void loadLessonProgress(StudentViewHolder holder, Integer lessonId) {
        if (lessonId == null) {
            holder.progressLesson.setProgress(0);
            holder.txtProgressPercent.setText("0%");
            holder.txtProgressDetail.setText("Không có tiến độ");
            return;
        }

        int studentId = getCurrentUserId();

        if (studentId == -1) {
            holder.progressLesson.setProgress(0);
            holder.txtProgressPercent.setText("0%");
            holder.txtProgressDetail.setText("Không tìm thấy học sinh");
            return;
        }

        ProgressApiService progressApiService =
                RetrofitClient.getClient().create(ProgressApiService.class);

        progressApiService.getLessonProgress(lessonId, studentId)
                .enqueue(new Callback<ApiResponse<LessonProgressResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<LessonProgressResponse>> call,
                            Response<ApiResponse<LessonProgressResponse>> response
                    ) {
                        if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) {
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            LessonProgressResponse progress = response.body().getResult();

                            if (progress == null) {
                                bindProgress(holder, 0, 0, 0);
                                return;
                            }

                            int percent = (int) Math.round(progress.getProgressPercent());
                            int completed = progress.getCompletedModules();
                            int total = progress.getTotalModules();

                            bindProgress(holder, percent, completed, total);

                        } else {
                            bindProgress(holder, 0, 0, 0);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<LessonProgressResponse>> call,
                            Throwable t
                    ) {
                        if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) {
                            return;
                        }

                        bindProgress(holder, 0, 0, 0);
                    }
                });
    }

    private void bindProgress(
            StudentViewHolder holder,
            int percent,
            int completed,
            int total
    ) {
        if (percent < 0) {
            percent = 0;
        }

        if (percent > 100) {
            percent = 100;
        }

        holder.progressLesson.setProgress(percent);
        holder.txtProgressPercent.setText(percent + "%");

        if (total <= 0) {
            holder.txtProgressDetail.setText("Chưa có bài học");
        } else {
            holder.txtProgressDetail.setText(
                    completed + "/" + total + " bài học đã hoàn thành"
            );
        }
    }

    private int getCurrentUserId() {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    @Override
    public int getItemCount() {
        return lessonList == null ? 0 : lessonList.size();
    }

    private String safe(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) {
            return "";
        }

        return value.trim();
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {

        ImageView imgThumbnail;
        TextView tvDescription;

        ProgressBar progressLesson;
        TextView txtProgressPercent;
        TextView txtProgressDetail;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);

            imgThumbnail = itemView.findViewById(R.id.imgCourseThumbnail);
            tvDescription = itemView.findViewById(R.id.txtCourseDescription);

            progressLesson = itemView.findViewById(R.id.progressLesson);
            txtProgressPercent = itemView.findViewById(R.id.txtProgressPercent);
            txtProgressDetail = itemView.findViewById(R.id.txtProgressDetail);
        }
    }
}