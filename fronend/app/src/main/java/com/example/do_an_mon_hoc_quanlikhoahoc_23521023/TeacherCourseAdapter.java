package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class TeacherCourseAdapter extends RecyclerView.Adapter<TeacherCourseAdapter.CourseViewHolder> {
    public interface OnCourseActionListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }
    private final List<LessonResponse> courseList;
    private final OnCourseActionListener listener;
    public TeacherCourseAdapter(List<LessonResponse> courseList, OnCourseActionListener listener) {
        this.courseList = courseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher_course_list, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        LessonResponse course = courseList.get(position);
        holder.tvTitle.setText(course.getTitle());
        holder.tvDescription.setText("Mô tả: " + course.getDescription());
        String status = course.getStatus() != null ? course.getStatus() : "PENDING";
        holder.tvTeacherName.setText("Trạng thái: " + status);
        com.bumptech.glide.Glide.with(holder.itemView.getContext())
                .load(course.getThumbnailUrl())
                .placeholder(R.drawable.course_python)
                .error(R.drawable.course_python)
                .into(holder.imgCourse);


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), CourseActivity.class);
            intent.putExtra("course_name", course.getTitle());
            intent.putExtra("course_id", course.getId());
            intent.putExtra("course_thumbnail", course.getThumbnailUrl());
            v.getContext().startActivity(intent);
        });

        holder.btnDeleteCourse.setOnClickListener(v -> {
            if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onDeleteClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseList == null ? 0 : courseList.size();
    }
    static class CourseViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCourse;
        TextView tvTitle, tvDescription, tvTeacherName;
        MaterialButton btnEditCourse, btnDeleteCourse;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCourse = itemView.findViewById(R.id.imgCourseThumbnail);
            tvTitle = itemView.findViewById(R.id.txtCourseTitle);
            tvDescription = itemView.findViewById(R.id.txtCourseDescription);
            tvTeacherName = itemView.findViewById(R.id.txtTeacherName);
            btnEditCourse = itemView.findViewById(R.id.btnEditCourse);
            btnDeleteCourse = itemView.findViewById(R.id.btnDeleteCourse);
        }
    }
}