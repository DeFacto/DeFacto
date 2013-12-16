 org_aksw_defacto_widget_charts_DateTimeHighChart = function() 
 {
     var element = $(this.getElement());
         // getData
     var title = this.getState().title;
     var startYear = this.getState().startYear;
     var endYear = this.getState().endYear;
    console.log(title);
 console.log(element);
console.log(Date.UTC(startYear,0,1));
console.log(Date.UTC(startYear-5,0,1));

    $(document).ready(readDataAndDraw())
    
    this.onStateChange = function() 
    {
        $(document).ready(readDataAndDraw())
    }
    
    function readDataAndDraw()
    {
        var id = document.getElementById("datetimechart");
        // double check if we really found the right div
                if (id == null) return;
        if(id.id != "datetimechart") return;
        
        var options = {
            chart: {
                zoomType: 'x',
                spacingRight: 20
            },
		credits: {
			enabled:false
		},
            title: {
                text: title
            },
            xAxis: {
                type: 'datetime',
                maxZoom: 14 * 24 * 3600000, // fourteen days
                title: {
                    text: null
                },
                min:Date.UTC(startYear-5,0,1),
                max:Date.UTC(endYear+5,0,1)
            },
            yAxis: {
                title: {
                    text: ' '
                },
                min:0,
                labels: {
                    enabled:false
                },
                tickInterval:2
                
            },
            tooltip: {
                enabled:false
            },
            legend: {
                enabled: false
            },
            plotOptions: {
                area: {
                    fillColor: {
                        linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1},
                        stops: [
                            [0, Highcharts.getOptions().colors[0]],
                            [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                        ]
                    },
                    lineWidth: 1,
                    marker: {
                        enabled: false
                    },
                    shadow: false,
                    states: {
                        hover: {
                            lineWidth: 1
                        }
                    },
                    threshold: null
                }
            },
    
            series: [{
                type: 'area',
                name: '',
                pointInterval: 24,
                pointStart: Date.UTC(2005,0,1),
                data: [  
                    [Date.UTC(startYear,0,1),1.0],
                    [Date.UTC(endYear,0,1),1.0]
                ]
            }]
   
            };
	
        
        // Create the chart
	$(id).highcharts(options);
        
    }
};
