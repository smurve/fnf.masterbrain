(function(){
	var myModule = angular.module('myApp.dataService', [ 'carrera.commons.stomp' ]);

	myModule.factory('loadTrack', function() {
		return function(url, callback) {
			d3.json(url, callback);
		};

	}).factory('akkaMasterBrainStream', [ 'ngstomp', '$timeout', '$http', '$window', '$mdDialog', function(ngstomp, $timeout, $http, $window, $mdDialog) {
		function ping() {
			console.log('ping')
			$http.get('/api/ping').success(function (data, status, headers, config) {
				console.log(data);
				$window.location.reload();
			}).error(function(data, status, headers, config) {
				console.log('server not reachable');
				$timeout(ping, 5000);
			});
		}
		return new Promise(function(resolve, reject) {
			var REST_API_URL = '';// localhost:9000';
			globalStompService = ngstomp(REST_API_URL + '/messages');
			globalStompService.connect({}, function(stompClient, frame) {
				console.log("connected");
				resolve(globalStompService);
			}, function(stompClient, frame) {
				console.log("Connection lost");
				$timeout(ping, 3000);
				$mdDialog.show(
					$mdDialog.alert()
						.parent(angular.element(document.body))
						.title('Connection lost')
						.content('Connection to backend server lost. I\'ll try to reconnect...')
						.ariaLabel('Connection lost')
						.ok('Got it!'));

			});
		});
	} ]);
})();