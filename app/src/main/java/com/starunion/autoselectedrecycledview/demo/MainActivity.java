package com.starunion.autoselectedrecycledview.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.starunion.autoselectedrecycledview.R;
import com.starunion.autoselectedrecycledview.demo.helper.AutoSelectStartHelper;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    SimpleAdapter simpleAdapter;
    RecyclerView.LayoutManager layoutManager;
    AutoSelectStartHelper mAutoSelectStartHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.rv_main);

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        mAutoSelectStartHelper = new AutoSelectStartHelper();
        mAutoSelectStartHelper.attachToRecyclerView(recyclerView);
        simpleAdapter = new SimpleAdapter(this);
        recyclerView.setAdapter(simpleAdapter);

    }
}
