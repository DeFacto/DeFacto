/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.defacto.webservices;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;
import java.io.File;
import java.util.logging.Level;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.DefactoModel;
import org.aksw.defacto.config.DefactoConfig;
import org.ini4j.Ini;

/**
 *
 * @author ngonga
 */
@Path("/getdefactotimes")
public class TimeSequenceService {

    @GET
    @Produces("application/json")
    public Response getJson(@QueryParam("s") String subject, @QueryParam("slabel") String slabel,
            @QueryParam("p") String property, @QueryParam("o") String object, @QueryParam("olabel") String olabel) {
        ServiceMain.log.log(Level.INFO, "Processing <" + subject + ", " + property + "," + object + ">");
        try {

            org.apache.log4j.PropertyConfigurator.configure("log/log4j.properties");
            Defacto.DEFACTO_CONFIG = new DefactoConfig(new Ini(new File("defacto.ini")));
            Model model = ModelFactory.createDefaultModel();
            Resource subj = model.createResource(subject);
            subj.addProperty(RDFS.label, slabel);
            Resource obj = model.createResource(property);
            obj.addProperty(RDFS.label, olabel);
            obj.addProperty(model.createProperty(property), subj);
            double score = Defacto.checkFact(new DefactoModel(model, subject + " " + property + " " + object, true)).getDeFactoScore();
            return Response.ok(score).build();
        } catch (Exception e) {
            ServiceMain.log.log(Level.WARNING, "Error while processing <" + subject + "," + property + "," + object + ">");
            ServiceMain.log.log(Level.WARNING, e.getMessage());
            e.printStackTrace();
        }
        return Response.serverError().build();
    }
}
