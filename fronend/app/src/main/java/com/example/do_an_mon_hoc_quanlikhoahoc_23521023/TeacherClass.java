package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class TeacherClass extends AppCompatActivity {

    private RecyclerView rvCourseList;
    private List<TeacherCourse> filteredList;
    private TeacherCourseAdapter adapter;
    private MaterialCardView btnMenuCard;
    private View btnAddCourse;

    private Uri selectedImageUri;
    private ImageView imgPreview;
    private ActivityResultLauncher<String> imagePickerLauncher;

    private final List<View> lessonViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_classes);

        btnMenuCard = findViewById(R.id.btnMenuCard);
        btnAddCourse = findViewById(R.id.btnAddCourse);
        rvCourseList = findViewById(R.id.rvCourseList);

        rvCourseList.setLayoutManager(new LinearLayoutManager(this));

        loadData();

        adapter = new TeacherCourseAdapter(filteredList, new TeacherCourseAdapter.OnCourseActionListener() {
            @Override
            public void onEditClick(int position) {
                showEditDialog(position);
            }

            @Override
            public void onDeleteClick(int position) {
                showDeleteDialog(position);
            }
        });
        rvCourseList.setAdapter(adapter);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    selectedImageUri = uri;
                    if (uri != null && imgPreview != null) {
                        imgPreview.setImageURI(uri);
                    }
                }
        );

        btnMenuCard.setOnClickListener(v -> showSidebarMenu());
        btnAddCourse.setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {

    }

    private void showEditDialog(int position) {
        TeacherCourse oldCourse = filteredList.get(position);

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_course, null);

        EditText edtTitle = view.findViewById(R.id.edtCourseTitle);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        LinearLayout containerIntro = view.findViewById(R.id.containerIntro);
        LinearLayout containerCurriculum = view.findViewById(R.id.containerCurriculum);
        ImageView imgCourseCover = view.findViewById(R.id.imgCourseCover);
        MaterialButton btnUploadImage = view.findViewById(R.id.btnUploadImage);
        LinearLayout lessonContainer = view.findViewById(R.id.lessonContainer);
        MaterialButton btnAddLesson = view.findViewById(R.id.btnAddLesson);
        MaterialButton btnSave = view.findViewById(R.id.btnSaveCourse);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);

        edtTitle.setText(oldCourse.getTitle());

        selectedImageUri = null;
        lessonViews.clear();
        imgPreview = imgCourseCover;
        imgCourseCover.setImageResource(oldCourse.getImageResource());

        btnSave.setText("Cập nhật");

        Dialog dialog = new Dialog(this, R.style.CustomDialogTheme);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
            window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }

        setupTabs(tabLayout, containerIntro, containerCurriculum);

        btnUploadImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnAddLesson.setOnClickListener(v -> addLesson(lessonContainer));

        btnSave.setOnClickListener(v -> {
            String newTitle = edtTitle.getText().toString().trim();

            if (newTitle.isEmpty()) {
                edtTitle.setError("Nhập tên khóa học");
                return;
            }

            TeacherCourse updatedCourse = new TeacherCourse(
                    newTitle,
                    oldCourse.getImageResource(),
                    oldCourse.getDescription(),
                    oldCourse.getTeacher()
            );

            filteredList.set(position, updatedCourse);
            adapter.notifyItemChanged(position);

            Toast.makeText(this, "Đã cập nhật khóa học", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void setupTabs(TabLayout tabLayout, LinearLayout containerIntro, LinearLayout containerCurriculum) {
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText("Giới thiệu"));
        tabLayout.addTab(tabLayout.newTab().setText("Giáo trình"));

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TextView tabTextView = (TextView) LayoutInflater.from(this)
                    .inflate(R.layout.tab_item_layout, null);

            if (tabLayout.getTabAt(i) != null) {
                tabTextView.setText(tabLayout.getTabAt(i).getText());
                tabTextView.setTextSize(14);
                tabLayout.getTabAt(i).setCustomView(tabTextView);
            }
        }

        containerIntro.setVisibility(View.VISIBLE);
        containerCurriculum.setVisibility(View.GONE);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    containerIntro.setVisibility(View.VISIBLE);
                    containerCurriculum.setVisibility(View.GONE);
                } else {
                    containerIntro.setVisibility(View.GONE);
                    containerCurriculum.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void addLesson(LinearLayout lessonContainer) {
        View lessonView = LayoutInflater.from(this).inflate(R.layout.item_lesson, null);

        ImageView imgLesson = lessonView.findViewById(R.id.imgCourseCover);
        MaterialButton btnPickImage = lessonView.findViewById(R.id.btnUploadImage);

        btnPickImage.setOnClickListener(v -> {
            imgPreview = imgLesson;
            imagePickerLauncher.launch("image/*");
        });

        if (!lessonViews.isEmpty()) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.topMargin = 20;
            lessonView.setLayoutParams(params);
        }

        lessonContainer.addView(lessonView);
        lessonViews.add(lessonView);
    }

    private void showDeleteDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa khóa học")
                .setMessage("Bạn có chắc muốn xóa khóa học này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    filteredList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, filteredList.size());
                    Toast.makeText(this, "Đã xóa khóa học", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadData() {
        filteredList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            filteredList.add(new TeacherCourse(
                    "Phân tích dữ liệu với Python và Machine Learning",
                    R.drawable.course_python,
                    "Khóa học dành cho người mới bắt đầu",
                    "Nguyễn Văn A"
            ));
        }
    }

    private void showSidebarMenu() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.teacher_layout_sidebar);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.END);
        }

        MaterialCardView btnCloseMenu = dialog.findViewById(R.id.btnCloseMenu);
        LinearLayout menuProfile = dialog.findViewById(R.id.menuProfile);
        LinearLayout menuMyClasses = dialog.findViewById(R.id.menuMyClasses);
        TextView txtLogout = dialog.findViewById(R.id.txtLogout);

        btnCloseMenu.setOnClickListener(v -> dialog.dismiss());

        menuProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, TeacherProfileActivity.class));
            dialog.dismiss();
        });

        menuMyClasses.setOnClickListener(v -> dialog.dismiss());

        txtLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }
}