'use strict';

controllers.controller('FactsCtrl', function($location, $routeParams, $scope, $http) {
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

          var options = {
            legend: 'none',
            hAxis: {
              viewWindow: {
                min: 0,
                max: 1
              }
            }
          };
          angular.forEach(datas, function(data) {
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
            }); // for each
            $scope.facts.fact.push(data);
          }); // for each

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
