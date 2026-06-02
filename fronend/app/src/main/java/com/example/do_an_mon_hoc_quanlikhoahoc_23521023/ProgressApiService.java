package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ProgressApiService {

    @GET("progress/{lessonId}/{studentId}")
    Call<ApiResponse<LessonProgressResponse>> getLessonProgress(
            @Path("lessonId") Integer lessonId,
            @Path("studentId") Integer studentId
    );

    @POST("progress/modules/{moduleId}/{studentId}/complete")
    Call<ApiResponse<Void>> completeModule(
            @Path("moduleId") Integer moduleId,
            @Path("studentId") Integer studentId
    );
}