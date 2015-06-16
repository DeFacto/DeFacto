'use strict';

var controllers = angular.module('defacto.controllers');
controllers.controller('FactsCtrl', function($routeParams, $scope, $http) {
    $scope.requested=0;
    var id = $routeParams.id 
    $scope.facts = { id:id };


    $http.get('fusion/exampleinput').success(function(data) {
            $scope.requested=1;
            delete $scope.fact;
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
            
	})

    /*
    $http.get('fusion/store/' + id).success(function(data) {
		$scope.data = data;
	})
    */
});
