package org.aksw.gui;

import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;

import java.text.NumberFormat;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 6/5/12
 * Time: 11:30 PM
 * This class overrides the normal behavior of StandardCategoryItemLabelGenerator required for PageRank,
 * as for PageRank if it is less than 0, or greater than 10 we should display N/A instead.
 */
public class PageRankCategoryItemLabelGenerator extends StandardCategoryItemLabelGenerator {

    /**
     * Creates a new item label generator with a default number formatter.
     */
    public PageRankCategoryItemLabelGenerator() {
        super();
    }


    public PageRankCategoryItemLabelGenerator(String labelFormat, NumberFormat formatter) {
        super(labelFormat, formatter);
    }

    public String generateLabel(CategoryDataset dataset, int row, int column) {
        if((dataset.getValue(row, column).doubleValue() > 1) || (dataset.getValue(row, column).byteValue() < 0))
            return "N/A";
        return generateLabelString(dataset, row, column);
    }

}
