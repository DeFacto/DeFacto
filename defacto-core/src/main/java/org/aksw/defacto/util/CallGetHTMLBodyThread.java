package org.aksw.defacto.util;

import org.aksw.defacto.helper.SQLiteHelper;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by esteves on 20/03/2017.
 */
public class CallGetHTMLBodyThread {

    public static void main(String args[]) throws Exception {

        /*
        Map<Integer, String> mapIdURL = SQLiteHelper.getInstance().getTopNWebSitesURL(10, 0);
        Map<Integer, String> mapIdURL2 = SQLiteHelper.getInstance().getTopNWebSitesURL(10, 10);

        GetHTMLBodyThread T1 = new GetHTMLBodyThread("T1", mapIdURL);
        GetHTMLBodyThread T2 = new GetHTMLBodyThread("T2", mapIdURL2);

        T1.start();
        T2.start();

        try {
            T1.join();
            T2.join();
        }catch( Exception e) {
            System.out.println("Interrupted");
        }

        SQLiteHelper.getInstance().UpdateHTMLforWebSite(T1.getMapIdHTML());
        SQLiteHelper.getInstance().UpdateHTMLforWebSite(T2.getMapIdHTML());


    */
    }
}

