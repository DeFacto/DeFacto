package org.aksw.defacto;

import java.io.File;
import java.io.IOException;

import org.aksw.defacto.config.DefactoConfig;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class Defacto {

    public static DefactoConfig DEFACTO_CONFIG;
    
    public static void init(){
    	
    	try {
    		
			DEFACTO_CONFIG = new DefactoConfig(new Ini(new File("defacto.ini")));
		}
    	catch (InvalidFileFormatException e) {
			e.printStackTrace();
		} 
    	catch (IOException e) {
			e.printStackTrace();
		}
    }
}
