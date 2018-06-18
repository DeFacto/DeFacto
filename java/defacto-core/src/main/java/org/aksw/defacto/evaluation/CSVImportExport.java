/**
 * 
 */
package org.aksw.defacto.evaluation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import rationals.converters.ToString;
import au.com.bytecode.opencsv.CSVWriter;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.UnassignedDatasetException;
import weka.core.Utils;

/**
 * @author Daniel Gerber <daniel.gerber@deinestadtsuchtdich.de>
 * 
 */
public class CSVImportExport {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		export();
	}

	public static void export() throws FileNotFoundException, IOException {

		// BufferedFileWriter writer = new
		// BufferedFileWriter("/Users/gerb/Development/workspaces/experimental/defacto/mltemp/machinelearning/defacto_fact_train.arff.csv",
		// Encoding.UTF_8,
		// WRITER_WRITE_MODE.OVERRIDE);
		Instances instances = new Instances(new BufferedReader(new FileReader("/Users/gerb/Development/workspaces/experimental/defacto/mltemp/machinelearning/defacto_fact_train.arff")));

		CSVWriter writer = new CSVWriter(new FileWriter("/Users/gerb/Development/workspaces/experimental/defacto/mltemp/machinelearning/defacto_fact_train.arff.csv"), '\t');
		for (int i = 0; i < instances.numInstances(); i++) {
//			writer.write(toStringArray(instances.instance(i)));
			writer.writeNext(toStringArray(instances.instance(i)));
		}

		writer.close();
	}

	public static String[] toStringArray(Instance instance) {

		List<String> line = new ArrayList<String>();

		for (int i = 0; i < instance.numAttributes(); i++) {
//			if (i > 0) line.add("\t");
			line.add(toString(instance, i));
		}

		return line.toArray(new String[]{});
	}

	public static final String toString(Instance instance, int attIndex) {

		StringBuffer text = new StringBuffer();

		if (instance.isMissing(attIndex)) {
			text.append("?");
		} else {
			switch (instance.dataset().attribute(attIndex).type()) {
			case Attribute.NOMINAL:
			case Attribute.STRING:
			case Attribute.DATE:
			case Attribute.RELATIONAL:
				text.append(Utils.quote(stringValue(instance, attIndex)));
				break;
			case Attribute.NUMERIC:
				text.append(Utils.doubleToString(value(instance, attIndex), 6));
				break;
			default:
				throw new IllegalStateException("Unknown attribute type");
			}
		}
		return text.toString();
	}

	public static final String stringValue(Instance instance, int attIndex) {

		if (instance.dataset() == null) {
			throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
		}
		return stringValue(instance, instance.dataset().attribute(attIndex));
	}

	public static final String stringValue(Instance instance, Attribute att) {

		int attIndex = att.index();
		switch (att.type()) {
		case Attribute.NOMINAL:
		case Attribute.STRING:
			return att.value((int) value(instance, attIndex));
		case Attribute.DATE:
			return att.formatDate(value(instance, attIndex));
		case Attribute.RELATIONAL:
			return att.relation((int) value(instance, attIndex)).toString();
		default:
			throw new IllegalArgumentException("Attribute isn't nominal, string or date!");
		}
	}

	public static double value(Instance instance, int attIndex) {

		return instance.value(attIndex);
	}
}