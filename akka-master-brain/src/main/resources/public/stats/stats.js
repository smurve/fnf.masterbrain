(function () {
    'use strict';
    angular.module('myApp.stats', [])
        .controller('StatsController', StatsController);

    StatsController.$inject = ['$scope', '$http', 'akkaMasterBrainStream'];

    function StatsController($scope, $http, akkaMasterBrainStream) {
        var vm = this;
        vm.maxLaps = 10;
        vm.lapTimes = [];
        vm.bestLap = {};
        vm.reset = reset;

        vm.getClass = getClass;

        function getClass(lap) {
            if (vm.bestLap.duration && lap.duration === vm.bestLap.duration) {
                return 'bestLap';
            }
            return 'normalLap';
        }

        function connect() {
            akkaMasterBrainStream.then(function (client) {
                vm.client = client;
                vm.subscriptionId = client.subscribe('/topic/laptime', function (message) {
                    var data = JSON.parse(message.body);
                    onNewLap(data);
                });
            }, function (error) {
                console.log(error);
            });
        }

        function onNewLap(data) {
            vm.lapTimes.push(data);
            if (vm.lapTimes.length > vm.maxLaps) {
                vm.lapTimes.shift();
            }
            if (!vm.bestLap.duration || data.duration < vm.bestLap.duration) {
                vm.bestLap = data;
            }
            calculateDiffToBest();
        }

        function calculateDiffToBest() {
            vm.lapTimes.forEach(function (lapTime) {
                lapTime.differenceToBest = lapTime.duration - vm.bestLap.duration;
            });
        }

        function disconnect() {
            vm.client.unsubscribe(vm.subscriptionId);
        }

        function loadLapTimes() {
            $http.get("/api/laptimes").then(function (lapTimes) {
                lapTimes.data.forEach(function (lap) {
                    onNewLap(lap)
                });
            });
        }

        function init() {
            connect();
            loadLapTimes();
        }

        $scope.$on('$destroy', function () {
            disconnect();
        });

        function reset() {
            vm.lapTimes = [];
            vm.bestLap = {};
        }

        init();
    }
})();