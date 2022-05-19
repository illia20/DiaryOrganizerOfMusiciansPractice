package com.example.dyplomapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PracticeAdapter  extends RecyclerView.Adapter<PracticeAdapter.ViewHolder>{
    interface OnPracticeClickListener {
        void onPracticeClick(Practice practice);
    }

    RecyclerViewInterface recyclerViewInterface;

    private final PracticeAdapter.OnPracticeClickListener onClickListener;

    private final LayoutInflater inflater;
    private final List<Practice> practices;

    PracticeAdapter(Context context, List<Practice> practices, PracticeAdapter.OnPracticeClickListener onClickListener, RecyclerViewInterface recyclerViewInterface) {
        this.onClickListener = onClickListener;
        this.practices = practices;
        this.inflater = LayoutInflater.from(context);
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @Override
    public PracticeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.practice_item, parent, false);
        return new PracticeAdapter.ViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(PracticeAdapter.ViewHolder holder, int position) {

        Practice practice = practices.get(position);

        holder.nameView.setText(practice.getDate());
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                onClickListener.onPracticeClick(practice);
            }
        });
    }

    @Override
    public int getItemCount() {
        return practices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameView;
        ViewHolder(View view, RecyclerViewInterface recyclerViewInterface){
            super(view);
            nameView = (TextView)view.findViewById(R.id.practicetv);

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(recyclerViewInterface != null){
                        int pos = getAdapterPosition();

                        if(pos != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemLongClick(pos);
                            return true;
                        }
                    }
                    return true;
                }
            });
        }

    }
}
