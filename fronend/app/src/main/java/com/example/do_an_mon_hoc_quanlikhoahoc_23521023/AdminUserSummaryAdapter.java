package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class AdminUserSummaryAdapter extends RecyclerView.Adapter<AdminUserSummaryAdapter.UserViewHolder> {

    private final Context context;
    private List<AdminUserSummary> userList;

    public AdminUserSummaryAdapter(Context context, List<AdminUserSummary> userList) {
        this.context = context;
        this.userList = userList;
    }

    public void updateList(List<AdminUserSummary> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.admin_user_summary, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        AdminUserSummary user = userList.get(position);

        holder.txtUserId.setText("Mã User: " + safe(user.getUserCode()));
        holder.txtUserName.setText("Họ tên: " + safe(user.getFullName()));

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().trim().isEmpty()) {
            Glide.with(context)
                    .load(user.getAvatarUrl())
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(holder.imgUserAvatar);
        } else {
            holder.imgUserAvatar.setImageResource(R.drawable.ic_profile);
        }

        holder.btnDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminUserDetailActivity.class);

            intent.putExtra("user_id", user.getUserId());
            intent.putExtra("user_code", user.getUserCode());
            intent.putExtra("username", user.getUsername());
            intent.putExtra("full_name", user.getFullName());
            intent.putExtra("email", user.getEmail());
            intent.putExtra("phone", user.getPhone());
            intent.putExtra("avatar_url", user.getAvatarUrl());
            intent.putExtra("role", user.getRole());
            intent.putExtra("status", user.getStatus());
            intent.putExtra("date_of_birth", user.getDateOfBirth());
            intent.putExtra("department", user.getDepartment());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList == null ? 0 : userList.size();
    }

    private String safe(String value) {
        if (value == null || "null".equalsIgnoreCase(value.trim())) {
            return "";
        }

        return value.trim();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        ImageView imgUserAvatar;
        TextView txtUserId, txtUserName;
        MaterialButton btnDetail;

        public UserViewHolder(View itemView) {
            super(itemView);

            imgUserAvatar = itemView.findViewById(R.id.imgUserAvatar);
            txtUserId = itemView.findViewById(R.id.txtUserId);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            btnDetail = itemView.findViewById(R.id.btnDetail);
        }
    }
}