package com.gulehri.edu.pk.screeny.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
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
import com.gulehri.edu.pk.screeny.databinding.DialogViewBinding;

import java.util.Objects;

import dmax.dialog.SpotsDialog;

import static android.app.DownloadManager.Request.NETWORK_MOBILE;
import static android.app.DownloadManager.Request.NETWORK_WIFI;
import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;

public class FullActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 4213;
    private ActivityFullBinding binding;
    private String url;
    private String photographer;
    private String photographerUrl;
    private String imageUrl;
    private String imageName;
    private final String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();

        getData();
        setImageName();
        setImageToView();
        setListener();

    }

    private void getData() {
        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        photographer = intent.getStringExtra("photographer");
        photographerUrl = intent.getStringExtra("photographerUrl");
        imageUrl = intent.getStringExtra("imageUrl");
    }

    private void setImageName() {

        imageName = imageUrl.substring(29);
        imageName = imageName.replaceAll("/", "");
        imageName = imageName.replaceAll("\\d", "");


    }

    private void setImageToView() {
        AlertDialog builder = new SpotsDialog.Builder()
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
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        builder.hide();
                        return false;
                    }
                }
        ).into(binding.photoView);
    }

    private void setListener() {
        binding.btnDownload.setOnClickListener(this);
        binding.btnInfo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_info) {
            viewDialog();
        } else if (id == R.id.btn_download) {
            if (haveNetworkConnection()) {
                askPermission();
            } else {
                Toast.makeText(this, "No Connection", Toast.LENGTH_SHORT).show();
            }

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

    @SuppressLint("SetTextI18n")
    private void viewDialog() {
        Dialog dialog = new Dialog(this);
        DialogViewBinding binding = DialogViewBinding.inflate(LayoutInflater.from(this));
        dialog.setContentView(binding.getRoot());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        binding.imageUrl.setText("Image Url: " + url);
        binding.photographer.setText("Photographer Name: " + photographer);
        binding.photographerUrl.setText("Profile: " + photographerUrl);
        binding.imageName.setText("Image Name: " + imageName);

        binding.btnYes.setOnClickListener(v -> {
            dialog.hide();
            dialog.dismiss();
        });

        dialog.create();
        dialog.show();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}