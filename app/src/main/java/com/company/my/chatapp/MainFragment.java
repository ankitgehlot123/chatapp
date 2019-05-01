package com.company.my.chatapp;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.company.my.chatapp.adapters.MessageAdapter;
import com.company.my.chatapp.modal.Message;
import com.company.my.chatapp.utils.Session;
import com.company.my.chatapp.utils.utils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.UpdateOptions;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * A chat fragment containing messages view and input form.
 */
public class MainFragment extends Fragment {
    static final int REQUEST_TAKE_PHOTO = 2;
    private static final String TAG = "MainFragment";
    private static final String image_webhook = "http://192.168.43.157:1880/getimage?url=";
    private static final int REQUEST_LOGIN = 0;
    private static final int TYPING_TIMER_LENGTH = 600;
    ActionBar actionBar;
    JSONObject messageObj;
    Document filter;
    UpdateOptions updateOption;
    String m_username, m_receiver = null;
    private RecyclerView mMessagesView;
    private EditText mInputMessageView;
    private List<Message> mMessages = new ArrayList<Message>();
    private RecyclerView.Adapter mAdapter;
    private boolean mTyping = false;
    private Handler mTypingHandler = new Handler();
    private String m_mob_no;
    private Date timestamp;
    private Socket mSocket;
    private boolean is_push;
    private int REQUEST_OPEN_GALLARY = 1;
    private Boolean isConnected = false;
    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "diconnected");
                    isConnected = false;
                    Snackbar.make(getActivity().findViewById(R.id.main_fragment),
                            R.string.disconnect, Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    };
    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "Error connecting");
                    Snackbar.make(getActivity().findViewById(R.id.main_fragment),
                            R.string.error_connect, Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    };
    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String status;
                    int numUsers;
                    try {
                        status = data.getString("status");
                        numUsers = data.getInt("numUsers");

                        if (numUsers == 2) {
                            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                            actionBar.setSubtitle("online");
                        }

                        //Log.d("status", "call: "+status);
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }

                    //addLog(getResources().getString(R.string.message_user_joined, status),0);
                    //addParticipantsLog(numUsers,0);
                }
            });
        }
    };
    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                        Log.d("left", "call: " + username);
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }
                    actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                    actionBar.setSubtitle(null);
                    //addLog(getResources().getString(R.string.message_user_left, username),1);
                    removeTyping(username);
                }
            });
        }
    };
    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }
                    addTyping(username, 1);
                }
            });
        }
    };
    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }
                    removeTyping(username);
                }
            });
        }
    };
    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!mTyping) return;

            mTyping = false;
            mSocket.emit("stop typing");
        }
    };
    private Uri mCurrentPhotoPath;
    private String mCurrentPhotoAbsoulutePath;
    private String mRoomid;
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isConnected) {
                        if (null != m_mob_no && null != mRoomid) {
                            JSONObject data = new JSONObject();
                            try {
                                data.put("username", m_mob_no);
                                data.put("room", mRoomid);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mSocket.emit("join_room", data);
                        }
                        Snackbar.make(getActivity().findViewById(R.id.main_fragment), R.string.connect,
                                Snackbar.LENGTH_SHORT)
                                .show();
                        isConnected = true;
                    }
                }
            });
        }
    };
    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    //Log.i("AnkitRecieved",data.toString());
                    String username;
                    String message;
                    String imageText;
                    try {
                        JSONObject jsonObject = data.getJSONObject("message");
                        Document updateDoc;
                        Log.e("jsonkima", data.toString());

                        if (jsonObject.getString("type").equals("text")) {
                            username = data.getString("username");
                            message = jsonObject.getString("message");
                            timestamp = stringToDate(jsonObject.getString("timestamp"));
                            removeTyping(username);
                            addMessage(username, message, 1, timestamp);
                            //Update MsgQ
                            jsonObject.put("trans_type", "1");
                            jsonObject.put("username", username);
                            JSONObject msgQ = new JSONObject().put("msgQ", jsonObject);
                            JSONObject doc = new JSONObject().put("$addToSet", msgQ);
                            updateDoc = Document.parse(doc.toString());
                            //Update LocalDBChat
                            utils.chatCollection.updateOne(filter, updateDoc, updateOption);
                            Log.d("dataCheck", doc.toString());
                        } else {
                            Log.e("imgOj", jsonObject.toString());
                            imageText = jsonObject.getString("message");
                            username = data.getString("username");
                            timestamp = stringToDate(jsonObject.getString("timestamp"));

                            addImage(username, Uri.parse(image_webhook + imageText), 4, 2, timestamp);

                            //Update MsgQ
                            jsonObject.put("trans_type", "4");
                            jsonObject.put("username", username);
                            JSONObject msgQ = new JSONObject().put("msgQ", jsonObject);
                            JSONObject doc = new JSONObject().put("$addToSet", msgQ);
                            updateDoc = Document.parse(jsonObject.toString());
                            Log.e("docdoc", updateDoc.toString());
                            //utils.chatCollection.updateOne(filter,updateDoc,updateOption);
                        }

                        //Update Recent Chat in Contact List
                        updateContactList();
                        //Delete the msgQ
                        if (data.has("type")) {
                            JSONObject time = new JSONObject();
                            time.put("room", mRoomid);
                            time.put("timestamp", timestamp);
                            mSocket.emit("delete_msgQ", time);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }
                }
            });
        }
    };


    public MainFragment() {
        super();
    }

    // This event fires 1st, before creation of fragment or any views
    // The onAttach method is called when the Fragment instance is associated with an Activity.
    // This does not mean the Activity is fully initialized.
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mAdapter = new MessageAdapter(context, mMessages);
        if (context instanceof Activity) {
            //this.listener = (MainActivity) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        Fresco.initialize(getContext());
        ChatApplication app = (ChatApplication) getActivity().getApplication();
        mSocket = app.getSocket();
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("new message", onNewMessage);
        mSocket.on("user joined", onUserJoined);
        mSocket.on("user left", onUserLeft);
        mSocket.on("typing", onTyping);
        mSocket.on("stop typing", onStopTyping);
        mSocket.connect();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Session session = new Session(getContext());
        m_mob_no = session.getMob_no().replace("+", "");
        Bundle bundle = this.getArguments();
        m_username = bundle.getString("username");
        m_receiver = bundle.getString("mob_no").replace("+", "");
        if (Long.parseLong(m_mob_no) > Long.parseLong(m_receiver))
            mRoomid = m_mob_no + m_receiver;
        else
            mRoomid = m_receiver + m_mob_no;
        if ((is_push = bundle.getBoolean("is_push", false)) == true) {
            try {
                messageObj = new JSONObject(bundle.getString("data"));
                //addPushMessage(m_username,messageObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


        //MongoDB Parameters
        filter = new Document().append("room_id", mRoomid);
        updateOption = new UpdateOptions().upsert(true);
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.emit("leave_room");
        mSocket.disconnect();

        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("new message", onNewMessage);
        mSocket.off("user joined", onUserJoined);
        mSocket.off("user left", onUserLeft);
        mSocket.off("typing", onTyping);
        mSocket.off("stop typing", onStopTyping);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMessagesView = view.findViewById(R.id.messages);
        mMessagesView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMessagesView.setAdapter(mAdapter);
        mInputMessageView = view.findViewById(R.id.message_input);
        mInputMessageView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == R.id.send) {
                    attemptSend();
                    return true;
                }
                return false;
            }
        });
        mInputMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (null == m_mob_no) return;
                if (!mSocket.connected()) return;

                if (!mTyping) {
                    mTyping = true;
                    mSocket.emit("typing");
                }

                mTypingHandler.removeCallbacks(onTypingTimeout);
                mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ImageButton sendButton = (ImageButton) view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });

        FindIterable<Document> cursor = utils.chatCollection.find(filter);
        Iterator<Document> iterator = cursor.iterator();
        if (iterator.hasNext()) {
            Document document = iterator.next();
            //Log.d("fetched", document.toJson());
            Map<String, Object> map = new HashMap<>(document);
            try {
                JSONArray messages = new JSONObject(map).getJSONArray("msgQ");
                for (int i = 0; i < messages.length(); i++) {
                    JSONObject jsonObject = messages.getJSONObject(i);
                    if (jsonObject.getString("type").equals("text")) {
                        Log.e("check111", stringToDate(jsonObject.getString("timestamp")).toString());
                        if (jsonObject.has("trans_type"))
                            addMessage(jsonObject.getString("username"), jsonObject.getString("message"), 1, stringToDate(jsonObject.getString("timestamp")));
                        else
                            addMessage(m_mob_no, jsonObject.getString("message"), 0, stringToDate(jsonObject.getString("timestamp")));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("asdf", Activity.RESULT_OK + " " + requestCode + " " + resultCode);
        if (Activity.RESULT_OK != resultCode) {
            Log.i("asdf", "in");
            getActivity().finish();
            return;
        } else {
            /*Intent Switch from main to chat screen
            if(requestCode == 0) {
                Log.i("fragment in","in");
                mRoomid = data.getStringExtra("room_id");
                m_mob_no = data.getStringExtra("username");
            }*/
            //Gallery result
            if (requestCode == 1) {
                Log.i("gallery", data.toString());
                Uri selectedImage = data.getData();
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i("galleryImagePath", ":" + selectedImage.toString());
                sendImage(bitmap, selectedImage, 3, 0);
            }
            //Camera result
            else {
                ParcelFileDescriptor parcelFileDescriptor = null;
                try {
                    parcelFileDescriptor = getActivity().getContentResolver().openFileDescriptor(mCurrentPhotoPath, "r");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                sendImage(BitmapFactory.decodeFileDescriptor(fileDescriptor), mCurrentPhotoPath, 3, 1);
            }
        }
    }

    public void sendImage(Bitmap bmp, Uri uri, int trans_type, int source_type) {
        JSONObject sendData = new JSONObject();
        try {
            timestamp = new Date();
            sendData.put("type", "image");
            sendData.put("message", encodeImage(bmp));
            sendData.put("timestamp", timestamp);
            addImage(m_mob_no, uri, trans_type, source_type, timestamp);
            mSocket.emit("new message", sendData);
        } catch (JSONException e) {

        }
    }

    private void galleryAddPic(Uri contentUri) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, contentUri.getPath());

        getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void addImage(String username, Uri uri, int trans_type, int source_type, Date timestamp) {

        Log.i("ankit", uri.toString());
        if (trans_type == 3)
            mMessages.add(new Message.Builder(Message.TYPE_MESSAGE_IMAGE_SENDER).username(username).timestamp(timestamp).image(uri).build());
        else if (trans_type == 4)
            mMessages.add(new Message.Builder(Message.TYPE_MESSAGE_IMAGE_RECEIVER).username(username).timestamp(timestamp).image(uri).build());
        mAdapter = new MessageAdapter(getContext(), mMessages);
        mAdapter.notifyItemInserted(0);
        galleryAddPic(uri);
        scrollToBottom();
    }

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encImage;
    }

    private Bitmap decodeImage(String data) {
        byte[] b = Base64.decode(data, Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
        return bmp;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_attach:
                Log.d("onOptionsItemSelected", "action_attach");
                openGallery();
                return true;
            case R.id.action_capture:
                Log.d("onOptionsItemSelected", "action_capture");
                openCamera();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void openCamera() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(0);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i("File Creation Error!", ex.getMessage());
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "com.company.my.chatapp",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    takePictureIntent.setClipData(ClipData.newRawUri("", photoURI));
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void openGallery() {
        Intent galleryintent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryintent, REQUEST_OPEN_GALLARY);
    }

    private File createImageFile(int Folder_saveImageType) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = null;
        if (Folder_saveImageType == 0)
            storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/sent");
        else
            storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/received");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoAbsoulutePath = image.getAbsolutePath();
        mCurrentPhotoPath = Uri.fromFile(image);
        return image;
    }

    private void addLog(String message, int trans_type) {
        mMessages.add(new Message.Builder(Message.TYPE_LOG)
                .message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void addParticipantsLog(int numUsers, int trans_type) {
        addLog(getResources().getQuantityString(R.plurals.message_participants, numUsers, numUsers), trans_type);
    }

    private void addMessage(String username, String message, int trans_type, Date timestamp) {
        Log.i("Transtype_inFragment", "x" + trans_type);
        if (trans_type == 0)
            mMessages.add(new Message.Builder(Message.TYPE_MESSAGE_SENDER)
                    .username(username).message(message).timestamp(timestamp).build());
        else if (trans_type == 1) {
            mMessages.add(new Message.Builder(Message.TYPE_MESSAGE_RECEIVER)
                    .username(username).message(message).timestamp(timestamp).build());
        }
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void addPushMessage(String username, JSONObject messageObj) {


        String message = null;
        String imageText;
        try {

            Document updateDoc;

            if (messageObj.getString("type").equals("text")) {
                message = messageObj.getString("message");
                timestamp = stringToDate(messageObj.getString("timestamp"));
                removeTyping(username);
                addMessage(username, message, 1, timestamp);
                //Update MsgQ
                messageObj.put("trans_type", "1");
                JSONObject msgQ = new JSONObject().put("msgQ", messageObj);
                JSONObject doc = new JSONObject().put("$addToSet", msgQ);
                updateDoc = Document.parse(doc.toString());
                //Update LocalDBChat
                utils.chatCollection.updateOne(filter, updateDoc, updateOption);
                Log.d("dataCheck", doc.toString());
            } else {
                Log.e("imgOj", messageObj.toString());
                imageText = messageObj.getString("message");
                timestamp = stringToDate(messageObj.getString("timestamp"));

                addImage(username, Uri.parse(image_webhook + imageText), 4, 2, timestamp);

                //Update MsgQ
                messageObj.put("trans_type", "4");
                JSONObject msgQ = new JSONObject().put("msgQ", messageObj);
                JSONObject doc = new JSONObject().put("$addToSet", msgQ);
                updateDoc = Document.parse(messageObj.toString());
                Log.e("docdoc", updateDoc.toString());
                //utils.chatCollection.updateOne(filter,updateDoc,updateOption);
            }

            //Update Recent Chat in Contact List
            updateContactList();
            //Delete the msgQ
            if (messageObj.has("type")) {
                JSONObject time = new JSONObject();
                time.put("room", mRoomid);
                time.put("timestamp", timestamp);
                mSocket.emit("delete_msgQ", time);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            return;
        }
    }

    private void addTyping(String username, int trans_type) {
        mMessages.add(new Message.Builder(Message.TYPE_ACTION)
                .username(username).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void removeTyping(String username) {
        for (int i = mMessages.size() - 1; i >= 0; i--) {
            Message message = mMessages.get(i);
            if (message.getType() == Message.TYPE_ACTION && message.getUsername().equals(username)) {
                mMessages.remove(i);
                mAdapter.notifyItemRemoved(i);
            }
        }
    }

    private void attemptSend() {
        if (null == m_mob_no) return;
        if (!mSocket.connected()) return;

        mTyping = false;

        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        mInputMessageView.setText("");
        timestamp = new Date();

        addMessage(m_mob_no, message, 0, timestamp);
        JSONObject sendData = new JSONObject();
        Document updateDoc = new Document();
        try {
            sendData.put("type", "text");
            sendData.put("message", message);
            sendData.put("timestamp", timestamp);
            //Update MsgQ
            JSONObject msgQ = new JSONObject().put("msgQ", sendData);
            JSONObject doc = new JSONObject().put("$addToSet", msgQ);
            updateDoc = Document.parse(doc.toString());
            //Update Recent Chat in Contact List
            updateContactList();
            //Log.d("dataCheck",doc.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //perform the sending message attempt.
        mSocket.emit("new message", sendData);
        //Update LocalDB
        utils.chatCollection.updateOne(filter, updateDoc, updateOption);
    }


    private void leave() {
        m_mob_no = null;
        mSocket.emit("leave_room");
    }


    private void scrollToBottom() {
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    private Date stringToDate(String aDate) {
        Date date = null;
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT+05:30' yyyy");

        try {
            date = format.parse(aDate);
            System.out.println(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private void updateContactList() throws JSONException {
        //Update Recent Chat in Contact List
        Date now = new Date();
        Document date = new Document().append("timestamp", now);
        Document set = new Document().append("$set", date);


        Document updateFilter = new Document().append("mob_no", "+" + m_receiver);
        utils.contactListCollection.updateOne(updateFilter, set);
    }
}

