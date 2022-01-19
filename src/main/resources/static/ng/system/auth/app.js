/*******************************************************************************
 * catlogging, open source tool for viewing, monitoring and analysing log data.
 * Copyright (c) 2021 xzpluszone, www.catlogging.com
 * Copyright (c) 2015 Scaleborn UG, www.scaleborn.com
 *
 * catlogging is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * catlogging is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
angular
.module('SystemMemberModule',["ngSanitize"])
.controller(
	"SystemMemberController",
	[
	 '$scope',
	 '$http',
	 '$log',
	 'lsfAlerts',
	 '$sce',
	 function($scope, $http, $log, lsfAlerts, $sce) {
		 $scope.alerts = lsfAlerts.create();
		 $scope.bindErrors = {};
		 $scope.state =  {
			 busy: false
		 };
		 $scope.members = null;
		 $scope.settingsRessource = $scope.contextPath + "/c/member";

		 var initResponse = function (data) {
			 $log.info("Init : ", data)
			 $scope.members = data;
		 };

		 // Init
		 $scope.loadSettings($scope).success(initResponse);

		 $scope.addMember = function() {
			 if ($scope.members == null) {
				 $scope.members = [];
			 }

			 // 깡통 row하나 만들기 위해
			 if ($scope.members.length > 0) {
				 $scope.members.push({});
			 }
		 };

		 $scope.deleteMember = function($index) {

             $scope.state.busy = true;
             $scope.alerts.clear();

             $http({
				 url : $scope.settingsRessource+"/del",
				 method : "POST",
				 data: $scope.members[$index]
			 })
				 .success(
					 function(data, status, headers, config) {
						 $scope.state.busy = false;
                         if(data.type == 'success') {
                             $scope.alerts.success(data.message);
                         }else{
                             $scope.alerts.warn(data.message);
                         }
					 }
				 )
				 .error(
					 function(data, status, headers, config, statusText) {
						 $scope.state.busy = false;
						 $scope.alerts.httpError("Failed to Delete Member", data, status, headers, config, statusText);
					 }
				 );

			 $scope.members.splice($index, 1);
		 }

		 $scope.saveMember = function($index) {
             $scope.state.busy = true;
             $scope.alerts.clear();

             //"", null, undefined, 0, NaN, false
		     if(!$scope.members[$index].memberId || !$scope.members[$index].password
                 || $scope.members[$index].isAdmin == null){

                 $scope.alerts.error("need Member input data..");
                 $scope.state.busy = false;
                 return;
             }

             $http({
				 url : $scope.settingsRessource+"/new",
				 method : "POST",
				 data: $scope.members[$index]
			 })
				 .success(
					 function(data, status, headers, config) {
						 $scope.state.busy = false;
                         if(data.type == 'success') {
                             $scope.alerts.success(data.message);
                         }else{
                             $scope.alerts.warn(data.message);
                         }
					 }
				 )
				 .error(
					 function(data, status, headers, config, statusText) {
						 $scope.state.busy = false;
						 $scope.alerts.httpError("Failed to Add Member", data, status, headers, config, statusText);
					 }
				 );
		 };

	 }]
);