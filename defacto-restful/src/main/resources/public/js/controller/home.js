'use strict';
angular.module('defacto.controllers.home', [])
.controller('HomeCtrl',  function($location, $scope, $http, $filter, ChartFactory) {

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
      $scope.fact = new ChartFactory().initialize(data);
    });
  };
});
