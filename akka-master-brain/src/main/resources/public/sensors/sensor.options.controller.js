(function () {
    'use strict';
    angular.module('myApp.sensors').controller('SensorsOptionsController', function ($scope, $mdBottomSheet) {
        $scope.items = [
            {name: 'Share', icon: 'share'},
            //{ name: 'Print this page', icon: 'print' },
        ];

        $scope.listItemClick = function ($index) {
            var clickedItem = $scope.items[$index];
            $mdBottomSheet.hide(clickedItem);
        };
    });
})();