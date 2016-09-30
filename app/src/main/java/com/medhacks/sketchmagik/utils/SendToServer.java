package com.medhacks.sketchmagik.utils;

import android.content.Context;
import android.util.Log;

import com.medhacks.sketchmagik.db.DatabaseHelper;
import com.medhacks.sketchmagik.models.Row;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Shreya on 25/09/16.
 */
public class SendToServer {
    String TAG = "SendToServer";
    String urlStr = "http://54.201.152.12:8080/submit";
    HttpURLConnection urlConn;
    DataOutputStream printout;
    DataInputStream input;
    static Context context;
    DatabaseHelper myDbHelp;

    public SendToServer(Context c){
        context = c;
        myDbHelp = DatabaseHelper.getInstance(context);
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Row> rowsList = myDbHelp.getPendingRows();
                if(myDbHelp.getNumberOfPendingRows()>0) {
                    for(Row row: rowsList) {
                        URL url = null;
                        try {
                            url = new URL(urlStr);
                            if (url != null) {
                                urlConn = (HttpURLConnection) url.openConnection();
                                urlConn.setDoInput(true);
                                urlConn.setDoOutput(true);
                                urlConn.setUseCaches(false);
                                urlConn.setRequestProperty("Content-Type", "application/json");
                                urlConn.setRequestProperty("Accept", "application/json");
                                urlConn.setRequestMethod("POST");
                               // urlConn.connect();
                                JSONObject jsonParam = new JSONObject();
                                Date mydate;
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                mydate = dateFormat.parse(row.getDate());
                                jsonParam.put("date", mydate.getTime());
                                jsonParam.put("sketch_number", row.getSketchNumber());
                                jsonParam.put("attempt_number", row.getAttemptNumber());
                                jsonParam.put("time_taken", row.getTimeTaken());
                                jsonParam.put("deviation", row.getDeviation());
                                String param = jsonParam.toString();
                                DataOutputStream wr = new DataOutputStream(urlConn.getOutputStream());
                                wr.writeBytes(param);
                                wr.close();

                                StringBuilder sb = new StringBuilder();
                                int HttpResult = urlConn.getResponseCode();
                                if (HttpResult == HttpURLConnection.HTTP_OK) {
                                    BufferedReader br = new BufferedReader(
                                            new InputStreamReader(urlConn.getInputStream(), "utf-8"));
                                    String line = null;
                                    while ((line = br.readLine()) != null) {
                                        sb.append(line + "\n");
                                    }
                                    br.close();
                                    System.out.println("sb: " + sb.toString());
                                    myDbHelp.updateStatus(row.getDate(), row.getSketchNumber(), row.getAttemptNumber(), 1);
                                } else {
                                    System.out.println(urlConn.getResponseMessage());
                                }
                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    int k = myDbHelp.getNumberOfPendingRows();
                    Log.e("NKHDUISHFUIHSGF: ", ""+k);
                }
            }
        }).start();
    }
}
