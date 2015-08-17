'use strict';

angular.module('defacto.controllers.facts', [])
.controller('FactsCtrl', function($location, $routeParams, $scope, $http, ChartFactory) {
  $scope.requested = 0;

  if ($routeParams.name) {
    // call backend with given id
    $scope.requested = 1;
    $http
      .get('/fusion/vote/' + $routeParams.name + '/')
      .success(function(datas) {

        $scope.facts = {
          id: $routeParams.name,
          fact: []
        };
        if (datas && typeof datas === 'object' && datas !== null && datas.length) {
          angular.forEach(datas, function(data) {
            $scope.facts.fact.push(new ChartFactory().initialize(data));
          });
        } else {
          $scope.status = 'No valid data!';
        }
      })
      .error(function(err) {
        $scope.status = err;
      });
  }

  $scope.sendExampleInput = function() {

    var postdata = [{
      s: 'http://dbpedia.org/resource/Albert_Einstein',
      p: 'http://dbpedia.org/ontology/award',
      o: 'http://dbpedia.org/resource/Nobel_Prize_in_Physics'
    }, {
      s: 'http://dbpedia.org/resource/Albert_Einstein',
      p: 'http://dbpedia.org/ontology/award',
      o: 'http://dbpedia.org/resource/AFL_Rising_Star'
    }, {
      s: 'http://dbpedia.org/resource/Albert_Einstein',
      p: 'http://dbpedia.org/ontology/award',
      o: 'http://dbpedia.org/resource/Academy_Award_for_Best_Original_Song'
    }, {
      s: 'http://dbpedia.org/resource/Albert_Einstein',
      p: 'http://dbpedia.org/ontology/award',
      o: 'http://dbpedia.org/resource/World_Food_Prize'
    }];
    /*
    var postdata = [{
      s: 'http://dbpedia.org/resource/Albert_Einstein',
      p: 'http://dbpedia.org/ontology/award',
      o: 'http://dbpedia.org/resource/Nobel_Prize_in_Physics'
    }];
    */
    $scope.exampleinput = postdata;

    $http
      .post('/fusion/inputs/', postdata)
      .success(function(data) {
        $scope.input = data;
      })
      .error(function(err) {
        $scope.status = err;
      });
  };

  $scope.downvote = function(fact) {
    vote(fact, 1);
  };
  $scope.upvote = function(fact) {
    vote(fact, -1);
  };
  //
  var vote = function(fact, score) {
    $http
      .post('/fusion/vote/', {
        id: $scope.facts.id,
        fact: fact,
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
