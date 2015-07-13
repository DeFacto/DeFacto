'use strict';

controllers.controller('HomeCtrl', function($location, $scope, $http, $filter) {

  $scope.requested = 0;

  // load examples and page
  $http.get('fusion/examples').success(function(data) {
    $scope.examples = data;

    $scope.searchFacts = [];
    angular.forEach(data, function(example) {
      $scope.searchFacts.push({
        fact: example.fact
      });
    });

    $scope.currentPage = 1;
    $scope.maxSize = 5;
    $scope.bigTotalItems = $scope.examples.length;
    $scope.bigCurrentPage = 1;
    $scope.pageSize = 10;
    $scope.filtered = [];

    $scope.$watch('input', function(input) {
      $scope.currentPage = 1;
      $scope.filtered = $filter('filter')($scope.searchFacts, input);
      $scope.noOfPages = $scope.filtered.length / $scope.pageSize;
    });
  });

  $scope.setPage = function(pageNo) {
    $scope.currentPage = pageNo;
  };

  $scope.getFact = function(example) {
    $scope.requested = 1;
    delete $scope.fact;

    $http.post('fusion/input/', {
      fact: example.fact
    }).success(function(data) {

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

      $scope.fact = data;
    }); // success function
  }; // getFact function
});
