package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private final List<LessonResponse> courseList;

    public CourseAdapter(List<LessonResponse> courseList) {
        this.courseList = courseList;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);

        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CourseViewHolder holder,
            int position
    ) {
        LessonResponse lesson = courseList.get(position);

        holder.tvTitle.setText(safe(lesson.getTitle()));

        String thumbnailUrl = safe(lesson.getThumbnailUrl());

        if (!thumbnailUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.course_python)
                    .error(R.drawable.course_python)
                    .into(holder.imgCourse);
        } else {
            holder.imgCourse.setImageResource(R.drawable.course_python);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), CourseActivity.class);

            intent.putExtra("LESSON_ID", lesson.getId());

            /*
             * Khóa học gợi ý chưa đăng ký,
             * nên phải là false để hiện nút THAM GIA.
             */
            intent.putExtra("IS_ENROLLED", false);

            intent.putExtra("LESSON_TITLE", safe(lesson.getTitle()));
            intent.putExtra("LESSON_DESCRIPTION", safe(lesson.getDescription()));
            intent.putExtra("LESSON_WHAT_YOU_LEARN", safe(lesson.getWhatYouLearn()));
            intent.putExtra("LESSON_SKILL_LEARNED", safe(lesson.getSkillLearned()));
            intent.putExtra("LESSON_THUMBNAIL", safe(lesson.getThumbnailUrl()));
            intent.putExtra("LESSON_TEACHER", safe(lesson.getTeacher_name()));

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return courseList == null ? 0 : courseList.size();
    }

    private String safe(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) {
            return "";
        }

        return value.trim();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {

        ImageView imgCourse;
        TextView tvTitle;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);

            imgCourse = itemView.findViewById(R.id.imgCourse);
            tvTitle = itemView.findViewById(R.id.tvCourseTitle);
        }
    }
}