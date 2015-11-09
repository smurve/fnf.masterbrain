(function () {
    'use strict';

    angular.module('myApp.track', [])
        .controller('TrackController', TrackController);

    TrackController.$inject = ['$scope', '$http', '$mdBottomSheet', '$interval', '$timeout', 'loadTrack', 'akkaMasterBrainStream'];

    function TrackController($scope, $http, $mdBottomSheet, $interval, $timeout, loadTrack, akkaMasterBrainStream) {
        var vm = this;
        vm.message = '';
        vm.locationMessage = '';
        vm.trackBuildingState = 0;
        vm.data = {};
        vm.localization = {};

        function loadInitialTrack() {
            loadTrack("data/initialTrack.json", function (error, data) {
                vm.data.segments = data;
            });
            // Useful when working without backend, hosted directly by IntelliJ
            //loadTrack("data/track.json", function(error, data) {
            //	vm.data.segments=data;
            //});
        }

        vm.reset = function () {
            $http.post('/api/reset');
        };

        function findIndexByAttribute(arraytosearch, key, valuetosearch) {
            for (var i = 0; i < arraytosearch.length; i++) {
                if (arraytosearch[i][key] === valuetosearch) {
                    return i;
                }
            }
            return null;
        }

        function connect() {
            akkaMasterBrainStream.then(function (client) {
                vm.client = client;
                vm.subscriptionId = client.subscribe('/topic/track', function (message) {
                    var data = JSON.parse(message.body);
                    if (data.status.code !== 'error') {
                        vm.data.segments = data;
                        if (data.status.code === 'warning') {
                            vm.data.trackStatus = data.status;
                        }
                        vm.data.trackStatus = {};
                    } else {
                        vm.data.trackStatus = data.status;
                    }
                });
                vm.locationId = client.subscribe('/topic/location', function (message) {
                    var data = JSON.parse(message.body);
                    if (data.locIndex) {
                        var idx = findIndexByAttribute(vm.data.allLocations, 'index', data.locIndex);
                        if (idx >= 0) {
                            var loc = vm.data.allLocations[idx];
                            if (typeof loc !== 'undefined'){
                                vm.data.currentLocation = [loc.x, loc.y];
                            }
                        }
                    } else {
                        if (typeof data.status !== 'undefined' && data.status.code === 'ok') {
                            vm.data.allLocations = data.locations;
                            // reduce number of displayed location probabilities
                            var filtered = [];
                            for (var i = 0; i < data.locations.length; i++) {
                                if (i % 6 === 1) {
                                    filtered.push(data.locations[i]);
                                }
                            }
                            vm.data.locations = filtered;
                            vm.data.locationStatus = {};
                        } else {
                            vm.data.locationStatus = data.status;
                        }
                    }
                });
                vm.infoSubscriptionId = client.subscribe('/topic/info', function (message) {
                    var data = JSON.parse(message.body);
                    if ('localization' === data.type) {
                        vm.localization[data.code] = data;
                    }
                });
                vm.powerProfileId = client.subscribe('/topic/learning/finished', function (message) {
                    var data = JSON.parse(message.body);
                    handlePowerProfile(data);
                });
                vm.resetSubscriptionId = client.subscribe('/topic/reset', function (message) {
                    resetData();
                    loadInitialTrack();
                    vm.message = 'Reset done';
                    $timeout(function () {
                        vm.message = '';
                    }, 2000);
                });
                vm.trackStateId = client.subscribe('/topic/track/state', function (message) {
                    var data = JSON.parse(message.body);
                    vm.trackBuildingState = data.n / data.of * 100;
                });
            }, function (error) {
                console.log(error);
            });
        }

        function handlePowerProfile(data) {
            var profile = [];
            if (typeof data.powerSettings === 'undefined' || typeof vm.data.segments === 'undefined')
                if (data.powerSettings.length > vm.data.segments.trackPoints.xs.length) {
                    console.error("More powerSettings than trackPoints! {} : {}", data.powerSettings.length, vm.data.segments.trackPoints.xs.length);
                }
            data.powerSettings.map(function (d, i) {
                if (i % 5 === 0 && i < vm.data.segments.trackPoints.xs.length) {
                    profile.push({
                        x: vm.data.segments.trackPoints.xs[i],
                        y: vm.data.segments.trackPoints.ys[i],
                        f: d.powerSetting
                    });
                }
            });
            vm.data.powerProfile = profile;
        }

        function disconnect() {
            vm.client.unsubscribe(vm.subscriptionId);
            vm.client.unsubscribe(vm.resetSubscriptionId);
            vm.client.unsubscribe(vm.trackStateId);
            vm.client.unsubscribe(vm.locationId);
            vm.client.unsubscribe(vm.powerProfileId);
            vm.client.unsubscribe(vm.infoSubscriptionId);
        }

        function init() {
            resetData();
            loadInitialTrack();
            connect();
            $http.get('/api/track').then(function (track) {
                vm.data.segments = track.data;
                $http.get('/api/track/powerprofile').then(function (powerProfile) {
                    handlePowerProfile({powerSettings: powerProfile.data});

                }, function (reason) {
                    // Handle powersettings not found
                })
            }, function (reason) {
                // Handle track not found
            })

        }

        $scope.$on('$destroy', function () {
            disconnect();
        });

        function resetData() {
            vm.data = {
                segments: [],
            };
            vm.localization = {};
        }

        init();

    }
})();