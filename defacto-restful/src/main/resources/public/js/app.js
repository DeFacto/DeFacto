'use strict';
var controllers = angular.module('defacto.controllers', []);

var dependencies = [
    'ngRoute',
    'ngSanitize',
    'ui.bootstrap',
    'defacto.controllers'
]; 

var Defacto = angular.module('Defacto', dependencies);

Defacto.constant('pages', [
    { routeName: 'home', controllerName: 'HomeCtrl', description: '' }
]);


Defacto.config(['$routeProvider', 'pages', function ($routeProvider, pages) {
    angular.forEach(pages, function (config) {
        $routeProvider.when('/' + config.routeName, {
            templateUrl: 'templates/' + config.routeName + '.html',
            controller: config.controllerName
        });
        if (!config.label) {
            config.label = config.routeName.substr(0, 1).toUpperCase() + config.routeName.substr(1);
        }
    });

    $routeProvider.when('/404', {
        templateUrl: 'templates/404.html'
    });

    $routeProvider.when('/', {
        redirectTo: '/home'
    });
    
    $routeProvider.when('/facts/:id', {
        templateUrl: 'templates/facts.html',
        controller: 'FactsCtrl'
    });
    
    $routeProvider.otherwise({
       redirectTo: '/404'
    });
}]);


Defacto.config(['$httpProvider', function ($httpProvider) {

}]);

Defacto.run(function (pages, $rootScope, $location) {
    $rootScope.pages = pages;
    
    $rootScope.replaceKeywords = function(data){
        angular.forEach(data.keywords, function(value) {
            data.tinyContext = data.tinyContext.replace(new RegExp(value, 'g'), '<b>'+value+'</b>');
        });
        return data.tinyContext;
    };
});

// filter
Defacto.filter('startFrom', function () {
    return function (inp, start) {
    if(inp){
        start = +start;
        return inp.slice(start);
    }else{
        return inp
        }
    };
});
