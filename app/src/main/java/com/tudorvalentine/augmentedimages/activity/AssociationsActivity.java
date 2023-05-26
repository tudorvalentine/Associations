package com.tudorvalentine.augmentedimages.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.tudorvalentine.augmentedimages.R;
import com.tudorvalentine.augmentedimages.app.AppConfig;
import com.tudorvalentine.augmentedimages.app.AppController;
import com.tudorvalentine.augmentedimages.associationslist.Association;
import com.tudorvalentine.augmentedimages.associationslist.AssociationAdapter;
import com.tudorvalentine.augmentedimages.helpers.SQLiteHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssociationsActivity extends FragmentActivity {
    private final String TAG = AssociationsActivity.class.getSimpleName();
    private SQLiteHandler db;
    private ArrayList<Association> associations = new ArrayList<>();
    private List<Map<String, String>> user_assoc;
    private Paint mClearPaint;
    private ColorDrawable mBackground;
    private int backgroundColor;
    private Drawable deleteDrawable;
    private int intrinsicWidth;
    private int intrinsicHeight;
    private CoordinatorLayout coordinatorLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.associations_activity);
        coordinatorLayout = findViewById(R.id.coordinator_assoc);
        mBackground = new ColorDrawable();
        backgroundColor = Color.parseColor("#9c0c0c");
        mClearPaint = new Paint();
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        deleteDrawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.icons8_trash_can_500);
        intrinsicWidth = deleteDrawable.getIntrinsicWidth();
        intrinsicHeight = deleteDrawable.getIntrinsicHeight();

        db = new SQLiteHandler(getApplicationContext());
        HashMap<String,String> user = db.getUserDetails();
        user_assoc = db.getUserData();
        TextView username = findViewById(R.id.username_associations);
        TextView isEmpty = findViewById(R.id.isEmpty);
        username.setText(user.get("username"));
        ExtendedFloatingActionButton createAssoc = findViewById(R.id.floating_action_button);

        createAssoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AssociationsActivity.this, UploadActivity.class);
                startActivity(intent);
            }
        });
        for (Map<String, String> row : user_assoc) {
            associations.add(new Association(row.get("image"),row.get("document"),row.get("document_prev") ,R.drawable.ic_settings_ethernet_24, user.get("username")));
        }
        if (associations.isEmpty())
            isEmpty.setText("Nu aveți nici o asociere la moment.");
        else
            isEmpty.setText("");
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.CYAN, Color.YELLOW);

        RecyclerView recyclerView = findViewById(R.id.list);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setDrawingCacheEnabled(true);
        AssociationAdapter associationAdapter = new AssociationAdapter(getLayoutInflater(), associations);
        recyclerView.setAdapter(associationAdapter);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                String id_user = user.get("id");
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
                        AppController.getInstance().downloadFile(AppConfig.URL_GET_IMGDB, "imgdb.imgdb", user.get("username"));
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Syncronize error : " + error.getMessage());
                        error.printStackTrace();
                    }
                });
                AppController.getInstance().addToRequestQueue(jsonObjectRequest,req_tag);
                user_assoc = db.getUserData();
                ArrayList<Association> newAssociations = new ArrayList<>();
                associations.clear();
                for (Map<String, String> row : user_assoc) {
                    associations.add(new Association(row.get("image"),row.get("document"),row.get("document_prev") ,R.drawable.ic_settings_ethernet_24, user.get("username")));
                }
                if (associations.isEmpty())
                    isEmpty.setText("Nu aveți nici o asociere la moment.");
                else
                    isEmpty.setText("");
                associationAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
            }
        });

        ItemTouchHelper.SimpleCallback itemTouchHelper = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position  = viewHolder.getAdapterPosition();
                Association item = associations.get(position);
                associations.remove(position);
                associationAdapter.notifyItemRemoved(position);
                db.deleteRowAssoc(item.getImage_name());
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.DELETE,
                        AppConfig.URL_DELETE_ASSOC + item.getImage_name() + "/" + user.get("username"),
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG,"Response JSON >> " + response.toString());
                                try {
                                    Log.e(TAG , response.getString("msg"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Delete assoc error : " + error.getMessage());
                        error.printStackTrace();
                    }
                });
                AppController.getInstance().addToRequestQueue(jsonObjectRequest, "delete_assoc");
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Item was removed from the list.", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        associationAdapter.restoreItem(item, position);
                        recyclerView.scrollToPosition(position);
                        db.addAssociation(item);
                        final JSONObject jsonFormatRequest = new JSONObject();
                        try {
                            jsonFormatRequest.put("image_name", item.getImage_name());
                            jsonFormatRequest.put("document_name", item.getDoc_name());
                            jsonFormatRequest.put("document_preview", item.getDoc_prev());
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                Request.Method.POST,
                                AppConfig.URL_RESTORE_ASSOC + user.get("username"),
                                jsonFormatRequest,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Log.d(TAG,"Response JSON >> " + response.toString());
                                        try {
                                            Log.e(TAG , response.getString("msg"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, "Undo deleted assoc error : " + error.getMessage());
                                error.printStackTrace();
                            }
                        });
                        AppController.getInstance().addToRequestQueue(jsonObjectRequest, "restore_assoc");
                    }
                });
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;
                int itemHeight = itemView.getHeight();

                boolean isCancelled = dX == 0 && !isCurrentlyActive;

                if (isCancelled) {
                    clearCanvas(c, itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    return;
                }

                mBackground.setColor(backgroundColor);
                mBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop() + 20, itemView.getRight(), itemView.getBottom() - 20);
                mBackground.draw(c);

                int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int deleteIconMargin = (itemHeight - intrinsicHeight) / 2;
                int deleteIconLeft = itemView.getRight() + 150 - deleteIconMargin - intrinsicWidth;
                int deleteIconRight = itemView.getRight() + 150 - deleteIconMargin;
                int deleteIconBottom = deleteIconTop + intrinsicHeight;

                deleteDrawable.setBounds(deleteIconLeft , deleteIconTop, deleteIconRight, deleteIconBottom);
                deleteDrawable.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 0.55f;
            }
        };
        ItemTouchHelper itemTouchHelper1 = new ItemTouchHelper(itemTouchHelper);
        itemTouchHelper1.attachToRecyclerView(recyclerView);
    }
    private void clearCanvas(Canvas c, Float left, Float top, Float right, Float bottom) {
        c.drawRect(left, top, right, bottom, mClearPaint);

    }
}
