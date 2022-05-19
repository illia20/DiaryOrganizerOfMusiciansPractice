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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;

public class SelectPracticeActivity extends AppCompatActivity implements RecyclerViewInterface{

    Work work;
    String extra;
    ArrayList<Practice> practices = new ArrayList<Practice>();
    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;
    PracticeAdapter adapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_practice);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Select practice: ");

        Bundle arguments = getIntent().getExtras();
        work = (Work)arguments.getSerializable(Work.class.getSimpleName());
        extra = arguments.getString("link");

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

        setData();

        Context context = this;

        recyclerView = findViewById(R.id.listPracticesOfWork);
        PracticeAdapter.OnPracticeClickListener practiceClickListener = new PracticeAdapter.OnPracticeClickListener(){
            @Override
            public  void onPracticeClick(Practice practice){
                ContentValues cv = new ContentValues();

                cv.put("name", "YouTube video");
                cv.put("type", 5);
                cv.put("value", extra);
                cv.put("practice_id", practice.getId());

                long insCount = mDb.insert("practice_materials", null, cv);

                Toast.makeText(context, "Media added", Toast.LENGTH_SHORT).show();
                Intent intentn = new Intent(context, MainActivity.class);
                intentn.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentn);
                finish();
            }
        };

        adapter = new PracticeAdapter(this, practices, practiceClickListener, this);

        recyclerView.setAdapter(adapter);
    }

    void setData(){
        Cursor cursor = mDb.rawQuery("SELECT * FROM practices WHERE work_id = " + work.getId() + " ORDER BY id DESC", null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Practice practice = new Practice();
            practice.setId(cursor.getInt(0));
            practice.setDate(cursor.getString(1));
            practice.setWorkId(cursor.getInt(2));

            practices.add(practice);

            System.out.println(practice.getId() + "  " + practice.getDate() + "  " + practice.getWorkId());

            cursor.moveToNext();
        }
    }

    @Override
    public void onDestroy() {
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