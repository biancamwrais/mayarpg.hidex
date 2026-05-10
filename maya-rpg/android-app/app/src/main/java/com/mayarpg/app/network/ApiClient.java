package com.mayarpg.app.network;

import android.content.Context;

import com.mayarpg.app.utils.SessionManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {


    public static final String BASE_URL = "http://192.168.0.159:3000/api/";

    private static Retrofit retrofit;

    public static ApiService getApi(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);


            Interceptor authInterceptor = chain -> {
                String token = SessionManager.getInstance(context).getToken();
                okhttp3.Request.Builder builder = chain.request().newBuilder();
                if (token != null) {
                    builder.addHeader("Authorization", "Bearer " + token);
                }
                return chain.proceed(builder.build());
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

    public static void reset() {
        retrofit = null;
    }
}
