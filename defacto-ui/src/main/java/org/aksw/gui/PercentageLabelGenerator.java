package org.aksw.gui;

import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;

import java.text.MessageFormat;
import java.text.NumberFormat;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 6/9/12
 * Time: 12:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class PercentageLabelGenerator extends StandardCategoryItemLabelGenerator {

    private String labelFormat;
    private NumberFormat percentFormat;
    private NumberFormat numberFormat;
    private String nullValueString;

    /**
     * Creates a new item label generator with a default number formatter.
     */
    public PercentageLabelGenerator() {
        super();
    }


    public PercentageLabelGenerator(String labelFormat, NumberFormat formatter) {
        super(labelFormat, formatter);
        this.labelFormat = labelFormat;
    }

    public PercentageLabelGenerator(String labelFormat, NumberFormat formatter, java.text.NumberFormat percentFormatter) {
        super(labelFormat, formatter, percentFormatter);
        if (labelFormat == null) {
            throw new IllegalArgumentException("Null 'labelFormat' argument.");
        }
        if (formatter == null) {
            throw new IllegalArgumentException("Null 'formatter' argument.");
        }
        if (percentFormatter == null) {
            throw new IllegalArgumentException("Null 'percentFormatter' argument.");
        }
        this.labelFormat = labelFormat;
        this.numberFormat = formatter;
        this.percentFormat = percentFormatter;
        this.nullValueString = "-";
    }

    public String generateLabel(CategoryDataset dataset, int row, int column) {

        if (dataset == null) {
            throw new IllegalArgumentException("Null 'dataset' argument.");
        }
        if((dataset.getValue(row, column).doubleValue() > 100) || (dataset.getValue(row, column).byteValue() < 0))
            return "N/A";

        String result = null;
        Object[] items = createItemArray(dataset, row, column);
        result = MessageFormat.format(this.labelFormat, items);
        return result;
    }

    @Override
    protected Object[] createItemArray(CategoryDataset dataset, int row, int column) {
//        return super.createItemArray(dataset, row, column);    //To change body of overridden methods use File | Settings | File Templates.

        Object[] result = new Object[4];
        result[0] = dataset.getRowKey(row).toString();
        result[1] = dataset.getColumnKey(column).toString();
        Number value = dataset.getValue(row, column);
        if (value != null) {
            if (this.numberFormat != null) {
                result[2] = this.numberFormat.format(value.doubleValue()/100);
            }
        }
        else {
            result[2] = this.nullValueString;
        }
        if (value != null) {
            double total = 100d;
            double percent = value.doubleValue() / total;
            result[3] = this.percentFormat.format(percent);
        }
        return result;
    }


}
