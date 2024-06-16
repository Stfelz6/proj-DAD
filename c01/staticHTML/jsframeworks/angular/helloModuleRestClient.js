angular.module('DemoModuleApp', [])
.controller('HelloCtrl', function($scope, $http) {
    $http.get('http://127.0.0.1:7000/listUsersNames').
        then(function(response) {
            $scope.myObjHttpGetResponseFromRESTAPI = response.data;
        });
});