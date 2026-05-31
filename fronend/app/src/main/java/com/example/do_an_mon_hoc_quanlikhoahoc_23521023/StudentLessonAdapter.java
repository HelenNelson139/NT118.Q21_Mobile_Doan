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
import com.google.android.material.button.MaterialButton;
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_lesson, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        LessonResponse lesson = lessonList.get(position);

        // 1. Đổ dữ liệu chuẩn lên giao diện (có check null an toàn)
        if (holder.tvTitle != null) {
            holder.tvTitle.setText(lesson.getTitle());
        }
        if (holder.tvDescription != null) {
            holder.tvDescription.setText(lesson.getDescription());
        }

        if (holder.imgThumbnail != null) {
            Glide.with(context)
                    .load(lesson.getThumbnailUrl())
                    .placeholder(R.drawable.course_python)
                    .error(R.drawable.course_python)
                    .into(holder.imgThumbnail);
        }

        // 2. ẨN TOÀN BỘ CÁC NÚT VÀ TRẠNG THÁI (Chỉ giữ lại thông tin cốt lõi cho Student)
        if (holder.tvStatus != null) holder.tvStatus.setVisibility(View.GONE);      // Ẩn Trạng thái
        if (holder.btnApprove != null) holder.btnApprove.setVisibility(View.GONE);  // Ẩn nút Duyệt
        if (holder.btnDelete != null) holder.btnDelete.setVisibility(View.GONE);    // Ẩn nút Xóa
        if (holder.btnAction != null) holder.btnAction.setVisibility(View.GONE);    // ĐÃ ĐỔI: Ẩn luôn nút hành động (Tham gia)

        // 3. Xử lý click vào item để chuyển tiếp sang màn hình chi tiết CourseActivity
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
        TextView tvTitle, tvDescription, tvStatus;
        ImageView imgThumbnail;
        MaterialButton btnApprove, btnDelete, btnAction;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.txtCourseTitle);
            tvDescription = itemView.findViewById(R.id.txtCourseDescription);
            imgThumbnail = itemView.findViewById(R.id.imgCourse);

            tvStatus = itemView.findViewById(R.id.txtStatus);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnDelete = itemView.findViewById(R.id.btnDelete);
 //           btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}