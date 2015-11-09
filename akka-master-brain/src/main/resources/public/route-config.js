(function () {
    angular.module('myApp').config(config);

    config.$inject = ['$routeProvider'];

    function config($routeProvider) {
        $routeProvider.when('/sensors', {
            templateUrl: 'sensors/sensors.html'
        }).when('/dashboard', {
            templateUrl: 'dashboard/dashboard.html',
            controller: 'DashboardController'
        }).when('/manual', {
            templateUrl: 'manual/manual.html'
        }).when('/track', {
            templateUrl: 'track/track.html'
        });
    }
})();