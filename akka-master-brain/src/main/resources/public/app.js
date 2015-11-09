'use strict';

// Declare app level module which depends on views, and components
angular.module('myApp', [
  'ngResource',
  'ngMaterial',
  'ngMdIcons',
  'ngRoute',
  'ngRadialGauge',
  'myApp.nav',
  'myApp.sensors',
  'myApp.track',
  'myApp.dashboard',
  'myApp.dataService',
  'myApp.powerGauge',
  'myApp.manual',
  'myApp.sensorPlot',
  'myApp.trackPlot',
  'myApp.trackbuilding',
  'myApp.info',
  'myApp.stats',
  'carrera.commons.stomp'
]).
config(['$routeProvider', '$mdIconProvider', '$mdThemingProvider', '$locationProvider', function($routeProvider, $mdIconProvider, $mdThemingProvider, $locationProvider) {
  $routeProvider.otherwise({redirectTo: '/sensors'});
  //$locationProvider.html5Mode(true); 
  $mdIconProvider
	  .defaultIconSet("./assets/svg/avatars.svg", 128)
	  .icon("menu"       , "./assets/svg/menu.svg"        , 24)
	  .icon("share"      , "./assets/svg/share.svg"       , 24)
	  .icon("google_plus", "./assets/svg/google_plus.svg" , 512)
	  .icon("hangouts"   , "./assets/svg/hangouts.svg"    , 512)
	  .icon("twitter"    , "./assets/svg/twitter.svg"     , 512)
	  .icon("phone"      , "./assets/svg/phone.svg"       , 512);

  $mdThemingProvider.theme('default')
    .primaryPalette('purple')
    .accentPalette('orange');
}]);
