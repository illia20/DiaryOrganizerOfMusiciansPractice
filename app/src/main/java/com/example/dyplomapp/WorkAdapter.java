package com.example.dyplomapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WorkAdapter extends RecyclerView.Adapter<WorkAdapter.ViewHolder>{

    RecyclerViewInterface recyclerViewInterface;

    interface OnWorkClickListener {
        void onWorkClick(Work work);
    }

    private final OnWorkClickListener onClickListener;

    private final LayoutInflater inflater;
    private final List<Work> works;

    WorkAdapter(Context context, List<Work> works, OnWorkClickListener onClickListener, RecyclerViewInterface recyclerViewInterface) {
        this.onClickListener = onClickListener;
        this.works = works;
        this.inflater = LayoutInflater.from(context);
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.work_item, parent, false);
        return new ViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Work work = works.get(position);

        holder.nameView.setText(work.getName());
        holder.statusView.setText(Integer.toString(work.getStatus()));
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                onClickListener.onWorkClick(work);
            }
        });
    }

    @Override
    public int getItemCount() {
        return works.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameView, statusView;
        ViewHolder(View view, RecyclerViewInterface recyclerViewInterface){
            super(view);
            nameView = (TextView)view.findViewById(R.id.workname);
            statusView = (TextView) view.findViewById(R.id.workstatus);

            view.setOnLongClickListener(new View.OnLongClickListener() {
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
}
