package com.company.my.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.company.my.chatapp.contactListShow.contactGetSet;
import com.company.my.chatapp.contactListShow.contactListAdapter;
import com.company.my.chatapp.utils.Session;
import com.company.my.chatapp.utils.utils;
import com.mongodb.client.FindIterable;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    RecyclerView rvContacts;
    Session session;
    List<contactGetSet> contactList = new ArrayList();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        rvContacts = (RecyclerView) findViewById(R.id.rvContacts);
        session=new Session(this);
        //Change isFirstRun to false
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("isFirstRun", false).apply();

        Button fab = findViewById(R.id.fab);

        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, contactList.class);
            startActivity(intent);
        });
        Button fab1 = findViewById(R.id.fab1);

        fab1.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Group_chat_base.class);
            startActivity(intent);
        });
       Log.e("Kutta",session.getUsername());
        contactList();

    }
    public void contactList(){

        contactListAdapter contactListAdapter = new contactListAdapter(getApplicationContext(),contactList);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvContacts.setLayoutManager(mLayoutManager);
        rvContacts.setAdapter(contactListAdapter);
        JSONArray list = new JSONArray();
        Document sort = new Document().append("timestamp",-1);
        Document timestamp = new Document("$type","date");
        Document filter = new Document().append("timestamp",timestamp);
        FindIterable<Document> cursor = utils.contactListCollection.find(filter).sort(sort);
        Log.d("Check111","Hello");
        Iterator<Document> iterator = cursor.iterator();
        while (iterator.hasNext()) {

            //Log.d("Check111",iterator.next().toJson());
            Document document = iterator.next();
            //document.remove("_id");
            Map<String, Object> map = new HashMap<>(document);
            JSONObject obj=new JSONObject(map);
            list.put(obj);
        }
        for (int i = 0; i < list.length(); i++) {
            try {
                String pic = null;
                JSONObject obj = (JSONObject) list.get(i);
                if(obj.has("pic"))
                    pic = obj.getString("pic");
                contactGetSet contact = new contactGetSet(
                        obj.getString("_id"),
                        obj.getString("username"),
                        obj.getString("mob_no"),
                        pic
                );
                contactList.add(contact);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                //Notify adapter about data changes
                contactListAdapter.notifyItemChanged(i);
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        // first clear the recycler view so items are not populated twice
        contactListAdapter contactListAdapter = new contactListAdapter(getApplicationContext(),contactList);
        contactListAdapter.clear();

        // then reload the data
        contactList();
    }
   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        //return super.onOptionsItemSelected(item);
    }*/
}
