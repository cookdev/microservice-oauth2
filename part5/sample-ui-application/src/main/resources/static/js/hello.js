/**
 * Created by Hahn on 2015-11-30.
 */
angular
    .module('hello', [ 'ngRoute' ])
    .config(function($routeProvider, $httpProvider) {

        $routeProvider.when('/', {
            templateUrl : 'home.html',
            controller : 'home'
        }).when('/login', {
            templateUrl : 'login.html',
            controller : 'navigation'
        }).otherwise('/');

        $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';

    })
    .controller('home', function($scope, $http) {

        // A more elegant solution might be to grab the token as needed,
        // and use an Angular interceptor to add the header to every request to the resource server.
        // The interceptor definition could then be abstracted
        // instead of doing it all in one place and cluttering up the business logic.
        $http.get('token').success(function(token) {
            $http({
                url : 'resource/',
                method : 'GET',
                headers : {
                    'X-Auth-Token' : token.token
                }
            }).success(function(data) {
                $scope.greeting = data;
            });
        })
    })
    .controller('navigation', function($rootScope, $scope, $http, $location, $route) {

        $http.get('user').success(function(data) {
            if (data.name) {
                $rootScope.authenticated = true;
            } else {
                $rootScope.authenticated = false;
            }
        }).error(function() {
            $rootScope.authenticated = false;
        });

        $scope.credentials = {};

        $scope.logout = function() {
            $http.post('logout', {}).success(function() {
                $rootScope.authenticated = false;
                $location.path("/");
            }).error(function(data) {
                $rootScope.authenticated = false;
            });
        }
    });