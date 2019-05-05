package com.company.my.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.my.chatapp.utils.Session;
import com.company.my.chatapp.utils.utils;
import com.google.firebase.auth.FirebaseAuth;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;


public class profile extends AppCompatActivity {
    Session session = new Session(profile.this);
    ImageView image;
    EditText user_name;
    String user_name_text;
    Button update;
    String imageString = null;
    Bitmap bitmap = null;
    ProgressDialog progressDialog;
    FirebaseAuth fbAuth;
    com.company.my.chatapp.utils.utils utils = new utils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //int source=getIntent().getIntExtra("source",0);
        fbAuth = FirebaseAuth.getInstance();

        image = findViewById(R.id.profile_pic);
        user_name = findViewById(R.id.user_name);
        update = findViewById(R.id.update);
       /* if(source == 1){
            user_name_text= getIntent().getStringExtra("username");
            imageString=getIntent().getStringExtra("contact_pic");
            Log.e("nana",imageString);
            setup_name_pic(1);
            LinearLayout linearLayout=findViewById(R.id.profile_buttons);
            ImageButton imageButton=findViewById(R.id.change_image);
            user_name.setEnabled(false);
            imageButton.setVisibility(View.GONE);
            linearLayout.setVisibility(View.GONE);

       }else*/
        setup_name_pic(0);
    }

    private void setup_name_pic(int source) {
        if (session.getUsername() != "") {
            if (source == 1)
                user_name.setText(user_name_text);
            else
                user_name.setText(session.getUsername());
        }
        if (session.getProfilePic() != "") {
            if (source == 1) {
                byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
                Bitmap decodeByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                decodeByte.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] b = baos.toByteArray();
                decodedString = Base64.decode(b, Base64.DEFAULT);
                decodeByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                image.setImageBitmap(decodeByte);
            } else {
                byte[] decodedString = Base64.decode(session.getProfilePic(), Base64.DEFAULT);
                Bitmap decodeByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                image.setImageBitmap(decodeByte);
            }
        }
    }


    //opening image chooser option
    public void open_gallery(View v) {
        CropImage.activity()
                .setActivityTitle("Upload Image")
                .setActivityMenuIconColor(getResources().getColor(R.color.colorWhite))
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .setFixAspectRatio(true)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                image.setImageURI(resultUri);
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }


    public void update(View v) {
        if (utils.checkConnection(getApplicationContext()) == 1) {
            progressDialog = new ProgressDialog(profile.this);
            progressDialog.setMessage("Updating Profile, please wait...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            Intent prevIntent = getIntent();

            //converting image to base64 string
            if (bitmap != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            }


            JSONObject postparams = new JSONObject();
            try {
                postparams.put("id", session.getUserId());
                postparams.put("mob_no", session.getMob_no());
                postparams.put("username", user_name.getText());
                postparams.put("regisToken", session.getRegisToken());
                Log.wtf("regisToken",session.getRegisToken());
                if (imageString != null) {
                    postparams.put("pic", imageString);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            utils.update_profile(getApplicationContext(), postparams, progressDialog);

        } else {
            Snackbar.make(findViewById(R.id.update_prof), "No Connection",
                    Snackbar.LENGTH_SHORT)
                    .show();

        }
    }

    public void logout(View view) {
        if (utils.checkConnection(getApplicationContext()) == 1) {
                progressDialog = new ProgressDialog(profile.this);
                progressDialog.setMessage("Logging Out..");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
               utils.logout(profile.this, session.getMob_no(), progressDialog);
        } else {
            Snackbar.make(findViewById(R.id.login), "No Connection", Snackbar.LENGTH_SHORT).show();
        }
        utils.clearShared(this);
        fbAuth.signOut();
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("isFirstRun", true).apply();
        Intent intent = new Intent(this, login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        this.startActivity(intent);
        finish();
        Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
    }
}
