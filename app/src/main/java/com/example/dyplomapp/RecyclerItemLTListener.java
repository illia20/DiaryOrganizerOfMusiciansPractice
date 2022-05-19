package com.example.dyplomapp;


import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerItemLTListener implements RecyclerView.OnItemTouchListener{

    protected OnItemClickListener listener;
    private GestureDetector gestureDetector;

    @Nullable
    private View childView;

    private int childViewPosition;

    public RecyclerItemLTListener(Context context, OnItemClickListener listener) {
        this.gestureDetector = new GestureDetector(context, new GestureListener());
        this.listener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        childView = rv.findChildViewUnder(e.getX(), e.getY());
        childViewPosition = rv.getChildPosition(childView);

        return childView != null && gestureDetector.onTouchEvent(e);
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public static abstract class SimpleOnItemClickListener implements OnItemClickListener {

        /**
         * Called when an item is clicked. The default implementation is a no-op.
         *
         * @param childView View of the item that was clicked.
         * @param position  Position of the item that was clicked.
         */
        public void onItemClick(View childView, int position) {
            // Do nothing.
        }

        /**
         * Called when an item is long pressed. The default implementation is a no-op.
         *
         * @param childView View of the item that was long pressed.
         * @param position  Position of the item that was long pressed.
         */
        public void onItemLongPress(View childView, int position) {
            // Do nothing.
        }

    }

    public interface OnItemClickListener {
        public void onItemClick(View childView, int position);
        public void onItemLongPress(View childView, int position);

    }

    protected class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            if (childView != null) {
                listener.onItemClick(childView, childViewPosition);
            }

            return true;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            if (childView != null) {
                listener.onItemLongPress(childView, childViewPosition);
            }
        }

        @Override
        public boolean onDown(MotionEvent event) {
            // Best practice to always return true here.
            // http://developer.android.com/training/gestures/detector.html#detect
            return true;
        }

    }
}
