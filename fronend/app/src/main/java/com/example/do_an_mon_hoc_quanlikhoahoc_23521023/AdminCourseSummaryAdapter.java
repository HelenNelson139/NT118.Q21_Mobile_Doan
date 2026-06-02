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

    private final Context context;
    private final List<LessonResponse> courseList;

    /*
     * APPROVED = tab đã duyệt
     * PENDING  = tab chưa duyệt
     */
    private String mode = "APPROVED";

    public AdminCourseSummaryAdapter(Context context, List<LessonResponse> courseList, String mode) {
        this.context = context;
        this.courseList = courseList;
        this.mode = mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.admin_course_summary, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CourseViewHolder holder,
            @SuppressLint("RecyclerView") int position
    ) {
        LessonResponse course = courseList.get(position);

        holder.txtTitle.setText(safe(course.getTitle()));
        holder.txtDate.setText("Mô tả: " + safe(course.getDescription()));

        String status = safe(course.getStatus());
        if (status.isEmpty()) {
            status = "PENDING";
        }

        holder.txtLecturer.setText("Trạng thái: " + status);

        com.bumptech.glide.Glide.with(context)
                .load(course.getThumbnailUrl())
                .placeholder(R.drawable.python)
                .error(R.drawable.python)
                .into(holder.imgCourseThumbnail);

        if ("APPROVED".equalsIgnoreCase(status) || "ACTIVE".equalsIgnoreCase(status)) {
            holder.txtLecturer.setTextColor(Color.parseColor("#4CAF50"));
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            holder.txtLecturer.setTextColor(Color.parseColor("#F44336"));
        } else {
            holder.txtLecturer.setTextColor(Color.parseColor("#FF9800"));
        }

        /*
         * Tab đã duyệt: không cần nút duyệt lesson.
         * Tab chưa duyệt:
         * - lesson PENDING: hiện Duyệt / Từ chối
         * - lesson ACTIVE có module pending: không hiện Duyệt lesson, chỉ vào detail để duyệt module.
         * - lesson REJECTED: không nên xuất hiện vì AdminManageCourse đã lọc.
         */
        if ("PENDING".equalsIgnoreCase(mode)
                && "PENDING".equalsIgnoreCase(status)) {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
        } else {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        }

        holder.btnDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminCourseDetailActivity.class);
            intent.putExtra("COURSE_ID", course.getId());
            intent.putExtra("COURSE_TITLE", course.getTitle());

            /*
             * DÒNG QUAN TRỌNG:
             * AdminCourseDetailActivity đọc ADMIN_PENDING_MODE.
             * Không dùng COURSE_MODE nữa.
             */
            intent.putExtra("ADMIN_PENDING_MODE", "PENDING".equalsIgnoreCase(mode));

            context.startActivity(intent);
        });

        holder.btnApprove.setOnClickListener(v -> {
            LessonApiService apiService =
                    RetrofitClient.getClient().create(LessonApiService.class);

            apiService.approveLesson(course.getId())
                    .enqueue(new Callback<ApiResponse<LessonResponse>>() {
                        @Override
                        public void onResponse(
                                Call<ApiResponse<LessonResponse>> call,
                                Response<ApiResponse<LessonResponse>> response
                        ) {
                            if (response.isSuccessful() && response.body() != null) {
                                ApiResponse<LessonResponse> apiResponse = response.body();

                                if (apiResponse.getCode() == 1000) {
                                    Toast.makeText(
                                            context,
                                            "Đã phê duyệt khóa học: " + course.getTitle(),
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    int index = holder.getAdapterPosition();
                                    if (index != RecyclerView.NO_POSITION) {
                                        courseList.remove(index);
                                        notifyItemRemoved(index);
                                    }

                                } else {
                                    Toast.makeText(
                                            context,
                                            "Lỗi: " + apiResponse.getMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }

                            } else {
                                Toast.makeText(
                                        context,
                                        "Duyệt thất bại, mã lỗi: " + response.code(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<ApiResponse<LessonResponse>> call,
                                Throwable t
                        ) {
                            Toast.makeText(context, "Lỗi kết nối Server", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        holder.btnReject.setOnClickListener(v -> {
            LessonApiService apiService =
                    RetrofitClient.getClient().create(LessonApiService.class);

            /*
             * Backend hiện tại của bạn dùng approve-delete để đổi lesson PENDING sang REJECTED.
             */
            apiService.approveDeleteLesson(course.getId())
                    .enqueue(new Callback<ApiResponse<LessonResponse>>() {
                        @Override
                        public void onResponse(
                                Call<ApiResponse<LessonResponse>> call,
                                Response<ApiResponse<LessonResponse>> response
                        ) {
                            if (response.isSuccessful() && response.body() != null) {
                                ApiResponse<LessonResponse> apiResponse = response.body();

                                if (apiResponse.getCode() == 1000) {
                                    Toast.makeText(
                                            context,
                                            "Đã từ chối khóa học: " + course.getTitle(),
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    int index = holder.getAdapterPosition();
                                    if (index != RecyclerView.NO_POSITION) {
                                        courseList.remove(index);
                                        notifyItemRemoved(index);
                                    }

                                } else {
                                    Toast.makeText(
                                            context,
                                            "Lỗi: " + apiResponse.getMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }

                            } else {
                                Toast.makeText(
                                        context,
                                        "Thao tác thất bại, mã lỗi: " + response.code(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<ApiResponse<LessonResponse>> call,
                                Throwable t
                        ) {
                            Toast.makeText(context, "Lỗi kết nối Server", Toast.LENGTH_SHORT).show();
                        }
                    });
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

    public static class CourseViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitle;
        TextView txtLecturer;
        TextView txtDate;

        MaterialButton btnDetails;
        MaterialButton btnApprove;
        MaterialButton btnReject;

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