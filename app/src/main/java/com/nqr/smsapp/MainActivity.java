package com.nqr.smsapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
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
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> smsMessagesList = new ArrayList<>();
    ListView messages;
    ArrayAdapter arrayAdapter;
    EditText address;
    TextView total;
    Button submit;
    XYPlot plot;
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
                    createGraph();
                }
            });

        }
        public void createGraph() {
            plot = (XYPlot) findViewById(R.id.plot);

            // create a couple arrays of y-values to plot:
            final Number[] domainLabels = {0.1, 0.2, 0.3, 0.6, 0.7, 0.8, 0.9, 0.10, 0.13, 0.14};
            Number[] series1Numbers = {0.1, 0.4, 0.2, 0.8, 0.4, 0.16, 0.8, 0.32, 0.16, 0.64};
            Number[] series2Numbers = {0.5, 0.2, 0.10, 0.5, 0.20, 0.10, 0.40, 0.20, 0.80, 0.40};

            // turn the above arrays into XYSeries':
            // (Y_VALS_ONLY means use the element index as the x value)
            XYSeries series1 = new SimpleXYSeries(
                    Arrays.asList(series1Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");
            XYSeries series2 = new SimpleXYSeries(
                    Arrays.asList(series2Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series2");

            // create formatters to use for drawing a series using LineAndPointRenderer
            // and configure them from xml:
            LineAndPointFormatter series1Format = new LineAndPointFormatter(Color.RED, Color.GREEN, Color.BLUE, null);
            LineAndPointFormatter series2Format = new LineAndPointFormatter(Color.RED, Color.GREEN, Color.BLUE, null);

            // add an "dash" effect to the series2 line:
            series2Format.getLinePaint().setPathEffect(new DashPathEffect(new float[] {

                    // always use DP when specifying pixel sizes, to keep things consistent across devices:
                    PixelUtils.dpToPix(20),
                    PixelUtils.dpToPix(15)}, 0));

            // just for fun, add some smoothing to the lines:
            // see: http://androidplot.com/smooth-curves-and-androidplot/
            series1Format.setInterpolationParams(
                    new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

            series2Format.setInterpolationParams(
                    new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

            // add a new series' to the xyplot:
            plot.addSeries(series1, series1Format);
            plot.addSeries(series2, series2Format);

            plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
                @Override
                public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                    int i = Math.round(((Number) obj).floatValue());
                    return toAppendTo.append(domainLabels[i]);
                }
                @Override
                public Object parseObject(String source, ParsePosition pos) {
                    return null;
                }
            });

        }


    public void refreshSmsInbox(String adrs) {
                int i = 0;
                float sum =0;
                ArrayList<Float> tot = new ArrayList<Float>();
            ContentResolver contentResolver = getContentResolver();
            Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
            int indexBody = smsInboxCursor.getColumnIndex("body");
            int indexAddress = smsInboxCursor.getColumnIndex("address");
            if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
            arrayAdapter.clear();
            createGraph();
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
                                tot.add(sum);
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
