package com.dominicsilveira.parkingsystem.utils.network;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("/v1/plate-reader")
    Call<ResponseBody> postImage(@Part MultipartBody.Part image, @Part("upload") RequestBody name, @Header("Authorization") String authHeader);
}

