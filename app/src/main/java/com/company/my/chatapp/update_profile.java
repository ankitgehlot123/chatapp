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

import com.company.my.chatapp.utils.Session;
import com.company.my.chatapp.utils.utils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class update_profile extends AppCompatActivity {
    Session session = new Session(update_profile.this);
    ImageView image;
    EditText user_name;
    Button update;
    String imageString = null;
    Bitmap bitmap = null;
    ProgressDialog progressDialog;
    com.company.my.chatapp.utils.utils utils = new utils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        getIntent().getIntExtra("source",0);
        image = (ImageView) findViewById(R.id.profile_pic);
        user_name = (EditText) findViewById(R.id.user_name);
        update = (Button) findViewById(R.id.update);

        setup_name_pic();
    }

    private void setup_name_pic() {
        if (session.getUsername() != "") {
            user_name.setText(session.getUsername());
        }
        if (session.getProfilePic() != "") {
            byte[] decodedString = Base64.decode(session.getProfilePic(), Base64.DEFAULT);
            Bitmap decodeByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            image.setImageBitmap(decodeByte);
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
            progressDialog = new ProgressDialog(update_profile.this);
            progressDialog.setMessage("Uploading, please wait...");
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
                postparams.put("regisToken",session.getRegisToken());
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
}
