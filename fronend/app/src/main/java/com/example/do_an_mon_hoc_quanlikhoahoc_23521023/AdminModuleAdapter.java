package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminModuleAdapter extends RecyclerView.Adapter<AdminModuleAdapter.ModuleViewHolder> {

    private List<ModuleResponse> moduleList;

    public AdminModuleAdapter(List<ModuleResponse> moduleList) {
        this.moduleList = moduleList;
    }

    public void setData(List<ModuleResponse> modules) {
        this.moduleList = modules;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_module, parent, false);
        return new ModuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleViewHolder holder, int position) {
        ModuleResponse module = moduleList.get(position);

        holder.tvModuleTitle.setText(safe(module.getTitle()));
        holder.tvModuleDescription.setText(safe(module.getContent()));

        String status = safe(module.getStatus());
        if (status.isEmpty()) {
            status = "PENDING";
        }

        holder.tvModuleStatus.setText("Trạng thái: " + status);

        if ("ACTIVE".equalsIgnoreCase(status) || "APPROVED".equalsIgnoreCase(status)) {
            holder.tvModuleStatus.setTextColor(Color.parseColor("#4CAF50"));
            holder.btnApproveModule.setVisibility(View.GONE);
            holder.btnRejectModule.setVisibility(View.GONE);
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            holder.tvModuleStatus.setTextColor(Color.parseColor("#F44336"));
            holder.btnApproveModule.setVisibility(View.GONE);
            holder.btnRejectModule.setVisibility(View.GONE);
        } else {
            holder.tvModuleStatus.setTextColor(Color.parseColor("#FF9800"));
            holder.btnApproveModule.setVisibility(View.VISIBLE);
            holder.btnRejectModule.setVisibility(View.VISIBLE);
        }

        /*
         * Quan trọng:
         * Không mở AdminModuleDetailActivity nữa.
         * Mở LessonActivity để dùng lại màn hình bài học giống Teacher/Student.
         */
        holder.itemView.setOnClickListener(v -> openLessonActivity(holder, position));

        holder.tvModuleTitle.setOnClickListener(v -> openLessonActivity(holder, position));
        holder.tvModuleDescription.setOnClickListener(v -> openLessonActivity(holder, position));

        holder.btnApproveModule.setOnClickListener(v -> {
            ModuleApiService apiService =
                    RetrofitClient.getClient().create(ModuleApiService.class);

            apiService.approveModule(module.getId())
                    .enqueue(new Callback<ApiResponse<ModuleResponse>>() {
                        @Override
                        public void onResponse(
                                Call<ApiResponse<ModuleResponse>> call,
                                Response<ApiResponse<ModuleResponse>> response
                        ) {
                            if (response.isSuccessful() && response.body() != null) {
                                ApiResponse<ModuleResponse> apiResponse = response.body();

                                if (apiResponse.getCode() == 1000) {
                                    Toast.makeText(
                                            holder.itemView.getContext(),
                                            "Đã duyệt bài giảng",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    module.setStatus("ACTIVE");

                                    int adapterPosition = holder.getAdapterPosition();
                                    if (adapterPosition != RecyclerView.NO_POSITION) {
                                        notifyItemChanged(adapterPosition);
                                    }

                                } else {
                                    Toast.makeText(
                                            holder.itemView.getContext(),
                                            "Lỗi: " + apiResponse.getMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }

                            } else {
                                Toast.makeText(
                                        holder.itemView.getContext(),
                                        "Duyệt thất bại: HTTP " + response.code(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<ApiResponse<ModuleResponse>> call,
                                Throwable t
                        ) {
                            Toast.makeText(
                                    holder.itemView.getContext(),
                                    "Lỗi kết nối server: " + t.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });

        holder.btnRejectModule.setOnClickListener(v -> {
            ModuleApiService apiService =
                    RetrofitClient.getClient().create(ModuleApiService.class);

            apiService.approveDeleteModule(module.getId())
                    .enqueue(new Callback<ApiResponse<ModuleResponse>>() {
                        @Override
                        public void onResponse(
                                Call<ApiResponse<ModuleResponse>> call,
                                Response<ApiResponse<ModuleResponse>> response
                        ) {
                            if (response.isSuccessful() && response.body() != null) {
                                ApiResponse<ModuleResponse> apiResponse = response.body();

                                if (apiResponse.getCode() == 1000) {
                                    Toast.makeText(
                                            holder.itemView.getContext(),
                                            "Đã xử lý bài giảng",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    int index = holder.getAdapterPosition();
                                    if (index != RecyclerView.NO_POSITION) {
                                        moduleList.remove(index);
                                        notifyItemRemoved(index);
                                    }

                                } else {
                                    Toast.makeText(
                                            holder.itemView.getContext(),
                                            "Lỗi: " + apiResponse.getMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }

                            } else {
                                Toast.makeText(
                                        holder.itemView.getContext(),
                                        "Thao tác thất bại: HTTP " + response.code(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<ApiResponse<ModuleResponse>> call,
                                Throwable t
                        ) {
                            Toast.makeText(
                                    holder.itemView.getContext(),
                                    "Lỗi kết nối server: " + t.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });
    }

    private void openLessonActivity(ModuleViewHolder holder, int clickedPosition) {
        if (moduleList == null || moduleList.isEmpty()) {
            Toast.makeText(
                    holder.itemView.getContext(),
                    "Không có bài giảng để mở",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        int adapterPosition = holder.getAdapterPosition();

        if (adapterPosition == RecyclerView.NO_POSITION) {
            adapterPosition = clickedPosition;
        }

        ArrayList<Integer> moduleIds = new ArrayList<>();

        for (ModuleResponse module : moduleList) {
            if (module != null && module.getId() != null) {
                moduleIds.add(module.getId());
            }
        }

        if (moduleIds.isEmpty()) {
            Toast.makeText(
                    holder.itemView.getContext(),
                    "Danh sách bài giảng không hợp lệ",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Intent intent = new Intent(
                holder.itemView.getContext(),
                LessonActivity.class
        );

        intent.putIntegerArrayListExtra("MODULE_IDS", moduleIds);
        intent.putExtra("CURRENT_INDEX", adapterPosition);

        holder.itemView.getContext().startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return moduleList == null ? 0 : moduleList.size();
    }

    private String safe(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) {
            return "";
        }

        return value.trim();
    }

    public static class ModuleViewHolder extends RecyclerView.ViewHolder {

        TextView tvModuleTitle;
        TextView tvModuleStatus;
        TextView tvModuleDescription;
        MaterialButton btnApproveModule;
        MaterialButton btnRejectModule;

        public ModuleViewHolder(@NonNull View itemView) {
            super(itemView);

            tvModuleTitle = itemView.findViewById(R.id.tvModuleTitle);
            tvModuleStatus = itemView.findViewById(R.id.tvModuleStatus);
            tvModuleDescription = itemView.findViewById(R.id.tvModuleDescription);
            btnApproveModule = itemView.findViewById(R.id.btnApproveModule);
            btnRejectModule = itemView.findViewById(R.id.btnRejectModule);
        }
    }
}