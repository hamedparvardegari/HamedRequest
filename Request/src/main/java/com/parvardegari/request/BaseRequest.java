package com.parvardegari.request;

import android.app.Activity;
import android.content.Context;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BaseRequest<H>{
    public static final int POST = 310;
    public static final int GET = 576;

    private final int method;
    private final String subUrl;
    private final RequestBody requestBody;
    private final Class<H> singleType;
    private final Class<H[]> arrayType;

    private static final String baseUrl = "";

    private BaseRequest(Builder<H> builder){
        this.method=builder.method;
        this.subUrl=builder.subUrl;
        this.requestBody= builder.formBody.build();
        this.singleType=builder.singleType;
        this.arrayType=builder.arrayType;

    }

    public void getResult(Context context, ResultListener<H> listener){
        OkHttpClient client = new OkHttpClient();
        Request request;

        if (method==POST){
            request=new Request.Builder()
                    .url(baseUrl+subUrl)
                    .post(requestBody)
                    .build();
        }
        else{
            request=new Request.Builder()
                    .url(baseUrl+subUrl)
                    .get()
                    .build();
        }

        Thread thread = new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                String responseBody = Objects.requireNonNull(response.body()).string();
                if (response.code()==200){
                    H result = new Gson().fromJson(responseBody,singleType);
                    ((Activity)context).runOnUiThread(() -> listener.onResult(result));
                }
                else{
                    ((Activity)context).runOnUiThread(() -> listener.onError(response.code(),responseBody));
                }
            } catch (IOException e) {
                ((Activity)context).runOnUiThread(() -> listener.onException(e.getMessage()));
            }
        });
        thread.start();
    }

    public void getResults(Context context,MultipleResultListener<H> listener){
        OkHttpClient client = new OkHttpClient();
        Request request;

        if (method==POST){
            request=new Request.Builder()
                    .url(baseUrl+subUrl+".php")
                    .post(requestBody)
                    .build();
        }
        else{
            request=new Request.Builder()
                    .url(baseUrl+subUrl+".php")
                    .get()
                    .build();
        }

        Thread thread = new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                String responseBody = Objects.requireNonNull(response.body()).string();
                if (response.code()==200){
                    H[] arrayResult = new Gson().fromJson(responseBody,arrayType);
                    ArrayList<H> results = new ArrayList<>(Arrays.asList(arrayResult));
                    ((Activity)context).runOnUiThread(() -> listener.onResult(results));
                }
                else{
                    ((Activity)context).runOnUiThread(() -> listener.onError(response.code(),responseBody));
                }

            } catch (IOException e) {
                ((Activity)context).runOnUiThread(() -> listener.onException(e.getMessage()));
            }
        });
        thread.start();


    }


    public static class  Builder<H>{
        private int method;
        private String subUrl;
        private Class<H> singleType;
        private Class<H[]> arrayType;

        private final FormBody.Builder formBody = new FormBody.Builder();



        public Builder<H> method(int method){
            this.method=method;

            return this;
        }


        public Builder<H> body(String key,String value){
            formBody.add(key,value);

            return this;
        }

        public Builder<H> subUrl(String subUrl){
            this.subUrl=subUrl;

            return this;
        }


        public Builder<H> singleType(Class<H> singleType){
            this.singleType=singleType;

            return this;
        }

        public Builder<H> arrayType(Class<H[]> arrayType){
            this.arrayType=arrayType;

            return this;
        }

        public BaseRequest<H> build(){

            return new BaseRequest<>(this);
        }


    }


    public interface ResultListener<H>{
        void onResult(H result);
        void onError(int code , String response);
        void onException(String message);
    }

    public interface MultipleResultListener<H>{
        void onResult(ArrayList<H> results);
        void onError(int code , String response);
        void onException(String message);
    }


}
