package org.aksw.defacto.util;

import org.aksw.defacto.helper.SQLiteHelper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by esteves on 20/03/2017.
 */
public class GetHTMLBodyThread extends Thread {

    private Thread t;
    private String threadName;
    private Map<Integer, String> mapIdURL;
    private HashMap<Integer, String> mapIdHTML = new HashMap<>();;
    HttpClient httpclient = HttpClients.createDefault();


    public GetHTMLBodyThread(String name, Map<Integer, String> map) {
        this.threadName = name;
        this.mapIdURL = map;
    }

    public void start () {
        System.out.println("Starting " +  threadName );
        if (t == null) {
            t = new Thread (this);
            t.start ();
        }
    }

    public HashMap<Integer, String> getMapIdHTML(){
        return this.mapIdHTML;
    }

    @Override
    public void run() {
        try {

            for (Map.Entry<Integer, String> entry : this.mapIdURL.entrySet())
            {
                String htmlContent = "";
                try{
                    URL url = new URL(entry.getValue());
                    InputStream is = (InputStream) url.getContent();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line = null;
                    StringBuffer sb = new StringBuffer();
                    while((line = br.readLine()) != null){
                        sb.append(line);
                    }
                    htmlContent = sb.toString();

                    //BasicResponseHandler respond = new BasicResponseHandler();
                    //System.out.println(entry.getValue());
                    //HttpResponse response = httpclient.execute(new HttpGet(entry.getValue()));
                    //out = respond.handleResponse(response);
                }catch (Exception e){
                    htmlContent = "-1";
                }

                  this.mapIdHTML.put(entry.getKey(), htmlContent);
            }

        }catch (Exception e){
            System.out.print(e.toString());
        }
        System.out.println("Thread " +  threadName + " exiting.");
    }

}
