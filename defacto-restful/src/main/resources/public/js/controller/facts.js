'use strict';

angular.module('defacto.controllers.facts', [])
  .controller('FactsCtrl', function($location, $routeParams, $scope, $http, ChartFactory) {
    $scope.requested = 0;

    function fetchdata() {
      if ($routeParams.name) {
        // call backend with given id
        $scope.requested = 1;
        $http
          .get('/fusion/id/' + $routeParams.name)
          .success(function(datas) {
            console.log(datas);
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
    }
    fetchdata();

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
          votes: score,
          s: fact.s,
          p: fact.p,
          o: fact.o
        })
        .success(function(data) {
            fetchdata();
        })
        .error(function(err) {
          $scope.status = err;
        });
    };
  });
