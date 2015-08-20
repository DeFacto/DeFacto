"use strict";

angular.module('defacto.directives.chart', [])
  .directive('chart', function() {
    return {
      restrict: "A",
      link: function($scope, $elem, $attr) {
        new google.visualization[$attr.chart]($elem[0])
          .draw(
            $scope[$attr.ngModel].dataTable,
            $scope[$attr.ngModel].options || {}
          );
      }
    }
  });
