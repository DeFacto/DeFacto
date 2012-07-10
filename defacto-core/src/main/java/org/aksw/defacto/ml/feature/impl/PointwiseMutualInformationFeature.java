/**
 * 
 */
package org.aksw.defacto.ml.feature.impl;

import java.io.File;
import java.io.IOException;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.BoaPatternSearcher;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.AbstractFeature;
import org.aksw.defacto.search.engine.bing.BingSearchEngine;
import org.aksw.defacto.search.query.MetaQuery;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class PointwiseMutualInformationFeature extends AbstractFeature {

    @Override
    public void extractFeature(Evidence evidence) {

        BingSearchEngine engine = new BingSearchEngine();
        BoaPatternSearcher searcher = new BoaPatternSearcher();
        
        String subjectLabel = evidence.getModel().getSubjectLabel();
        String propertyUri  = evidence.getModel().getPropertyUri();
        String objectLabel  = evidence.getModel().getObjectLabel();

        MetaQuery queryWithoutPattern   = new MetaQuery(String.format("%s|-|%s|-|%s", subjectLabel, "??? NONE ???", objectLabel));
        Long without = engine.getNumberOfResults(queryWithoutPattern);
        
        for ( Pattern pattern : searcher.getNaturalLanguageRepresentations(propertyUri)) {
            
            MetaQuery queryWithPattern      = new MetaQuery(String.format("%s|-|%s|-|%s", subjectLabel, pattern.naturalLanguageRepresentation, objectLabel));
            Long with = engine.getNumberOfResults(queryWithPattern);
            
            System.out.println(String.format("With: %s - Without:%s Ratio: %s", with, without, (double) with / (double) without));
        }
    }
}
