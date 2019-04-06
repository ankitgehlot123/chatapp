package com.company.my.chatapp;

import android.app.Activity;
import android.content.ClipData;
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
import android.widget.Toast;

import com.company.my.chatapp.adapters.MessageAdapterGroup;
import com.company.my.chatapp.modal.Message;
import com.facebook.drawee.backends.pipeline.Fresco;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * A chat fragment containing messages view and input form.
 */
public class GroupFragment extends Fragment {

    private static final String TAG = "GroupFragment";
    static final int REQUEST_TAKE_PHOTO = 2;
    private static final int REQUEST_LOGIN = 0;
    private static  final String image_webhook="http://192.168.43.157:1880/getimage?url=";
    private static final int TYPING_TIMER_LENGTH = 600;
    private RecyclerView mMessagesView;
    private EditText mInputMessageView;
    private List<Message> mMessages = new ArrayList<Message>();
    private RecyclerView.Adapter mAdapter;
    private boolean mTyping = false;
    private Handler mTypingHandler = new Handler();
    private String mUsername;
    private Date timestamp;
    private Socket mSocket;
    private int REQUEST_OPEN_GALLARY=1;
    private Boolean isConnected = true;
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!isConnected) {
                        if(null!=mUsername && null !=mRoomid) {
                            JSONObject data = new JSONObject();
                            try {
                                data.put("username", mUsername);
                                data.put("room", mRoomid);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mSocket.emit("Group join_room", data);
                        }
                        Snackbar.make(getActivity().findViewById(R.id.group_fragment),R.string.connect,Snackbar.LENGTH_SHORT).show();
                        isConnected = true;
                    }
                }
            });
        }
    };
    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "diconnected");
                    isConnected = false;
                    Snackbar.make(getActivity().findViewById(R.id.group_fragment),R.string.disconnect,Snackbar.LENGTH_SHORT).show();

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
                    Snackbar.make(getActivity().findViewById(R.id.group_fragment),R.string.error_connect,Snackbar.LENGTH_SHORT).show();


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

                    String username;
                    String message;
                    String imageText;
                    try {
                        JSONObject jsonObject = data.getJSONObject("message");

                        if(jsonObject.getString("type").equals("text")){
                            username = data.getString("username");
                            message = jsonObject.getString("message");

                            removeTyping(username);
                            Log.i("check",jsonObject.getString("timestamp").toString());
                            timestamp= stringToDate(jsonObject.getString("timestamp"));
                            addMessage(username, message,1,timestamp);
                        } else {
                            imageText = jsonObject.getString("message");
                            username = data.getString("username");
                            timestamp= stringToDate(jsonObject.getString("timestamp"));
                            addImage(username,Uri.parse(image_webhook+imageText),4,2,timestamp);

                        }
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }
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
                        //Log.d("status", "call: "+status);
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }

                    addLog(getResources().getString(R.string.message_user_joined, status),0);
                    addParticipantsLog(numUsers,0);
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
                        Log.d("left", "call: "+username);
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }

                    addLog(getResources().getString(R.string.message_user_left, username),1);
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
                    addTyping(username,1);
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
            mSocket.emit("Group stop typing");
        }
    };
    private Uri mCurrentPhotoPath;
    private String mCurrentPhotoAbsoulutePath;
    private String mRoomid;

    public GroupFragment() {
        super();
    }

    // This event fires 1st, before creation of fragment or any views
    // The onAttach method is called when the Fragment instance is associated with an Activity.
    // This does not mean the Activity is fully initialized.
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mAdapter = new MessageAdapterGroup(context, mMessages);
        if (context instanceof Activity){
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
        mSocket.on(Socket.EVENT_CONNECT,onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT,onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("Group new message", onNewMessage);
        mSocket.on("Group user joined", onUserJoined);
        mSocket.on("Group user left", onUserLeft);
        mSocket.on("Group typing", onTyping);
        mSocket.on("Group stop typing", onStopTyping);
        mSocket.connect();

        startSignIn();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group, container, false);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.emit("Group leave_room");
        mSocket.disconnect();

        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("Group new message", onNewMessage);
        mSocket.off("Group user joined", onUserJoined);
        mSocket.off("Group user left", onUserLeft);
        mSocket.off("Group typing", onTyping);
        mSocket.off("Group stop typing", onStopTyping);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMessagesView = (RecyclerView) view.findViewById(R.id.messages);
        mMessagesView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMessagesView.setAdapter(mAdapter);
        mInputMessageView = (EditText) view.findViewById(R.id.message_input);
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
                if (null == mUsername) return;
                if (!mSocket.connected()) return;

                if (!mTyping) {
                    mTyping = true;
                    mSocket.emit("Group typing");
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("asdf", Activity.RESULT_OK+" "+requestCode+" "+resultCode);
        if (Activity.RESULT_OK != resultCode) {
            Log.i("asdf","in");
            getActivity().finish();
            return;
        }else {
            //Intent Switch from main to chat screen
            if(requestCode == 0) {
                Log.i("fragment in","in");
                mRoomid = data.getStringExtra("room_id");
                mUsername = data.getStringExtra("username");
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(mRoomid );
            }
            //Gallery result
            else if(requestCode == 1){
                Log.i("gallery",data.toString());
                Uri selectedImage = data.getData();
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i("galleryImagePath",":"+selectedImage.toString());
                sendImage(bitmap,selectedImage,3,0);
            }
            //Camera result
            else{
                ParcelFileDescriptor parcelFileDescriptor = null;
                try {
                    parcelFileDescriptor = getActivity().getContentResolver().openFileDescriptor(mCurrentPhotoPath, "r");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                sendImage(BitmapFactory.decodeFileDescriptor(fileDescriptor),mCurrentPhotoPath,3,1);
            }
        }
    }
    public void sendImage(Bitmap bmp, Uri uri, int trans_type, int source_type)
    {
        timestamp=new Date();
        JSONObject sendData = new JSONObject();
        try{
            sendData.put("type","image");
            sendData.put("message", encodeImage(bmp));
            sendData.put("timestamp",timestamp);
            addImage(mUsername,uri,trans_type,source_type,timestamp);
            mSocket.emit("Group new message",sendData);
        }catch(JSONException e){

        }
    }
    private void galleryAddPic(Uri contentUri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(contentUri);
        getContext().sendBroadcast(mediaScanIntent);
    }
    private void addImage(String username, Uri uri, int trans_type, int source_type,Date timestamp){

        Log.i("ankit",uri.toString());
        if(trans_type==3)
            mMessages.add(new Message.Builder(Message.TYPE_MESSAGE_IMAGE_SENDER).username(username).timestamp(timestamp).image(uri).build());
        else if(trans_type==4)
            mMessages.add(new Message.Builder(Message.TYPE_MESSAGE_IMAGE_RECEIVER).username(username).timestamp(timestamp).image(uri).build());
        mAdapter = new MessageAdapterGroup(getContext(),mMessages);
        mAdapter.notifyItemInserted(0);
        galleryAddPic(uri);
        scrollToBottom();
    }
    private String encodeImage(Bitmap bitmap)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,70,baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encImage;
    }

    private Bitmap decodeImage(String data)
    {
        byte[] b = Base64.decode(data, Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(b,0,b.length);
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
        switch(id){
            case R.id.action_attach:
                Log.d("onOptionsItemSelected","action_attach");
                openGallery();
                return true;
            case R.id.action_capture:
                Log.d("onOptionsItemSelected","action_capture");
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
                Log.i("File Creation Error!",ex.getMessage());
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "com.company.my.chatapp",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                if ( Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP ) {
                    takePictureIntent.setClipData( ClipData.newRawUri( "", photoURI ) );
                    takePictureIntent.addFlags( Intent.FLAG_GRANT_WRITE_URI_PERMISSION| Intent.FLAG_GRANT_READ_URI_PERMISSION );
                }
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void openGallery()
    {
        Intent galleryintent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryintent, REQUEST_OPEN_GALLARY);
    }

    private File createImageFile(int Folder_saveImageType) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir=null;
        if(Folder_saveImageType==0)
            storageDir  =  getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES+"/sent");
        else
            storageDir =  getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES+"/received");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoAbsoulutePath = image.getAbsolutePath();
        mCurrentPhotoPath= Uri.fromFile(image);
        return image;
    }
    private void addLog(String message, int trans_type) {
        mMessages.add(new Message.Builder(Message.TYPE_LOG)
                .message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void addParticipantsLog(int numUsers,int trans_type) {
        addLog(getResources().getQuantityString(R.plurals.message_participants, numUsers, numUsers),trans_type);
    }

    private void addMessage(String username, String message, int trans_type,Date timestamp) {
        Log.i("Transtype_inFragment","x"+trans_type);
        if(trans_type==0)
            mMessages.add(new Message.Builder(Message.TYPE_MESSAGE_SENDER)
                    .username(username).message(message).timestamp(timestamp).build());
        else if(trans_type==1){
            mMessages.add(new Message.Builder(Message.TYPE_MESSAGE_RECEIVER)
                    .username(username).message(message).timestamp(timestamp).build());
        }
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
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
        if (null == mUsername) return;
        if (!mSocket.connected()) return;

        mTyping = false;

        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        mInputMessageView.setText("");
        timestamp= new Date();
        addMessage(mUsername, message,0,timestamp);
        JSONObject sendData = new JSONObject();
        try {
            sendData.put("type","text");
            sendData.put("message",message);
            sendData.put("timestamp",timestamp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // perform the sending message attempt.
        mSocket.emit("Group new message", sendData);
    }

    private void startSignIn() {
        mUsername = null;
        Intent intent = new Intent(getActivity(), GroupNameActivity.class);
        startActivityForResult(intent, REQUEST_LOGIN);
    }

    private void leave() {
        mUsername = null;
        mSocket.emit("Group leave_room");
        startSignIn();
    }


    private void scrollToBottom() {
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    private Date stringToDate(String aDate) {
        Date date=null;
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT+05:30' yyyy");

        try {
            date = format.parse(aDate);
            System.out.println(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

}

