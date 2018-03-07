package org.aksw.defacto.evaluation;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.model.DefactoTimePeriod;
import org.aksw.defacto.rest.RestModel;
import org.aksw.defacto.search.engine.bing.AzureBingSearchEngine;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.util.BufferedFileWriter;
import org.aksw.defacto.util.Encoder;
import org.aksw.defacto.util.Frequency;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by esteves on 07.03.18.
 */
public class YearFrequencyDistribution {

    private static List<DefactoModel> getModels() throws Exception{

        List<String> triples = FileUtils.readLines(new File(YearFrequencyDistribution.class.getResource("/eval/us_politician_goldstandard.tsv").getFile()), "UTF-8");
        List<DefactoModel> models = new ArrayList<DefactoModel>();
        RestModel restmodel = new RestModel();

        int i = 1;

        for (String t: triples){


            String[] parts = t.split("\t");
            String subject = parts[0];
            String subject_label = parts[0].replace("http://dbpedia.org/resource/", "").replace("_", " ");
            String predicate = parts[1];
            String object = parts[2];
            String object_label = parts[2].replace("http://dbpedia.org/resource/", "").replace("_", " ");
            String from = parts[3];
            String to = parts[4];
            Triple triple =
                    new Triple(NodeFactory.createURI(subject), NodeFactory.createURI(predicate),
                            NodeFactory.createURI(object));
            DefactoModel defactoModel = restmodel.getModel(triple, from, to);
            defactoModel.setName("model_" + String.valueOf(i));
            models.add(defactoModel);
            i=i+1;

        }

        return models;

    }

    public static void main(String[] args) throws Exception {

        Defacto.init();

        List<DefactoModel> models = getModels();
        BufferedFileWriter w1 = new BufferedFileWriter(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "yearFrequencyDistribution.tiny.tsv", Encoder.Encoding.UTF_8, BufferedFileWriter.WRITER_WRITE_MODE.OVERRIDE);
        BufferedFileWriter w2 = new BufferedFileWriter(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "yearFrequencyDistribution.small.tsv", Encoder.Encoding.UTF_8, BufferedFileWriter.WRITER_WRITE_MODE.OVERRIDE);
        BufferedFileWriter w3 = new BufferedFileWriter(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "yearFrequencyDistribution.medium.tsv", Encoder.Encoding.UTF_8, BufferedFileWriter.WRITER_WRITE_MODE.OVERRIDE);
        BufferedFileWriter w4 = new BufferedFileWriter(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "yearFrequencyDistribution.large.tsv", Encoder.Encoding.UTF_8, BufferedFileWriter.WRITER_WRITE_MODE.OVERRIDE);

        for ( DefactoModel model : models ) {
            final Evidence evidence = Defacto.checkFact(model, Defacto.TIME_DISTRIBUTION_ONLY.YES);

            for ( Map.Entry<String, Long> entry : evidence.tinyContextYearOccurrences.entrySet())
                    w1.write(model.getSubjectUri() + "\t" +
                            model.getPropertyUri().toString() + "\t" +
                            model.getObjectUri() + "\t" +
                            entry.getKey() + "\t" + entry.getValue());
            for ( Map.Entry<String, Long> entry : evidence.smallContextYearOccurrences.entrySet())
                w2.write(model.getSubjectUri() + "\t" +
                        model.getPropertyUri().toString() + "\t" +
                        model.getObjectUri() + "\t" +
                        entry.getKey() + "\t" + entry.getValue());
            for ( Map.Entry<String, Long> entry : evidence.mediumContextYearOccurrences.entrySet())
                w3.write(model.getSubjectUri() + "\t" +
                        model.getPropertyUri().toString() + "\t" +
                        model.getObjectUri() + "\t" +
                        entry.getKey() + "\t" + entry.getValue());
            for ( Map.Entry<String, Long> entry : evidence.largeContextYearOccurrences.entrySet())
                w4.write(model.getSubjectUri() + "\t" +
                        model.getPropertyUri().toString() + "\t" +
                        model.getObjectUri() + "\t" +
                        entry.getKey() + "\t" + entry.getValue());

        }
        w1.close();
        w2.close();
        w3.close();
        w4.close();
    }

}
