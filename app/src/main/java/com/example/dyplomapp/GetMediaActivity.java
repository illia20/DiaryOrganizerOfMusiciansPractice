package com.example.dyplomapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class GetMediaActivity extends AppCompatActivity implements RecyclerViewInterface{

    ArrayList<Work> items = new ArrayList<Work>();
    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_media);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Select work");

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        mDBHelper = new DatabaseHelper(this);
        try {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }

        try {
            mDb = mDBHelper.getWritableDatabase();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }
        recyclerView = (RecyclerView)findViewById(R.id.listViewWork);



        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            }}




    }

    void setData(){
        Cursor cursor = mDb.rawQuery("SELECT * FROM works ORDER BY status ASC", null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Work work = new Work();
            work.setId(cursor.getInt(0));
            work.setName(cursor.getString(1));
            work.setStatus(cursor.getInt(2));

            items.add(work);

            System.out.println(work.getId() + "  " + work.getName() + "  " + work.getStatus());

            cursor.moveToNext();
        }
    }

    void handleSendText(Intent intent){
        Context context = this;
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);

        setData();

        WorkAdapter.OnWorkClickListener workClickListener = new WorkAdapter.OnWorkClickListener(){
            @Override
            public  void onWorkClick(Work work){
                ContentValues cv = new ContentValues();
                cv.put("name", "YouTube video");
                cv.put("type", 5);
                cv.put("source", sharedText);
                cv.put("work_id", work.getId());

                long insCount = mDb.insert("documents", null, cv);

                Toast.makeText(context, "Media added", Toast.LENGTH_SHORT).show();
                Intent intentn = new Intent(context, MainActivity.class);
                intentn.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentn);
                finish();
            }
        };

        WorkAdapter adapter = new WorkAdapter(this, items, workClickListener, this);

        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDBHelper.close();
    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onItemLongClick(int position) {

    }
}