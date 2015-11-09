(function () {
    'use strict';

    angular.module('myApp.sensors', [])
        .controller('SensorsController', SensorController);

    SensorController.$inject = ['$scope', '$http', '$mdBottomSheet', '$interval', '$timeout', 'akkaMasterBrainStream'];

    function SensorController($scope, $http, $mdBottomSheet, $interval, $timeout, akkaMasterBrainStream) {
        var vm = this;
        vm.data = {
            lineData: [],
            lapData: []
        };
        vm.bufferedData = {
            lineData: [],
            lapData: []
        };

        vm.powerConfig = {
            yAxis: {orientation: 'left', ticks: 5, dynamic: false},
            yExtractor: function (d) {
                return d.force
            },
            y: d3.scale.linear().domain([0, 300])
        };

        vm.connect = function () {
            var buffer = [];
            akkaMasterBrainStream.then(function (client) {
                vm.client = client;
                vm.subscriptionId = client.subscribe('/topic/sensorEvents', function (message) {
                    var data = JSON.parse(message.body);
                    //console.log(data.t);
                    //console.log(data);
                    vm.data.lineData.push(data);
                    if (vm.data.lineData.length > 200) {
                        vm.data.lineData.shift();
                    }
                    buffer.push(data);
                    if (buffer.length > 100) {
                        vm.bufferedData.lineData = vm.bufferedData.lineData.concat(buffer);
                        buffer = [];
                    }
                });
            }, function (error) {
                console.log(error);
            });
        };

        vm.disconnect = function () {
            vm.client.unsubscribe(vm.subscriptionId);
        };

        vm.init = function () {
            vm.connect();
        };

        $scope.$on('$destroy', function () {
            vm.disconnect();
        });

        vm.init();

        $scope.showListBottomSheet = function ($event) {
            $scope.alert = '';
            $mdBottomSheet.show({
                template: '<md-bottom-sheet class="md-list md-has-header"> <md-list> <md-item ng-repeat="item in items"><ng-md-icon icon="{{item.icon}}" style="fill: orange" size="18"></ng-md-icon><md-item-content md-ink-ripple flex class="inset"> <a flex aria-label="{{item.name}}" ng-click="listItemClick($index)"> <span class="md-inline-list-icon-label">{{ item.name }}</span> </a></md-item-content> </md-item> </md-list></md-bottom-sheet>',
                controller: 'SensorsOptionsController',
                targetEvent: $event
            }).then(function (clickedItem) {
                console.log(clickedItem.name + ' clicked!');
            });


        };
    }
})();