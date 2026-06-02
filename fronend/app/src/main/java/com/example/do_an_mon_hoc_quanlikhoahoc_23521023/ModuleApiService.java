package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ModuleApiService {

    @Multipart
    @POST("api/modules")
    Call<ApiResponse<ModuleResponse>> createModule(
            @Part("lessonId") RequestBody lessonId,
            @Part("title") RequestBody title,
            @Part("objective") RequestBody objective,
            @Part("content") RequestBody content,
            @Part("example") RequestBody example,
            @Part("order_index") RequestBody orderIndex
    );

    @GET("api/modules/search")
    Call<ApiResponse<List<ModuleResponse>>> searchModules(
            @Query("keyword") String keyword
    );

    @GET("api/modules/{id}")
    Call<ApiResponse<ModuleResponse>> getModuleById(
            @Path("id") Integer id
    );

    @DELETE("api/modules/{id}")
    Call<ApiResponse<String>> deleteModule(
            @Path("id") Integer id
    );

    @PUT("api/modules/{id}/approve")
    Call<ApiResponse<ModuleResponse>> approveModule(
            @Path("id") Integer id
    );

    @PUT("api/modules/{id}/approve-delete")
    Call<ApiResponse<ModuleResponse>> approveDeleteModule(
            @Path("id") Integer id
    );

    @GET("api/modules/lesson/{lessonId}")
    Call<ApiResponse<List<ModuleResponse>>> getModulesByLessonId(
            @Path("lessonId") Integer lessonId
    );

    @GET("api/modules/lesson/pending/{lessonId}")
    Call<ApiResponse<List<ModuleResponse>>> getPendingModulesByLessonId(
            @Path("lessonId") Integer lessonId
    );

    @GET("api/modules/allPending")
    Call<ApiResponse<List<ModuleResponse>>> getAllModulesPending();
}