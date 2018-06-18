package org.aksw.defacto.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.aksw.defacto.util.Encoder.Encoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BufferedFileReader extends Reader {

	private final Logger logger = LoggerFactory.getLogger(BufferedFileReader.class);
	
	private BufferedReader reader = null; 
	
	/**
	 * Creates a reader with the given encoding for the given
	 * filename.
	 * 
	 * @param fileName - filename of the file to be read
	 * @param encoding - the encoding of the file
	 * @deprecated  please use the constructor <code>BufferedFileReader(String fileName, Encoding encoding)</code> instead
	 */
	public BufferedFileReader(String fileName, String encoding) {
		
		try {
			
			this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)), encoding));
		}
		catch (UnsupportedEncodingException e) {
			
			e.printStackTrace();
			logger.error("Could not open reader with encoding: \"" + encoding + "\"", e);
			throw new RuntimeException("Could not open reader with encoding: \"" + encoding + "\"", e);
		}
		catch (FileNotFoundException e) {
			
			e.printStackTrace();
			logger.error("Could not open reader for filename: \"" + fileName + "\"", e);
			throw new RuntimeException("Could not open reader for filename: \"" + fileName + "\"", e);
		}
	}
	
	/**
     * Creates a reader with the given encoding for the given
     * filename.
     * 
     * @param fileName - filename of the file to be read
     * @param encoding - the encoding of the file
     */
	public BufferedFileReader(String fileName, Encoding encoding) {
        
        try {
            
            this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)), encoding.toString()));
        }
        catch (UnsupportedEncodingException e) {
            
            e.printStackTrace();
            logger.error("Could not open reader with encoding: \"" + encoding + "\"", e);
            throw new RuntimeException("Could not open reader with encoding: \"" + encoding + "\"", e);
        }
        catch (FileNotFoundException e) {
            
            e.printStackTrace();
            logger.error("Could not open reader for filename: \"" + fileName + "\"", e);
            throw new RuntimeException("Could not open reader for filename: \"" + fileName + "\"", e);
        }
    }

	/**
	 * Creates a reader with java default encoding for the given
	 * filename.
	 * 
	 * @param fileName - filename of the file to be read
	 */
	public BufferedFileReader(String fileName) {
		
		try {
			
			this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))));
		}
		catch (FileNotFoundException e) {
			
			e.printStackTrace();
			logger.error("Could not open reader for filename: \"" + fileName + "\"", e);
			throw new RuntimeException("Could not open reader for filename: \"" + fileName + "\"", e);
		}
	}

	/**
	 * @return the line or null if eof
	 */
	public String readLine() {

		try {
			
			return this.reader.readLine();
		}
		catch (IOException e) {

			e.printStackTrace();
			logger.error("Could not read line!", e);
		}
		return null;
	}
	
	/**
	 * closes the reader
	 */
	@Override
	public void close() {

		try {
			
			this.reader.close();
		}
		catch (IOException e) {
			
			e.printStackTrace();
			logger.error("Could not close reader!", e);
			throw new RuntimeException("Could not close reader!", e);
		}
	}

	@Override
	public int read(char[] cbuf, int off, int len) {

		try {
			
			return this.reader.read(cbuf, off, len);
		}
		catch (IOException e) {
			
			e.printStackTrace();
			logger.error("Could not read file!", e);
			throw new RuntimeException("Could not read file!", e);
		}
	}
}
