 org_aksw_defacto_widget_charts_HighChart = function() 
 {
     var element = $(this.getElement());
         // getData
     var title = this.getState().title;
     var data = this.getState().data;
     var units = this.getState().units;
console.log(title);
    
    $(document).ready(readDataAndDraw())
    
    this.onStateChange = function() 
    {
        $(document).ready(readDataAndDraw())
    }
    
    function readDataAndDraw()
    {
        var id = document.getElementById("myJSComponent");
        // double check if we really found the right div
                if (id == null) return;
        if(id.id != "myJSComponent") return;
        
        var options = {
                chart: {
                    renderTo: 'myJSComponent',
                    defaultSeriesType: 'line',
                    marginRight: 130,
                    marginBottom: 25
                },
                title: {
                    text: title
                },
                legend: {
                    layout: 'vertical',
                    align: 'right',
                    verticalAlign: 'top',
                    x: -10,
                    y: 100,
                    borderWidth: 0
                },
                xAxis: {
                    categories: []
                },
                yAxis: {
                    title: {
                        text: units
                    }
                },
                series: []
            };
        
        // Split the lines
        var lines = data.split('\n');        
        // Iterate over the lines and add categories or series
        $.each(lines, function(lineNo, line) {
            var items = line.split(',');
            
            // header line contains categories
            if (lineNo == 0) {
                $.each(items, function(itemNo, item) {
                    if (itemNo > 0) options.xAxis.categories.push(item);
                });
            }
            
            // the rest of the lines contain data with their name in the first position
            else {
                var series = {
                    data: []
                };
                $.each(items, function(itemNo, item) {
                    if (itemNo == 0) {
                        series.name = item;
                    } else {
                        series.data.push(parseFloat(item));
                    }
                });
                
                options.series.push(series);
        
            }
            
        });
        
        // Create the chart
        var chart = new Highcharts.Chart(options);
    }
};
