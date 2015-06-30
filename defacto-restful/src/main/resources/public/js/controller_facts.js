'use strict';

controllers.controller('FactsCtrl', function($location, $routeParams, $scope, $http) {
  $scope.requested = 0;
  if ($routeParams.name) {
    // call backend with given id
    $scope.requested = 1;
    $http
      .get('/fusion/vote/' + $routeParams.name + '/')
      .success(function(data) {

        $scope.facts = {
          id: $routeParams.name,
          fact: []
        };

        if (data && typeof data === 'object' && data !== null && data.length) {
          $scope.facts.fact = data;
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
    vote(fact, 'down');
  };
  $scope.upvote = function(fact) {
    vote(fact, 'up');
  };
  //
  var vote = function(fact, dir) {
    $http
      .post('/fusion/vote/', {
        id: $scope.facts.id,
        fact: fact,
        dir: dir
      })
      .success(function(data) {
        $scope.input = data;
      })
      .error(function(err) {
        $scope.status = err;
      });
  };
});
