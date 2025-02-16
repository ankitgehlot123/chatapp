package com.company.my.chatapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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
    private static final String CHANNEL_ID = "Chat";
    private static final String CHANNEL_DESC = "chatapp notification";
    RecyclerView rvContacts;
    Session session;
    ImageView image;
    List<contactGetSet> contactList = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        image = findViewById(R.id.imgLogo);
        setSupportActionBar(toolbar);
        rvContacts = (RecyclerView) findViewById(R.id.rvContacts);
        session = new Session(this);
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
        Log.e("Kutta", session.getUsername());
        contactList();

        setup_pic();
        createNotificationChannel();
    }

    public void contactList() {


        contactListAdapter contactListAdapter = new contactListAdapter(getApplicationContext(), contactList);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvContacts.setLayoutManager(mLayoutManager);
        rvContacts.setAdapter(contactListAdapter);
        JSONArray list = new JSONArray();
        Document sort = new Document().append("timestamp", -1);
        Document timestamp = new Document("$type", "date");
        Document filter = new Document().append("timestamp", timestamp);
        FindIterable<Document> cursor = utils.contactListCollection.find(filter).sort(sort);

        Iterator<Document> iterator = cursor.iterator();
        //Log.d("Check111", "" + iterator.hasNext());
        int i=0;
        while (iterator.hasNext()) {
            try {
                String pic = null;
            //Log.d("Check111", iterator.next().toJson());
            Document document = iterator.next();
            //document.remove("_id");
            Map<String, Object> map = new HashMap<>(document);
            JSONObject obj = new JSONObject(map);
            Log.d("Check111", obj.toString());
                if (obj.has("pic"))
                    pic = obj.getString("pic");
                contactGetSet contact = new contactGetSet(
                        obj.getString("_id"),
                        obj.getString("username"),
                        obj.getString("mob_no"),
                        pic
                );
                Log.d("Check3", obj.getString("mob_no")+"   "+obj.getString("username"));
                contactList.add(contact);
            //list.put(obj);
            }catch(Exception e)
            {
                Log.e("MongoDB-Error:",e.toString());
            }finally {
                //Notify adapter about data changes
                contactListAdapter.notifyItemChanged(i);
                i++;
            }
        }
        /*Log.i("check4",list.toString());
        for (int i = 0; i < list.length(); i++) {
            try {
                String pic = null;
                JSONObject obj = (JSONObject) list.get(i);
                if (obj.has("pic"))
                    pic = obj.getString("pic");
                contactGetSet contact = new contactGetSet(
                        obj.getString("_id"),
                        obj.getString("username"),
                        obj.getString("mob_no"),
                        pic
                );
                Log.d("Check3", obj.getString("mob_no")+"   "+obj.getString("username"));
                contactList.add(contact);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                //Notify adapter about data changes
                contactListAdapter.notifyItemChanged(i);
            }
        }*/
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        // first clear the recycler view so items are not populated twice
        contactListAdapter contactListAdapter = new contactListAdapter(getApplicationContext(), contactList);
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
    public void profile(View view) {
        Intent intent = new Intent(this, profile.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void setup_pic() {

        if (session.getProfilePic() != "") {
            byte[] decodedString = Base64.decode(session.getProfilePic(), Base64.DEFAULT);
            Bitmap decodeByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            image.setImageBitmap(decodeByte);
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = CHANNEL_ID;
            String description = CHANNEL_DESC;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setLightColor(Color.BLUE);
            channel.setShowBadge(true);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);


            if (notificationManager.getNotificationChannel("fcm_fallback_notification_channel") != null) {
                return;
            }

            String channelName = getString(R.string.fcm_fallback_notification_channel_label);
            NotificationChannel Miscchannel = new NotificationChannel("fcm_fallback_notification_channel", channelName, NotificationManager.IMPORTANCE_HIGH);
            Miscchannel.enableVibration(true);
            Miscchannel.enableLights(true);
            Miscchannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            Miscchannel.setLightColor(Color.BLUE);
            Miscchannel.setShowBadge(true);
            Miscchannel.setImportance(importance);
            notificationManager.createNotificationChannel(Miscchannel);
        }

    }
}
