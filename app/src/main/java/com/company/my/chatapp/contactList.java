package com.company.my.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

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


public class contactList extends AppCompatActivity {

    RecyclerView rvContacts;
    Session session = new Session(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_list);

        rvContacts = (RecyclerView) findViewById(R.id.rvContacts);
        getAllContacts();
    }

    private void getAllContacts() {
        List<contactGetSet> contactList = new ArrayList();
        contactListAdapter contactListAdapter = new contactListAdapter(getApplicationContext(), contactList);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvContacts.setLayoutManager(mLayoutManager);
        rvContacts.setAdapter(contactListAdapter);
        JSONArray list = new JSONArray();
        Document sort = new Document();
        sort.put("username", 1);
        FindIterable<Document> cursor = utils.contactListCollection.find().sort(sort);

        Iterator<Document> iterator = cursor.iterator();
        while (iterator.hasNext()) {
            Document document = iterator.next();
            //document.remove("_id");
            Map<String, Object> map = new HashMap<>(document);
            JSONObject obj = new JSONObject(map);
            list.put(obj);
        }
        Log.d("ContactsFetched", list.toString());

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
                Log.i("kala", obj.getString("mob_no"));
                if (!obj.getString("mob_no").equals(session.getMob_no()))
                    contactList.add(contact);


            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                //Notify adapter about data changes
                contactListAdapter.notifyItemChanged(i);
            }
        }

    }

    public void refresh(View view) {
        Intent intent = new Intent(this, contactListUpdate.class);
        intent.putExtra("source", 1);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        //finish();
    }
}
