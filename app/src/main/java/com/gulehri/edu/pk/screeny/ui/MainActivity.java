package com.gulehri.edu.pk.screeny.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
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
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityMainBinding binding;
    private List<Model> wallpaperList;
    private WallpaperAdapter adapter;
    private int pageNumber = 1;
    private boolean flag;
    private boolean searchFlag;
    private String searchedText;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MobileAds.initialize(MainActivity.this, initializationStatus -> {
        });

        askPermissions();
        setAdapter();
        fetchValue();
        setListeners();
        setToolbar();

        //Loading Adds
        AdRequest adRequest = new AdRequest.Builder().build();
        binding.adView.loadAd(adRequest);

    }

    private void fetchValue() {
        SharedPreferences sharedPreferences = getPreferences(0);
        pageNumber = sharedPreferences.getInt("page",1);

    }

    private void setToolbar() {

        setSupportActionBar(binding.tBarMain.toolBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        binding.tBarMain.toolbarText.setText(R.string.app_name);

    }

    private void setListeners() {
        binding.btnPre.setOnClickListener(this);
        binding.btnNext.setOnClickListener(this);
    }

    private void fetchWallpapers(String wUrl) {
        if (haveConnection()) {

            //&per_page=80  == Show 80 images per pages
            String url = wUrl + "&per_page=80";
            StringRequest request = new StringRequest(Request.Method.GET,
                    url,
                    response -> {
                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray photos = jsonObject.getJSONArray("photos");
                            int len = photos.length();

                            for (int i = 0; i < len; i++) {

                                final JSONObject object = photos.getJSONObject(i);
                                final int id = object.getInt("id");
                                final String imageUrl = object.getString("url");
                                JSONObject src = object.getJSONObject("src");
                                final String originalUrl = src.getString("large2x");
                                final String mediumUrl = src.getString("medium");

                                Model model = new Model(id, imageUrl,originalUrl, mediumUrl);
                                wallpaperList.add(model);
                            }
                            adapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, error -> {
            }) {

                //Setting Header and Passing Over API Key without that API request won't work
                @Override
                public Map<String, String> getHeaders() {
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
        binding.preList.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new WallpaperAdapter(wallpaperList);
        binding.preList.setAdapter(adapter);
    }

    private void askPermissions() {
        if (haveConnection()) {
            binding.btnPre.setVisibility(View.VISIBLE);
            binding.btnNext.setVisibility(View.VISIBLE);
            fetchWallpapers("https://api.pexels.com/v1/curated/?page=" + pageNumber);
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

    @SuppressLint("NotifyDataSetChanged")
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
                        if (searchFlag) {
                            String url = "https://api.pexels.com/v1/search?query=" + searchedText + "&page=" + pageNumber;
                            fetchWallpapers(url);
                        } else {
                            fetchWallpapers("https://api.pexels.com/v1/curated/?page=" + pageNumber);
                        }


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
                    if (searchFlag) {
                        String url = "https://api.pexels.com/v1/search?query=" + searchedText + "&page=" + pageNumber;
                        fetchWallpapers(url);
                    } else {
                        fetchWallpapers("https://api.pexels.com/v1/curated/?page=" + pageNumber);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit = edit.putInt("page", pageNumber);
        edit.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.itemSearch);
        searchView = (SearchView) item.getActionView();
        searchView.setQueryHint("Search Wallpaper");

        //Changing Search Icon Color
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_button);
        searchIcon.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public boolean onQueryTextSubmit(String query) {
                wallpaperList.clear();
                adapter.notifyDataSetChanged();
                searchedText = query.trim();
                String url = "https://api.pexels.com/v1/search?query=" + query.trim() + "&page=" + pageNumber;
                fetchWallpapers(url);
                searchFlag = true;
                hideKeyboard(MainActivity.this);
                return true;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnCloseListener(() -> {
            searchFlag = false;

            //Hide SearchView
            searchView.onActionViewCollapsed();
            pageNumber = 0;
            return true;
        });

        return super.onCreateOptionsMenu(menu);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        if (flag) {
            super.onBackPressed();
            return;
        }

        this.flag = true;
        Toast.makeText(this, "Click again to exit", Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> flag = false, 2000);

    }

}