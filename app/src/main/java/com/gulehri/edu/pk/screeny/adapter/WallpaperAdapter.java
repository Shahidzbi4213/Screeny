package com.gulehri.edu.pk.screeny.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.gulehri.edu.pk.screeny.R;
import com.gulehri.edu.pk.screeny.databinding.ImageItemBinding;
import com.gulehri.edu.pk.screeny.model.Model;
import com.gulehri.edu.pk.screeny.ui.FullActivity;

import java.util.List;

public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.PicsHolder> {
    private Context mContext;
    private final List<Model> wallpaperList;


    public WallpaperAdapter(List<Model> wallpaperList) {
        this.wallpaperList = wallpaperList;
    }

    @NonNull
    @Override
    public PicsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        return new PicsHolder(ImageItemBinding.inflate(LayoutInflater.from(mContext), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull WallpaperAdapter.PicsHolder holder, int position) {

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.loading)
                .error(R.drawable.close)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)
                .dontAnimate()
                .dontTransform();

        Glide.with(mContext).load(wallpaperList.get(position).getMediumUrl()).apply(options).into(holder.binding.imageView);


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, FullActivity.class);
            intent.putExtra("url", wallpaperList.get(position).getOriginalUrl());
            intent.putExtra("photographer", wallpaperList.get(position).getPhotographer());
            intent.putExtra("photographerUrl", wallpaperList.get(position).getPhotographerUrl());
            intent.putExtra("imageUrl", wallpaperList.get(position).getImageUrl());
            mContext.startActivity(intent);
        });

    }


    @Override
    public int getItemCount() {
        return wallpaperList.size();
    }

    public static class PicsHolder extends RecyclerView.ViewHolder {
        private final ImageItemBinding binding;

        public PicsHolder(@NonNull ImageItemBinding itemBinding) {
            super(itemBinding.getRoot());
            binding = itemBinding;

        }
    }
}
