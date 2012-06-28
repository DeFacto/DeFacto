package org.aksw.provenance.search.fact;

import java.util.List;

import org.aksw.provenance.boa.Pattern;
import org.aksw.provenance.evidence.Evidence;
import org.aksw.provenance.evidence.WebSite;

import com.hp.hpl.jena.rdf.model.Model;

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
    public void generateProofs(Evidence evidence, WebSite website, Model model, Pattern pattern);
}