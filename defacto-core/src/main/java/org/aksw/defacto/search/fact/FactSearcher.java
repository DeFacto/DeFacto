package org.aksw.defacto.search.fact;

import org.aksw.defacto.DefactoModel;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public interface FactSearcher {

    /**
     * 
     * @param website
     * @param model
     * @param pattern 
     * @return
     */
    public void generateProofs(Evidence evidence, WebSite website, DefactoModel model, Pattern pattern);
}