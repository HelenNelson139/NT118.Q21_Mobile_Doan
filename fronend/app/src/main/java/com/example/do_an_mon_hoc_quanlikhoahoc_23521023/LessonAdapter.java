package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // THÊM MỚI
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.ViewHolder> {
    private List<ModuleResponse> moduleList;
    private String lessonDescription = "";
    private String lessonThumbnailUrl = "";

    public interface OnModuleActionListener {
        void onDeleteClick(int position, ModuleResponse module);
    }
    private OnModuleActionListener actionListener;

    public LessonAdapter(List<ModuleResponse> moduleList, OnModuleActionListener actionListener) {
        this.moduleList = moduleList;
        this.actionListener = actionListener;
    }

    public void updateData(List<ModuleResponse> newList, String lessonDescription, String lessonThumbnailUrl) {
        this.moduleList = newList;
        this.lessonDescription = lessonDescription != null ? lessonDescription : "";
        this.lessonThumbnailUrl = lessonThumbnailUrl != null ? lessonThumbnailUrl : "";
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lesson_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModuleResponse module = moduleList.get(position);

        holder.txtLesson.setText("Bài " + (position + 1));
        holder.txtTitle.setText(module.getTitle());
        holder.btnDeleteModule.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION && actionListener != null) {
                actionListener.onDeleteClick(currentPosition, module);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            Context context = v.getContext();
            Intent intent = new Intent(context, LessonActivity.class);
            intent.putExtra("MODULE_TITLE", module.getTitle());
            intent.putExtra("MODULE_OBJECT", module.getObjective());
            intent.putExtra("MODULE_CONTENT", module.getContent());
            intent.putExtra("MODULE_EXAMPLE", module.getExample());
            intent.putExtra("LESSON_DESCRIPTION", lessonDescription);
            intent.putExtra("LESSON_THUMBNAIL", lessonThumbnailUrl);

            ArrayList<Integer> moduleIds = new ArrayList<>();
            for (ModuleResponse m : moduleList) {
                moduleIds.add(m.getId());
            }

            intent.putIntegerArrayListExtra("MODULE_IDS", moduleIds);
            intent.putExtra("CURRENT_INDEX", currentPosition);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return moduleList == null ? 0 : moduleList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtLesson;
        TextView txtTitle;
        ImageView btnDeleteModule;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtLesson = itemView.findViewById(R.id.txtChapterLesson);
            txtTitle = itemView.findViewById(R.id.txtLessonTitle);
            btnDeleteModule = itemView.findViewById(R.id.btnDeleteModule);
        }
    }
}