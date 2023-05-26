package com.tudorvalentine.augmentedimages;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;

import com.tudorvalentine.augmentedimages.activity.AssociationsActivity;
import com.tudorvalentine.augmentedimages.activity.AugmentedImageActivity;
import com.tudorvalentine.augmentedimages.activity.AuthenticationActivity;
import com.tudorvalentine.augmentedimages.helpers.SQLiteHandler;
import com.tudorvalentine.augmentedimages.helpers.SessionManager;

import java.util.HashMap;

public class ActionActivity extends FragmentActivity {
    private static final String TAG = ActionActivity.class.getSimpleName();
    private TextView txtUsername;
    private ImageButton btnLogout;

    private CardView cardCamera;
    private CardView cardAssociations;

    private SQLiteHandler db;
    private SessionManager session;

    private ProgressDialog pDialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.action_activity);

        txtUsername = findViewById(R.id.username_associations);
        btnLogout = findViewById(R.id.btnLogout);

        cardAssociations = findViewById(R.id.card_asoc);
        cardCamera = findViewById(R.id.card_camera);

        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());
        pDialog = new ProgressDialog(this);

        if (!session.isLoggedIn()){
            logoutUser();
        }
        HashMap<String,String> user = db.getUserDetails();
        Log.d(TAG, "Id_user: " + user.get("id"));
        String username = user.get("username");

        txtUsername.setText(username);
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        cardAssociations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActionActivity.this, AssociationsActivity.class);
                startActivity(intent);
            }
        });
        cardCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActionActivity.this, AugmentedImageActivity.class);
                startActivity(intent);
            }
        });
    }

    private void logoutUser() {
        session.setLogin(false);

        db.deleteUserAndData();

        Intent intent = new Intent(ActionActivity.this, AuthenticationActivity.class);
        startActivity(intent);
        finish();
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
