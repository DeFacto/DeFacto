package org.aksw.defacto.util.warc;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * Created by dnes on 06/04/16.
 */
public class Example {


    public static void main(String[] args) throws IOException {
        String inputWarcFile;

        //inputWarcFile = args[0];

        inputWarcFile = "/Users/dnes/Downloads/0013wb-88.warc.gz";

        // open our gzip input stream
        GZIPInputStream gzInputStream=new GZIPInputStream(new FileInputStream(inputWarcFile));

        // cast to a data input stream
        DataInputStream inStream=new DataInputStream(gzInputStream);

        // iterate through our stream
        WarcRecord thisWarcRecord;
        while ((thisWarcRecord=WarcRecord.readNextWarcRecord(inStream))!=null) {
            // see if it's a response record
            if (thisWarcRecord.getHeaderRecordType().equals("response")) {
                // it is - create a WarcHTML record
                WarcHTMLResponseRecord htmlRecord=new WarcHTMLResponseRecord(thisWarcRecord);
                // get our TREC ID and target URI
                String thisTRECID=htmlRecord.getTargetTrecID();
                String thisTargetURI=htmlRecord.getTargetURI();
                // print our data
                System.out.println(thisTRECID + " : " + thisTargetURI);
            }
        }

        inStream.close();
    }
}

