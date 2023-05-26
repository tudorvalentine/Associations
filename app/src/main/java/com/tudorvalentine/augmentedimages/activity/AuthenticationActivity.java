package com.tudorvalentine.augmentedimages.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.textfield.TextInputEditText;
import com.tudorvalentine.augmentedimages.ActionActivity;
import com.tudorvalentine.augmentedimages.R;
import com.tudorvalentine.augmentedimages.app.AppConfig;
import com.tudorvalentine.augmentedimages.app.AppController;
import com.tudorvalentine.augmentedimages.associationslist.Association;
import com.tudorvalentine.augmentedimages.helpers.SQLiteHandler;
import com.tudorvalentine.augmentedimages.helpers.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AuthenticationActivity extends Activity {
    private static final String TAG = AuthenticationActivity.class.getSimpleName();

    private Button btnSign;
    private Button btn_create_account;
    private TextInputEditText inputLogin;
    private TextInputEditText inputPass;

    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler SQLiteDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth);
        btn_create_account = (Button) findViewById(R.id.btn_create_account);
        btnSign = (Button) findViewById(R.id.btn_signin_auth);
        inputLogin = (TextInputEditText) findViewById(R.id.input_login_auth);
        inputPass = (TextInputEditText) findViewById(R.id.input_pass_auth);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        SQLiteDatabase = new SQLiteHandler(getApplicationContext());

        session = new SessionManager(getApplicationContext());
        View.OnClickListener signin = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = inputLogin.getText().toString().trim();
                String pass = inputPass.getText().toString().trim();

                if(!username.isEmpty() && !pass.isEmpty()){
                    checkLogin(username,pass);

                }else{
                    Toast.makeText(AuthenticationActivity.this, "Introduceti datele de intrare", Toast.LENGTH_LONG).show();
                }
            }
        };
        btnSign.setOnClickListener(signin);

        View.OnClickListener createAcc = new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent forwardToRegistrationActivity = new Intent(getApplicationContext(), RegistrationActivity.class);
                startActivity(forwardToRegistrationActivity);
                finish();
            }
        };
        btn_create_account.setOnClickListener(createAcc);


    }
    private void checkLogin(String username, String password){
        final JSONObject jsonFormatRequest = new JSONObject();
        try {
            jsonFormatRequest.put("username",username);
            jsonFormatRequest.put("password", password);
        }catch (JSONException e){
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        String tag_req = "request_to_login";

        pDialog.setMessage("Verificăm . . .");
        showDialog();
        JsonObjectRequest JsonReq = new JsonObjectRequest(Request.Method.POST,
                AppConfig.URL_AUTH,jsonFormatRequest, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                pDialog.setMessage("Download user data . . .");
                Log.d(TAG, "Response > " + response.toString());
                try {
                    boolean error = response.getBoolean("error");
                    if (!error){
                        session.setLogin(true);
                        int id_user = response.getInt("id_user");
                        String username = response.getString("username");
                        String email = response.getString("email");
                        JSONArray jsonArrayAssoc = response.getJSONArray("association");
                        SQLiteDatabase.addUser(id_user,username,email);

                        if (jsonArrayAssoc.length() > 0){
                            showDialog();
                            for (int i = 0; i < jsonArrayAssoc.length(); i++) {
                                JSONObject assocRow = jsonArrayAssoc.getJSONObject(i);
                                int id_assoc = assocRow.getInt("id_assoc");
                                String image_name = assocRow.getString("image_name");
                                String document_name = assocRow.getString("document_name");
                                String document_prev = assocRow.getString("document_preview");
                                SQLiteDatabase.addAssociation(new Association(id_assoc,image_name, document_name, document_prev));
                            }
                            AppController.getInstance().downloadFile(AppConfig.URL_GET_IMGDB,"imgdb.imgdb", username);
                            hideDialog();
                        }
                        Toast.makeText(getApplicationContext(),"Welcome !", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(AuthenticationActivity.this, ActionActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(getApplicationContext(),"Unable to login", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
        , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG,"Login Error: " + error.getMessage());
                Toast.makeText(AuthenticationActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                hideDialog();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> param = new HashMap<String,String>();
                param.put("Content-Type", "application/json");
                return param;
            }
        };
        AppController.getInstance().addToRequestQueue(JsonReq,tag_req);
    }
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }
    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}