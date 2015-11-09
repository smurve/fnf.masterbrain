(function () {
    'use strict';
    angular.module('myApp.manual', [])
        .controller('ManualController', ManualController);

    ManualController.$inject = ['$scope', '$http', '$mdBottomSheet', '$interval', '$timeout', 'akkaMasterBrainStream'];


    function ManualController($scope, $http, $mdBottomSheet, $interval, $timeout, akkaMasterBrainStream) {
        var vm = this;
        var initDone = false;
        vm.value = {
            liveValue: 0,
            newValue: 0
        };
        vm.max = 255;

        vm.pilots = [];

        vm.safety = { code: 'off'};
        vm.properties = {};

        // gauge doesn't notice when the values get updated right now. deferring the loading helps.
        // Good enough till someone has time to look into this.
        $timeout(function () {
            $http.get('/api/power').success(function (data, status, headers, config) {
                vm.value.liveValue = parseFloat(data.value);
                vm.value.newValue = parseFloat(data.value);
            });
        }, 1000);

        $timeout(function () {
            $http.get('/api/pilots/current').success(function (data, status, headers, config) {
                vm.activePilot = data;
                vm.pilot = JSON.stringify(data);
            });
        }, 1000);
        $timeout(function () {
            $http.get('/api/pilots/current/properties').success(function (data, status, headers, config) {
                vm.properties = data;
            });
        }, 1000);

        $scope.$watch(function () {
            return vm.value.newValue;
        }, function (v) {
            if (initDone) {
                $http.post('/api/power/' + v);
            } else {
                initDone = true;
            }
        }, true);


        function connect() {
            akkaMasterBrainStream.then(function (client) {
                vm.client = client;
                vm.subscriptionId = client.subscribe('/topic/control', function (message) {
                    var data = JSON.parse(message.body);
                    vm.value.liveValue = parseFloat(data.value);
                });
                vm.infoSubscriptionId = client.subscribe('/topic/info', function (message) {
                    var data = JSON.parse(message.body);
                    if ('safety' === data.type) {
                        vm.safety = data;
                    } else if ('penalty' === data.type) {
                        vm.penalty = data;
                        $timeout(function() {
                            vm.penalty = {};
                        }, 5000);
                    } else if ('config' === data.code) {
                        vm.configError = data;
                    }
                });
                vm.propertiesId = client.subscribe('/topic/properties/pilot', function (message) {
                    var data = JSON.parse(message.body);
                    //console.log(data);
                    vm.properties = data;
                });
            }, function (error) {
                console.log(error);
            });
        }

        function disconnect() {
            vm.client.unsubscribe(vm.subscriptionId);
            vm.client.unsubscribe(vm.infoSubscriptionId);
            vm.client.unsubscribe(vm.propertiesId);
        }


        function init() {
            connect();
            loadPilots();
        }

        $scope.$on('$destroy', function () {
            disconnect();
        });

        function loadPilots() {
            $http.get("/api/pilots").then(function (pilots) {
                vm.pilots = pilots.data;
            });
        }

        vm.activatePilot = function () {
            var parsedPilot = JSON.parse(vm.pilot);
            $http.put('/api/pilots', { pilot: parsedPilot.name});
            vm.activePilot = parsedPilot;
            vm.properties = {};
            //vm.pilot = {};
        };

        vm.panic = function() {
            vm.pilot = JSON.stringify(vm.pilots[0]);
            vm.value.newValue = 0;
            vm.activatePilot();
            vm.configError = {};
        };


        vm.submitProperties = submitProperties;
        function submitProperties() {
            vm.configError = {};
            $http.put('/api/pilots/current/properties', vm.properties);
        }

        vm.showProperties = function() {
            if (typeof vm.properties.properties === 'undefined') {
                return false;
            }
            return Object.keys(vm.properties.properties).length;
        }
        init();

    }
})();