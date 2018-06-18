package org.aksw.defacto.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * 
 * Tool to write CSV files. Its main feature is that it suppresses entries in the 
 * document when they are equal to the entry in the previous record.
 * 
 * @author Jens Lehmann
 *
 */
public class CSVWriter {

	private File file;
	
	private String[] previousRecord = null;
	
	public CSVWriter(String fileName, int nrOfRecords) {
		file = new File(fileName);
		previousRecord = new String[nrOfRecords];
		FileUtils.deleteQuietly(file);
	}
	
	public void addRecord(String[] record) {
		String str = "";
		for(int i=0; i<record.length; i++) {
			String s = record[i];
			if(s.equals("yes") || s.equals("no") || !s.equals(previousRecord[i])) {
				str += record[i].replace(",", "KOMMA") + ",";
			} else {
				str+= ",";
			}
		}
		previousRecord = record;
		appendLine(str + "\n");
	}
	
	private void appendLine(String str) {
		// append line to CSV file
		try {
			FileUtils.write(file, str, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
