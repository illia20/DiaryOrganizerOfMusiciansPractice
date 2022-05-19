package com.example.dyplomapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
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

public class PracticesFragment extends Fragment implements RecyclerViewInterface{

    String work_id;
    ArrayList<Practice> practices = new ArrayList<Practice>();
    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;
    EditText mark;
    FloatingActionButton fab;
    PracticeAdapter adapter;

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
        View view = inflater.inflate(R.layout.layout_practices, container, false);

        mark = view.findViewById(R.id.conditionTextView);

        fab = view.findViewById(R.id.add_practicebutton);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                addPractice();
            }
        });
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.listPracticesOfWork);

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

        Cursor cursor = mDb.rawQuery("SELECT status FROM works WHERE id = " + work_id, null);
        cursor.moveToFirst();
        mark.setText(String.valueOf(cursor.getInt(0)));

        mark.addTextChangedListener(new TextWatcher() {
            String prev;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                prev = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString();
                if(str.length() != 0) {
                    ContentValues cv = new ContentValues();
                    cv.put("status", str);
                    mDb.update("works", cv, "id=?", new String[]{work_id});
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                prev = s.toString();
            }
        });

        PracticeAdapter.OnPracticeClickListener practiceClickListener = new PracticeAdapter.OnPracticeClickListener(){
            @Override
            public  void onPracticeClick(Practice practice){
                Intent intent = new Intent(getContext(), PracticeActivity.class);
                intent.putExtra(Practice.class.getSimpleName(), practice);
                intent.putExtra(Work.class.getSimpleName(), work_id);
                startActivity(intent);
            }
        };

        adapter = new PracticeAdapter(getContext(), practices, practiceClickListener, this);

        recyclerView.setAdapter(adapter);

        return view;
    }

    void setData(){
        Cursor cursor = mDb.rawQuery("SELECT * FROM practices WHERE work_id = " + work_id + " ORDER BY id DESC", null);
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

    void addPractice(){
        final EditText editTextField = new EditText(this.getContext());
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Add New Practice")
                .setView(editTextField)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String input = editTextField.getText().toString().trim();
                        List<String> practiceNames = new ArrayList<>();
                        for(Practice practice : practices){
                            practiceNames.add(practice.getDate());
                        }
                        if(practiceNames.contains(input)){
                            Toast.makeText(getContext(), "Such practice already exists!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ContentValues cv = new ContentValues();

                        cv.put("date", input);
                        cv.put("work_id", work_id);

                        long insCount = mDb.insert("practices", null, cv);

                        System.out.println("Inserted " + insCount + " row.");

                        Practice q = new Practice();
                        Cursor cursor = mDb.rawQuery("SELECT * FROM practices ORDER BY id DESC LIMIT 1", null);
cursor.moveToFirst();
                        q.setId(cursor.getInt(0));
                        q.setDate(cursor.getString(1));
                        q.setWorkId(cursor.getInt(2));

                        practices.add(0, q);

                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View layout = getLayoutInflater().inflate(R.layout.custom_alert_dialog, null);
        ImageButton editb = layout.findViewById(R.id.edit_btn);
        ImageButton rmb = layout.findViewById(R.id.rm_btn);
        builder.setView(layout);
        AlertDialog dialog = builder.create();
        editb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPractice(position);
                dialog.dismiss();
            }
        });
        rmb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rmPractice(position);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    void editPractice(int position){
        Practice practice = practices.get(position);
        final EditText editTextField = new EditText(this.getContext());
        editTextField.setText(practice.getDate());
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Edit Practice")
                .setView(editTextField)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String input = editTextField.getText().toString().trim();
                        List<String> practiceNames = new ArrayList<>();
                        for(Practice practice : practices){
                            practiceNames.add(practice.getDate());
                        }
                        if(practiceNames.contains(input)){
                            Toast.makeText(getContext(), "Such practice already exists!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ContentValues cv = new ContentValues();

                        cv.put("date", input);

                        mDb.update("practices", cv, "id=?", new String[]{String.valueOf(practice.getId())});

                        practice.setDate(input);

                        practices.set(position, practice);

                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    void rmPractice(int position){
        Practice practice = practices.get(position);

        mDb.delete("practices", "id=?", new String[]{String.valueOf(practice.getId())});

        practices.remove(practice);

        adapter.notifyItemRemoved(position);
    }
}



















