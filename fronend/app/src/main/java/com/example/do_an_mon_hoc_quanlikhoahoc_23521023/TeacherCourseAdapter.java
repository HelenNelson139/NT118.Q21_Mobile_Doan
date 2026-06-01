package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class TeacherCourseAdapter extends RecyclerView.Adapter<TeacherCourseAdapter.TeacherCourseViewHolder> {

    private final List<LessonResponse> courseList;
    private final OnCourseActionListener listener;

    public interface OnCourseActionListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public TeacherCourseAdapter(List<LessonResponse> courseList, OnCourseActionListener listener) {
        this.courseList = courseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TeacherCourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher_course_list, parent, false);

        return new TeacherCourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherCourseViewHolder holder, int position) {
        LessonResponse course = courseList.get(position);

        holder.txtCourseTitle.setText(safe(course.getTitle()));
        holder.txtDescription.setText("Mô tả: " + safe(course.getDescription()));
        holder.txtStatus.setText("Trạng thái: " + safe(course.getStatus()));

        String thumbnailUrl = course.getThumbnailUrl();

        if (thumbnailUrl != null && !thumbnailUrl.trim().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.course_python)
                    .error(R.drawable.course_python)
                    .into(holder.imgCourse);
        } else {
            holder.imgCourse.setImageResource(R.drawable.course_python);
        }

        holder.btnEdit.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();

            if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onEditClick(adapterPosition);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();

            if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onDeleteClick(adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseList == null ? 0 : courseList.size();
    }

    static class TeacherCourseViewHolder extends RecyclerView.ViewHolder {

        ImageView imgCourse;
        TextView txtCourseTitle;
        TextView txtDescription;
        TextView txtStatus;
        MaterialButton btnEdit;
        MaterialButton btnDelete;

        public TeacherCourseViewHolder(@NonNull View itemView) {
            super(itemView);

            imgCourse = itemView.findViewById(R.id.imgCourse);
            txtCourseTitle = itemView.findViewById(R.id.txtCourseTitle);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private static String safe(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) {
            return "";
        }

        return value.trim();
    }
}