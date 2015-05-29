angular.module('test', []).controller('home', function($scope, $http) {
	$http.get('rest/').success(function(data) {
		$scope.test = data;
	})
});