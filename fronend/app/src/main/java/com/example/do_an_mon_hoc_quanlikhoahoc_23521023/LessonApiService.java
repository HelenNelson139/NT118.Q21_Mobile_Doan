package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;
public interface LessonApiService {
    @Multipart
    @POST("api/lessons")
    Call<ApiResponse<LessonResponse>> createLesson(
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part("what_you_learn") RequestBody whatYouLearn,
            @Part("skill_learned") RequestBody skillLearned,
            @Part("teacherId") RequestBody teacherId,
            @Part MultipartBody.Part thumbnail
    );
    @GET("api/lessons/search")
    Call<ApiResponse<List<LessonResponse>>> searchLessons(
            @Query("keyword") String keyword
    );
    @DELETE("api/lessons/{id}")
    Call<ApiResponse<String>> deleteLesson(
            @Path("id") Integer id
    );
    @PUT("api/lessons/{id}/approve-delete")
    Call<ApiResponse<LessonResponse>> approveDeleteLesson(
            @Path("id") Integer id
    );
    @GET("api/lessons/{id}")
    Call<ApiResponse<LessonResponse>> getLessonById(
            @Path("id") Integer id
    );
    @PUT("api/lessons/{id}/approve")
    Call<ApiResponse<LessonResponse>> approveLesson(
            @Path("id") Integer id
    );
    @GET("api/lessons/all")
    Call<ApiResponse<List<LessonResponse>>> getAllLessons();
    @GET("api/lessons")
    Call<ApiResponse<PageResponse<Lesson>>> getLessons(
            @Query("status") String status,
            @Query("teacherId") Integer teacherId,
            @Query("keyword") String keyword,
            @Query("page") int page,
            @Query("size") int size
    );
}
