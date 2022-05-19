package com.example.dyplomapp;

import static android.app.Activity.RESULT_OK;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaFragment extends Fragment implements RecyclerViewInterface{
    String work_id;
    ArrayList<WorkDoc> docs = new ArrayList<WorkDoc>();
    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;
    FloatingActionButton fab;
    MyWorkDocAdapter adapter;
    WorkDoc temp;
    WorkDoc workDocN;

    private static final int PICK_FILE = 1;
    private static final int EDIT_FILE = 2;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        work_id = getArguments().getString("work_id");
        mDBHelper = new DatabaseHelper(getContext());
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
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_media, container, false);
        fab = view.findViewById(R.id.add_mediabutton);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                addMedia();
            }
        });
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.listMediaOfWork);

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

        MediaAdapter.OnMediaClickListener mediaClickListener = new MediaAdapter.OnMediaClickListener(){
            @Override
            public  void onMediaClick(WorkDoc media){
                Intent intent = new Intent(getContext(), WorkActivity.class);
                intent.putExtra(WorkDoc.class.getSimpleName(), media);
                startActivity(intent);
            }
        };

        adapter = new MyWorkDocAdapter(docs, getContext(), this.getLifecycle(), this);

        recyclerView.setAdapter(adapter);
        return view;
    }

    void setData(){
        Cursor cursor = mDb.rawQuery("SELECT * FROM documents WHERE work_id = " + work_id, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            WorkDoc workDoc = new WorkDoc();
            workDoc.setId(cursor.getInt(0));
            workDoc.setName(cursor.getString(1));
            workDoc.setType(MediaType.valueOf(cursor.getInt(2)));
            workDoc.setSource(cursor.getString(3));
            workDoc.setWorkId(cursor.getInt(4));

            docs.add(workDoc);

            cursor.moveToNext();
        }
    }

    void addMedia(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_FILE){
            if(resultCode == RESULT_OK){
                try {
                    Uri uri = data.getData();
                    Cursor returnCursor = getContext().getContentResolver().query(uri, null, null, null, null);
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    String fileName = returnCursor.getString(nameIndex);

                    ContentValues cv = new ContentValues();
                    cv.put("name", fileName);
                    cv.put("type", workDocN.getType().getValue());
                    cv.put("source", uri.toString());
                    cv.put("work_id", work_id);


                    long insCount = mDb.insert("documents", null, cv);

                    System.out.println("Inserted " + insCount + " row.");

                    Cursor cursor = mDb.rawQuery("SELECT * FROM documents ORDER BY id DESC LIMIT 1", null);
                    cursor.moveToFirst();

                    WorkDoc workDoc = new WorkDoc();
                    workDoc.setId(cursor.getInt(0));
                    workDoc.setName(cursor.getString(1));
                    workDoc.setType(MediaType.valueOf(cursor.getInt(2)));
                    workDoc.setSource(cursor.getString(3));
                    workDoc.setWorkId(cursor.getInt(4));

                    docs.add(workDoc);

                    adapter.notifyDataSetChanged();
                }
                catch (Exception e){
                    Toast toast = Toast.makeText(getContext(), "Data not loaded!", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
            else{
                Toast toast = Toast.makeText(getContext(), "No new material!", Toast.LENGTH_LONG);
                toast.show();
            }
        }
        else if(requestCode == EDIT_FILE){
            if(resultCode == RESULT_OK){
                try {
                    WorkDoc workDoc = temp;
                    Uri uri = data.getData();
                    Cursor returnCursor = getContext().getContentResolver().query(uri, null, null, null, null);
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    String fileName = returnCursor.getString(nameIndex);

                    ContentValues cv = new ContentValues();
                    cv.put("name", fileName);
                    cv.put("source", uri.toString());


                    mDb.update("documents", cv, "id=?", new String[]{String.valueOf(workDoc.getId())});

                    int position = docs.indexOf(workDoc);
                    workDoc.setName(fileName);
                    workDoc.setSource(uri.toString());

                    docs.set(position, workDoc);

                    adapter.notifyItemChanged(position);
                }
                catch (Exception e){
                    e.printStackTrace();
                    Toast toast = Toast.makeText(getContext(), "Data not loaded!", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
            else{
                Toast toast = Toast.makeText(getContext(), "No new material!", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    public void selectFile(String type) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        workDocN = new WorkDoc();

        if(type.equals("PDF")){
            intent.setType("application/pdf");
            workDocN.setType(MediaType.valueOf(1));
        }
        else if(type.equals("AUDIO")){
            intent.setType("audio/*");
            workDocN.setType(MediaType.valueOf(2));
        }
        else if(type.equals("VIDEO")){
            intent.setType("video/*");
            workDocN.setType(MediaType.valueOf(3));
        }
        else{
            EditText editTextField = new EditText(this.getContext());
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Add Work Info")
                    .setView(editTextField)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String input = editTextField.getText().toString().trim();
                            ContentValues cv = new ContentValues();

                            cv.put("name", input);
                            cv.put("type", 4);
                            cv.put("source", "0");
                            cv.put("work_id", work_id);

                            long insCount = mDb.insert("documents", null, cv);

                            System.out.println("Inserted " + insCount + " row.");

                            Cursor cursor = mDb.rawQuery("SELECT * FROM documents ORDER BY id DESC LIMIT 1", null);
                            cursor.moveToFirst();

                            WorkDoc workDoc = new WorkDoc();
                            workDoc.setId(cursor.getInt(0));
                            workDoc.setName(cursor.getString(1));
                            workDoc.setType(MediaType.valueOf(cursor.getInt(2)));
                            workDoc.setSource(cursor.getString(3));
                            workDoc.setWorkId(cursor.getInt(4));

                            docs.add(workDoc);

                            adapter.notifyDataSetChanged();
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
    public void onPause() {
        super.onPause();
        try{
            adapter.player.stop();
        }
        catch (Exception e){}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            adapter.player.stop();
        } catch (Exception e){}
        mDBHelper.close();
    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onItemLongClick(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

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

    public void updateFile(WorkDoc workDoc){
        MediaType type = workDoc.getType();
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        temp = workDoc;

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
            Toast a = Toast.makeText(getContext(), "Can\'t edit online videos.", Toast.LENGTH_SHORT);
            return;
        }
        else{
            EditText editTextField = new EditText(getContext());
            editTextField.setText(workDoc.getName());
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Edit Work Info")
                    .setView(editTextField)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String input = editTextField.getText().toString().trim();
                            ContentValues cv = new ContentValues();

                            cv.put("name", input);

                            mDb.update("documents", cv, "id=?", new String[]{String.valueOf(workDoc.getId())});

                            int position = docs.indexOf(workDoc);

                            workDoc.setName(input);

                            docs.set(position, workDoc);

                            adapter.notifyDataSetChanged();
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
        WorkDoc workDoc = docs.get(position);
        updateFile(workDoc);
    }

    void rmMaterial(int position){
        WorkDoc workDoc = docs.get(position);

        removeFile(workDoc);

        adapter.notifyItemRemoved(position);
    }

    void removeFile(WorkDoc workDoc){
        mDb.delete("documents", "id=?", new String[]{String.valueOf(workDoc.getId())});
        docs.remove(workDoc);
    }
}
