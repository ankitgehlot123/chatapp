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
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.company.my.chatapp.utils.Session;
import com.company.my.chatapp.utils.utils;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.apache.http.NameValuePair;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.android.volley.VolleyLog.TAG;

public class otp_layout_1 extends Activity {

    private static final int REQUEST_CODE = 1;
    EditText ed_otp, ed_otp1, ed_otp2, ed_otp3, ed_otp5, ed_otp4;
    Button otp_button;
    String otptext, mobtext;
    TextView resend_otp;
    List<NameValuePair> params;
    ProgressDialog progressDialog;
    com.company.my.chatapp.utils.utils utils = new utils();
    private String phoneVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            verificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private FirebaseAuth fbAuth;
    private Session session = new Session(this);
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_layout1);

        fbAuth = FirebaseAuth.getInstance();
        otp_button = findViewById(R.id.otp_button);
        resend_otp = findViewById(R.id.resend_otp);
        mobtext = getIntent().getStringExtra("mob_no");

        ed_otp = findViewById(R.id.ed_otp);
        ed_otp1 = findViewById(R.id.ed_otp1);
        ed_otp2 = findViewById(R.id.ed_otp2);
        ed_otp3 = findViewById(R.id.ed_otp3);
        ed_otp4 = findViewById(R.id.ed_otp4);
        ed_otp5 = findViewById(R.id.ed_otp5);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) {
            sendCode(mobtext);
            fetchOtp();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, REQUEST_CODE);
        }
        startEditTextListner();


        //Count Down Timer for Resend Otp
        new CountDownTimer(60000, 1000) {

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
        otp_button.setEnabled(false);
        otp_button.setOnClickListener(view -> {
            Log.i("submit", "submit");
            submit_otp();
        });

        progressDialog = new ProgressDialog(otp_layout_1.this);
        progressDialog.setMessage("verifying...");
        progressDialog.setCanceledOnTouchOutside(false);

    }


    public void submit_otp() {
        if (utils.checkConnection(getApplicationContext()) == 1) {
            otptext = ed_otp.getText().toString() +
                    ed_otp1.getText().toString() +
                    ed_otp2.getText().toString() +
                    ed_otp3.getText().toString() +
                    ed_otp4.getText().toString() +
                    ed_otp5.getText().toString();


            verifyCode(otptext);

        } else

            Snackbar.make(findViewById(R.id.otp_parent), "No Connection", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                fetchOtp();
            } else {

                Snackbar.make(findViewById(R.id.otp_parent), "You denied the permission", Snackbar.LENGTH_SHORT).show();
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
        signOut();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ed_otp5.setText("");
        ed_otp4.setText("");
        ed_otp3.setText("");
        ed_otp2.setText("");
        ed_otp1.setText("");
        ed_otp.setText("");
        signOut();
    }


    private void startEditTextListner() {

        ed_otp.setOnKeyListener((v, keyCode, event) -> {
            //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                ed_otp5.setText("");
                ed_otp4.setText("");
                ed_otp3.setText("");
                ed_otp2.setText("");
                ed_otp1.setText("");
                ed_otp.setText("");
                ed_otp.requestFocus();
            } else if (ed_otp.getText().toString().trim().length() == 1)
                ed_otp1.requestFocus();

            return false;
        });
        ed_otp1.setOnKeyListener((v, keyCode, event) -> {
            //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                ed_otp5.setText("");
                ed_otp4.setText("");
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
        });
        ed_otp2.setOnKeyListener((v, keyCode, event) -> {
            //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                ed_otp5.setText("");
                ed_otp4.setText("");
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
        });
        ed_otp3.setOnKeyListener((v, keyCode, event) -> {
            //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_

            if (keyCode == KeyEvent.KEYCODE_DEL) {
                ed_otp5.setText("");
                ed_otp4.setText("");
                ed_otp3.setText("");
                ed_otp2.setText("");
                ed_otp1.setText("");
                ed_otp.setText("");
                ed_otp.requestFocus();

            } else {

                if (ed_otp3.getText().toString().trim().length() == 1) {
                    ed_otp4.requestFocus();

                }
            }
            return false;

        });
        ed_otp4.setOnKeyListener((v, keyCode, event) -> {
            //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_

            if (keyCode == KeyEvent.KEYCODE_DEL) {
                ed_otp5.setText("");
                ed_otp4.setText("");
                ed_otp3.setText("");
                ed_otp2.setText("");
                ed_otp1.setText("");
                ed_otp.setText("");
                ed_otp.requestFocus();

            } else {

                if (ed_otp4.getText().toString().trim().length() == 1) {
                    ed_otp5.requestFocus();
                }
            }
            return false;

        });
        ed_otp5.setOnKeyListener((v, keyCode, event) -> {
            //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_

            if (keyCode == KeyEvent.KEYCODE_DEL) {
                ed_otp5.setText("");
                ed_otp4.setText("");
                ed_otp3.setText("");
                ed_otp2.setText("");
                ed_otp1.setText("");
                ed_otp.setText("");
                ed_otp.requestFocus();

            } else {

                if (ed_otp5.getText().toString().trim().length() == 1) {
                    ed_otp5.clearFocus();
                    ed_otp4.clearFocus();
                    ed_otp3.clearFocus();
                    ed_otp2.clearFocus();
                    ed_otp1.clearFocus();
                    ed_otp.clearFocus();
                    otp_button.setEnabled(false);
                    submit_otp();
                }
            }
            return false;

        });


    }

    //Resend Otp Function
    public void resend(View view) {
        if (utils.checkConnection(getApplicationContext()) == 1) {
            resendCode(mobtext);
            progressDialog.show();
            utils.login(getApplicationContext(), mobtext, progressDialog, new utils.VolleyCallback() {
                final Intent[] intent = new Intent[1];

                @Override
                public void onSuccess(String result) {
                    if (result.equals("true")) {
                        intent[0] = new Intent(otp_layout_1.this, otp_layout_1.class);
                        intent[0].putExtra("mob_no", mobtext);
                        startActivity(intent[0]);
                        finish();
                    }
                }
            });
        } else {

            Snackbar.make(findViewById(R.id.otp_parent), "No Connection", Snackbar.LENGTH_SHORT).show();
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
                    ed_otp4.setText(String.valueOf(message.charAt(4)));
                    ed_otp5.setText(String.valueOf(message.charAt(5)));

                    submit_otp();
                }
            }
        };
    }

    public void sendCode(String mob) {

        Log.e("molo", mob);
        setUpVerificatonCallbacks();
        TelephonyManager phoneMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mob = PhoneNumberUtils.formatNumberToE164("+" + mob, phoneMgr.getNetworkCountryIso());
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mob,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                verificationCallbacks);
    }

    private void setUpVerificatonCallbacks() {

        verificationCallbacks =
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(
                            PhoneAuthCredential credential) {
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {

                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                            Log.d(TAG, "Invalid credential: "
                                    + e.getLocalizedMessage());
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            // SMS quota exceeded
                            Log.d(TAG, "SMS Quota exceeded.");
                        }
                    }

                    @Override
                    public void onCodeSent(String verificationId,
                                           PhoneAuthProvider.ForceResendingToken token) {

                        phoneVerificationId = verificationId;
                        resendToken = token;

                    }
                };
    }

    public void verifyCode(String otptext) {

        try {
            PhoneAuthCredential credential =
                    PhoneAuthProvider.getCredential(phoneVerificationId, otptext);
            signInWithPhoneAuthCredential(credential);
        } catch (Exception e) {
            Snackbar.make(findViewById(R.id.otp_parent), "Invslid OTP", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        fbAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = task.getResult().getUser();
                        progressDialog.show();
                        utils.auth(getApplicationContext(), mobtext, "true", progressDialog, new utils.VolleyCallback() {
                            final Intent[] intent = new Intent[1];

                            @Override
                            public void onSuccess(String result) {
                                if (result.equals("true")) {
                                    intent[0] = new Intent(otp_layout_1.this, update_profile.class);
                                    startActivity(intent[0]);
                                    finish();
                                }
                            }
                        });
                    } else {
                        if (task.getException() instanceof
                                FirebaseAuthInvalidCredentialsException) {
                            Snackbar.make(findViewById(R.id.otp_parent), "Invalid OTP", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public void resendCode(String mob) {
        TelephonyManager phoneMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mob = PhoneNumberUtils.formatNumberToE164("+" + mob, phoneMgr.getNetworkCountryIso());
        setUpVerificatonCallbacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mob,
                60,
                TimeUnit.SECONDS,
                this,
                verificationCallbacks,
                resendToken);
    }

    public void signOut() {
        Log.e("Signout", "signout");
        fbAuth.signOut();
    }

}
