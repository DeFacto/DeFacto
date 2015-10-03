'use strict';
angular.module('defacto.controllers.home', [])
  .controller('HomeCtrl', function($location, $scope, $http, $filter, ChartFactory) {

    $scope.requested = 0;

    // load examples and page
    $http.get('examples/all').success(function(data) {
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
        $scope.bigTotalItems = $scope.filtered.length;
      });
    }).error(function(data, status, headers, config) {
      $scope.error = {
        data: data,
        status: status,
        headers: headers,
        config: config
      };
    });

    $scope.setPage = function(pageNo) {
      $scope.currentPage = pageNo;
    };

    $scope.getFact = function(example) {
      if (example) {
        $scope.requested = 1;
        delete $scope.error;
        delete $scope.fact;
        $http.post('fusion/input', example).success(function(data) {
          $scope.fact = new ChartFactory().initialize(data);
          $scope.requested = 0;
        }).error(function(data, status, headers, config) {
          $scope.error = {
            data: data,
            status: status,
            headers: headers,
            config: config
          };
          $scope.requested = 0;
        });
      }
    };
  });
