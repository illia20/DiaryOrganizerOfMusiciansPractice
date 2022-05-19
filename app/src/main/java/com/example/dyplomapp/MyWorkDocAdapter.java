package com.example.dyplomapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.List;

public class MyWorkDocAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    final int textModeVar = 0, mediaModeVar = 1, pdfModeVar = 2, ytModeVar = 3;
    final RecyclerViewInterface recyclerViewInterface;

    List<WorkDoc> listItems;
    Context context;
    public ExoPlayer player;
    Lifecycle lifecycle;

    public MyWorkDocAdapter(List<WorkDoc> listItems, Context context, Lifecycle lifecycle, RecyclerViewInterface recyclerViewInterface) {
        this.listItems = listItems;
        this.context = context;
        this.lifecycle = lifecycle;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @Override
    public int getItemViewType(int position){
        if(listItems.get(position).getType() == MediaType.TXT) return textModeVar;
        else if(listItems.get(position).getType() == MediaType.PDF) return pdfModeVar;
        else if(listItems.get(position).getType() == MediaType.YT) return ytModeVar;
        else return mediaModeVar;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View modelView;
        RecyclerView.ViewHolder viewHolder = null;
        if(viewType == textModeVar){
            modelView = LayoutInflater.from(parent.getContext()).inflate(R.layout.materialtext, parent, false);
            viewHolder = new TextViewHolder(modelView);
        }
        else if(viewType == pdfModeVar){
            modelView = LayoutInflater.from(parent.getContext()).inflate(R.layout.materialpdf, parent, false);
            viewHolder = new MyWorkDocAdapter.PDFViewHolder(modelView);
        }
        else if(viewType == ytModeVar){
            YouTubePlayerView youTubePlayerView = (YouTubePlayerView) LayoutInflater.from(parent.getContext()).inflate(R.layout.materialyt, parent, false);
            lifecycle.addObserver(youTubePlayerView);

            viewHolder = new MyWorkDocAdapter.YTViewHolder(youTubePlayerView);
            return viewHolder;
        }
        else
        {
            modelView = LayoutInflater.from(parent.getContext()).inflate(R.layout.materialvideo, parent, false);
            viewHolder = new ExoPlayerViewHolder(modelView);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder.getItemViewType() == textModeVar){
            WorkDoc practiceMaterial = listItems.get(position);
            TextViewHolder textViewHolder = (TextViewHolder)holder;
            textViewHolder.infoTextView.setText(practiceMaterial.getName());
        }
        else if(holder.getItemViewType() == pdfModeVar){
            WorkDoc practiceMaterial = listItems.get(position);
            MyWorkDocAdapter.PDFViewHolder pdfViewHolder = (MyWorkDocAdapter.PDFViewHolder)holder;
            pdfViewHolder.titleTextView.setText(practiceMaterial.getName());
            pdfViewHolder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PDFWorkDocActivity.class);
                    intent.putExtra(WorkDoc.class.getSimpleName(), practiceMaterial);
                    context.startActivity(intent);
                }
            });
        }
        else if(holder.getItemViewType() == ytModeVar){
            WorkDoc workDoc = listItems.get(position);
            YTViewHolder ytViewHolder = (YTViewHolder)holder;

            String source = workDoc.getSource();
            String id = source.substring(source.indexOf("be/") + 3);

            ytViewHolder.cueVideo(id);
        }
        else {
            WorkDoc practiceMaterial = listItems.get(position);
            ExoPlayerViewHolder exoPlayerViewHolder = (ExoPlayerViewHolder)holder;
            exoPlayerViewHolder.titleTextView.setText(practiceMaterial.getName());
            player = new ExoPlayer.Builder(context).build();
            exoPlayerViewHolder.playerv.setPlayer(player);
            exoPlayerViewHolder.playerv.setControllerHideOnTouch(false);
            exoPlayerViewHolder.playerv.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
            exoPlayerViewHolder.playerv.setMinimumHeight(24);

            MediaItem mediaItem = MediaItem.fromUri(practiceMaterial.getSource());
            player.setMediaItem(mediaItem);
            player.prepare();
        }
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public class TextViewHolder extends RecyclerView.ViewHolder{
        TextView infoTextView;
        public TextViewHolder(@NonNull View itemView){
            super(itemView);
            infoTextView = itemView.findViewById(R.id.practiceMaterialText);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(recyclerViewInterface != null){
                        int pos = getAdapterPosition();

                        if(pos != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemLongClick(pos);
                        }
                    }

                    return true;
                }
            });
        }
    }

    public class ExoPlayerViewHolder extends RecyclerView.ViewHolder{
        TextView titleTextView;
        StyledPlayerView playerv;
        public ExoPlayerViewHolder(@NonNull View itemView){
            super(itemView);
            titleTextView = itemView.findViewById(R.id.materialvideoname);
            playerv = itemView.findViewById(R.id.playerv);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(recyclerViewInterface != null){
                        int pos = getAdapterPosition();

                        if(pos != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemLongClick(pos);
                        }
                    }

                    return true;
                }
            });
        }
    }

    public class PDFViewHolder extends RecyclerView.ViewHolder{
        TextView titleTextView;
        ImageButton button;

        public PDFViewHolder(@NonNull View itemView){
            super(itemView);
            titleTextView = itemView.findViewById(R.id.materialpdftitle);
            button = itemView.findViewById(R.id.pdfbutton);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(recyclerViewInterface != null){
                        int pos = getAdapterPosition();

                        if(pos != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemLongClick(pos);
                        }
                    }

                    return true;
                }
            });
        }
    }

    public class YTViewHolder extends RecyclerView.ViewHolder{
        YouTubePlayerView webView;
        YouTubePlayer youTubePlayer;
        String currentVideoId;

        public YTViewHolder(@NonNull YouTubePlayerView itemView){
            super(itemView);

            webView = itemView;

            webView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer initializedYouTubePlayer) {
                    youTubePlayer = initializedYouTubePlayer;
                    youTubePlayer.cueVideo(currentVideoId, 0);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(recyclerViewInterface != null){
                        int pos = getAdapterPosition();

                        if(pos != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemLongClick(pos);
                        }
                    }

                    return true;
                }
            });
        }

        void cueVideo(String videoId) {
            currentVideoId = videoId;

            if(youTubePlayer == null)
                return;

            youTubePlayer.cueVideo(videoId, 0);
        }
    }
}
