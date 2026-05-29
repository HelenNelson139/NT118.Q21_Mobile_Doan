package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class AdminCourseSummaryAdapter extends RecyclerView.Adapter<AdminCourseSummaryAdapter.CourseViewHolder> {

    private Context context;
    // ĐÃ ĐỔI: Chuyển kiểu dữ liệu danh sách sang LessonResponse từ Backend
    private List<LessonResponse> courseList;

    // Constructor nhận vào đúng List<LessonResponse> để hết lỗi ở AdminManageCourse
    public AdminCourseSummaryAdapter(Context context, List<LessonResponse> courseList) {
        this.context = context;
        this.courseList = courseList;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.admin_course_summary, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        // Lấy bài học (Lesson) thực tế từ danh sách mạng
        LessonResponse course = courseList.get(position);

        // Đổ dữ liệu từ Backend vào đúng các biến txt gốc của bạn
        holder.txtTitle.setText(course.getTitle()); // Tên bài học
        holder.txtLecturer.setText("Trạng thái: " + (course.getStatus() != null ? course.getStatus() : "Chưa duyệt"));
        holder.txtDate.setText("Mô tả: " + course.getDescription());

        // CHỨC NĂNG XEM CHI TIẾT ONLINE (Giữ nguyên cấu trúc Intent cũ của bạn)
        holder.btnDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminCourseDetailActivity.class);

            // Ép kiểu ID về String để truyền đi nếu getId() ở BE trả về kiểu số (Integer/Long)
            intent.putExtra("COURSE_ID", String.valueOf(course.getId()));
            intent.putExtra("COURSE_TITLE", course.getTitle());
            context.startActivity(intent);
        });

        // Xử lý nút Duyệt / Từ chối (Giữ nguyên nút bấm của bạn)
        holder.btnApprove.setOnClickListener(v -> {
            android.widget.Toast.makeText(context, "Phê duyệt bài học: " + course.getTitle(), android.widget.Toast.LENGTH_SHORT).show();
            // Sau này gọi API Approve ở đây
        });

        holder.btnReject.setOnClickListener(v -> {
            android.widget.Toast.makeText(context, "Từ chối bài học: " + course.getTitle(), android.widget.Toast.LENGTH_SHORT).show();
            // Sau này gọi API Reject ở đây
        });
    }

    @Override
    public int getItemCount() {
        return courseList == null ? 0 : courseList.size();
    }

    // GIỮ NGUYÊN 100% tên Class ViewHolder và các ID View gốc khít với XML của bạn
    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtLecturer, txtDate;
        MaterialButton btnDetails, btnApprove, btnReject;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtCourseTitle);
            txtLecturer = itemView.findViewById(R.id.txtLecturer);
            txtDate = itemView.findViewById(R.id.txtPostDate);
            btnDetails = itemView.findViewById(R.id.btnDetails);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}