package com.example.do_an_mon_hoc_quanlikhoahoc_23521023;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static final String BASE_URL = "http://10.0.2.2:8080/NT118/";
    public static String authToken = "";

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request originalRequest = chain.request();
                        if (authToken != null && !authToken.isEmpty()) {
                            Request authenticatedRequest = originalRequest.newBuilder()
                                    .header("Authorization", "Bearer " + authToken)
                                    .build();
                            return chain.proceed(authenticatedRequest);
                        }
                        return chain.proceed(originalRequest);
                    })
                    .build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}




