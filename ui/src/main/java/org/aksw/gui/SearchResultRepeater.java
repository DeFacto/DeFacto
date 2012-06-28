package org.aksw.gui;

import com.jensjansson.pagedtable.PagedTableContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.*;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import org.aksw.provenance.evidence.WebSite;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.vaadin.addon.JFreeChartWrapper;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 6/2/12
 * Time: 9:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class SearchResultRepeater extends VerticalLayout {
    
    Logger logger = Logger.getLogger(SearchResultRepeater.class);

    private static final int DEFAULT_NUMBER_OF_RESULTS_PER_PAGE = 10;
    protected boolean alwaysRecalculateColumnWidths = false;
    
    private int renderStartIndex = 0;

    private PagedTableContainer container;
    
    private ArrayList<WebSite> arrResultingWebsites = null;
    GridLayout resultsLayout = null;

    private int numOfResultsPerPage;

    public SearchResultRepeater(ArrayList<WebSite> arrResultingWebsites, int numOfResultsPerPage) {
        this.arrResultingWebsites = arrResultingWebsites;
        this.numOfResultsPerPage = numOfResultsPerPage;
        
        resultsLayout = createCoreGridLayout(numOfResultsPerPage);
        this.addComponent(resultsLayout);

        renderResults();
    }

    private GridLayout createCoreGridLayout(int numOfResultsPerPage) {
        //Prepare a grid for the results, containing the required number of rows
        resultsLayout = new GridLayout(3, numOfResultsPerPage);
        resultsLayout.setSizeFull();
        resultsLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        resultsLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        return resultsLayout;
    }

    public SearchResultRepeater(int numOfResultsPerPage) {

        this.numOfResultsPerPage = numOfResultsPerPage;

        resultsLayout = createCoreGridLayout(numOfResultsPerPage);
        this.addComponent(resultsLayout);
    }

    public SearchResultRepeater(ArrayList<WebSite> arrResultingWebsites) {
        this.arrResultingWebsites = arrResultingWebsites;

        numOfResultsPerPage = DEFAULT_NUMBER_OF_RESULTS_PER_PAGE;

        resultsLayout = createCoreGridLayout(numOfResultsPerPage);
        this.addComponent(resultsLayout);

        renderResults();
    }

    public ArrayList<WebSite> getDataSource() {
        return arrResultingWebsites;
    }

    public void setDataSource(ArrayList<WebSite> arrResultingWebsites) {
        this.arrResultingWebsites = arrResultingWebsites;

        renderResults();
    }

    public void setPageLength(int pageLength) {

        if (pageLength >= 0 && getPageLength() != pageLength) {
            container.setPageLength(pageLength);

            numOfResultsPerPage = pageLength;
        }

    }

    public int getPageLength() {

        return numOfResultsPerPage;
    }

    private void renderResults(){

        //If there is no data in the array, then just do nothing
        if((arrResultingWebsites == null) || (arrResultingWebsites.size() == 0))
            return;

        //Create the layout, or clear it if it was already created before
        if(resultsLayout == null)
            resultsLayout = createCoreGridLayout(numOfResultsPerPage);
        else
            resultsLayout.removeAllComponents();

        int remainingItemsInList = arrResultingWebsites.size() - renderStartIndex;

        //Determines the last index at which the render stops, as the could be the remaining number of results
        //is less than number of results per page
        int renderStopIndex = numOfResultsPerPage < remainingItemsInList ? numOfResultsPerPage : remainingItemsInList;

        for(int i = 0; i < renderStopIndex; i++ ){
            TextField txtPredicate = new com.vaadin.ui.TextField();
            txtPredicate.setWidth(60, Sizeable.UNITS_PERCENTAGE);

            txtPredicate.setValue(arrResultingWebsites.get(renderStartIndex + i).getUrl());

            resultsLayout.addComponent(txtPredicate, 1, i);

            Panel chartPanel = createPageRankChart(createDataset(arrResultingWebsites.get(renderStartIndex + i).getPageRank()));

            resultsLayout.addComponent(chartPanel, 2, i);
        }

        this.addComponent(createControls());
    }

    /**
     * Creates a dataset suitable for the chart
     * @param pageRank  The page rank that should be used in the chart
     * @return  Dataset containing the PageRank, but to be used in the chart
     */
    private CategoryDataset createDataset(int pageRank) {
        double[][] data = new double[][] {
                {pageRank},
        };
        return DatasetUtilities.createCategoryDataset("",
                "", data);
    }

    private Panel createPageRankChart(CategoryDataset pageRankDataset){

        JFreeChart pageRankChart = ChartFactory.createBarChart("", // Title
                "", // x-axis Label
                "PageRank", // y-axis Label
                pageRankDataset, // Dataset
                PlotOrientation.HORIZONTAL, // Plot Orientation
                false, // Show Legend
                false, // Use tooltips
                false // Configure chart to generate URLs?
        );

        JFreeChartWrapper wrapper = new JFreeChartWrapper(pageRankChart);

        Panel chartPanel = new Panel();

        chartPanel.addComponent(wrapper);
        chartPanel.setScrollable(true);
        ((BarRenderer)pageRankChart.getCategoryPlot().getRenderer()).setMaximumBarWidth(0.2);;
        ((BarRenderer)pageRankChart.getCategoryPlot().getRenderer()).setShadowVisible(false);
        ((BarRenderer)pageRankChart.getCategoryPlot().getRenderer()).setItemMargin(-2);
        pageRankChart.getCategoryPlot().setBackgroundPaint(Color.WHITE);
        pageRankChart.getCategoryPlot().getRangeAxis(0).setRange(0d, 10d);
        ((NumberAxis)pageRankChart.getCategoryPlot().getRangeAxis(0)).setTickUnit(new NumberTickUnit(1));

        wrapper.setHeight(100, Sizeable.UNITS_PIXELS);
        wrapper.setWidth(300, Sizeable.UNITS_PIXELS);

        return chartPanel;
    }

    /*@Override
    public void setPageLength(int pageLength) {
        if (pageLength >= 0 && getPageLength() != pageLength) {
            container.setPageLength(pageLength);
            super.setPageLength(pageLength);
            firePagedChangedEvent();
        }
    }*/

    public void nextPage() {
        setPageFirstIndex(container.getStartIndex() + getPageLength());
    }

    public void previousPage() {
        setPageFirstIndex(container.getStartIndex() - getPageLength());
    }

    public int getCurrentPage() {
        double pageLength = getPageLength();
        int page = (int) Math.floor((double) container.getStartIndex()
                / pageLength) + 1;
        if (page < 1) {
            page = 1;
        }
        return page;
    }

    public void setCurrentPage(int page) {
        int newIndex = (page - 1) * getPageLength();
        if (newIndex < 0) {
            newIndex = 0;
        }
        if (newIndex >= 0 && newIndex != container.getStartIndex()) {
            setPageFirstIndex(newIndex);
        }
    }

    public int getTotalAmountOfPages() {
        int size = container.getContainer().size();
        double pageLength = getPageLength();
        int pageCount = (int) Math.ceil(size / pageLength);
        if (pageCount < 1) {
            pageCount = 1;
        }
        return pageCount;
    }

    private void setPageFirstIndex(int firstIndex) {
        if (container != null) {
            if (firstIndex <= 0) {
                firstIndex = 0;
            }
            if (firstIndex > container.getRealSize() - 1) {
                int size = container.getRealSize() - 1;
                int pages = 0;
                if (getPageLength() != 0) {
                    pages = (int) Math.floor(0.0 + size / getPageLength());
                }
                firstIndex = pages * getPageLength();
            }
            container.setStartIndex(firstIndex);

            if (alwaysRecalculateColumnWidths) {
                for (Object columnId : container.getContainerPropertyIds()) {
                    //setColumnWidth(columnId, -1);
                }
            }
            //firePagedChangedEvent();
        }
    }

    /*private void firePagedChangedEvent() {
        if (listeners != null) {
            PagedTableChangeEvent event = new PagedTableChangeEvent(this);
            for (PageChangeListener listener : listeners) {
                listener.pageChanged(event);
            }
        }
    }*/

    public HorizontalLayout createControls() {
        /*com.vaadin.ui.Label itemsPerPageLabel = new com.vaadin.ui.Label("Items per page:");
        final ComboBox itemsPerPageSelect = new ComboBox();

        itemsPerPageSelect.addItem("5");
        itemsPerPageSelect.addItem("10");
        itemsPerPageSelect.addItem("25");
        itemsPerPageSelect.addItem("50");
        itemsPerPageSelect.addItem("100");
        itemsPerPageSelect.addItem("600");
        itemsPerPageSelect.setImmediate(true);
        itemsPerPageSelect.setNullSelectionAllowed(false);
        itemsPerPageSelect.setWidth("50px");
        itemsPerPageSelect.addListener(new Property.ValueChangeListener() {
            private static final long serialVersionUID = -2255853716069800092L;

            public void valueChange(
                    com.vaadin.data.Property.ValueChangeEvent event) {
                setPageLength(Integer.valueOf(String.valueOf(event
                        .getProperty().getValue())));
            }
        });
        //itemsPerPageSelect.select("25");
        com.vaadin.ui.Label pageLabel = new com.vaadin.ui.Label("Page:&nbsp;", com.vaadin.ui.Label.CONTENT_XHTML);
        final TextField currentPageTextField = new TextField();
        currentPageTextField.setValue(String.valueOf(getCurrentPage()));
        currentPageTextField.addValidator(new IntegerValidator(null));
        com.vaadin.ui.Label separatorLabel = new com.vaadin.ui.Label("&nbsp;/&nbsp;", com.vaadin.ui.Label.CONTENT_XHTML);
        final com.vaadin.ui.Label totalPagesLabel = new com.vaadin.ui.Label(
                String.valueOf(getTotalAmountOfPages()), com.vaadin.ui.Label.CONTENT_XHTML);
        currentPageTextField.setStyleName(Reindeer.TEXTFIELD_SMALL);
        currentPageTextField.setImmediate(true);
        currentPageTextField.addListener(new Property.ValueChangeListener() {
            private static final long serialVersionUID = -2255853716069800092L;

            public void valueChange(
                    com.vaadin.data.Property.ValueChangeEvent event) {
                if (currentPageTextField.isValid()
                        && currentPageTextField.getValue() != null) {
                    int page = Integer.valueOf(String
                            .valueOf(currentPageTextField.getValue()));
                    setCurrentPage(page);
                }
            }
        });
        pageLabel.setWidth(null);
        currentPageTextField.setWidth("20px");
        separatorLabel.setWidth(null);
        totalPagesLabel.setWidth(null);

        HorizontalLayout controlBar = new HorizontalLayout();
        HorizontalLayout pageSize = new HorizontalLayout();
        HorizontalLayout pageManagement = new HorizontalLayout();
        final com.vaadin.ui.Button first = new com.vaadin.ui.Button("<<", new com.vaadin.ui.Button.ClickListener() {
            private static final long serialVersionUID = -355520120491283992L;

            public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
                setCurrentPage(0);
            }
        });
        final com.vaadin.ui.Button previous = new com.vaadin.ui.Button("<", new com.vaadin.ui.Button.ClickListener() {
            private static final long serialVersionUID = -355520120491283992L;

            public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
                previousPage();
            }
        });
        final com.vaadin.ui.Button next = new com.vaadin.ui.Button(">", new com.vaadin.ui.Button.ClickListener() {
            private static final long serialVersionUID = -1927138212640638452L;

            public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
                nextPage();
            }
        });
        final com.vaadin.ui.Button last = new com.vaadin.ui.Button(">>", new com.vaadin.ui.Button.ClickListener() {
            private static final long serialVersionUID = -355520120491283992L;

            public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
                setCurrentPage(getTotalAmountOfPages());
            }
        });
        first.setStyleName(Reindeer.BUTTON_LINK);
        previous.setStyleName(Reindeer.BUTTON_LINK);
        next.setStyleName(Reindeer.BUTTON_LINK);
        last.setStyleName(Reindeer.BUTTON_LINK);

        itemsPerPageLabel.addStyleName("pagedtable-itemsperpagecaption");
        itemsPerPageSelect.addStyleName("pagedtable-itemsperpagecombobox");
        pageLabel.addStyleName("pagedtable-pagecaption");
        currentPageTextField.addStyleName("pagedtable-pagefield");
        separatorLabel.addStyleName("pagedtable-separator");
        totalPagesLabel.addStyleName("pagedtable-total");
        first.addStyleName("pagedtable-first");
        previous.addStyleName("pagedtable-previous");
        next.addStyleName("pagedtable-next");
        last.addStyleName("pagedtable-last");

        itemsPerPageLabel.addStyleName("pagedtable-label");
        itemsPerPageSelect.addStyleName("pagedtable-combobox");
        pageLabel.addStyleName("pagedtable-label");
        currentPageTextField.addStyleName("pagedtable-label");
        separatorLabel.addStyleName("pagedtable-label");
        totalPagesLabel.addStyleName("pagedtable-label");
        first.addStyleName("pagedtable-button");
        previous.addStyleName("pagedtable-button");
        next.addStyleName("pagedtable-button");
        last.addStyleName("pagedtable-button");

        pageSize.addComponent(itemsPerPageLabel);
        pageSize.addComponent(itemsPerPageSelect);
        pageSize.setComponentAlignment(itemsPerPageLabel, Alignment.MIDDLE_LEFT);
        pageSize.setComponentAlignment(itemsPerPageSelect,
                Alignment.MIDDLE_LEFT);
        pageSize.setSpacing(true);
        pageManagement.addComponent(first);
        pageManagement.addComponent(previous);
        pageManagement.addComponent(pageLabel);
        pageManagement.addComponent(currentPageTextField);
        pageManagement.addComponent(separatorLabel);
        pageManagement.addComponent(totalPagesLabel);
        pageManagement.addComponent(next);
        pageManagement.addComponent(last);
        pageManagement.setComponentAlignment(first, Alignment.MIDDLE_LEFT);
        pageManagement.setComponentAlignment(previous, Alignment.MIDDLE_LEFT);
        pageManagement.setComponentAlignment(pageLabel, Alignment.MIDDLE_LEFT);
        pageManagement.setComponentAlignment(currentPageTextField,
                Alignment.MIDDLE_LEFT);
        pageManagement.setComponentAlignment(separatorLabel,
                Alignment.MIDDLE_LEFT);
        pageManagement.setComponentAlignment(totalPagesLabel,
                Alignment.MIDDLE_LEFT);
        pageManagement.setComponentAlignment(next, Alignment.MIDDLE_LEFT);
        pageManagement.setComponentAlignment(last, Alignment.MIDDLE_LEFT);
        pageManagement.setWidth(null);
        pageManagement.setSpacing(true);
        controlBar.addComponent(pageSize);
        controlBar.addComponent(pageManagement);
        controlBar.setComponentAlignment(pageManagement,
                Alignment.MIDDLE_CENTER);
        controlBar.setWidth("100%");
        controlBar.setExpandRatio(pageSize, 1);
//        addListener(new PageChangeListener() {
//            public void pageChanged(PagedTableChangeEvent event) {
//                first.setEnabled(container.getStartIndex() > 0);
//                previous.setEnabled(container.getStartIndex() > 0);
//                next.setEnabled(container.getStartIndex() < container
//                        .getRealSize() - getPageLength());
//                last.setEnabled(container.getStartIndex() < container
//                        .getRealSize() - getPageLength());
//                currentPageTextField.setValue(String.valueOf(getCurrentPage()));
//                totalPagesLabel.setValue(getTotalAmountOfPages());
//                itemsPerPageSelect.setValue(String.valueOf(getPageLength()));
//            }
//        });
        return controlBar;*/

        HorizontalLayout controlBar = new HorizontalLayout();
        controlBar.setWidth(100, Sizeable.UNITS_PERCENTAGE);

        int totalNumberOfPages = arrResultingWebsites.size() / numOfResultsPerPage;

        for(int i = 1; i < totalNumberOfPages; i++){
            Link pageNumberLink = new Link();
            pageNumberLink.setCaption(String.valueOf(i));
            pageNumberLink.addListener(new Listener() {
                @Override
                public void componentEvent(Event event) {
                    logger.info("CLICKED");
                }
            });

            controlBar.addComponent(pageNumberLink);
        }


        return controlBar;

    }
}
