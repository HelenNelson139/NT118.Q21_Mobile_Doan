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
    Context context;
    List<LessonResponse> lessonList;
    public LessonAdapter(Context context, List<LessonResponse> lessonList) {
        this.context = context;
        this.lessonList = lessonList;
    }

    public void updateData(List<LessonResponse> newList) {
        this.lessonList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_lesson_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LessonResponse lesson = lessonList.get(position);

        holder.txtLesson.setText("Bài " + (position + 1));
        holder.txtTitle.setText(lesson.getTitle());

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;
            Intent intent = new Intent(context, LessonActivity.class);
            intent.putExtra("index", currentPosition);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return lessonList == null ? 0 : lessonList.size();
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