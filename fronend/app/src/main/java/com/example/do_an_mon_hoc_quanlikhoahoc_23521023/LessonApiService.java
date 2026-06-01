package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LessonApiService {

    @Multipart
    @POST("api/lessons")
    Call<ApiResponse<LessonResponse>> createLesson(
            @Part MultipartBody.Part title,
            @Part MultipartBody.Part description,
            @Part MultipartBody.Part what_you_learn,
            @Part MultipartBody.Part skill_learned,
            @Part MultipartBody.Part teacherId,
            @Part MultipartBody.Part thumbnail
    );

    @GET("api/lessons/search")
    Call<ApiResponse<List<LessonResponse>>> searchLessons(
            @Query("keyword") String keyword
    );

    @GET("api/lessons/{id}")
    Call<ApiResponse<LessonResponse>> getLessonById(
            @Path("id") Integer id
    );

    @GET("api/lessons/my-lessons")
    Call<ApiResponse<List<LessonResponse>>> getMyLessons();

    @GET("api/lessons/allActive")
    Call<ApiResponse<List<LessonResponse>>> getAllActiveLessons();

    @GET("api/lessons/allPending")
    Call<ApiResponse<List<LessonResponse>>> getAllPendingLessons();

    @DELETE("api/lessons/{id}")
    Call<ApiResponse<String>> deleteLesson(
            @Path("id") Integer id
    );

    @PUT("api/lessons/{id}/approve")
    Call<ApiResponse<LessonResponse>> approveLesson(
            @Path("id") Integer id
    );

    @PUT("api/lessons/{id}/approve-delete")
    Call<ApiResponse<LessonResponse>> approveDeleteLesson(
            @Path("id") Integer id
    );

    @PATCH("api/lessons/update/{id}")
    Call<ApiResponse<String>> updateLesson(
            @Path("id") Integer id,
            @Body LessonUpdateRequest request
    );

}