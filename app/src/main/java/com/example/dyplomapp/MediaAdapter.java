package com.example.dyplomapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder>{
    interface OnMediaClickListener {
        void onMediaClick(WorkDoc workDoc);
    }

    private final MediaAdapter.OnMediaClickListener onClickListener;

    private final LayoutInflater inflater;
    private final List<WorkDoc> docs;

    MediaAdapter(Context context, List<WorkDoc> docs, MediaAdapter.OnMediaClickListener onClickListener) {
        this.onClickListener = onClickListener;
        this.docs = docs;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public MediaAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.doc_item, parent, false);
        return new MediaAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MediaAdapter.ViewHolder holder, int position) {

        WorkDoc workDoc = docs.get(position);

        holder.docname.setText(workDoc.getName());
        holder.type = workDoc.getType();
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if(!holder.type.equals(MediaType.TXT)) onClickListener.onMediaClick(workDoc);
                else ;
            }
        });
    }

    @Override
    public int getItemCount() {
        return docs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView docname;
        MediaType type;
        ViewHolder(View view){
            super(view);
            docname = (TextView)view.findViewById(R.id.docname);
        }
    }
}
