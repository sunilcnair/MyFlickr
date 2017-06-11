package com.tigerspike.assignment.flickr.activity;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import com.tigerspike.assignment.flickr.R;
import com.tigerspike.assignment.flickr.adapter.GalleryAdapter;
import com.tigerspike.assignment.flickr.app.AppController;
import com.tigerspike.assignment.flickr.model.Image;


/**
 * Created by Sunil C
 * This he the main activity of the application.
 * Download the json by making volley http request. fetchImages() method is used for this purpose
 * Parse the json and add the object models to array list.
 * Pass the array list to recyclerView’s adapter class.
 *
 */

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private static final String endpoint = "https://api.flickr.com/services/feeds/photos_public.gne?format=json";
    private ArrayList<Image> images;
    private ProgressDialog pDialog;
    private GalleryAdapter mAdapter;
    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        pDialog = new ProgressDialog(this);
        images = new ArrayList<>();
        mAdapter = new GalleryAdapter(getApplicationContext(), images);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

         recyclerView.addOnItemTouchListener(new GalleryAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView, new GalleryAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("images", images);
                bundle.putInt("position", position);

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                SlideshowDialogFragment newFragment = SlideshowDialogFragment.newInstance();
                newFragment.setArguments(bundle);
                newFragment.show(ft, "slideshow");
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        fetchImages();

    }

    private void fetchImages() {

        pDialog.setMessage("Downloading json...");
        pDialog.show();


        StringRequest req = new StringRequest(Request.Method.GET,
                endpoint, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject obj = new JSONObject(response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1));
                    JSONArray users = obj.getJSONArray("items");
                    pDialog.dismiss();
                    images.clear();
                    for (int i = 0; i < users.length(); i++) {
                        try {
                            JSONObject object = users.getJSONObject(i);
                            Image image = new Image();
                            image.setName(object.getString("title"));

                            JSONObject url = object.getJSONObject("media");
                            image.setSmall(url.getString("m"));
                            Log.e(TAG, "Image URL: " + url.getString("m"));
                            image.setMedium(url.getString("m"));
                            image.setLarge(url.getString("m"));
                            image.setTimestamp(object.getString("date_taken"));

                            images.add(image);

                        } catch (JSONException e) {
                            Log.e(TAG, "Json parsing error: " + e.getMessage());
                            pDialog.dismiss();
                        }
                    }
                    Log.d(TAG, users.toString());

            } catch (JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    pDialog.dismiss();
            }

                mAdapter.notifyDataSetChanged();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.getMessage());
                pDialog.dismiss();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);
    }

}