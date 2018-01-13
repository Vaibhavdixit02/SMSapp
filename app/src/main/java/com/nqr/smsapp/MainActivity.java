package com.nqr.smsapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.database.sqlite.*;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> smsMessagesList = new ArrayList<>();
    ListView messages;
    ArrayAdapter arrayAdapter;
    EditText address;
    TextView total;
    Button submit;
    SmsManager smsManager = SmsManager.getDefault();
    private static MainActivity inst;

    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;

    public static MainActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messages = (ListView) findViewById(R.id.messages);
        total = (TextView) findViewById(R.id.total);
        address = (EditText) findViewById(R.id.editText);
        submit = (Button) findViewById(R.id.button);
//        input = (EditText) findViewById(R.id.input);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smsMessagesList);
        messages.setAdapter(arrayAdapter);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            getPermissionToReadSMS();
        } else {
            inputAddress();
        }


    }

    public void updateInbox(final String smsMessage) {
        arrayAdapter.insert(smsMessage, 0);
        arrayAdapter.notifyDataSetChanged();
    }

//public void onSendClick(View view) {
//
//    if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
//            != PackageManager.PERMISSION_GRANTED) {
//        getPermissionToReadSMS();
//    } else {
//        smsManager.sendTextMessage("07701056337", null, input.getText().toString(), null, null);
//        Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show();
//    }
//}

        public void getPermissionToReadSMS() {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_SMS)) {
                    Toast.makeText(this, "Please allow permission!", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.READ_SMS},
                        READ_SMS_PERMISSIONS_REQUEST);
            }
        }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_SMS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read SMS permission granted", Toast.LENGTH_SHORT).show();
                inputAddress();
            } else {
                     Toast.makeText(this, "Read SMS permission denied", Toast.LENGTH_SHORT).show();
                    }

            } else {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }



        }
        public void inputAddress(){
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String adrs = address.getText().toString();
                    refreshSmsInbox(adrs);
                }
            });

        }

            public void refreshSmsInbox(String adrs) {
                int i = 0;
                float sum =0;

            ContentResolver contentResolver = getContentResolver();
            Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
            int indexBody = smsInboxCursor.getColumnIndex("body");
            int indexAddress = smsInboxCursor.getColumnIndex("address");
            if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
            arrayAdapter.clear();
            do {
                ArrayList<Integer> amount = new ArrayList<>();
                if (smsInboxCursor.getString(indexAddress).contains(adrs) && (smsInboxCursor.getString(indexBody).contains("Purchase") || smsInboxCursor.getString(indexBody).contains("debited"))) {
                    String str =  smsInboxCursor.getString(indexBody);
                    ArrayList<String> wordlst = new ArrayList<>();
                    String sop = "";
                    int t =0;
                    for(int u = 0;u<str.length();u++) {
                        char j = ' ';
                        if (str.charAt(u) != j) {
                            sop += str.charAt(u);
                        } else {
                            wordlst.add(sop);
                            sop = "";
                            t++;
                        }

                    }
                    String y = ".,0123456789INR";
                    String p = "0123456789";
                    for(int u = 0;u<wordlst.size();u++){
                        int h = 0;
                        int k = 0;
                        for (int o = 0;o<wordlst.get(u).length();o++){
                            if(y.indexOf(wordlst.get(u).charAt(o)) >= 0 ) {
                                h++;
                                k++;
                            }
                        }

                        if(k == wordlst.get(u).length()) {
                            amount.add(u);
                        }
                    }
//                    int ji = amount.get(0);
//                    float tu = Float.valueOf(wordlst.get(amount.get(0)));
                    if(amount.size() > 0) {
//                        if(wordlst.get(amount.get(0)).equals("INR")){
//                            arrayAdapter.add(wordlst.get(amount.get(1)));
//                        }
//                        else if(wordlst.get(amount.get(0)).substring(0,2).equals("INR")){
//                            arrayAdapter.add(wordlst.get(amount.get(0)).substring(3,wordlst.get(amount.get(0)).length()-1));
//                        }
//                         if(p.indexOf(wordlst.get(amount.get(0)).charAt(0)) > 0){
//                            arrayAdapter.add(wordlst.get(amount.get(0)));
//                        }
//                    for(int w = 0;w<amount.size();w++){
//                        arrayAdapter.add(wordlst.get(amount.get(w)));
//                    }
                        for(int x = 0;x<amount.size();x++){
                        try {
                            if (wordlst.get(amount.get(x)).equals("INR")) {
                                continue;
                            } else if (wordlst.get(amount.get(x)).substring(0, 2).equals("INR")) {
                                arrayAdapter.add(wordlst.get(amount.get(x)).substring(3, wordlst.get(amount.get(0)).length() - 1));
                                String c = wordlst.get(amount.get(x)).substring(3, wordlst.get(amount.get(0)).length() -1);
                                c = c.replaceAll(",","");
                                sum = sum + Float.parseFloat(c);
                            } else if (p.indexOf(wordlst.get(amount.get(x)).charAt(0)) > 0) {
                         arrayAdapter.add(wordlst.get(amount.get(x)));
                                String c = wordlst.get(amount.get(x));
                                c = c.replaceAll(",","");
                                sum = sum + Float.parseFloat(c);
                            }
                        }
                        catch (Exception e){
                            continue;
                        }


                        }
//                        arrayAdapter.add(amount);

                    }
//                    sum += tu;
//                    String g = wordlist.get(amount.get(0));


                }
                else {
                    continue;
                }
                i++;
            } while (smsInboxCursor.moveToNext());

        total.setText("Total =" + String.valueOf(sum));
    }



}
