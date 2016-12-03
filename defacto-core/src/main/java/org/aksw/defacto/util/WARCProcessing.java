package org.aksw.defacto.util;

import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;
import org.jwat.warc.WarcValidationError;

import java.io.*;
import java.util.Collection;
import java.util.Iterator;


/**
 * Created by dnes on 06/04/16.
 */
public class WARCProcessing {



    static String warcFile = "/Users/dnes/Downloads/0013wb-88.warc";
    //static String warcFile = "/home/nicl/Downloads/MYWARC.warc";

    public static void main(String[] args) {
        File file = new File( warcFile );
        try {
            InputStream in = new FileInputStream( file );

            int records = 0;
            int errors = 0;


            WarcReader reader = WarcReaderFactory.getReader( in );
            WarcRecord record;

            while ( (record = reader.getNextRecord()) != null ) {
                printRecord(record);
                printRecordErrors(record);

                ++records;

                if (record.hasErrors()) {
                    errors += record.getValidationErrors().size();
                }
            }

            System.out.println("--------------");
            System.out.println("       Records: " + records);
            System.out.println("        Errors: " + errors);
            reader.close();
            in.close();
        }
        catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void printRecord(WarcRecord record) {
        System.out.println("--------------");
        //System.out.println("       Version: " + record.bMagicIdentified + " " + record.bVersionParsed + " " + record.major + "." + record.minor);
        System.out.println("       TypeIdx: " + record.warcTypeIdx);
        System.out.println("          Type: " + record.warcTypeStr);
        System.out.println("      Filename: " + record.warcFilename);
        System.out.println("     Record-ID: " + record.warcRecordIdUri);
        System.out.println("          Date: " + record.warcDate);
        System.out.println("Content-Length: " + record.contentLength);
        System.out.println("  Content-Type: " + record.contentType);
        System.out.println("     Truncated: " + record.warcTruncatedStr);
        System.out.println("   InetAddress: " + record.warcInetAddress);
        System.out.println("  ConcurrentTo: " + record.warcConcurrentToUriList);
        System.out.println("      RefersTo: " + record.warcRefersToUri);
        System.out.println("     TargetUri: " + record.warcTargetUriUri);
        System.out.println("   WarcInfo-Id: " + record.warcWarcInfoIdUri);
        System.out.println("   BlockDigest: " + record.warcBlockDigest);
        System.out.println(" PayloadDigest: " + record.warcPayloadDigest);
        System.out.println("IdentPloadType: " + record.warcIdentifiedPayloadType);
        System.out.println("       Profile: " + record.warcProfileStr);
        System.out.println("      Segment#: " + record.warcSegmentNumber);
        System.out.println(" SegmentOrg-Id: " + record.warcSegmentOriginIdUrl);
        System.out.println("SegmentTLength: " + record.warcSegmentTotalLength);

    }

    public static void printRecordErrors(WarcRecord record) {
        if (record.hasErrors()) {
            Collection<WarcValidationError> errorCol = record.getValidationErrors();
            if (errorCol != null && errorCol.size() > 0) {
                Iterator<WarcValidationError> iter = errorCol.iterator();
                while (iter.hasNext()) {
                    WarcValidationError error = iter.next();
                    System.out.println( error.error );
                    System.out.println( error.field );
                    System.out.println( error.value );
                }
            }
        }
    }



























    public WARCProcessing(){

    }

    public void read(String warcFile){

        /*

        Iterator<ArchiveRecord> archIt = WARCReaderFactory.get(warcFile).iterator();
        while (archIt.hasNext()) {
            handleRecord(archIt.next());
        }

        WARCReader warcReader = WARCReaderFactory.get(warcFile);
        int inputSequence = -1;


        ArchiveRecord record = warcReader.get();
        while(record != null){
            inputSequence++;

            // Skip the 0th record, which is just the archive guff.
            if (inputSequence == 0) {
                // print some info but do not process this record
            }
            else if (! record.hasContentHeaders()) {
                // print some info but do not process this record
            }
            else  {
                HeaderedArchiveRecord hRecord = new HeaderedArchiveRecord(record);
                ArchiveRecordHeader archiveHeader = hRecord.getHeader();
                gate.Document document = makeDocumentHeritrix(archiveHeader,
                        inputSequence,  hRecord);
                //...
            }
            record.close();
            record = warcReader.get();  // line 754
        }

        warcReader.close();

    */

    }



}
