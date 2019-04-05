package com.company.my.chatapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.company.my.chatapp.utils.Session;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;


/**
 * A login screen that offers login via username.
 */
public class GroupNameActivity extends Activity {

    Session session;
    private EditText mGroupName;
    private String room_id = null;
    private String groupName;
    private Socket mSocket;
    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

            String status;
            try {
                status = data.getString("status");

                if (status.equals("true")) {
                    Log.d("Check1111", "true");
                    Intent intent = new Intent();
                    intent.putExtra("username", session.getUsername());
                    intent.putExtra("room_id", groupName);
                    setResult(RESULT_OK, intent);
                    finish();
                }

            } catch (JSONException e) {
                return;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_name);

        ChatApplication app = (ChatApplication) getApplication();
        mSocket = app.getSocket();
        session = new Session(getApplicationContext());
        // Set up the login form.
        mGroupName = (EditText) findViewById(R.id.groupName);

        mSocket.on("Group login", onLogin);
    }

    public void onRoomSelect(View view) {
        attemptLogin();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.off("Group login", onLogin);
    }

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mGroupName.setError(null);

        // Store values at the time of the login attempt.
        String username = mGroupName.getText().toString().trim();

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            mGroupName.setError(getString(R.string.error_field_required));
            mGroupName.requestFocus();
            return;
        }

        groupName = username;
        Log.i("chat", "" + groupName);
        JSONObject data = new JSONObject();
        try {
            data.put("username", session.getUsername());
            data.put("room", groupName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("Group join_room", data);
    }
}



