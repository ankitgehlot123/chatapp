package com.company.my.chatapp.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.my.chatapp.contactListUpdate;
import com.company.my.chatapp.login;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.services.mongodb.local.LocalMongoDbService;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class utils extends Activity {

    static public String url = "http://192.168.43.81:1880/";
    static public String profile_url = "http://192.168.43.81:1880/ChatApp?pic=";

    // Create the default Stitch Client
    static public StitchAppClient client = Stitch.getAppClient("chatapp-acbgk");

    // Create a Client for MongoDB Mobile (initializing MongoDB Mobile)
    static public MongoClient mobileClient = client.getServiceClient(LocalMongoDbService.clientFactory);

    // Point to the Database
    static public MongoDatabase localDatabase = mobileClient.getDatabase("ChatApp");

    // Point to the target collection and insert a document
    static public MongoCollection<Document> contactListCollection = utils.localDatabase.getCollection("contactList");
    static public MongoCollection<Document> chatCollection = utils.localDatabase.getCollection("chatMessage");
    static public MongoCollection<Document> chatListCollection = utils.localDatabase.getCollection("chatList");
    //public  static boolean result = false;
    public static int maxLenght = 10;

    // shared methods
    public static void setShared(Context context, String name, String value) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(name, value);
        editor.commit();
    }

    public static String getShared(Context context, String name, String defaultValue) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return prefs.getString(name, defaultValue);

    }

    //Check Internet Connectivity
    public static int checkConnection(Context context) {

        boolean wifiConnected;
        boolean mobileConnected;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        int status = 0;
        if (networkInfo != null && networkInfo.isConnected()) {
            wifiConnected = networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            if (wifiConnected)
                status = 1;
            else if (mobileConnected)
                status = 1;
            else
                status = 0;
        }
        return status;
    }

    //contact_list_update
    public static void contact_list_update(final Context context, final ArrayList<String> list, final ProgressDialog progressDialog, final VolleyCallbackJSONArray callback) {
        JSONObject postparams = new JSONObject();
        try {
            JSONArray jsonArray = new JSONArray(list);
            String dataToSend = jsonArray.toString();
            //Log.d("List_Phone_Array",jsonArray.toString());
            postparams.put("list", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //sending mob_no to server
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                url + "update_contact_list",
                postparams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject res) {
                        JSONArray contactList;
                        try {
                            contactList = res.getJSONArray("contactList");
                            callback.onSuccess(contactList);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressDialog.dismiss();
                Toast.makeText(context, "Some error occurred" + volleyError, Toast.LENGTH_LONG).show();
            }
        });
        // Adding the request to the queue along with a unique string tag
        RequestQueue rQueue = Volley.newRequestQueue(context);
        rQueue.add(request);
        request.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    //Login
    public void login(final Context context, final String mob_no_string, final ProgressDialog progressDialog, final VolleyCallback callback) {
        JSONObject postparams = new JSONObject();
        try {
            postparams.put("mob_no", mob_no_string);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //sending mob_no to server
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                url + "login",
                postparams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject res) {
                        progressDialog.dismiss();
                        String log_status = null;
                        try {
                            log_status = res.getString("log_status");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (log_status.equals("true")) {
                            callback.onSuccess("true");
                        } else {
                            Toast.makeText(context, "Some error occurred", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressDialog.dismiss();
                Toast.makeText(context, "Some error occurred" + volleyError, Toast.LENGTH_LONG).show();
            }
        });
        // Adding the request to the queue along with a unique string tag
        RequestQueue rQueue = Volley.newRequestQueue(context);
        rQueue.add(request);
        request.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public void auth(final Context context, final String mobtext, final String otptext, final ProgressDialog progressDialog,
                     final VolleyCallback callback) {

        final Session session = new Session(context);
        JSONObject postparams = new JSONObject();
        try {
            postparams.put("mob_no", mobtext);
            postparams.put("otp", otptext);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //sending mob_no & otp to server
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                url + "auth",
                postparams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject res) {
                        progressDialog.dismiss();
                        try {
                            String auth_status = res.getString("auth_status");
                            if (auth_status.equals("true")) {

                                session.setMob_no(mobtext);
                                if (res.has("_id"))
                                    session.setUserId(res.getString("_id"));
                                if (res.has("username"))
                                    session.setUsername(res.getString("username"));
                                if (res.has("pic"))
                                    session.setProfilePic(res.getString("pic"));
                                callback.onSuccess("true");
                            } else if (auth_status.equals("invalid_otp")) {
                                Toast.makeText(context, "Invalid Otp", Toast.LENGTH_SHORT).show();
                            } else if (auth_status.equals("otp_expire")) {
                                Toast.makeText(context, "Otp Expire", Toast.LENGTH_SHORT).show();
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(context, "Some error occurred", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressDialog.dismiss();
                Toast.makeText(context, "Some error occurred" + volleyError, Toast.LENGTH_LONG).show();
            }
        });
        // Adding the request to the queue along with a unique string tag
        RequestQueue rQueue = Volley.newRequestQueue(context);
        rQueue.add(request);
        request.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    //Authentication

    public void logout(Context context) {
        //Clear all the Session Values
        final Session session = new Session(context);
        session.ClearSession();
        Intent intent = new Intent(context, login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        finish();
        Toast.makeText(context, "LogOut Successfully", Toast.LENGTH_SHORT).show();
    }

    public void update_profile(final Context context, final JSONObject postparams, final ProgressDialog progressDialog) {
        final Session session = new Session(context);
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                utils.url + "update_profile",
                postparams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject res) {
                        progressDialog.dismiss();
                        String status = null;
                        try {
                            status = res.getString("status");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (status.equals("true")) {
                            //Toast.makeText(update_profile.this, "Uploaded Successful", Toast.LENGTH_LONG).show();
                            //Set the Session Variables
                            try {

                                session.setUsername(postparams.getString("username"));
                                if (postparams.getString("pic") != null) {
                                    session.setProfilePic(postparams.getString("pic"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Intent intent = new Intent(context, contactListUpdate.class);
                            // set the new task and clear flags
                            intent.putExtra("source",0);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            context.startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(context, "Some error occurred", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressDialog.dismiss();
                Toast.makeText(context, "Some error occurred" + volleyError, Toast.LENGTH_LONG).show();
            }
        });
        // Adding the request to the queue along with a unique string tag
        RequestQueue rQueue = Volley.newRequestQueue(context);
        rQueue.add(request);
        request.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    //Volley Callback
    public interface VolleyCallback {
        void onSuccess(String result);
    }

    //Volley Callback
    public interface VolleyCallbackJSONArray {
        void onSuccess(JSONArray result);
    }
}
