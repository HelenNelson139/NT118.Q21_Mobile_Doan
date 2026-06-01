package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface FileApiService {

    @Multipart
    @POST("api/file/upload")
    Call<ApiResponse<String>> uploadFile(
            @Part("module_id") RequestBody moduleId,
            @Part("file_name") RequestBody fileName,
            @Part MultipartBody.Part file
    );

    @GET("api/file/get/{id}")
    Call<List<FileResponse>> getFilesByModule(
            @Path("id") Integer moduleId
    );
}