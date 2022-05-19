package com.example.dyplomapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import com.github.barteksc.pdfviewer.PDFView;

public class PDFWorkDocActivity extends AppCompatActivity {

    PDFView pdfView;
    WorkDoc workDoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfwork_doc);

        Bundle arguments = getIntent().getExtras();
        workDoc = (WorkDoc) arguments.getSerializable(WorkDoc.class.getSimpleName());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(workDoc.getName());

        pdfView = findViewById(R.id.pdfView);
        pdfView.fromUri(Uri.parse(workDoc.getSource()))
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