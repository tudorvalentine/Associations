package com.tudorvalentine.augmentedimages.activity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.squareup.picasso.Picasso;
import com.tudorvalentine.augmentedimages.R;
import com.tudorvalentine.augmentedimages.app.AppConfig;
import com.tudorvalentine.augmentedimages.app.AppController;
import com.tudorvalentine.augmentedimages.helpers.SQLiteHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import okhttp3.Response;

public class UploadActivity extends FragmentActivity {
    private static final String  TAG_DIALOG = UploadActivity.class.getSimpleName();
    private Toolbar toolbar;
    private static final int IMAGE_PICKER_REQUEST_CODE = 1;
    private static final int DOCUMENT_PICKER_REQUEST_CODE = 2;
    private ImageView picked_image;
    private ImageView picked_document;
    private Button btn_pick_document;
    private TextView grade_desc;
    private ActionMenuItemView item_create;
    LinearProgressIndicator gradeImageRecvFromServer;
    private SQLiteHandler db;
    private Bitmap imageUpload;
    private Bitmap document_prev;
    private byte[] document;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new SQLiteHandler(getApplicationContext());
        HashMap<String,String> user = db.getUserDetails();
        String username = user.get("username");
        setContentView(R.layout.upload_image);
        picked_image = findViewById(R.id.picked_image);
        picked_document = findViewById(R.id.picked_document);

        toolbar = findViewById(R.id.toolbar);

        Button btn_pick_image = findViewById(R.id.btn_pick_image);
        btn_pick_document = findViewById(R.id.btn_pick_document);
        grade_desc = findViewById(R.id.description_grad);
        gradeImageRecvFromServer = findViewById(R.id.linearProgressIndicator);
        gradeImageRecvFromServer.setTrackThickness(15);
        Bundle options = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.slide_up, R.anim.slide_down).toBundle();
        btn_pick_image.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
            intent.setType("image/*");
            // Start the activity for result with the Intent
            startActivityForResult(intent, IMAGE_PICKER_REQUEST_CODE, options);
        });

        btn_pick_document.setEnabled(false);
        btn_pick_document.setOnClickListener(v ->{
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("application/*");
            startActivityForResult(intent, DOCUMENT_PICKER_REQUEST_CODE, options);
        });
        toolbar.setNavigationOnClickListener(v ->
                finish()
                );
        toolbar.setTitle("Create association");
        toolbar.inflateMenu(R.menu.bottom_app_bar);
        item_create = toolbar.findViewById(R.id.action_save);
        item_create.setEnabled(false);
        toolbar.setOnMenuItemClickListener(item -> {

            Response upload_image_res = AppController.getInstance().uploadImage(imageUpload, username, false);
            Response upload_document_prev_res = AppController.getInstance().uploadImage(document_prev, username, true);
            Response upload_doc_res =  AppController.getInstance().uploadDocument(document, username);

            int id_user = Integer.parseInt(user.get("id"));
            if (upload_doc_res.code() == 200 && upload_image_res.code() == 200 && upload_document_prev_res.code() == 200){
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_CREATE_ASSOC + id_user, null ,new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG_DIALOG, response.getString("message"));
                            Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG_DIALOG, error.getMessage());
                    }
                });
                AppController.getInstance().addToRequestQueue(jsonObjectRequest, TAG_DIALOG);
                finish();
            }else{
                Toast.makeText(getApplicationContext(), "Server not working ", Toast.LENGTH_LONG).show();
            }
            finish();
            return true;
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            item_create.setEnabled(false);
            Uri imageUri = data.getData();
            Picasso.get().load(imageUri).into(picked_image);
            Bitmap bitmap = null;
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                bitmap = BitmapFactory.decodeStream(inputStream);
                imageUpload = bitmap;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            gradeImageRecvFromServer.setIndeterminate(true);

            Response response = AppController.getInstance().uploadImage(bitmap);

            if (response.code() == 200){
                try {
                    JSONObject res_json = new JSONObject(response.body().string());
                    if (!res_json.getBoolean("error")){
                        int grade = Integer.parseInt(res_json.getString("message"));
                        gradeImageRecvFromServer.setProgressCompat(grade,true);
                        if (grade >= 75){
                            gradeImageRecvFromServer.setIndicatorColor(Color.GREEN);
                            grade_desc.setText("Imaginea este potrivita si va fi recunoscuta efectiv.");
                            btn_pick_document.setEnabled(true);

                        }else if (grade >= 60 && grade < 75){
                            gradeImageRecvFromServer.setIndicatorColor(Color.YELLOW);
                            grade_desc.setText("Imaginea este potrivita dar nu va fi recunoscuta efectiv");
                            btn_pick_document.setEnabled(true);
                        }else {
                            gradeImageRecvFromServer.setIndicatorColor(Color.RED);
                            grade_desc.setText("Imaginea nu este potrivita, va fi recunoscuta greu");
                            btn_pick_document.setEnabled(false);
                        }

                    }else{
                        Log.d(TAG_DIALOG, res_json.getString("message"));
                        grade_desc.setText(res_json.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG_DIALOG, "Success upload operation");
                Toast.makeText(this, "Success upload image", Toast.LENGTH_LONG);
            }else {
                Log.e(TAG_DIALOG, "Unsuccessfull upload :(");
            }

        }else if (requestCode == DOCUMENT_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri doc = data.getData();
            // Extract first page from pdf
            item_create.setEnabled(true);
            try {
                ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(doc, "r");
                PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);

                PdfRenderer.Page page = pdfRenderer.openPage(0);
                // Render the page as a bitmap
                int width = getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
                int height = getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();

                Bitmap bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
//                for (int x = 0; x < width; x++) {
//                    for (int y = 0; y < height; y++) {
//                        bitmap.setPixel(x, y, Color.TRANSPARENT);
//                    }
//                }
                document_prev = bitmap;
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                //Close the page and the renderer
                page.close();
                pdfRenderer.close();
                fileDescriptor.close();

                picked_document.setImageBitmap(bitmap);

                InputStream inputStream = getContentResolver().openInputStream(doc);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                byte [] buffer = new byte[1024];
                int lenght;

                while ((lenght = inputStream.read(buffer)) > - 1) baos.write(buffer, 0 ,lenght);
                baos.flush();

                document = baos.toByteArray();

            }catch (FileNotFoundException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }

        }
    }
}