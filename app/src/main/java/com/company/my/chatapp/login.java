package com.company.my.chatapp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.company.my.chatapp.utils.Session;
import com.company.my.chatapp.utils.utils;
import com.google.firebase.auth.FirebaseAuth;
import com.hbb20.CountryCodePicker;

import java.util.List;


public class login extends Activity {
    private static final int PERMISSION_REQUEST_CODE = 1;
    EditText mob_no;
    Button login;
    CheckBox checkbox;
    String mob_no_string, country_code;
    Spinner spinner;
    TextView owner_no;
    String wantPermission = Manifest.permission.READ_PHONE_STATE;
    String phone_n = null;
    CountryCodePicker codePicker;
    com.company.my.chatapp.utils.utils utils = new utils();
    Session session = new Session(this);
    private FirebaseAuth fbAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mob_no = (EditText) findViewById(R.id.mob_no);
        login = (Button) findViewById(R.id.request_otp_button);
        fbAuth = FirebaseAuth.getInstance();

        if (!checkPermission(wantPermission)) {
            requestPermission(wantPermission);
        } else {
            Log.e("Phone number: ", "" + getPhone());

            if (getPhone() != null) {
                String phoneNumber = getPhone();
                phoneNumber = phoneNumber.replaceAll("-", "").replaceAll("\\s", "");
                if (phoneNumber.length() > 10) {
                    phoneNumber = phoneNumber.substring(phoneNumber.length() - 10);
                }
                mob_no.setText(phoneNumber);
            }
        }

        codePicker = (CountryCodePicker) findViewById(R.id.code);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (utils.checkConnection(getApplicationContext()) == 1) {
                    if (mob_no.getText().length() != 10)
                        Snackbar.make(findViewById(R.id.login), "Invalid Mobile Number", Snackbar.LENGTH_SHORT).show();
                    else {
                        mob_no_string = codePicker.getSelectedCountryCode() + mob_no.getText().toString();
                        mob_no_string = mob_no_string.replaceAll("-", "").replaceAll("\\s", "");
                        if (mob_no_string.length() > 10) {
                            mob_no_string = mob_no_string.substring(mob_no_string.length() - 10);
                        }
                        mob_no_string = "91" + mob_no_string;
                        //Log.i("Phone",mob_no_string);
                        ProgressDialog progressDialog = new ProgressDialog(login.this);
                        progressDialog.setMessage("Logging in...");
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();
                        final Intent[] intent = new Intent[1];
                        utils.login(getApplicationContext(), mob_no_string, progressDialog, new utils.VolleyCallback() {
                            @Override
                            public void onSuccess(String result) {
                                if (result.equals("true")) {
                                    intent[0] = new Intent(login.this, otp_layout.class);
                                    intent[0].putExtra("mob_no", mob_no_string);
                                    startActivity(intent[0]);
                                    finish();
                                }
                            }
                        });

                    }
                } else {
                    Snackbar.make(findViewById(R.id.login), "No Connection", Snackbar.LENGTH_SHORT).show();
                }
            }
        });


    }

    //Enable login button when Cheeckbox is Checked
    public void agreechecked(View view) {
        if (((CheckBox) view).isChecked())
            login.setEnabled(true);
        else
            login.setEnabled(false);
    }


    private String getPhone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            List<SubscriptionInfo> subscription = SubscriptionManager.from(getApplicationContext()).getActiveSubscriptionInfoList();
            SubscriptionInfo info = subscription.get(0);
            phone_n = info.getNumber();
            return phone_n;
        } else {
            TelephonyManager phoneMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, wantPermission) != PackageManager.PERMISSION_GRANTED) {
                phone_n = "";
                Log.i("code123", phoneMgr.getNetworkCountryIso());
                return phone_n;
            }
            phone_n = phoneMgr != null ? phoneMgr.getLine1Number() : "";
            return phone_n;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("Phone number: ", "" + getPhone());

                    if (getPhone() != null) {
                        String phoneNumber = getPhone();
                        phoneNumber = phoneNumber.replaceAll("-", "").replaceAll("\\s", "");
                        if (phoneNumber.length() > 10) {
                            phoneNumber = phoneNumber.substring(phoneNumber.length() - 10);
                        }
                        mob_no.setText(phoneNumber);
                    } else {
                        Snackbar.make(findViewById(R.id.login), "Permission Denied. We can't get phone number.", Snackbar.LENGTH_SHORT).show();

                    }
                }
                break;
        }
    }

    private boolean checkPermission(String permission) {
        if (Build.VERSION.SDK_INT >= 23) {
            int result = ActivityCompat.checkSelfPermission(this, permission);
            if (result == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private void requestPermission(String permission) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            Snackbar.make(findViewById(R.id.login), "Phone state permission allows us to get phone number. Please allow it for additional functionality.", Snackbar.LENGTH_SHORT).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
    }
}
