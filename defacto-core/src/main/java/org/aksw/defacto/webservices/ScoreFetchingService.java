/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.defacto.webservices;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import java.util.logging.Level;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.DefactoModel;

/**
 *
 * @author ngonga
 */
@Path("/getdefactoscore")
public class ScoreFetchingService {

    @GET
    @Produces("application/json")
    public Response getJson(@QueryParam("s") String subject, @QueryParam("p") String property, @QueryParam("o") String object) {
        ServiceMain.log.log(Level.INFO, "Processing <" + subject + ", " + property + "," + object + ">");
        try {
            Model model = ModelFactory.createDefaultModel();

            Resource subj = model.createResource(subject);
            Resource obj = model.createResource(property);
            obj.addProperty(model.createProperty(property), subj);
            double score = Defacto.checkFact(new DefactoModel(model, "subj", true)).getDeFactoScore();
            return Response.ok(score).build();
        } catch (Exception e) {
            ServiceMain.log.log(Level.WARNING, "Error while processing <" + subject + ", " + property + "," + object + ">");
            ServiceMain.log.log(Level.WARNING, e.getMessage());
            e.printStackTrace();
        }
        return Response.serverError().build();
    }
}
