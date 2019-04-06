package com.company.my.chatapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class chat_base extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_base);
     
        if (!checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        else{
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
    private void requestPermission(String permission) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            Toast.makeText(this, "Phone Storage Permission is required for storing media. Please allow it for additional functionality.", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
    }
    private boolean checkPermission(String permission) {
        if (Build.VERSION.SDK_INT >= 23) {
            int result = ActivityCompat.checkSelfPermission(this, permission);
            if (result == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
                } else {
                    Toast.makeText(this, "Permission Denied. We can't get phone number.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
