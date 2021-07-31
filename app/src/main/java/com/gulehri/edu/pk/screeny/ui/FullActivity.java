package com.gulehri.edu.pk.screeny.ui;

import static android.app.DownloadManager.Request.NETWORK_MOBILE;
import static android.app.DownloadManager.Request.NETWORK_WIFI;
import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;

import android.Manifest;
import android.app.AlertDialog;
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
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
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
import com.gulehri.edu.pk.screeny.R;
import com.gulehri.edu.pk.screeny.databinding.ActivityFullBinding;

import dmax.dialog.SpotsDialog;

public class FullActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 4213;
    private ActivityFullBinding binding;
    private String url;
    private String imageUrl;
    private String imageName;
    private AlertDialog builder;
    private final String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getData();
        setImageName();
        hideButton();
        setImageToView();
        setListener();

    }

    private void hideButton() {
        binding.btnDownload.setVisibility(View.GONE);
        binding.setWallpaper.setVisibility(View.GONE);
    }

    private void getData() {
        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        imageUrl = intent.getStringExtra("imageUrl");
    }

    private void setImageName() {

        imageName = imageUrl.substring(29);
        imageName = imageName.replaceAll("/", "");
        imageName = imageName.replaceAll("\\d", "");


    }

    private void setImageToView() {
        builder = new SpotsDialog.Builder()
                .setCancelable(false)
                .setContext(this)
                .setMessage("Loading Image...")
                .setTheme(R.style.Custom)
                .build();
        builder.show();
        Glide.with(FullActivity.this).load(url).dontAnimate().listener(
                new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        builder.hide();
                        hideButton();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        builder.hide();
                        binding.setWallpaper.setVisibility(View.VISIBLE);
                        binding.btnDownload.setVisibility(View.VISIBLE);
                        return false;
                    }
                }
        ).into(binding.photoView);
    }

    private void setListener() {
        binding.btnDownload.setOnClickListener(this);
        binding.setWallpaper.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_download) {
            if (haveNetworkConnection()) {
                askPermission();

                if (builder.isShowing()){
                    builder.hide();
                    builder.dismiss();
                }

            } else {
                Toast.makeText(this, "No Connection", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.setWallpaper) {
            putWallpaper();
        }
    }

    private void putWallpaper() {
        WallpaperManager manager = (WallpaperManager) this.getSystemService(WALLPAPER_SERVICE);
        Bitmap bitmap = ((BitmapDrawable) binding.photoView.getDrawable()).getBitmap();

        try {
            manager.setBitmap(bitmap);
            Toast.makeText(getApplicationContext(), "Wallpaper Changed", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void askPermission() {
        if (ContextCompat.checkSelfPermission(this, perms[0]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, perms, REQUEST_CODE);
        } else {
            downloadImage();
        }
    }

    private void downloadImage() {
        Uri uri = Uri.parse(url);
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(NETWORK_WIFI | NETWORK_MOBILE);
        request.setTitle(imageName + ".jpeg");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, imageName + ".jpeg");
        request.setMimeType("image/*");
        downloadManager.enqueue(request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults) {
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


}