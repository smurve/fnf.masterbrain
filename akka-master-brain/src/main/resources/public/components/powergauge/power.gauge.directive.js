(function () {
    'use strict';

    angular.module('myApp.powerGauge', [])
        .directive('powerGauge', powerGauge);

    function powerGauge() {
        return {
            link: link,
            templateUrl: 'components/powergauge/power.gauge.html',
            restrict: 'EA',
            scope: {
                max: '=?',
                data: '='
            }
        }
    }

    function link(scope) {
        scope.value = 0;
        scope.$watch('data', function (v) {
            scope.value = scope.data.liveValue;
        }, true);

        if (typeof scope.max === 'undefined') {
            scope.max = 255;
        }
        //scope.value = 1.5;
        scope.upperLimit = scope.max;
        scope.lowerLimit = 0;
        scope.unit = "";
        scope.precision = 0;
        scope.ranges = [
            {
                min: 0,
                max: scope.max / 10 * 5,
                color: '#8DCA2F'
            },
            {
                min: scope.max / 10 * 5,
                max: scope.max / 10 * 7,
                color: '#FDC702'
            },
            {
                min: scope.max / 10 * 7,
                max: scope.max / 10 * 9,
                color: '#FF7700'
            },
            {
                min: scope.max / 10 * 9,
                max: scope.max,
                color: '#C50200'
            }
        ];

    }
})();