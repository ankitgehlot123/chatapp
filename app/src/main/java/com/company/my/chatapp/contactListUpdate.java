package com.company.my.chatapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.company.my.chatapp.utils.utils;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// Base Stitch Packages
import com.mongodb.client.FindIterable;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;

// Packages needed to interact with MongoDB and Stitch
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;

// Necessary component for working with MongoDB Mobile
import com.mongodb.stitch.android.services.mongodb.local.LocalMongoDbService;

public class contactListUpdate extends AppCompatActivity {
    ArrayList<String> contact_mob_no = new ArrayList<>();
    ArrayList<String> contact_username = new ArrayList<>();
    public  static final int RequestPermissionCode  = 1 ;
    int source;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_contact_list);
        source= getIntent().getIntExtra("source",0);
        EnableRuntimePermission();
    }

    public void GetContactsIntoArrayList() throws JSONException {

        ArrayList<String> list = new ArrayList<String>();
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");
        while (phones.moveToNext())
        {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));



            phoneNumber = phoneNumber.replaceAll("-","").replaceAll("\\s","");
            if (phoneNumber.length() > 10) {
                phoneNumber = phoneNumber.substring(phoneNumber.length() - 10);
            }
            phoneNumber = "+91"+phoneNumber;
            if(!list.contains(phoneNumber)){
                //Log.d("List_Phone>>",phoneNumber);
                if (phoneNumber.length() == 13) {
                    list.add(phoneNumber);
                    contact_username.add(name);
                    contact_mob_no.add(phoneNumber);
                }
            }

        }
        phones.close();

        Log.d("List_Phone>>",list.toString());
        //Log.d("List_Contacts_user>>",contact_username.toString());
        //Log.d("List_Contacts_mob>>",contact_mob_no.toString());
        listRequest(list);
    }


    public void listRequest(ArrayList<String> list){
        if (utils.checkConnection(getApplicationContext()) == 1) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Updating, please wait...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            utils.contact_list_update(getApplicationContext(), list, progressDialog, new utils.VolleyCallbackJSONArray() {
                final Intent[] intent = new Intent[1];
                @Override
                public void onSuccess(JSONArray result) {
                    //Log.d("List_Contacts", result.toString());
                    //Fetch Previous saved conttacts
                    ArrayList<String> previousList = new ArrayList<String>();
                    if (source == 1){
                        Document proj = new Document().append("mob_no",1);
                        FindIterable<Document> cursor = utils.contactListCollection.find().projection(proj);

                        Iterator<Document> iterator = cursor.iterator();
                        while (iterator.hasNext()) {
                            Document document = iterator.next();
                            //document.remove("_id");
                            Map<String, Object> map = new HashMap<>(document);
                            JSONObject obj=new JSONObject(map);
                            try {
                                previousList.add(obj.getString("mob_no"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.d("contactsList", "hi"+previousList);
                    }

                    for (int i = 0; i < result.length(); i++) {
                        int index = -1;
                        try {
                            JSONObject obj = result.getJSONObject(i);
                            if ((index = contact_mob_no.indexOf(obj.getString("mob_no"))) != -1) {
                                result.getJSONObject(i).put("username", contact_username.get(index));
                                Document doc = Document.parse( result.getJSONObject(i).toString() );
                                if(source==0)
                                    utils.contactListCollection.insertOne(doc);
                                else {
                                    if (!previousList.contains(obj.getString("mob_no"))) {
                                        utils.contactListCollection.insertOne(doc);
                                    }
                                }

                            }
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    //Log.d("List_Contacts_Updated", result.toString());
                    progressDialog.dismiss();
                    if(source==0){
                        Intent intent = new Intent(contactListUpdate.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        Intent intent = new Intent(contactListUpdate.this, contactList.class);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        }
    }

    public void EnableRuntimePermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                contactListUpdate.this,
                Manifest.permission.READ_CONTACTS))
        {

            Snackbar.make(findViewById(R.id.contact_list_update),"CONTACTS permission allows us to Access CONTACTS app",Snackbar.LENGTH_SHORT).show();
        } else {

            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.READ_CONTACTS}, RequestPermissionCode);

        }
    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {

        switch (RC) {

            case RequestPermissionCode:

                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        GetContactsIntoArrayList();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Snackbar.make(findViewById(R.id.contact_list_update),"Permission Granted, Now your application can access CONTACTS.",Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(findViewById(R.id.contact_list_update),"Permission Canceled, Now your application cannot access CONTACTS.",Snackbar.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
