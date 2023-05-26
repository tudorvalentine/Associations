package com.tudorvalentine.augmentedimages.app;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AppController extends Application {
    public static final String TAG = AppController.class.getSimpleName();
    private static final int BUFFER_SIZE = 4096;
    private RequestQueue mRequestQueue;
    private Response response;
    private static AppController mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public void downloadFile(String URL, String fileName, String user){

        String fileURL = URL  + fileName + "/" + user;
        Thread thread = new Thread(() -> {
            try  {
                try (BufferedInputStream inputStream = new BufferedInputStream(new URL(fileURL).openStream())) {
                    URL url = new URL(fileURL);

                    Log.d(TAG, "Download file > " + url.getFile());

                    FileOutputStream fileOS = getApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE);

                    byte [] data = new byte[BUFFER_SIZE];
                    int byteContent;
                    while ((byteContent = inputStream.read(data, 0, BUFFER_SIZE)) != -1) {
                        fileOS.write(data, 0, byteContent);
                    }
                } catch (IOException e) {
                    Log.e(TAG,"Error Download  " + e.getMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }
    public Response uploadImage(Bitmap image_bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image_bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] imageData = byteArrayOutputStream.toByteArray();
        CountDownLatch latch = new CountDownLatch(1);
        Thread thread = new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("test-img", "image-test.png", RequestBody.create(imageData, MediaType.parse("image/jpeg")))
                    .build();
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(AppConfig.URL_TESTIMG)
                    .post(requestBody)
                    .build();

            try {
                response = client.newCall(request).execute();
                latch.countDown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return response;
    }
    public Response uploadImage(Bitmap image_bitmap, String user, boolean prev){
        long timestamp = System.currentTimeMillis();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image_bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] imageData = byteArrayOutputStream.toByteArray();

        String form_data = prev ? "doc-prev" : "image";

        CountDownLatch latch = new CountDownLatch(1);

        Thread thread = new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(form_data, timestamp + "image.png", RequestBody.create(imageData, MediaType.parse("image/jpeg")))
                    .build();
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(prev ? AppConfig.URL_UPLOAD_DOC_PREV + user : AppConfig.URL_UPLOAD_IMAGES + user)
                    .post(requestBody)
                    .build();

            try {
                response = client.newCall(request).execute();
                latch.countDown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return response;
    }

    public Response uploadDocument(byte[] doc, String user){
        CountDownLatch countDownLatch = new CountDownLatch(1);
        long timestamp = System.currentTimeMillis();

        Thread thread = new Thread(() -> {
            OkHttpClient okHttpClient = new OkHttpClient();
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            builder.addFormDataPart("doc", timestamp + "document.pdf", RequestBody.create(doc, MediaType.parse("application/pdf")));
            MultipartBody requestBody = builder.build();

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(AppConfig.URL_UPLOAD_DOC + user)
                    .post(requestBody).build();

            try {
                response = okHttpClient.newCall(request).execute();
                countDownLatch.countDown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }
}
