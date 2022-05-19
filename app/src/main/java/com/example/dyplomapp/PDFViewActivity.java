package com.example.dyplomapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import com.github.barteksc.pdfviewer.PDFView;

public class PDFViewActivity extends AppCompatActivity {

    PDFView pdfView;
    PracticeMaterial practiceMaterial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfview);

        Bundle arguments = getIntent().getExtras();
        practiceMaterial = (PracticeMaterial) arguments.getSerializable(PracticeMaterial.class.getSimpleName());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(practiceMaterial.getName());

        pdfView = findViewById(R.id.pdfView);
        pdfView.fromUri(Uri.parse(practiceMaterial.getTxt()))
                .enableSwipe(true)
                .enableDoubletap(true)
                .swipeHorizontal(true)
                .password(null)
                .pageSnap(true)
                .autoSpacing(true)
                .pageFling(true)
                .load();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}