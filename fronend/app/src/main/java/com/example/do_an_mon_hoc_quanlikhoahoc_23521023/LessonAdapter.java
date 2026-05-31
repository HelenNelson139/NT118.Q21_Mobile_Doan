package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.ViewHolder> {
    private List<ModuleResponse> moduleList;
    public LessonAdapter(List<ModuleResponse> moduleList) {
        this.moduleList = moduleList;
    }

    public void updateData(List<ModuleResponse> newList) {
        this.moduleList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // CHỈNH SỬA 2: Lấy trực tiếp context từ biến "parent" cực kỳ tiện lợi
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lesson_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModuleResponse module = moduleList.get(position);

        holder.txtLesson.setText("Bài " + (position + 1));
        holder.txtTitle.setText(module.getTitle());

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            Context context = v.getContext();
            Intent intent = new Intent(context, LessonActivity.class);
            intent.putExtra("MODULE_TITLE", module.getTitle());
            intent.putExtra("MODULE_OBJECT", module.getObjective());
            intent.putExtra("MODULE_CONTENT", module.getContent());
            intent.putExtra("MODULE_EXAMPLE", module.getExample());

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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtLesson = itemView.findViewById(R.id.txtChapterLesson);
            txtTitle = itemView.findViewById(R.id.txtLessonTitle);
        }
    }
}