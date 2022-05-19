package com.example.dyplomapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;

public class PracticeActivity extends AppCompatActivity implements RecyclerViewInterface {

    Practice practice;
    ArrayList<PracticeMaterial> practiceMaterials = new ArrayList<PracticeMaterial>();
    String practice_id;
    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;
    FloatingActionButton fab;
    String work_id;
    PracticeMaterial temp;

    RecyclerView pmRecyclerView;
    MyPracticeMaterialAdapter myPracticeMaterialAdapter;

    PracticeMaterial create;

    private static final int PICK_FILE = 1;
    private static final int EDIT_FILE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        Bundle arguments = getIntent().getExtras();
        practice = (Practice)arguments.getSerializable(Practice.class.getSimpleName());
        work_id = arguments.getString(Work.class.getSimpleName());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        practice_id = String.valueOf(practice.getId());

        fab = findViewById(R.id.add_practicematerialbtn);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                addPracticeMaterial();
            }
        });

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

        String work = getWork();

        actionBar.setTitle(work + " - " + practice.getDate());

        setData();

        pmRecyclerView = findViewById(R.id.listViewPracticeMaterials);

        pmRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        myPracticeMaterialAdapter = new MyPracticeMaterialAdapter(practiceMaterials, this, this.getLifecycle(), this);
        pmRecyclerView.setAdapter(myPracticeMaterialAdapter);
        pmRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    void addPracticeMaterial(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Choose file type:");
        String[] items = {"PDF","AUDIO","VIDEO","TEXT"};
        int checkedItem = 0;
        alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        selectFile("PDF");
                        dialog.dismiss();
                        break;
                    case 1:
                        selectFile("AUDIO");
                        dialog.dismiss();
                        break;
                    case 2:
                        selectFile("VIDEO");
                        dialog.dismiss();
                        break;
                    case 3:
                        selectFile("TEXT");
                        dialog.dismiss();
                        break;
                }
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    String getWork(){
        Cursor cursor = mDb.rawQuery("SELECT * FROM works WHERE id = " + work_id, null);
        cursor.moveToFirst();

        return cursor.getString(1);
    }

    void setData(){
        Cursor cursor = mDb.rawQuery("SELECT * FROM practice_materials WHERE practice_id = " + practice_id, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            PracticeMaterial practiceMaterial = new PracticeMaterial();
            practiceMaterial.setId(cursor.getInt(0));
            practiceMaterial.setName(cursor.getString(1));
            practiceMaterial.setType(MediaType.valueOf(cursor.getInt(2)));
            practiceMaterial.setTxt(cursor.getString(3));
            practiceMaterial.setPracticeId(cursor.getInt(4));


            practiceMaterials.add(practiceMaterial);

            cursor.moveToNext();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_FILE){
            if(resultCode == RESULT_OK){
                try {
                    Uri uri = data.getData();
                    Cursor returnCursor = this.getContentResolver().query(uri, null, null, null, null);
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    String fileName = returnCursor.getString(nameIndex);

                    ContentValues cv = new ContentValues();
                    cv.put("name", fileName);
                    cv.put("type", create.getType().getValue());
                    cv.put("value", uri.toString());
                    cv.put("practice_id", practice_id);


                    long insCount = mDb.insert("practice_materials", null, cv);

                    System.out.println("Inserted " + insCount + " row.");

                    Cursor cursor = mDb.rawQuery("SELECT * FROM practice_materials ORDER BY id DESC LIMIT 1", null);
                    cursor.moveToFirst();

                    PracticeMaterial p = new PracticeMaterial();
                    p.setId(cursor.getInt(0));
                    p.setName(cursor.getString(1));
                    p.setType(MediaType.valueOf(cursor.getInt(2)));
                    p.setTxt(cursor.getString(3));
                    p.setPracticeId(cursor.getInt(4));

                    practiceMaterials.add(p);

                    myPracticeMaterialAdapter.notifyDataSetChanged();
                }
                catch (Exception e){
                    Toast toast = Toast.makeText(this, "Data not loaded!", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
            else{
                Toast toast = Toast.makeText(this, "No new material!", Toast.LENGTH_LONG);
                toast.show();
            }
        }
        else if(requestCode == EDIT_FILE){
            if(resultCode == RESULT_OK){
                try {
                    PracticeMaterial practiceMaterial = temp;
                    Uri uri = data.getData();
                    Cursor returnCursor = this.getContentResolver().query(uri, null, null, null, null);
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    String fileName = returnCursor.getString(nameIndex);

                    ContentValues cv = new ContentValues();
                    cv.put("name", fileName);
                    cv.put("value", uri.toString());


                    mDb.update("practice_materials", cv, "id=?", new String[]{String.valueOf(practiceMaterial.getId())});

                    int position = practiceMaterials.indexOf(practiceMaterial);
                    practiceMaterial.setName(fileName);
                    practiceMaterial.setTxt(uri.toString());

                    practiceMaterials.set(position, practiceMaterial);

                    myPracticeMaterialAdapter.notifyItemChanged(position);
                }
                catch (Exception e){
                    Toast toast = Toast.makeText(this, "Data not loaded!", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
            else{
                Toast toast = Toast.makeText(this, "No new material!", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    public void selectFile(String type) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        create = new PracticeMaterial();

        if(type.equals("PDF")){
            intent.setType("application/pdf");
            create.setType(MediaType.valueOf(1));
        }
        else if(type.equals("AUDIO")){
            intent.setType("audio/*");
            create.setType(MediaType.valueOf(2));
        }
        else if(type.equals("VIDEO")){
            intent.setType("video/*");
            create.setType(MediaType.valueOf(3));
        }
        else{
            EditText editTextField = new EditText(this);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Add Work Info")
                    .setView(editTextField)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String input = editTextField.getText().toString().trim();
                            ContentValues cv = new ContentValues();

                            cv.put("name", input);
                            cv.put("type", 4);
                            cv.put("value", "0");
                            cv.put("practice_id", practice_id);

                            long insCount = mDb.insert("practice_materials", null, cv);

                            System.out.println("Inserted " + insCount + " row.");

                            Cursor cursor = mDb.rawQuery("SELECT * FROM practice_materials ORDER BY id DESC LIMIT 1", null);
                            cursor.moveToFirst();

                            PracticeMaterial p = new PracticeMaterial();
                            p.setId(cursor.getInt(0));
                            p.setName(cursor.getString(1));
                            p.setType(MediaType.valueOf(cursor.getInt(2)));
                            p.setTxt(cursor.getString(3));
                            p.setPracticeId(cursor.getInt(4));

                            practiceMaterials.add(p);

                            myPracticeMaterialAdapter.notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create();
            dialog.show();
            return;
        }

        startActivityForResult(intent, PICK_FILE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            myPracticeMaterialAdapter.player.stop();
        } catch (Exception e){}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            myPracticeMaterialAdapter.player.stop();
        } catch (Exception e){}
        mDBHelper.close();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;

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

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onItemLongClick(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View layout = getLayoutInflater().inflate(R.layout.custom_alert_dialog, null);
        ImageButton editb = layout.findViewById(R.id.edit_btn);
        ImageButton rmb = layout.findViewById(R.id.rm_btn);
        builder.setView(layout);
        AlertDialog dialog = builder.create();
        editb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editMaterial(position);
                dialog.dismiss();
            }
        });
        rmb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rmMaterial(position);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void updateFile(PracticeMaterial practiceMaterial){
        MediaType type = practiceMaterial.getType();
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        temp = practiceMaterial;

        if(type == MediaType.PDF){
            intent.setType("application/pdf");
        }
        else if(type == MediaType.AUDIO){
            intent.setType("audio/*");
        }
        else if(type == MediaType.VIDEO){
            intent.setType("video/*");
        }
        else if(type == MediaType.YT){
            Toast a = Toast.makeText(this, "Can\'t edit online videos.", Toast.LENGTH_SHORT);
            return;
        }
        else{
            EditText editTextField = new EditText(this);
            editTextField.setText(practiceMaterial.getName());
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Edit Work Info")
                    .setView(editTextField)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String input = editTextField.getText().toString().trim();
                            ContentValues cv = new ContentValues();

                            cv.put("name", input);

                            mDb.update("practice_materials", cv, "id=?", new String[]{String.valueOf(practiceMaterial.getId())});

                            int position = practiceMaterials.indexOf(practiceMaterial);
                            
                            practiceMaterial.setName(input);

                            practiceMaterials.set(position, practiceMaterial);

                            myPracticeMaterialAdapter.notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create();
            dialog.show();
            return;
        }

        startActivityForResult(intent, EDIT_FILE);
    }

    void editMaterial(int position){
        PracticeMaterial practiceMaterial = practiceMaterials.get(position);
        updateFile(practiceMaterial);
    }

    void rmMaterial(int position){
        PracticeMaterial practiceMaterial = practiceMaterials.get(position);

        removeFile(practiceMaterial);

        myPracticeMaterialAdapter.notifyItemRemoved(position);
    }

    public void removeFile(PracticeMaterial practiceMaterial){
        practiceMaterials.remove(practiceMaterial);
        mDb.delete("practice_materials", "id=?", new String[]{String.valueOf(practiceMaterial.getId())});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}