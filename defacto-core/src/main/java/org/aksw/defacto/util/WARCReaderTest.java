package org.aksw.defacto.util;
import org.apache.commons.io.IOUtils;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;
import org.archive.io.warc.WARCReaderFactory;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * A raw example of how to process a WARC file using the org.archive.io package.
 * Common Crawl S3 bucket without credentials using JetS3t.
 *
 * @author Stephen Merity (Smerity)
 */
public class WARCReaderTest {
    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // Set up a local compressed WARC file for reading
        String fn = "/Users/dnes/Downloads/0013wb-88.warc.gz";
        FileInputStream is = new FileInputStream(fn);
        // The file name identifies the ArchiveReader and indicates if it should be decompressed
        ArchiveReader ar = WARCReaderFactory.get(fn, is, true);

        // Once we have an ArchiveReader, we can work through each of the records it contains
        int i = 0;
        for(ArchiveRecord r : ar) {
            // The header file contains information such as the type of record, size, creation time, and URL
            System.out.println(r.getHeader());
            System.out.println(r.getHeader().getUrl());
            System.out.println();

            // If we want to read the contents of the record, we can use the ArchiveRecord as an InputStream
            // Create a byte array that is as long as the record's stated length
            byte[] rawData = IOUtils.toByteArray(r, r.available());

            // Why don't we convert it to a string and print the start of it? Let's hope it's text!
            String content = new String(rawData);
            System.out.println(content.substring(0, Math.min(500, content.length())));
            System.out.println((content.length() > 500 ? "..." : ""));

            // Pretty printing to make the output more readable
            System.out.println("=-=-=-=-=-=-=-=-=");
            if (i++ > 4) break;
        }
    }
}