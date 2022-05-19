package com.example.dyplomapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RecyclerViewInterface{

    ArrayList<Work> items = new ArrayList<Work>();
    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        fab = findViewById(R.id.add_fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                addWork();
            }
        });

        setData();

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.listViewWork);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if(newState == RecyclerView.SCROLL_STATE_IDLE)
                    fab.show();
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0 || dy < 0 && fab.isShown())
                    fab.hide();
            }
        });

        WorkAdapter.OnWorkClickListener workClickListener = new WorkAdapter.OnWorkClickListener(){
            @Override
            public  void onWorkClick(Work work){
                Intent intent = new Intent(getApplicationContext(), WorkActivity.class);
                intent.putExtra(Work.class.getSimpleName(), work);
                startActivity(intent);
            }
        };

        WorkAdapter adapter = new WorkAdapter(this, items, workClickListener, this);

        recyclerView.setAdapter(adapter);    }

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

    void addWork(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        alert.setTitle("Add work:");
        alert.setCancelable(true);
        alert.setView(editText);
        LinearLayout layoutName = new LinearLayout(this);
        layoutName.setOrientation(LinearLayout.VERTICAL);
        layoutName.addView(editText);
        alert.setView(layoutName);
        alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String input = editText.getText().toString().trim();
                List<String> worksNames = new ArrayList<>();
                for(Work work : items){
                    worksNames.add(work.getName());
                }
                if(worksNames.contains(input)){
                    Toast.makeText(MainActivity.this, "Such work already exists!", Toast.LENGTH_SHORT).show();
                    return;
                }
                ContentValues cv = new ContentValues();

                cv.put("name", input);
                cv.put("status", 1);

                long insCount = mDb.insert("works", null, cv);

                System.out.println("Inserted " + insCount + " row.");
                onRestart();
            }
        });
        alert.show();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View layout = getLayoutInflater().inflate(R.layout.custom_alert_edit, null);
        ImageButton editb = layout.findViewById(R.id.edit_btn);
        builder.setView(layout);
        AlertDialog dialog = builder.create();
        editb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editWork(position);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    void editWork(int position){
        Work work = items.get(position);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        alert.setTitle("Edit work:");
        alert.setCancelable(true);
        alert.setView(editText);
        editText.setText(work.getName());
        LinearLayout layoutName = new LinearLayout(this);
        layoutName.setOrientation(LinearLayout.VERTICAL);
        layoutName.addView(editText);
        alert.setView(layoutName);
        alert.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String input = editText.getText().toString().trim();
                List<String> worksNames = new ArrayList<>();
                for(Work work1 : items){
                    worksNames.add(work1.getName());
                }
                if(worksNames.contains(input)){
                    Toast.makeText(MainActivity.this, "Such work already exists!", Toast.LENGTH_SHORT).show();
                    return;
                }
                ContentValues cv = new ContentValues();

                cv.put("name", input);

                mDb.update("works", cv, "id=?", new String[]{String.valueOf(work.getId())});
                onRestart();
            }
        });
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        Intent intent;
        switch (id){
            case R.id.camertone_b:
                intent = new Intent(this, CamertoneActivity.class);
                startActivity(intent);
                return true;

            case R.id.manual_b:
                intent = new Intent(this, ManualActivity.class);
                startActivity(intent);
                return true;

            case R.id.about_b:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}