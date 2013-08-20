/**
 * 
 */
package org.aksw.defacto.config;

import org.ini4j.Ini;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefactoConfig {

    private Ini defactoConfig;

    public DefactoConfig(Ini config) {
        
        this.defactoConfig =  config;
    }
    
    /**
     * returns boolean values from the config file
     * 
     * @param section
     * @param key
     * @return
     */
    public boolean getBooleanSetting(String section, String key) {
        
        return Boolean.valueOf(defactoConfig.get(section, key));
    }
    
    /**
     * returns string values from defacto config
     * 
     * @param section
     * @param key
     * @return
     */
    public String getStringSetting(String section, String key) {
        
        return defactoConfig.get(section, key);
    }

    /**
     * this should overwrite a config setting, TODO make sure that it does
     * 
     * @param string
     * @param string2
     */
    public void setStringSetting(String section, String key, String value) {

        this.defactoConfig.add(section, key, value);
    }

    /**
     * returns integer values for defacto setting
     * 
     * @param section
     * @param key
     * @return
     */
    public Integer getIntegerSetting(String section, String key) {

        return Integer.valueOf(this.defactoConfig.get(section, key));
    }

    /**
     * returns double values from the config
     * 
     * @param section
     * @param key
     * @return
     */
    public Double getDoubleSetting(String section, String key) {

        return Double.valueOf(this.defactoConfig.get(section, key));
    }
}
