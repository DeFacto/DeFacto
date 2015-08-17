"use strict";

angular.module('defacto.factories.chart', [])
.factory('ChartFactory', function(data) {

});


"use strict";

angular.module('defacto.factories.chart', [])
.factory('ChartFactory',function() {
  var ChartFactory = function() {
    this.initialize = function(data) {
      var options = {
        legend: 'none',
        hAxis: {
          viewWindow: {
            min: 0,
            max: 1
          }
        }
      };
      angular.forEach(data.websites, function(value, key) {
        var dataTable = new google.visualization.arrayToDataTable([
          ['Name', 'Score', {
            role: 'style'
          }],
          ['Defacto', value.score / data.maxScore, '#4daf4a'],
          ['Topic Score', value.coverage / data.maxCoverage, '#984ea3'],
          ['TM in SF', value.search / data.maxSearch, '#377eb8'],
          ['TM in WF', value.web / data.maxWeb, '#e41a1c']
        ]);
        data.websites[key].chartdata = {
          options: options,
          dataTable: dataTable
        };
      });
      return data;
    };
  };
  return (ChartFactory);
});
