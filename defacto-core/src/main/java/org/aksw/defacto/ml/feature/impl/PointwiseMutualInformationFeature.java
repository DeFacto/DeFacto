/**
 * 
 */
package org.aksw.defacto.ml.feature.impl;

import org.aksw.defacto.boa.BoaPatternSearcher;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.AbstractFeature;
import org.aksw.defacto.search.engine.DefaultSearchEngine;
import org.aksw.defacto.search.engine.bing.AzureBingSearchEngine;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class PointwiseMutualInformationFeature extends AbstractFeature {

    @Override
    public void extractFeature(Evidence evidence) {

        DefaultSearchEngine engine = new AzureBingSearchEngine();
        BoaPatternSearcher searcher = new BoaPatternSearcher();
        
//        String subjectLabel = evidence.getModel().getSubjectLabel();
//        String propertyUri  = evidence.getModel().getPropertyUri();
//        String objectLabel  = evidence.getModel().getObjectLabel();
//
//        MetaQuery queryWithoutPattern   = new MetaQuery(String.format("%s|-|%s|-|%s", subjectLabel, "??? NONE ???", objectLabel));
//        Long without = engine.getNumberOfResults(queryWithoutPattern);
//        
//        for ( Pattern pattern : searcher.getNaturalLanguageRepresentations(propertyUri)) {
//            
//            MetaQuery queryWithPattern      = new MetaQuery(String.format("%s|-|%s|-|%s", subjectLabel, pattern.naturalLanguageRepresentation, objectLabel));
//            Long with = engine.getNumberOfResults(queryWithPattern);
//            
//            System.out.println(String.format("With: %s - Without:%s Ratio: %s", with, without, (double) with / (double) without));
//        }
    }
    
//    public static void main(String[] args) {
//
//        long sumarry = 0L;
//        long sumlist = 0L;
//        
//        int iteration = 10;
//        
//        for (int n = 0; n <iteration ; n++ ) {
//            
//            System.out.println("iteration: " + n);
//            
//            int size = 10000000;
//            
//            Integer[] array = new Integer[size];
//            List<Integer> list = new ArrayList<Integer>(size);
//             
//            List<Integer> indexes = new ArrayList<Integer>(size);
//            for ( Integer i = 0; i < size; i++ ) {
//                indexes.add(i);
//                array[i] = i;
//                list.add(i);
//            }
//            Collections.shuffle(indexes);
//            
//            long start = System.nanoTime();
//            for ( Integer i : indexes) {
//                
//                Integer j = array[i];
//            }
//            
//            sumarry += System.nanoTime() - start;
//            
//            start = System.nanoTime();
//            for ( Integer i : indexes) {
//                
//                Integer j = list.get(i);
//            }
//            
//            sumlist += System.nanoTime() - start;
//        }
//        
//        System.out.println("Average-List:\t" + (sumlist * 1D) / iteration);
//        System.out.println("Average-Array:\t" + (sumarry * 1D) / iteration);
//    }
}
