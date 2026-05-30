package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.CombinedData;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class TeacherHome extends AppCompatActivity {

    private MaterialCardView btnMenuCard;
    private TextView txtTotalStudents, txtTotalCourses, txtWelcome, txtTeacherName;
    private CombinedChart combinedChart;

    private static final String PREF_NAME = "APP_PREFS";
    private static final String KEY_FULL_NAME = "FULL_NAME";
    private static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String KEY_ROLE = "ROLE";
    private static final String KEY_USERNAME = "USERNAME";
    private static final String KEY_IS_LOGGED_IN = "IS_LOGGED_IN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_home);

        btnMenuCard = findViewById(R.id.btnMenuCard);
        txtTotalStudents = findViewById(R.id.txtTotalStudents);
        txtTotalCourses = findViewById(R.id.txtTotalCourses);
        txtWelcome = findViewById(R.id.txtWelcome);
        txtTeacherName = findViewById(R.id.txtTeacherName);
        combinedChart = findViewById(R.id.combinedChart);

        loadStats();
        setupChart();

        btnMenuCard.setOnClickListener(v -> showSidebarMenu());
    }

    private void loadStats() {
        txtTotalStudents.setText("120");
        txtTotalCourses.setText("6");

        txtWelcome.setText("Chào giảng viên,");

        SharedPreferences sharedPreferences =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        String fullName = sharedPreferences.getString(KEY_FULL_NAME, "");

        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = "Giảng viên";
        }

        txtTeacherName.setText(fullName);
    }

    private void setupChart() {

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(1, 2));
        barEntries.add(new BarEntry(2, 3));
        barEntries.add(new BarEntry(3, 1));
        barEntries.add(new BarEntry(4, 4));

        BarDataSet barDataSet = new BarDataSet(barEntries, "Khóa học");
        barDataSet.setColor(Color.parseColor("#112D4E"));

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.4f);

        ArrayList<Entry> lineEntries = new ArrayList<>();
        lineEntries.add(new Entry(1, 20));
        lineEntries.add(new Entry(2, 40));
        lineEntries.add(new Entry(3, 30));
        lineEntries.add(new Entry(4, 60));

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Sinh viên");
        lineDataSet.setColor(Color.parseColor("#3F72AF"));
        lineDataSet.setCircleColor(Color.parseColor("#3F72AF"));
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(4f);
        lineDataSet.setValueTextSize(10f);

        LineData lineData = new LineData(lineDataSet);

        CombinedData combinedData = new CombinedData();
        combinedData.setData(barData);
        combinedData.setData(lineData);

        combinedChart.setData(combinedData);

        combinedChart.getDescription().setEnabled(false);
        combinedChart.setDrawGridBackground(false);
        combinedChart.animateY(1000);

        XAxis xAxis = combinedChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        combinedChart.invalidate();
    }

    private void showSidebarMenu() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.teacher_layout_sidebar);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.END);
        }

        MaterialCardView btnCloseMenu = dialog.findViewById(R.id.btnCloseMenu);

        LinearLayout menuHome = dialog.findViewById(R.id.menuHome);
        LinearLayout menuMyClasses = dialog.findViewById(R.id.menuMyClasses);
        LinearLayout menuProfile = dialog.findViewById(R.id.menuProfile);

        TextView txtLogout = dialog.findViewById(R.id.txtLogout);

        btnCloseMenu.setOnClickListener(v -> dialog.dismiss());

        menuHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, TeacherHome.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            dialog.dismiss();
        });

        menuMyClasses.setOnClickListener(v -> {
            startActivity(new Intent(this, TeacherClass.class));
            dialog.dismiss();
        });

        menuProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, TeacherProfileActivity.class));
            dialog.dismiss();
        });

        txtLogout.setOnClickListener(v -> {
            logout();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void logout() {
        SharedPreferences sharedPreferences =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_ACCESS_TOKEN);
        editor.remove(KEY_ROLE);
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_FULL_NAME);
        editor.remove(KEY_IS_LOGGED_IN);
        editor.apply();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}