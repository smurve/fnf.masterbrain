(function () {
    'use strict';

    angular.module('myApp.sensors').directive('sensorIcon', function () {
        var link = function (scope, element, attr) {
            scope.iconName = function (icon) {
                switch (icon) {
                    case 'rotation':
                        return 'autorenew';
                    case 'y':
                        return 'import_export';
                    default:
                        return icon;
                }
            };
        };

        return {
            link: link,
            replace: true,
            template: '<div><ng-md-icon icon="{{iconName(icon)}}" style="fill: orange" size="64"></ng-md-icon></div>',
            restrict: 'E',
            scope: {
                icon: '='
            }
        };
    });
})();