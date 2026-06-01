package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.Context;
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

public class StudentLessonAdapter extends RecyclerView.Adapter<StudentLessonAdapter.StudentViewHolder> {

    private Context context;
    private List<LessonResponse> lessonList;

    public StudentLessonAdapter(Context context, List<LessonResponse> lessonList) {
        this.context = context;
        this.lessonList = lessonList;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ĐÃ SỬA: Bơm đúng layout item_course_list.xml bạn vừa gửi
        View view = LayoutInflater.from(context).inflate(R.layout.item_course_list, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        LessonResponse lesson = lessonList.get(position);

        // 1. Đổ dữ liệu chữ (Gộp cả Tiêu đề và Mô tả vào chung 1 TextView theo thiết kế XML của bạn)
        if (holder.tvDescription != null) {
            String fullInfo = "Khóa học: " + lesson.getTitle() + "\nMô tả: " + lesson.getDescription();
            holder.tvDescription.setText(fullInfo);
        }

        // 2. Tải ảnh khóa học lên ShapeableImageView
        if (holder.imgThumbnail != null) {
            Glide.with(context)
                    .load(lesson.getThumbnailUrl())
                    .placeholder(R.drawable.course_python)
                    .error(R.drawable.course_python)
                    .into(holder.imgThumbnail);
        }

        // 3. Xử lý click vào thẻ để chuyển sang màn hình chi tiết khóa học (CourseActivity)
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CourseActivity.class);
            intent.putExtra("course_id", lesson.getId());
            intent.putExtra("course_name", lesson.getTitle());
            intent.putExtra("course_thumbnail", lesson.getThumbnailUrl());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return lessonList != null ? lessonList.size() : 0;
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {

        ImageView imgThumbnail;
        TextView tvDescription;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgCourseThumbnail);
            tvDescription = itemView.findViewById(R.id.txtCourseDescription);
        }
    }
}