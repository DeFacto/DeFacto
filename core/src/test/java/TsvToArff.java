import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;


/**
 * 
 */

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class TsvToArff {

    /**
     * @param args
     */
    public static void main(String[] args) {

        try {
            
            List<String> lines = FileUtils.readLines(new File("/Users/gerb/defacto_fact.csv"));
            for ( String line : lines ) {
                
                List<String> newLine = new ArrayList<String>();
                
                String[] parts = line.split("\\t");
                for ( String part : parts ) {
                    
                    try {
                        
                        Double value = Double.valueOf(part);
                        newLine.add(part);
                    }
                    catch ( NumberFormatException nfe ) {
                        
                        if ( part.equals("TRUE") || part.equals("false") ) 
                            newLine.add(part.toLowerCase());
                        else
                            newLine.add(String.format("\"%s\"", part.toLowerCase()));
                    }
                }
                
                System.out.println(StringUtils.join(newLine, ","));
            }
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
