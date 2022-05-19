package com.example.dyplomapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WorkActivity extends AppCompatActivity {

    Work work;
    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);

        Bundle arguments = getIntent().getExtras();
        work = (Work)arguments.getSerializable(Work.class.getSimpleName());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(work.getName());

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

        TabLayout tabLayout = findViewById(R.id.tabsOfWork);
        ViewPager2 viewPager2 = findViewById(R.id.viewpagerOfWork);


        ViewPagerOfWorkAdapter adapter = new ViewPagerOfWorkAdapter(this);
        viewPager2.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager2,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        if(position == 0) tab.setText("Practices");
                        else if(position == 1) tab.setText("Media");
                        else tab.setText("Tab " + (position + 1));
                    }
                }).attach();
    }

    public class ViewPagerOfWorkAdapter extends FragmentStateAdapter {
        public ViewPagerOfWorkAdapter(@NonNull FragmentActivity fragmentActivity)
        {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Bundle bundle = new Bundle();
            bundle.putString("work_id", Integer.toString(work.getId()));
            switch (position) {
                case 0:
                    PracticesFragment f = new PracticesFragment();
                    f.setArguments(bundle);
                    return f;
                case 1:
                    MediaFragment m = new MediaFragment();
                    m.setArguments(bundle);
                    return m;
                default:
                    return new PracticesFragment();
            }
        }
        @Override
        public int getItemCount() {return 2; }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_work, menu);
        return true;
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

            case R.id.rmWork:
                removeCurrentWork();
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

    void removeCurrentWork(){
        Context context = this;
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Are You sure to remove current work?");
        alert.setCancelable(true);
        LinearLayout layoutName = new LinearLayout(this);
        layoutName.setOrientation(LinearLayout.VERTICAL);
        alert.setView(layoutName);
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mDb.delete("works", "id=?", new String[]{String.valueOf(work.getId())});

                Intent intentn = new Intent(context, MainActivity.class);
                intentn.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentn);
                finish();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }
}