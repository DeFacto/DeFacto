"use strict";

angular.module('defacto.factories.chart', [])
  .factory('ChartFactory', function() {
    var ChartFactory = function() {
      this.initialize = function(data) {
        var data = data;
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
            ['Defacto', Math.round(value.score/data.maxScore*100)/100, '#4daf4a'],
            ['Topic Score', Math.round(value.coverage/data.maxCoverage*100)/100, '#984ea3'],
            ['TM in SF', Math.round(value.search/data.maxSearch*100)/100, '#377eb8'],
            ['TM in WF', Math.round(value.web/data.maxWeb*100)/100, '#e41a1c']
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
