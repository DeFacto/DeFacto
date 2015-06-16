'use strict';

var controllers = angular.module('defacto.controllers');

controllers.controller('HomeCtrl', function($location, $scope, $http,$filter) {
      
      $scope.requested=0;
      
    // load examples and page
    $http.get('fusion/examples').success(function(data) {
		$scope.examples = data;

        $scope.searchFacts = [];
        angular.forEach(data, function(example) {
             $scope.searchFacts.push({fact:example.fact});
        });
        
        $scope.currentPage = 1;
        $scope.maxSize = 5;
        $scope.bigTotalItems = $scope.examples.length;
        $scope.bigCurrentPage = 1;
        $scope.pageSize = 10;
        $scope.filtered = [];
        
        $scope.$watch('input', function (input) {
            $scope.currentPage = 1;
            $scope.filtered = $filter('filter')( $scope.searchFacts , input);
            $scope.noOfPages = $scope.filtered.length / $scope.pageSize;
        });
    });

    $scope.setPage = function (pageNo) {
        $scope.currentPage = pageNo;
    };

    //
    $scope.getFact = function(example){
                        $scope.requested=1;
                        delete $scope.fact;
                        
                        var body = {
                                fact:example.fact,
                                s: example.s,
                                p: example.p,
                                o: example.o,
                                from:'',
                                to:''            
                            };

                        $http.post('fusion/input/',body).success(function(data) {

                            var maxScore=0,maxCoverage=0,maxSearch=0,maxWeb=0;
                            angular.forEach(data.websites, function(website) {
                                maxScore = website.score > maxScore?website.score:maxScore;
                                maxCoverage= website.coverage > maxCoverage?website.coverage:maxCoverage;
                                maxSearch = website.search > maxSearch?website.search:maxSearch;
                                maxWeb = website.web > maxWeb?website.web:maxWeb;
                            });

                      
                            $scope.fact = {
                                    score:data.score,
                                    from:data.from,
                                    to:data.to,
                                    websitesSize:data.websitesSize,
                                    websites: data.websites,
                                    
                                    maxScore : maxScore,
                                    maxCoverage: maxCoverage,
                                    maxSearch :maxSearch,
                                    maxWeb :maxWeb
                             };                                
                        });
                    };
});
