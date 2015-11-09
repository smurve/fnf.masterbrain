'use strict';

angular.module('myApp.nav', ['ngRoute'])
.controller('SideNavController', ['$scope', '$location', '$mdSidenav', '$mdBottomSheet', '$q', function($scope, $location, $mdSidenav, $mdBottomSheet, $q) {
	var self = this;
	$scope.selectedIndex = 3;
    
    /**
     * First hide the bottomsheet IF visible, then
     * hide or Show the 'left' sideNav area
     */
    self.toggleList = function toggleUsersList() {
      var pending = $mdBottomSheet.hide() || $q.when(true);

      pending.then(function(){
        $mdSidenav('left').toggle();
      });
    }

    $scope.$watch('selectedIndex', function(current, old) {
      switch(current) {
        case 0: $location.url("/sensors"); break;
        case 1: $location.url("/track"); break;
        case 2: $location.url("/manual"); break;
        case 3: $location.url("/dashboard"); break;
      }
    });
}]);