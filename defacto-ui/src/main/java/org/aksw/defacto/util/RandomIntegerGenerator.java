package org.aksw.defacto.util;

import org.apache.log4j.Logger;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 6/12/12
 * Time: 2:00 PM
 * Generates random integer numbers, which are used in generating the provenance information, and also in generating
 * website thumbnail.
 */
public class RandomIntegerGenerator {

    private static Logger logger = Logger.getLogger(RandomIntegerGenerator.class);

    /**
     * This function is based on StackOverflow article available in
     *      http://stackoverflow.com/questions/5328822/generating-10-digits-unique-random-number-in-java
     * This function generates a random number to be used as a part of the URI which will be given to the triple
     * in the output RDF provenance information
     * @param aStart    The smallest allowed number
     * @param aEnd  The largest allowed number
     * @return  A random number
     */
    public static long generateRandomInteger(int aStart, long aEnd){

        Random aRandom = new Random();

        if ( aStart > aEnd ) {
            throw new IllegalArgumentException("Start cannot exceed End.");
        }
        //get the range, casting to long to avoid overflow problems
        long range = aEnd - (long)aStart + 1;
//        logger.info("range>>>>>>>>>>>"+range);
        // compute a fraction of the range, 0 <= frac < range
        long fraction = (long)(range * aRandom.nextDouble());
//        logger.info("fraction>>>>>>>>>>>>>>>>>>>>"+fraction);
        long randomNumber =  fraction + (long)aStart;
        logger.info("Generated : " + randomNumber);
        return randomNumber;

    }

}
