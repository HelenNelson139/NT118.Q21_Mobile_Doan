package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CourseActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager2 viewPager;
    ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if(position == 0) {
                        tab.setText(getString(R.string.tab_intro));
                    } else {
                        tab.setText(getString(R.string.tab_curriculum));
                    }
                }).attach();

        String name = getIntent().getStringExtra("course_name");
        TextView tv = findViewById(R.id.txtCourseTitle);
        tv.setText(name);
        ImageView imgCourse = findViewById(R.id.imgCourse);
        String thumbnailUrl = getIntent().getStringExtra("course_thumbnail");
        com.bumptech.glide.Glide.with(this)
                .load(thumbnailUrl)
                .placeholder(R.drawable.course_python)
                .error(R.drawable.course_python)
                .into(imgCourse);
    }
}