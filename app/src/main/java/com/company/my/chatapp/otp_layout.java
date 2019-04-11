package com.company.my.chatapp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.company.my.chatapp.utils.Session;
import com.company.my.chatapp.utils.utils;

import org.apache.http.NameValuePair;

import java.util.List;

public class otp_layout extends Activity {
    private Session session= new Session(this);
    private static final int REQUEST_CODE = 1;
    EditText ed_otp, ed_otp1, ed_otp2, ed_otp3;
    Button otp_button;
    String otptext, mobtext;
    TextView resend_otp;
    List<NameValuePair> params;
    ProgressDialog progressDialog;
    com.company.my.chatapp.utils.utils utils = new utils();
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_layout);
        //otp = (EditText)findViewById(R.id.otp);

        resend_otp = (TextView) findViewById(R.id.resend_otp);

        ed_otp = findViewById(R.id.ed_otp);
        ed_otp1 = findViewById(R.id.ed_otp1);
        ed_otp2 = findViewById(R.id.ed_otp2);
        ed_otp3 = findViewById(R.id.ed_otp3);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) {
            fetchOtp();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, REQUEST_CODE);
        }
        startEditTextListner();


        //Count Down Timer for Resend Otp
        new CountDownTimer(120000, 1000) {

            @Override
            public void onTick(long l) {
                resend_otp.setText("Resend OTP in " + l / 1000 + "sec");
            }

            @Override
            public void onFinish() {
                resend_otp.setEnabled(true);
                resend_otp.setText("Resend OTP Now");
            }
        }.start();
    }


    public void submit_otp() {
        if (utils.checkConnection(getApplicationContext()) == 1) {
            otptext = ed_otp.getText().toString() +
                    ed_otp1.getText().toString() +
                    ed_otp2.getText().toString() +
                    ed_otp3.getText().toString();

            mobtext = getIntent().getStringExtra("mob_no");
            ProgressDialog progressDialog = new ProgressDialog(otp_layout.this);
            progressDialog.setMessage("Login...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            utils.auth(getApplicationContext(), mobtext, otptext, progressDialog, new utils.VolleyCallback() {
                final Intent[] intent = new Intent[1];

                @Override
                public void onSuccess(String result) {
                    if (result.equals("true")) {
                        intent[0] = new Intent(otp_layout.this, update_profile.class);
                        startActivity(intent[0]);
                        finish();
                    }
                }
            });
        } else

        Snackbar.make(findViewById(R.id.otp_parent) ,"No Connection", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                fetchOtp();
            } else {

                Snackbar.make(findViewById(R.id.otp_parent) ,"You denied the permission", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("otp"));
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ed_otp3.setText("");
        ed_otp2.setText("");
        ed_otp1.setText("");
        ed_otp.setText("");
    }


    private void startEditTextListner() {

        ed_otp.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
                if (keyCode == KeyEvent.KEYCODE_DEL) {

                    ed_otp3.setText("");
                    ed_otp2.setText("");
                    ed_otp1.setText("");
                    ed_otp.setText("");
                    ed_otp.requestFocus();
                } else if (ed_otp.getText().toString().trim().length() == 1)
                    ed_otp1.requestFocus();

                return false;
            }
        });
        ed_otp1.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
                if (keyCode == KeyEvent.KEYCODE_DEL) {

                    ed_otp3.setText("");
                    ed_otp2.setText("");
                    ed_otp1.setText("");
                    ed_otp.setText("");
                    ed_otp.requestFocus();
                } else {
                    if (ed_otp1.getText().toString().trim().length() == 1)
                        ed_otp2.requestFocus();
                }
                return false;
            }
        });
        ed_otp2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
                if (keyCode == KeyEvent.KEYCODE_DEL) {

                    ed_otp3.setText("");
                    ed_otp2.setText("");
                    ed_otp1.setText("");
                    ed_otp.setText("");
                    ed_otp.requestFocus();

                } else {
                    if (ed_otp2.getText().toString().trim().length() == 1)
                        ed_otp3.requestFocus();
                }

                return false;
            }
        });
        ed_otp3.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_

                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    ed_otp3.setText("");
                    ed_otp2.setText("");
                    ed_otp1.setText("");
                    ed_otp.setText("");
                    ed_otp.requestFocus();

                } else {

                    if (ed_otp3.getText().toString().trim().length() == 1) {
                        submit_otp();
                        ed_otp3.clearFocus();
                        ed_otp2.clearFocus();
                        ed_otp1.clearFocus();
                        ed_otp.clearFocus();
                    }
                }
                return false;

            }
        });


    }

    //Resend Otp Function
    public void resend(View view) {
        if (utils.checkConnection(getApplicationContext()) == 1) {
            utils.login(getApplicationContext(), mobtext, progressDialog, new utils.VolleyCallback() {
                final Intent[] intent = new Intent[1];

                @Override
                public void onSuccess(String result) {
                    if (result.equals("true")) {
                        intent[0] = new Intent(otp_layout.this, otp_layout.class);
                        intent[0].putExtra("mob_no", mobtext);
                        startActivity(intent[0]);
                        finish();
                    }
                }
            });
        } else {

            Snackbar.make(findViewById(R.id.otp_parent) ,"No Connection", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void fetchOtp() {

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase("otp")) {
                    String message = intent.getStringExtra("message");

                    ed_otp.setText(String.valueOf(message.charAt(0)));
                    ed_otp1.setText(String.valueOf(message.charAt(1)));
                    ed_otp2.setText(String.valueOf(message.charAt(2)));
                    ed_otp3.setText(String.valueOf(message.charAt(3)));

                    submit_otp();
                }
            }
        };
    }

}
