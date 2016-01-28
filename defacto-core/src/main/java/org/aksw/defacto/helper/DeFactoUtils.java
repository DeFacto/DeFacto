package org.aksw.defacto.helper;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by Diego on 1/28/2016.
 */
public class DefactoUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefactoUtils.class);


    public static void main(String[] args) {

    }

    public static void createCacheFile(String fileName) {

        try {
            File f = new File(fileName);
            if(!f.exists()) {
                f.createNewFile();
            }

        }catch (Exception e){
            LOGGER.error(e.toString());
        }

    }

    public static void writeToCSV(String fileName, String text) throws IOException {

        createCacheFile(fileName);

        try {

            CSVWriter writer = new CSVWriter(new FileWriter(fileName, true), ',' , '\0' , '\t');
            String[] t = new String[] {text};
            writer.writeNext(t);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static ArrayList<String> readCacheFromCSV(String fn) throws Exception{

        ArrayList<String> cache = new ArrayList<>();

        try {

            createCacheFile(fn);

            CSVReader reader = new CSVReader(new FileReader(fn), ',' , '\'' , '\t');

            String [] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                cache.add(nextLine[0]);
            }
            reader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cache;

    }
}
