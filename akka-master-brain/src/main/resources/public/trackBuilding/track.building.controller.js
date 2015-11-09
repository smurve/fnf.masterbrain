(function () {
    'use strict';

    angular.module('myApp.trackbuilding', ['ngRoute'])
        .controller('TrackBuildingController', TrackBuildingController);

    TrackBuildingController.$inject = ['$http'];

    function TrackBuildingController($http) {
        var vm = this;
        vm.tracks = [];
        $http.get('/api/tracks').success(function (data, status, headers, config) {
            vm.tracks = data;
        });

        vm.loadTrack = function () {
            $http.post('/api/tracks/' + vm.selected);
        };
    }
})();
