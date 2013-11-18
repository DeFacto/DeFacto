/**
 * 
 */
package org.aksw.defacto.evaluation;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.aksw.defacto.util.BufferedFileReader;
import org.aksw.defacto.util.BufferedFileWriter;
import org.aksw.defacto.util.BufferedFileWriter.WRITER_WRITE_MODE;
import org.aksw.defacto.util.Encoder.Encoding;

/**
 * @author Daniel Gerber <daniel.gerber@deinestadtsuchtdich.de>
 *
 */
public class GoogleQuery {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {

		BufferedFileWriter writer = new BufferedFileWriter("/Users/gerb/Development/workspaces/tex/AKSW_Papers/2013/JWS_Temporal_Multilingual_Defacto/statistics/years.tsv", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
		
		for ( int i = 1000; i < 2014 ; i++) {
			
			Thread.sleep(2500);
			
			Document document = Jsoup.connect("http://www.google.com/search?q="+i+"&ie=utf-8&oe=utf-8").timeout(10000).
					userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
				
			writer.write(i + "\t" + document.getElementById("resultStats").text().replace("About ", "").replaceAll(" results.*", "").trim().replace(",", ""));
			writer.flush();
		}
		writer.close();
	}
}
