package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCourseSummaryAdapter extends RecyclerView.Adapter<AdminCourseSummaryAdapter.CourseViewHolder> {

    private Context context;
    private List<LessonResponse> courseList;

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
    public void onBindViewHolder(@NonNull CourseViewHolder holder, @SuppressLint("RecyclerView") int position) {
        LessonResponse course = courseList.get(position);
        holder.txtTitle.setText(course.getTitle());
        holder.txtDate.setText("Mô tả: " + course.getDescription());
        String status = course.getStatus();

        if (status == null) status = "PENDING";

        holder.txtLecturer.setText("Trạng thái: " + status);
        com.bumptech.glide.Glide.with(context)
                .load(course.getThumbnailUrl())
                .placeholder(R.drawable.python)
                .error(R.drawable.python)
                .into(holder.imgCourseThumbnail);

        if ("APPROVED".equalsIgnoreCase(status) || "ACTIVE".equalsIgnoreCase(status)) {
            holder.txtLecturer.setTextColor(Color.parseColor("#4CAF50"));

            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            holder.txtLecturer.setTextColor(Color.parseColor("#F44336"));
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        } else {
            holder.txtLecturer.setTextColor(Color.parseColor("#FF9800"));
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
        }
        holder.btnDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminCourseDetailActivity.class);
            intent.putExtra("COURSE_ID", course.getId());
            intent.putExtra("COURSE_TITLE", course.getTitle());
            context.startActivity(intent);
        });
        holder.btnApprove.setOnClickListener(v -> {
            LessonApiService apiService = RetrofitClient.getClient().create(LessonApiService.class);
            apiService.approveLesson(course.getId()).enqueue(new Callback<ApiResponse<LessonResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<LessonResponse>> call, Response<ApiResponse<LessonResponse>> response) {

                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<LessonResponse> apiResponse = response.body();

                        if (apiResponse.getCode() == 1000) {
                            Toast.makeText(context, "Đã phê duyệt bài học: " + course.getTitle(), Toast.LENGTH_SHORT).show();
                            course.setStatus("APPROVED");
                            notifyItemChanged(position);
                        } else {
                            Toast.makeText(context, "Lỗi: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Duyệt thất bại, mã lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<LessonResponse>> call, Throwable t) {
                    Toast.makeText(context, "Lỗi kết nối Server", Toast.LENGTH_SHORT).show();
                }
            });
        });
        holder.btnReject.setOnClickListener(v -> {
            LessonApiService apiService = RetrofitClient.getClient().create(LessonApiService.class);
            apiService.approveDeleteLesson(course.getId()).enqueue(new Callback<ApiResponse<LessonResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<LessonResponse>> call, Response<ApiResponse<LessonResponse>> response) {

                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<LessonResponse> apiResponse = response.body();

                        if (apiResponse.getCode() == 1000) {
                            Toast.makeText(context, "Từ chối bài học: " + course.getTitle(), Toast.LENGTH_SHORT).show();
                            course.setStatus("REJECTED");
                            notifyItemChanged(position);
                        } else {
                            Toast.makeText(context, "Lỗi: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Thao tác thất bại, mã lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<LessonResponse>> call, Throwable t) {
                    Toast.makeText(context, "Lỗi kết nối Server", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return courseList == null ? 0 : courseList.size();
    }
    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtLecturer, txtDate;
        MaterialButton btnDetails, btnApprove, btnReject;
        ImageView imgCourseThumbnail;
        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtCourseTitle);
            txtLecturer = itemView.findViewById(R.id.txtLecturer);
            txtDate = itemView.findViewById(R.id.txtPostDate);
            btnDetails = itemView.findViewById(R.id.btnDetails);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            imgCourseThumbnail = itemView.findViewById(R.id.imgCourseThumbnail);
        }
    }
}