package com.tudorvalentine.augmentedimages.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import com.tudorvalentine.augmentedimages.helpers.SQLiteHandler;
import com.tudorvalentine.augmentedimages.helpers.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends Activity {
    private static final String TAG = RegistrationActivity.class.getSimpleName();

    private TextInputEditText inputUsername;
    private TextInputEditText inputEmail;
    private TextInputEditText inputPass;
    private Button btnSignup;

    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reg);

        btnSignup = (Button) findViewById(R.id.btn_sign_up_reg);
        inputUsername = findViewById(R.id.input_login_reg);
        inputEmail = findViewById(R.id.input_email_reg);
        inputPass = findViewById(R.id.input_pass_reg);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        session = new SessionManager(getApplicationContext());

        database = new SQLiteHandler(getApplicationContext());
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(RegistrationActivity.this,
                    ActionActivity.class);
            startActivity(intent);
            finish();
        }
        View.OnClickListener signup = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String username = inputUsername.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String pass = inputPass.getText().toString().trim();

                if (!username.isEmpty() && !email.isEmpty() && !pass.isEmpty()){
                    registerUser(username,email,pass);
                }else {
                    Toast.makeText(RegistrationActivity.this, "Completati toate campurile", Toast.LENGTH_SHORT).show();
                }
            }
        };
        btnSignup.setOnClickListener(signup);
    }
    private void registerUser(String username, String email, String pass){
        String tag_req = "request_register";
        JSONObject credentialJson = new JSONObject();
        try {
            credentialJson.put("username", username);
            credentialJson.put("email", email);
            credentialJson.put("password", pass);
        }catch (JSONException exception){
            exception.printStackTrace();
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show();
        }
        pDialog.setMessage("Vă înregistrăm . . .");
        showDialog();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, credentialJson, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG,"Register Response: " + response.toString());
                hideDialog();
                try {
                    boolean error = response.getBoolean("error");
                    if (!error){
                        int id = response.getInt("id_user");
                        String username = response.getString("username");
                        String email = response.getString("email");
                        database.addUser(id,username,email);
                        Toast.makeText(RegistrationActivity.this, "Înregistrarea a delurat cu succes !", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(getApplicationContext(), AuthenticationActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        String errMsg = response.getString("error_msg");
                        Toast.makeText(RegistrationActivity.this, errMsg, Toast.LENGTH_LONG).show();
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(RegistrationActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,"Registration Error: " + error.getMessage());
                    Toast.makeText(RegistrationActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
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
        AppController.getInstance().addToRequestQueue(jsonObjectRequest,tag_req);
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
