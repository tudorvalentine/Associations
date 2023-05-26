package com.tudorvalentine.augmentedimages;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.tudorvalentine.augmentedimages.app.AppConfig;
import com.tudorvalentine.augmentedimages.app.AppController;
import com.tudorvalentine.augmentedimages.associationslist.Association;
import com.tudorvalentine.augmentedimages.helpers.SQLiteHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoadActivity extends Activity {
    private SQLiteHandler db;
    private final String TAG = LoadActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new SQLiteHandler(getApplicationContext());
        setContentView(R.layout.load_activity);
        View gradientView = findViewById(R.id.gradient_view);
        ImageView imageView = findViewById(R.id.image_rotation);

        ObjectAnimator image_rotate = ObjectAnimator.ofFloat(imageView, "rotation", 0.0f, 360f);
        image_rotate.setDuration(2000);
        image_rotate.setRepeatCount(5);
        image_rotate.start();

        ObjectAnimator animator1 = ObjectAnimator.ofInt(gradientView, "backgroundColor", Color.RED, Color.GREEN);
        animator1.setDuration(4500);
        animator1.setEvaluator(new ArgbEvaluator());


        ObjectAnimator animator2 = ObjectAnimator.ofInt(gradientView, "backgroundColor", Color.GREEN, Color.RED);
        animator2.setDuration(4500);
        animator2.setEvaluator(new ArgbEvaluator());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(animator1, animator2);
        animatorSet.start();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        AppController.getInstance().downloadFile(AppConfig.URL_GET_IMGDB, "imgdb.imgdb", db.getUserDetails().get("username"));
        String id_user = db.getUserDetails().get("id");
        String req_tag = "sync_request";
        String url = AppConfig.URL_SYNC + id_user;
        Log.d(TAG,"URL >> " + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_SYNC + id_user, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG,"Response JSON >> " + response.toString());
                db.deleteRowsAssoc();
                JSONArray jsonArray = null;
                try {
                    jsonArray = response.getJSONArray("rows");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int id_assoc = jsonObject.getInt("id_assoc");
                        String image_name = jsonObject.getString("image_name");
                        String doc_name = jsonObject.getString("document_name");
                        String doc_prev = jsonObject.getString("document_preview");

                        db.addAssociation(new Association(id_assoc,image_name, doc_name, doc_prev));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Syncronize error : " + error.getMessage());
                error.printStackTrace();
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjectRequest,req_tag);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(LoadActivity.this, EntryPointActivity.class));
                finish();
            }

        }, 1000);

    }
}