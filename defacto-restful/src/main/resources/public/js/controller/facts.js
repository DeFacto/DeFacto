'use strict';

angular.module('defacto.controllers.facts', [])
  .controller('FactsCtrl', function($location, $routeParams, $scope, $http, ChartFactory) {
    $scope.requested = 0;

    if ($routeParams.name) {
      // call backend with given id
      $scope.requested = 1;
      $http
        .get('/fusion/id/' + $routeParams.name + '/input')
        .success(function(datas) {
              if (datas && typeof datas === 'object' && datas !== null && datas.length) {
                $scope.facts = {
                  id: $routeParams.name,
                  fact: []
                };
                angular.forEach(datas, function(data) {
                  $scope.facts.fact.push(new ChartFactory().initialize(data));
                });
              } else {
                $scope.status = 'No valid data!';
              }
        }).error(function(err) {
          $scope.status = err;
        });
    }

    $scope.sendExampleInput = function() {
      $http
        .get('/examples/triples')
        .success(function(postdata) {
          $scope.exampleinput = postdata;
          $http
            .post('/fusion/insert/', postdata)
            .success(function(data) {
              $scope.input = data;
            })
            .error(function(err) {
              $scope.status = err;
            });
        }).error(function(err) {
          $scope.status = err;
        });
    };

    // voting
    $scope.upvote = function(fact) {
      vote(fact, 1);
    };
    $scope.downvote = function(fact) {
      vote(fact, -1);
    };

    var vote = function(fact, score) {
      $http
        .post('/fusion/vote/', {
          factId: $scope.facts.id,
          id: fact.id,
          score: score
        })
        .success(function(data) {
          $scope.input = data;
        })
        .error(function(err) {
          $scope.status = err;
        });
    };
  });
