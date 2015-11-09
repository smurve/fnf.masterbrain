(function () {
    'use strict';

    angular.module('myApp.dashboard', [])
        .controller('DashboardController', DashboardController);

    DashboardController.$inject = ['$scope', '$http', '$mdBottomSheet', '$interval', '$timeout'];

    function DashboardController($scope, $http, $mdBottomSheet, $interval, $timeout) {
    }
})();
