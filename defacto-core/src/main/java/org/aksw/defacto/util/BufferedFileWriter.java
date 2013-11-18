package org.aksw.defacto.util;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;

import org.aksw.defacto.Constants;
import org.aksw.defacto.util.Encoder.Encoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BufferedFileWriter extends Writer {

	public static enum WRITER_WRITE_MODE { APPEND, OVERRIDE };
	public static enum WRITER_MODE { CLOSE, NONE };
	
	private final Logger logger = LoggerFactory.getLogger(BufferedFileReader.class);
	
	private BufferedWriter writer = null;
	
	/**
	 * Creates a new writer with the given encoding or UTF-8 if no
	 * encoding was provided. Opens the writer in append or override
	 * mode according to the given setting.
	 * 
	 * @param pathToFile - the filename to write to
	 * @param encoding - encoding of the file
	 * @param mode - append or override
	 * @deprecated - please use the constructor <code>BufferedFileWriter(String pathToFile, Encoding encoding, WRITER_WRITE_MODE mode)</code> instead
	 */
	public BufferedFileWriter(String pathToFile, String encoding, WRITER_WRITE_MODE mode) {

		try {
			
			// set the mode if we want to append to the current file or override it
			Boolean append = null;
			if ( mode.equals(WRITER_WRITE_MODE.APPEND) ) append = true;
			if ( mode.equals(WRITER_WRITE_MODE.OVERRIDE) ) append = false;
			// chose UTF-8 if no encoding was provided
			encoding = (encoding == null || encoding.isEmpty()) ? "UTF-8" : encoding;
			
			this.writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathToFile, append), encoding));
		}
		catch (UnsupportedEncodingException e) {
			
			e.printStackTrace();
			this.logger.error("Could not open writer for encoding: \"" + encoding + "\"", e);
			throw new RuntimeException("Could not open writer for encoding: \"" + encoding + "\"", e);
		}
		catch (FileNotFoundException e) {

			e.printStackTrace();
			this.logger.error("Could not open writer for filename: \"" + pathToFile + "\"", e);
			throw new RuntimeException("Could not open writer for filename: \"" + pathToFile + "\"", e);
		}
	}
	
	/**
     * Creates a new writer with the given encoding or UTF-8 if no
     * encoding was provided. Opens the writer in append or override
     * mode according to the given setting.
     * 
     * @param pathToFile - the filename to write to
     * @param encoding - encoding of the file
     * @param mode - append or override
     */
	public BufferedFileWriter(String pathToFile, Encoding encoding, WRITER_WRITE_MODE mode) {
		
        try {
            
            // set the mode if we want to append to the current file or override it
            Boolean append = null;
            if ( mode.equals(WRITER_WRITE_MODE.APPEND) ) append = true;
            if ( mode.equals(WRITER_WRITE_MODE.OVERRIDE) ) append = false;
            // chose UTF-8 if no encoding was provided
            encoding = (encoding == null ) ? Encoding.UTF_8 : encoding;
            
            this.writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathToFile, append), encoding.toString()));
        }
        catch (UnsupportedEncodingException e) {
            
            e.printStackTrace();
            this.logger.error("Could not open writer for encoding: \"" + encoding + "\"", e);
            throw new RuntimeException("Could not open writer for encoding: \"" + encoding + "\"", e);
        }
        catch (FileNotFoundException e) {

            e.printStackTrace();
            this.logger.error("Could not open writer for filename: \"" + pathToFile + "\"", e);
            throw new RuntimeException("Could not open writer for filename: \"" + pathToFile + "\"", e);
        }
    }

	@Override
	public void close() {

		try {
			
			this.writer.close();
		}
		catch (IOException e) {

			e.printStackTrace();
			this.logger.error("Could not close writer!", e);
			throw new RuntimeException("Could not close writer!", e);
		}
	}
	
	@Override
	/**
	 * Writes a string to a file with a new line at the end!
	 * 
	 * @param the string to write
	 */
	public void write(String string) {
		
		this.writeLineNoNewLine(string + Constants.NEW_LINE_SEPARATOR);
	}
	
	/**
     * Writes a string to a file WITHOUT a new line at the end!
     * 
     * @param the string to write
     */
	public void writeLineNoNewLine(String string) {
	    
	    try {
            
            this.writer.write(string);
        }
        catch (IOException e) {
            
            e.printStackTrace();
            this.logger.error("Could not write to file string: " + string, e);
            throw new RuntimeException("Could not write to file string: " + string, e);
        }
	}
	

	@Override
	public void flush() {

		try {
			
			this.writer.flush();
		}
		catch (IOException e) {

			e.printStackTrace();
			this.logger.error("Could not flush writer!", e);
			throw new RuntimeException("Could not flush writer!", e);
		}
	}

	@Override
	public void write(char[] cbuf, int off, int len) {

		try {
			
			writer.write(cbuf, off, len);
		}
		catch (IOException e) {

			this.logger.error("Could not write to file!", e);
			e.printStackTrace();
			throw new RuntimeException("Could not write to file!", e);
		}
	}

	/**
	 * Writes a list of elements to a given writer. Every 
	 * element of the list is written to one line. The type
	 * of the elements doesn't matter, because we use the 
	 * toString()-method to write it out. This method does 
	 * also close the writer if wanted.  
	 * 
	 * @param writer the writer to write to
	 * @param list the list where each element should be written out
	 * @param mode WRITER_MODE.CLOSE if you want to close the writer after writing
	 * or WRITER_MODE.NONE if nothing should be happening 
	 */
	public <E> void writeListToFile(List<E> list, WRITER_MODE mode) {

		try {
			
			// write every element in the list to one line in the file using the toString method of the contained elements
			for (E entry : list) {

				writer.write(entry.toString() + Constants.NEW_LINE_SEPARATOR);
			}
			// close the writer if correct mode was selected
			if ( mode.equals(WRITER_MODE.CLOSE) ) writer.close();
		}
		catch (IOException e) {
			
			e.printStackTrace();
			this.logger.error("Could not write list in mode: " + mode + " to file", e);
			throw new RuntimeException("Could not write list in mode: " + mode + " to file", e);
		}
	}
}
