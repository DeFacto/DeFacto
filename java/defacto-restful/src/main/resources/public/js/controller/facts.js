'use strict';

angular.module('defacto.controllers.facts', [])
  .controller('FactsCtrl', function($location, $routeParams, $scope, $http, ChartFactory) {
    $scope.requested = 0;

    if ($routeParams.name) {
      $scope.requested = 1;
      $http
        .get('/fusion/id/' + $routeParams.name)
        .success(function(triple) {
          console.log(triple);
          if (triple) {
            $scope.triple = triple;
            $scope.triple.name = $routeParams.name;
            angular.forEach(triple.data, function(data) {
              $scope.triple.data.result = [];
              $scope.triple.data.result.push(new ChartFactory().initialize(data.result));
            });
          } else {
            $scope.status = 'No valid data!';
          }
        }).error(function(err) {
          $scope.status = err;
        });
    }


    // voting
    $scope.upvote = function(data) {
      vote(data, 1);
    };
    $scope.downvote = function(data) {
      vote(data, -1);
    };

    var vote = function(data, dir) {
      $http
        .post('/fusion/vote/', {
          id: data._id.$oid,
          votes: dir
        })
        .success(function(d) {
          if (dir > 0) {
            data.upvotes++;
          } else {
            data.downvotes++;
          }
        })
        .error(function(err) {
          $scope.status = err;
        });
    };
  });
