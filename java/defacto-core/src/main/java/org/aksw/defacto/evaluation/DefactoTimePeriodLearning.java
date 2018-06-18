/**
 * 
 */
package org.aksw.defacto.evaluation;

import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.Defacto.TIME_DISTRIBUTION_ONLY;
import org.aksw.defacto.comp.FMeasureComparator;
import org.aksw.defacto.evaluation.configuration.Configuration;
import org.aksw.defacto.evaluation.measure.PrecisionRecallFMeasure;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.model.DefactoTimePeriod;
import org.aksw.defacto.reader.DefactoModelReader;
import org.apache.commons.lang3.StringUtils;
import org.aksw.defacto.util.BufferedFileReader;
import org.aksw.defacto.util.BufferedFileWriter;
import org.aksw.defacto.util.BufferedFileWriter.WRITER_WRITE_MODE;
import org.aksw.defacto.util.Encoder.Encoding;
import org.aksw.defacto.util.Frequency;

/**
 * @author Daniel Gerber <daniel.gerber@deinestadtsuchtdich.de>
 *
 */
public class DefactoTimePeriodLearning {
	
	private static String CURRENT_LANGUAGE = "";
	public static List<String> results = new ArrayList<>();
	public static String trainDirectory	= "";
	static Set<DefactoModel> wrongModels =  new LinkedHashSet<>();
	static DecimalFormat df = new DecimalFormat("#0.#");
	
	static Frequency tiny = new Frequency();
	static Frequency small = new Frequency();
	static Frequency medium = new Frequency();
	static Frequency large = new Frequency();

	public static void main(String[] args) throws FileNotFoundException {
		
		Defacto.init();
		trainDirectory = Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") 
				+ Defacto.DEFACTO_CONFIG.getStringSetting("eval", args[0] + "-directory");
		
//		if ( args[0].equals("train") ) startEvaluation();
//		else if (args[0].equals("test")) printLatexTable();
//		else System.out.println("Nothing to do! Wrong arguments!");
		
//		System.out.println(startSingleConfigurationEvaluation("award", Arrays.asList("en", "fr", "de"),
//				Arrays.asList("award"), "domain", "frequency", "tiny"));
		
		System.out.println(startSingleConfigurationEvaluation("award", Arrays.asList("en", "fr", "de"),
				Arrays.asList("birth"), "domain", "frequency", "tiny"));
//		System.out.println(startSingleConfigurationEvaluation("point", Arrays.asList("en", "fr", "de"),
//				Arrays.asList("award", "birth", "death", "foundationPlace", "publicationDate", "starring", "subsidiary"),
//				"occurrence", "frequency", "tiny"));
//		System.out.println(startSingleConfigurationEvaluation("point", Arrays.asList("en", "fr", "de"),
//				Arrays.asList("award", "birth", "death", "foundationPlace", "publicationDate", "starring", "subsidiary"),
//				"domain", "frequency", "tiny"));
//		System.out.println(startSingleConfigurationEvaluation("point", Arrays.asList("en", "fr", "de"),
//				Arrays.asList("award", "birth", "death", "foundationPlace", "publicationDate", "starring", "subsidiary"),
//				"global", "frequency", "tiny"));
//		
//		System.out.println(startSingleConfigurationEvaluation("period", Arrays.asList("en", "fr", "de"),
//				Arrays.asList("spouse", "nbateam", "leader"), "occurrence", "frequency", "tiny"));
//		System.out.println(startSingleConfigurationEvaluation("period", Arrays.asList("en", "fr", "de"),
//				Arrays.asList("spouse", "nbateam", "leader"), "domain", "frequency", "tiny"));
//		System.out.println(startSingleConfigurationEvaluation("period", Arrays.asList("en", "fr", "de"),
//				Arrays.asList("spouse", "nbateam", "leader"), "global", "frequency", "tiny"));
//		
//		System.out.println(startSingleConfigurationEvaluation("period", Arrays.asList("en", "fr", "de"),
//				Arrays.asList("spouse", "nbateam", "leader"), "occurrence", "frequency", "medium"));
//		System.out.println(startSingleConfigurationEvaluation("period", Arrays.asList("en", "fr", "de"),
//				Arrays.asList("spouse", "nbateam", "leader"), "domain", "frequency", "medium"));
//		System.out.println(startSingleConfigurationEvaluation("period", Arrays.asList("en", "fr", "de"),
//				Arrays.asList("spouse", "nbateam", "leader"), "global", "frequency", "medium"));
//		
//		System.out.println(startSingleConfigurationEvaluation("all", Arrays.asList("en", "fr", "de"),
//				Arrays.asList("award", "birth", "death", "foundationPlace", "publicationDate", "starring", "subsidiary", "spouse", "nbateam", "leader"),
//				"occurrence", "frequency", "tiny"));
//		System.out.println(startSingleConfigurationEvaluation("all", Arrays.asList("en", "fr", "de"),
//				Arrays.asList("award", "birth", "death", "foundationPlace", "publicationDate", "starring", "subsidiary", "spouse", "nbateam", "leader"),
//				"domain", "frequency", "tiny"));
//		System.out.println(startSingleConfigurationEvaluation("all", Arrays.asList("en", "fr", "de"),
//				Arrays.asList("award", "birth", "death", "foundationPlace", "publicationDate", "starring", "subsidiary", "spouse", "nbateam", "leader"),
//				"global", "frequency", "tiny"));
		
//		startBestConfigurationEvaluation();
		
		
//		System.out.println("IMPORTANT! Die Ergebnisse machen nur sinn wenn nur eine Konfiguration gewählt ist!");
//		for ( DefactoModel model : wrongModels ){
//			System.out.println(model.name);
//		}
	}
	
	private static void printLatexTable() throws FileNotFoundException {
		
		System.out.println(configToLatex(startSingleConfigurationEvaluation("award", Arrays.asList("en"), Arrays.asList("award"), "domain", "frequency", "medium")));
		System.out.println(configToLatex(startSingleConfigurationEvaluation("award", Arrays.asList("en", "fr", "de"), Arrays.asList("award"), "domain", "frequency", "tiny")));
		System.out.println("\\midrule");
		
		System.out.println(configToLatex(startSingleConfigurationEvaluation("birth", Arrays.asList("en"), Arrays.asList("birth"), "global", "frequency", "small")));
		System.out.println(configToLatex(startSingleConfigurationEvaluation("birth", Arrays.asList("en", "fr", "de"), Arrays.asList("birth"), "global", "frequency", "tiny")));
		System.out.println("\\midrule");
		
		System.out.println(configToLatex(startSingleConfigurationEvaluation("death", Arrays.asList("en"), Arrays.asList("death"), "domain", "frequency", "tiny")));
		System.out.println(configToLatex(startSingleConfigurationEvaluation("death", Arrays.asList("en", "fr", "de"), Arrays.asList("death"), "domain", "frequency", "tiny")));
		System.out.println("\\midrule");
		
		System.out.println(configToLatex(startSingleConfigurationEvaluation("foundation", Arrays.asList("en"), Arrays.asList("foundationPlace"), "domain", "frequency", "large")));
		System.out.println(configToLatex(startSingleConfigurationEvaluation("foundation", Arrays.asList("en", "fr", "de"), Arrays.asList("foundationPlace"), "domain", "frequency", "large")));
		System.out.println("\\midrule");
		
		System.out.println(configToLatex(startSingleConfigurationEvaluation("publication", Arrays.asList("en"), Arrays.asList("publicationDate"), "global", "frequency", "large")));
		System.out.println(configToLatex(startSingleConfigurationEvaluation("publication", Arrays.asList("en", "fr", "de"), Arrays.asList("publicationDate"), "global", "frequency", "large")));
		System.out.println("\\midrule");
		
		System.out.println(configToLatex(startSingleConfigurationEvaluation("starring", Arrays.asList("en"), Arrays.asList("starring"), "global", "frequency", "small")));
		System.out.println(configToLatex(startSingleConfigurationEvaluation("starring", Arrays.asList("en", "fr", "de"), Arrays.asList("starring"), "domain", "frequency", "medium")));
		System.out.println("\\midrule");
		
		System.out.println(configToLatex(startSingleConfigurationEvaluation("subsidiary", Arrays.asList("en"), Arrays.asList("subsidiary"), "domain", "frequency", "large")));
		System.out.println(configToLatex(startSingleConfigurationEvaluation("subsidiary", Arrays.asList("en", "fr", "de"), Arrays.asList("subsidiary"), "domain", "frequency", "tiny")));
		System.out.println("\\midrule");
		
		System.out.println(configToLatex(startSingleConfigurationEvaluation("spouse", Arrays.asList("en"), Arrays.asList("spouse"), "domain", "frequency", "tiny")));
		System.out.println(configToLatex(startSingleConfigurationEvaluation("spouse", Arrays.asList("en", "fr", "de"), Arrays.asList("spouse"), "domain", "frequency", "tiny")));
		System.out.println("\\midrule");
		
		System.out.println(configToLatex(startSingleConfigurationEvaluation("team", Arrays.asList("en"), Arrays.asList("nbateam"), "domain", "frequency", "large")));
		System.out.println(configToLatex(startSingleConfigurationEvaluation("team", Arrays.asList("en", "fr", "de"), Arrays.asList("nbateam"), "domain", "frequency", "tiny")));
		System.out.println("\\midrule");
		
		System.out.println(configToLatex(startSingleConfigurationEvaluation("leader", Arrays.asList("en"), Arrays.asList("leader"), "domain", "frequency", "medium")));
		System.out.println(configToLatex(startSingleConfigurationEvaluation("leader", Arrays.asList("en", "fr", "de"), Arrays.asList("leader"), "domain", "frequency", "medium")));
		System.out.println("\\midrule");
		
		System.out.println(configToLatex(startSingleConfigurationEvaluation("timepoint", Arrays.asList("en"), Arrays.asList("award", "birth", "death", "foundationPlace", "publicationDate", "starring", "subsidiary"), "occurrence", "frequency", "tiny")));
		System.out.println(configToLatex(startSingleConfigurationEvaluation("timepoint", Arrays.asList("en", "fr", "de"), Arrays.asList("award", "birth", "death", "foundationPlace", "publicationDate", "starring", "subsidiary"), "occurrence", "frequency", "tiny")));
		System.out.println("\\midrule");
		
		System.out.println(configToLatex(startSingleConfigurationEvaluation("timeperiod", Arrays.asList("en"), Arrays.asList("spouse", "nbateam", "leader"), "domain", "frequency", "medium")));
		System.out.println(configToLatex(startSingleConfigurationEvaluation("timeperiod", Arrays.asList("en", "fr", "de"), Arrays.asList("spouse", "nbateam", "leader"), "domain", "frequency", "medium")));
		System.out.println("\\midrule");
		
		System.out.println(configToLatex(startSingleConfigurationEvaluation("all", Arrays.asList("en"), Arrays.asList("award", "birth", "death", "foundationPlace", "publicationDate", "starring", "subsidiary", "spouse", "nbateam", "leader"), "domain", "frequency", "medium")));
		System.out.println(configToLatex(startSingleConfigurationEvaluation("all", Arrays.asList("en", "fr", "de"), Arrays.asList("award", "birth", "death", "foundationPlace", "publicationDate", "starring", "subsidiary", "spouse", "nbateam", "leader"), "domain", "frequency", "medium")));
		System.out.println("\\midrule");
	}

	private static String configToLatex(Configuration cfg) {
		
		List<String> line = new ArrayList<>();
		
		String context = cfg.context.equals("tiny") ?  "25" : cfg.context.equals("small") ?  "50" : cfg.context.equals("medium") ? "100" : "150";
		line.add(cfg.language.equals("[en]") ? cfg.name + "$_{en}^{"+context+"}$" : cfg.language.equals("[en, fr, de]") ? cfg.name + "$_{ml}^{"+context+"}$" : "ERROR");
		line.add(format(cfg.precision));
		line.add(format(cfg.recall));
		line.add(format(cfg.fMeasure));
		line.add(format(cfg.mrrAverage));
		line.add(cfg.correctStart+ "");
		if ( Arrays.asList("award", "birth", "death", "foundation", "publication", "starring", "subsidiary").contains(cfg.name) )
			line.add("-");
		else
			line.add(cfg.correctEnd + "");
		line.add(cfg.isPossible.split("/")[0]);
		line.add(format(cfg.correct / (double)Integer.valueOf(cfg.isPossible.split("/")[0])));
		
		return StringUtils.join(line, "\t&\t") + "\t\\\\";
	}

	private static void generateProofFrequencies() throws FileNotFoundException {
		
		System.out.println(startSingleConfigurationEvaluation("all", Arrays.asList("en", "fr", "de"),
				Arrays.asList("award", "birth", "death", "foundationPlace", "publicationDate", "starring", "subsidiary", "spouse", "nbateam", "leader"),
				"occurrence", "frequency", "tiny"));
		
		System.out.println(startSingleConfigurationEvaluation("all", Arrays.asList("en", "fr", "de"),
				Arrays.asList("award", "birth", "death", "foundationPlace", "publicationDate", "starring", "subsidiary", "spouse", "nbateam", "leader"),
				"occurrence", "frequency", "small"));
		
		System.out.println(startSingleConfigurationEvaluation("all", Arrays.asList("en", "fr", "de"),
				Arrays.asList("award", "birth", "death", "foundationPlace", "publicationDate", "starring", "subsidiary", "spouse", "nbateam", "leader"),
				"occurrence", "frequency", "medium"));
		
		System.out.println(startSingleConfigurationEvaluation("all", Arrays.asList("en", "fr", "de"),
				Arrays.asList("award", "birth", "death", "foundationPlace", "publicationDate", "starring", "subsidiary", "spouse", "nbateam", "leader"),
				"occurrence", "frequency", "large"));
	}
	
	private static Configuration startSingleConfigurationEvaluation(String name, List<String> languages, List<String> relations, 
			String normalizer, String searchMethod, String contextSize) throws FileNotFoundException {
		
		CURRENT_LANGUAGE = languages.toString();
		List<DefactoModel>  models = new ArrayList<>();
		for ( String relation : relations ) models.addAll(DefactoModelReader.readModels(trainDirectory + "correct/" + relation, true, languages));
		Defacto.DEFACTO_CONFIG.setStringSetting("settings", "TIME_PERIOD_SEARCHER", normalizer);
		Defacto.DEFACTO_CONFIG.setStringSetting("settings", "periodSearchMethod", searchMethod);
		Defacto.DEFACTO_CONFIG.setStringSetting("settings", "context-size", contextSize);
		Collections.shuffle(models, new Random(100));
//		models = models.subList(0, 20);
		
		return learn(name, models, new ArrayList<Configuration>());
	}
	
	private static Configuration startBestConfigurationEvaluation() throws FileNotFoundException {
		
		return startSingleConfigurationEvaluation("all", Arrays.asList("en", "fr", "de"),
				Arrays.asList("award", "birth", "death", "foundationPlace", "publicationDate", "starring", "subsidiary", "spouse", "nbateam", "leader"),
				"occurrence", "frequency", "tiny");
	}

	public static void startEvaluation() throws FileNotFoundException{
		
		// single - points
		printBestConfigurations(createOptionsAndStart("award", Arrays.asList("award")));
		printBestConfigurations(createOptionsAndStart("birth", Arrays.asList("birth")));
		printBestConfigurations(createOptionsAndStart("death", Arrays.asList("death")));
		printBestConfigurations(createOptionsAndStart("foundation", Arrays.asList("foundationPlace")));
		printBestConfigurations(createOptionsAndStart("publication", Arrays.asList("publicationDate")));
		printBestConfigurations(createOptionsAndStart("starring", Arrays.asList("starring")));
		printBestConfigurations(createOptionsAndStart("subsidiary", Arrays.asList("subsidiary")));
		// single - periods
		printBestConfigurations(createOptionsAndStart("spouse", Arrays.asList("spouse")));
		printBestConfigurations(createOptionsAndStart("nbateam", Arrays.asList("nbateam")));
		printBestConfigurations(createOptionsAndStart("leader", Arrays.asList("leader")));
		
		// timepoints
		printBestConfigurations(createOptionsAndStart("timepoint", Arrays.asList("birth", "death", "foundationPlace", "publicationDate", "starring", "subsidiary")));
		
		// time periods
		printBestConfigurations(createOptionsAndStart("timeperiod", Arrays.asList("spouse", "nbateam", "leader")));
		
		// all
		printBestConfigurations(createOptionsAndStart("all", Arrays.asList("award", "birth", "death", "foundationPlace", "publicationDate", "starring", "subsidiary", "spouse", "nbateam", "leader")));
	}
	
	private static void printBestConfigurations(List<Configuration> configs) {
		
		Configuration occEn 	= getBestCfg(configs, "occurrence", Arrays.asList("en"));
		Configuration gloEn 	= getBestCfg(configs, "global", 	Arrays.asList("en"));
		Configuration domEn 	= getBestCfg(configs, "domain", 	Arrays.asList("en"));
		
		Configuration occEnFrDe = getBestCfg(configs, "occurrence", Arrays.asList("en", "fr", "de"));
		Configuration gloEnFrDe = getBestCfg(configs, "global", 	Arrays.asList("en", "fr", "de"));
		Configuration domEnFrDe = getBestCfg(configs, "domain", 	Arrays.asList("en", "fr", "de"));
		
		System.out.println(fillCells(occEn.name + "$_{en}$", occEn, gloEn, domEn));
		System.out.println(fillCells(occEnFrDe.name + "$_{ml}$", occEnFrDe, gloEnFrDe, domEnFrDe) + "\\midrule");
	}
	
	/**
	 * 
	 * @param cells
	 * @param cfgs
	 * @return
	 */
	private static String fillCells(String name, Configuration ... cfgs) {
		
		Double maxF1 = Collections.max(new ArrayList<Double>(Arrays.asList(cfgs[0].fMeasure, cfgs[1].fMeasure, cfgs[2].fMeasure)));
		
		List<String> cells = new ArrayList<>();
		cells.add(name);
		
		int i = 0;
		for ( Configuration cfg : cfgs) {
			
			i++;
			
			cells.add(cfg.context.equals("tiny") ?  "25" : cfg.context.equals("small") ?  "50" : cfg.context.equals("medium") ? "100" : "150" );
			cells.add(format(cfg.precision));
			cells.add(format(cfg.recall));
			cells.add(maxF1.equals(cfg.fMeasure) ? "\\textbf{" + format(cfg.fMeasure) + "}" : format(cfg.fMeasure));
			cells.add(format(cfg.mrrAverage));
			cells.add(cfg.isPossible.split("/")[0]);
			cells.add(format(cfg.correct / (double) Integer.valueOf(cfg.isPossible.split("/")[0])));
			if (i < 3) cells.add(" ");
		}
		
		return StringUtils.join(cells, " & ") + "\\\\";
	}
	
	/**
	 * 
	 * @param value
	 * @return
	 */
	private static String format(Double value) {
		return df.format(value * 100).replace(",", ".");
	}
	
	public static Configuration getBestCfg(List<Configuration> configs, String normalizer, List<String> languages){
		
		List<Configuration> filteredConfigs = new ArrayList<>();
		
		for ( Configuration cfg : configs)
			if ( cfg.normalizer.equals(normalizer) && cfg.language.equals(languages.toString()) ) filteredConfigs.add(cfg);
		
		Collections.sort(filteredConfigs, new FMeasureComparator());
		
		return filteredConfigs.get(0);
	}

	public static List<Configuration> createOptionsAndStart(String name, List<String> relations) throws FileNotFoundException {
		
		List<Configuration> configurations = new ArrayList<>();
		
		List<String> contextSizes = new ArrayList<>();
		contextSizes.add("tiny");
		contextSizes.add("small");
		contextSizes.add("medium");
		contextSizes.add("large");
		
		List<String> normalizers = new ArrayList<>();
		normalizers.add("occurrence");
		normalizers.add("global");
		normalizers.add("domain");
		
		List<List<String>> languages 		= new ArrayList<List<String>>();
		languages.add(Arrays.asList("en", "fr", "de"));
		languages.add(Arrays.asList("en"));
		
		for ( String contextSize : contextSizes  ) {
			Defacto.DEFACTO_CONFIG.setStringSetting("settings", "context-size", contextSize);

			for ( List<String> language : languages) {
				CURRENT_LANGUAGE = language.toString();
					
				List<DefactoModel>  models = new ArrayList<>();
				for ( String relation : relations ) models.addAll(DefactoModelReader.readModels(trainDirectory + "correct/" + relation, true, language));
				
				for ( String normalizer : normalizers ) {
					Defacto.DEFACTO_CONFIG.setStringSetting("settings", "TIME_PERIOD_SEARCHER", normalizer);
					
					List<String> patternSearch = new ArrayList<>();
//					patternSearch.add("pattern");
					patternSearch.add("frequency");
					
					for ( String periodSearchMethod : patternSearch ) {
						Defacto.DEFACTO_CONFIG.setStringSetting("settings", "periodSearchMethod", periodSearchMethod);
						
						learn(name, models, configurations);
					}
				}
			}
		}
		
		Collections.sort(configurations, new FMeasureComparator());
		
		BufferedFileWriter writer  = new BufferedFileWriter(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "machinelearning/eval/" + name + ".tsv", Encoding.UTF_8, WRITER_WRITE_MODE.APPEND);
		for ( Configuration configuration : configurations ) writer.write(configuration.toString());
		writer.close();
		
		return configurations;
	}
	
	/**
	 * 
	 * @param defactoTimePeriod
	 * @param model
	 * @return
	 */
	private static PrecisionRecallFMeasure getMeasures(DefactoTimePeriod defactoTimePeriod, DefactoModel model) {
		
		Set<Integer> relevantYears = new LinkedHashSet<>();
		for ( int i = model.timePeriod.from ; i <= model.timePeriod.to ; i++) relevantYears.add(i);
		
		Set<Integer> retrievedYears = new LinkedHashSet<>();
		if ( !defactoTimePeriod.equals(DefactoTimePeriod.EMPTY_DEFACTO_TIME_PERIOD) )
			for ( int i = defactoTimePeriod.from ; i <= defactoTimePeriod.to ; i++) 
				retrievedYears.add(i);
		
		return new PrecisionRecallFMeasure(relevantYears, retrievedYears);
	}

	public static Configuration learn(String name, List<DefactoModel> models, List<Configuration> configurations){
		
		int precisionCounter = 0;
		int isPossible = 0;
		int correct = 0;
		
		Double macroPrecision	= 0D;
		Double macroRecall		= 0D;
		Double macroFmeasure	= 0D;
		
		int correctStart = 0;
		int correctEnd = 0;
		
		Double meanReciprocalRank = 0D;
		int meanReciprocalRankCounter = 0;
		
		// check all facts for this configuration
		for (int i = 0; i < models.size(); i++) {

			DefactoModel model = models.get(i); 
			
			Evidence evidence = Defacto.checkFact(model, TIME_DISTRIBUTION_ONLY.YES);
//			createProofFrequency(evidence);
			DefactoTimePeriod dtp = evidence.defactoTimePeriod;
			
			
			System.out.println("Found:"+dtp);
			System.out.println("GS:   "+model.timePeriod);
			
			// correct year is in retrieved year set
			if (evidence.getPreferedContext().containsKey(model.timePeriod.from + "")) isPossible++; 
			
			// calculate MRR only for time point
			if (model.timePeriod.isTimePoint()) {
				
				// get the index of the correct year in the retrieved year set
				int hitIndex = getHitIndex(evidence.getPreferedContext(), model.timePeriod.from);
					
//				// debug wrong facts
//				if ( hitIndex > 1 )  {
//					
//					System.out.println("FROM: "+ model.timePeriod.from + " TO: " + model.timePeriod.to + " INDEX: " + hitIndex);
//					for ( Entry<String, Long> entry : evidence.getPreferedContext().entrySet()){
//						System.out.println(entry.getKey() + "\t" + entry.getValue() + "\t" +  
//								entry.getValue() * org.aksw.defacto.search.time.TimeUtil.getGlobalNormalizedPopularity(Integer.valueOf(entry.getKey())) + "\t" +  
//								entry.getValue() * org.aksw.defacto.search.time.TimeUtil.getDomainNormalizedPopularity(Integer.valueOf(entry.getKey())));
//					}
//					System.out.println();
//				}
				
				// we only want to count years of facts where we can find a correct year in the given context
				if ( hitIndex >= 0 ) {
					
					meanReciprocalRank += 1D / (double) hitIndex;
					meanReciprocalRankCounter++;
				}
			}
			
			PrecisionRecallFMeasure results = getMeasures(dtp, model);
			
			// no years found so we can't calculate a proper precision 
			if ( !results.precision.isNaN() ) { 
				
				macroPrecision += results.precision;
				precisionCounter++;
			}
			macroRecall += results.recall;
			macroFmeasure = getFmeasure(macroPrecision / precisionCounter, macroRecall / (i + 1));
			
			if ( model.timePeriod.from.equals(dtp.from) ) correctStart++;
			if ( model.timePeriod.to.equals(dtp.to) ) correctEnd++;
			
			if ( results.fmeasure == 1 ) correct++;
			else wrongModels.add(model);
//			System.out.println(String.format(Locale.ENGLISH, "%s/%s total --> P: %.5f, R: %.5f, F: %.5f, MRR: %.5f", 
//					i+1, models.size(), macroPrecision / (precisionCounter),
//					macroRecall / (i + 1), macroFmeasure, meanReciprocalRank / meanReciprocalRankCounter)
//					+  " current --> " + results);
		}
		
		Configuration config = new Configuration(name, results);
		
		config.precision 			= macroPrecision / precisionCounter;
		config.recall 				= macroRecall / (models.size());
		config.fMeasure 			= macroFmeasure;
		config.correct 				= correct;
		config.correctStart			= correctStart;
		config.correctEnd			= correctEnd;
		config.normalizer 			= Defacto.DEFACTO_CONFIG.getStringSetting("settings", "TIME_PERIOD_SEARCHER");
		config.context 				= Defacto.DEFACTO_CONFIG.getStringSetting("settings", "context-size");
		config.language 			= CURRENT_LANGUAGE;
		config.periodSearchMethod 	= Defacto.DEFACTO_CONFIG.getStringSetting("settings", "periodSearchMethod");
		config.isPossible 			= isPossible +"/"+ models.size();
		config.mrrAverage 			= meanReciprocalRankCounter == 0 ? 0 : meanReciprocalRank / meanReciprocalRankCounter;
		
		configurations.add(config);
		
		return config;
	}
	
	/**
	 * 
	 * @param evidence
	 */
	private static void createProofFrequency(Evidence evidence) {
		
		switch ( Defacto.DEFACTO_CONFIG.getStringSetting("settings", "context-size") ) {
		
			case "tiny" : {
				for ( Entry<String, Long> entry : evidence.tinyContextYearOccurrences.entrySet())
					for ( int i = 0; i < entry.getValue() ; i++) if ( Integer.valueOf(entry.getKey()) < 2014) tiny.addValue(entry.getKey());
				break;
			}
			case "small" : {
				for ( Entry<String, Long> entry : evidence.smallContextYearOccurrences.entrySet())
					for ( int i = 0; i < entry.getValue() ; i++) if ( Integer.valueOf(entry.getKey()) < 2014) small.addValue(entry.getKey());
				break;
			}
			case "medium" : {
				for ( Entry<String, Long> entry : evidence.mediumContextYearOccurrences.entrySet())
					for ( int i = 0; i < entry.getValue() ; i++) if ( Integer.valueOf(entry.getKey()) < 2014) medium.addValue(entry.getKey());
				
				break;
			}
			case "large" : {
				for ( Entry<String, Long> entry : evidence.largeContextYearOccurrences.entrySet())
					for ( int i = 0; i < entry.getValue() ; i++) if ( Integer.valueOf(entry.getKey()) < 2014) large.addValue(entry.getKey());
				
				break;
			}
			
			default: throw new RuntimeException("Context size: " + 
				Defacto.DEFACTO_CONFIG.getStringSetting("settings", "context-size") + " not supported!");
		}
		
		BufferedFileWriter tiny		= new BufferedFileWriter("/Users/gerb/Development/workspaces/experimental/defacto/mltemp/eval/freq/tiny.tsv", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
		for ( Entry<Comparable<?>, Long> entry : DefactoTimePeriodLearning.tiny.sortByValue() ) tiny.write(entry.getKey() + "\t" + entry.getValue());
		tiny.close();
		
		BufferedFileWriter small	= new BufferedFileWriter("/Users/gerb/Development/workspaces/experimental/defacto/mltemp/eval/freq/small.tsv", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
		for ( Entry<Comparable<?>, Long> entry : DefactoTimePeriodLearning.small.sortByValue() ) small.write(entry.getKey() + "\t" + entry.getValue());
		small.close();
		
		BufferedFileWriter medium	= new BufferedFileWriter("/Users/gerb/Development/workspaces/experimental/defacto/mltemp/eval/freq/medium.tsv", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
		for ( Entry<Comparable<?>, Long> entry : DefactoTimePeriodLearning.medium.sortByValue() ) medium.write(entry.getKey() + "\t" + entry.getValue());
		medium.close();
		
		BufferedFileWriter large	= new BufferedFileWriter("/Users/gerb/Development/workspaces/experimental/defacto/mltemp/eval/freq/large.tsv", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
		for ( Entry<Comparable<?>, Long> entry : DefactoTimePeriodLearning.large.sortByValue() ) large.write(entry.getKey() + "\t" + entry.getValue());
		large.close();
	}

	/**
	 * 
	 * @param preferedContext
	 * @param year
	 * @return
	 */
	private static int getHitIndex(Map<String, Long> preferedContext, Integer year) {
		
		Frequency freq = new Frequency();
		for ( Entry<String, Long> entry : preferedContext.entrySet())
			for (int i = 0; i < entry.getValue(); i++ )
				freq.addValue(entry.getKey());
		
		List<Entry<Comparable<?>, Long>> values = freq.sortByValue();
		
		// find the first and second most occurring year values
		for ( int i = 0; i < values.size() ; i++ ) {
			
			if ( values.get(i).getKey().equals(year + "") ) return i + 1;
		}
		
		return -1;
	}

	public static Double getFmeasure(Double precision, Double recall) {
		
		if ( precision + recall == 0) return 0D;
		return (2*precision*recall) / (precision + recall);
	}
}
