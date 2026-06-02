package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface StudentCourseApiService {

    @GET("api/students/{userId}/lessons")
    Call<ApiResponse<List<Integer>>> getStudentLessonIds(
            @Path("userId") Integer userId
    );

    @GET("api/students/{userId}/lessons_not_enroll")
    Call<ApiResponse<List<Integer>>> getStudentLessonNotEnrollIds(
            @Path("userId") Integer userId
    );

    @POST("api/students/course")
    Call<ApiResponse<String>> enrollCourse(
            @Body StudentCourseRequest request
    );

}