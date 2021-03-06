package com.gulehri.edu.pk.screeny.ui;

import static android.app.DownloadManager.Request.NETWORK_MOBILE;
import static android.app.DownloadManager.Request.NETWORK_WIFI;
import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;

import android.Manifest;
import android.app.DownloadManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.gulehri.edu.pk.screeny.R;
import com.gulehri.edu.pk.screeny.databinding.ActivityFullBinding;

public class FullActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 4213;
    private ActivityFullBinding binding;
    private String url;
    private final String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullBinding.inflate(getLayoutInflater());

        //Hiding StatusBar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(binding.getRoot());

        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(FullActivity.this,
                "UNIT_ID",
                adRequest, new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        super.onAdLoaded(interstitialAd);
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        mInterstitialAd = null;
                    }
                });


        getData();
        hideButton();
        setImageToView();
        setListener();

    }

    private void hideButton() {
        binding.btnDownload.setVisibility(View.GONE);
        binding.setHomeWallpaper.setVisibility(View.GONE);
        binding.setLockWallpaper.setVisibility(View.GONE);
    }

    private void getData() {
        Intent intent = getIntent();
        url = intent.getStringExtra("url");
    }


    private void setImageToView() {

        Glide.with(FullActivity.this)
                .load(url)
                .placeholder(R.drawable.pp)
                .listener(
                        new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                hideButton();
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                binding.setLockWallpaper.setVisibility(View.VISIBLE);
                                binding.setHomeWallpaper.setVisibility(View.VISIBLE);
                                binding.btnDownload.setVisibility(View.VISIBLE);
                                return false;
                            }
                        }
                )
                .dontAnimate()
                .into(binding.photoView);
    }

    private void setListener() {
        binding.btnDownload.setOnClickListener(this);
        binding.setHomeWallpaper.setOnClickListener(this);
        binding.setLockWallpaper.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_download) {
            if (haveNetworkConnection()) {
                askPermission();
            } else {
                Toast.makeText(this, "No Connection", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.setHomeWallpaper) {
            putHomeWallpaper();
        } else if (id == R.id.setLockWallpaper) {
            putLockWallpaper();
        }
    }

    private void putLockWallpaper() {

        WallpaperManager manager = (WallpaperManager) this.getSystemService(WALLPAPER_SERVICE);
        Bitmap bitmap = ((BitmapDrawable) binding.photoView.getDrawable()).getBitmap();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
            } else {
                manager.setBitmap(bitmap);
            }
            Toast.makeText(getApplicationContext(), "Wallpaper Changed", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void putHomeWallpaper() {
        WallpaperManager manager = (WallpaperManager) this.getSystemService(WALLPAPER_SERVICE);
        Bitmap bitmap = ((BitmapDrawable) binding.photoView.getDrawable()).getBitmap();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM);
            } else {
                manager.setBitmap(bitmap);
            }
            Toast.makeText(getApplicationContext(), "Wallpaper Changed", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void askPermission() {
        if (ContextCompat.checkSelfPermission(this, perms[0]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, perms, REQUEST_CODE);
        } else {
            // Show status bar
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            downloadImage();
        }
    }

    private void downloadImage() {
        Uri uri = Uri.parse(url);
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(NETWORK_WIFI | NETWORK_MOBILE);
        request.setTitle(System.currentTimeMillis() + ".jpeg");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, System.currentTimeMillis() + ".jpeg");
        request.setMimeType("image/*");
        downloadManager.enqueue(request);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadImage();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
            }
        }

    }

    private boolean haveNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        return (wifi != null && wifi.isConnected()) || (mobile != null && mobile.isConnected());
    }



    @Override
    public void onBackPressed() {

        if (mInterstitialAd != null) {
            mInterstitialAd.show(FullActivity.this);

            //Calls when Add close button is clicked
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    FullActivity.super.onBackPressed();
                }
            });
        } else {
            super.onBackPressed();
        }

    }
}
