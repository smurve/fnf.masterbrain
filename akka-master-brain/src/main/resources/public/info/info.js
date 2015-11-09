(function () {
    'use strict';
    angular.module('myApp.info', [])
        .controller('InfosController', StatsController);

    StatsController.$inject = ['$scope', '$http', 'akkaMasterBrainStream'];

    function StatsController($scope, $http, akkaMasterBrainStream) {
        var vm = this;
        vm.infos = [];
        vm.maxInfos = 10;
        vm.reset = reset;
        vm.types = ['MPA', 'THO', 'TOT', 'localization', 'safety'];
        vm.selected = ['MPA', 'THO', 'TOT'];

        vm.toggle = function (item, list) {
            var idx = list.indexOf(item);
            if (idx > -1) list.splice(idx, 1);
            else list.push(item);
        };

        vm.exists = function (item, list) {
            return list.indexOf(item) > -1;
        };

        function connect() {
            akkaMasterBrainStream.then(function (client) {
                vm.client = client;
                vm.subscriptionId = client.subscribe('/topic/info', function (message) {
                    var data = JSON.parse(message.body);
                    //console.log(data);
                    if (vm.selected.indexOf(data.type) < 0) {
                        return;
                    }
                    vm.infos.push(data);
                    if(vm.infos.length > vm.maxInfos) {
                        vm.infos.shift();
                    }
                });
            }, function (error) {
                console.log(error);
            });
        }



        function disconnect() {
            vm.client.unsubscribe(vm.subscriptionId);
        }

        function init() {
            connect();
        }

        $scope.$on('$destroy', function () {
            disconnect();
        });

        function reset() {
            vm.infos = [];
        }

        init();
    }
})();