package com.company.my.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class chat_base extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_base);
        Intent intent = getIntent();
        String mob_no = intent.getStringExtra("mob_no");
        String username = intent.getStringExtra("username");
        Bundle bundle = new Bundle();
        bundle.putString("mob_no", mob_no);
        bundle.putString("username", username);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(username);
        MainFragment mainFragment = new MainFragment();
        mainFragment.setArguments(bundle);
        Log.e("blah1", username + " " + mob_no);
        //THEN NOW SHOW OUR FRAGMENT
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mainFragment).commit();
    }
}
