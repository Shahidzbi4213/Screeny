package com.gulehri.edu.pk.screeny.ui;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gulehri.edu.pk.screeny.R;
import com.gulehri.edu.pk.screeny.adapter.WallpaperAdapter;
import com.gulehri.edu.pk.screeny.databinding.ActivityMainBinding;
import com.gulehri.edu.pk.screeny.model.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityMainBinding binding;
    private List<Model> wallpaperList;
    private WallpaperAdapter adapter;
    private int pageNumber = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        askPermissions();
        setAdapter();
        fetchWallpapers();
        setListeners();
    }

    private void setListeners() {
        binding.btnPre.setOnClickListener(this);
        binding.btnNext.setOnClickListener(this);
    }

    private void fetchWallpapers() {
        if (haveConnection()) {
            String url = "https://api.pexels.com/v1/curated/?page=" + pageNumber + "&per_page=40";
            StringRequest request = new StringRequest(Request.Method.GET,
                    url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {

                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray photos = jsonObject.getJSONArray("photos");
                                int len = photos.length();

                                for (int i = 0; i < len; i++) {

                                    final JSONObject object = photos.getJSONObject(i);

                                    final int id = object.getInt("id");
                                    final String imageUrl = object.getString("url");
                                    final String photographer = object.getString("photographer");
                                    final String photographerUrl = object.getString("photographer_url");

                                    JSONObject src = object.getJSONObject("src");
                                    final String originalUrl = src.getString("original");
                                    final String mediumUrl = src.getString("medium");

                                    Model model = new Model(id, imageUrl, photographer, photographerUrl, originalUrl, mediumUrl);
                                    wallpaperList.add(model);
                                }
                                adapter.notifyDataSetChanged();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, error -> {
                Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
            }) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> map = new HashMap<>();
                    map.put("Authorization", "563492ad6f917000010000013c35869795db4034972b1408c54283c4");
                    return map;
                }
            };


            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(request);

        } else {
            Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show();
        }

    }

    private void setAdapter() {
        wallpaperList = new ArrayList<>();
        binding.preList.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new WallpaperAdapter(wallpaperList);
        binding.preList.setAdapter(adapter);
    }

    private void askPermissions() {
        if (haveConnection()) {
            binding.btnPre.setVisibility(View.VISIBLE);
            binding.btnNext.setVisibility(View.VISIBLE);
            fetchWallpapers();
        } else {
            binding.btnPre.setVisibility(View.GONE);
            binding.btnNext.setVisibility(View.GONE);
            Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show();
        }

    }


    private boolean haveConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        return (wifi != null && wifi.isConnected()) || (mobile != null && mobile.isConnected());
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (id == R.id.btn_pre) {
            try {
                if (pageNumber > 1) {
                    pageNumber--;
                    if (haveConnection()) {
                        wallpaperList.clear();
                        adapter.notifyDataSetChanged();
                        fetchWallpapers();

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (id == R.id.btn_next) {
            try {
                pageNumber++;
                if (haveConnection()) {
                    wallpaperList.clear();
                    adapter.notifyDataSetChanged();
                    fetchWallpapers();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}