package com.tudorvalentine.augmentedimages;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.tudorvalentine.augmentedimages.activity.AuthenticationActivity;
import com.tudorvalentine.augmentedimages.activity.RegistrationActivity;
import com.tudorvalentine.augmentedimages.helpers.SessionManager;

public class EntryPointActivity extends AppCompatActivity {
    private SessionManager session;

    Button btn_authentication , btn_registration;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(getApplicationContext());

        if(session.isLoggedIn()){
            Intent intent = new Intent(this, ActionActivity.class);
            startActivity(intent);
            finish();
        }else{
            setContentView(R.layout.auth_reg);

            btn_authentication = findViewById(R.id.btn_sign_in);
            btn_registration = findViewById(R.id.btn_sign_up);

            View.OnClickListener authClick = new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Intent authIntent = new Intent(getApplicationContext(), AuthenticationActivity.class);
                    startActivity(authIntent);
                }
            };
            View.OnClickListener regClick = new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Intent regAuth = new Intent(getApplicationContext(), RegistrationActivity.class);
                    startActivity(regAuth);
                }
            };
            btn_authentication.setOnClickListener(authClick);
            btn_registration.setOnClickListener(regClick);
        }
    }
}
