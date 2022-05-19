package com.example.dyplomapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetPracticeMaterialWorkActivity extends AppCompatActivity implements RecyclerViewInterface{

    ArrayList<Work> items = new ArrayList<Work>();
    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;
    RecyclerView recyclerView;
    String extra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_practice_material_work);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Select work: ");

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
        extra = intent.getStringExtra(Intent.EXTRA_TEXT);

        setData();

        WorkAdapter.OnWorkClickListener workClickListener = new WorkAdapter.OnWorkClickListener(){
            @Override
            public  void onWorkClick(Work work){
                Intent intent = new Intent(getApplicationContext(), SelectPracticeActivity.class);
                intent.putExtra(Work.class.getSimpleName(), work);
                intent.putExtra("link", extra);
                startActivity(intent);
            }
        };

        WorkAdapter adapter = new WorkAdapter(this, items, workClickListener, this);

        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        items.clear();

        setData();

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.listViewWork);

        WorkAdapter.OnWorkClickListener workClickListener = new WorkAdapter.OnWorkClickListener(){
            @Override
            public  void onWorkClick(Work work){
                Intent intent = new Intent(getApplicationContext(), WorkActivity.class);
                intent.putExtra(Work.class.getSimpleName(), work);
                startActivity(intent);
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