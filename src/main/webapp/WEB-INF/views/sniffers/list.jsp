<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://catlogging.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<spring:message code="catlogging.breadcrumb.sniffers" var="title"/>
<tpl:bodySidebar title="${title}" activeNavbar="sniffers" ngModules="'SnifferListModule'">
	<jsp:attribute name="sidebar"><jsp:include page="sidebar.jsp" /></jsp:attribute>
	<jsp:body>
		<ul class="breadcrumb">
			<li class="active"><spring:message code="catlogging.breadcrumb.sniffers"/></li>
			<li class="pull-right dropdown"><a href="<c:url value="/c/sniffers/new" />" class="btn btn-primary btn-xs" role="menuitem"><i class="glyphicon glyphicon-plus"></i> <spring:message code="catlogging.common.new"/> <spring:message code="catlogging.common.sniffer"/></a></li>
			<!-- 
				<li class="pull-right dropdown">
					<button data-toggle="dropdown" href="#" class="btn btn-xs btn-primary"><i class="glyphicon glyphicon-cog"></i> <span class="caret"></span></button>
					<ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
						<li role="presentation"><a href="<c:url value="/c/sniffers/new" />" role="menuitem"><i class="glyphicon glyphicon-plus"></i> Create New Sniffer</a></li>
					</ul>
				</li>
			 -->
		</ul>
	
		<t:messages />

		<script type="text/javascript">
		
		var SnifferListModule = angular.module('SnifferListModule', ['ui.bootstrap', 'angularSpinner', 'MessageCenterModule']);
		SnifferListModule.controller(
			"SnifferListController", ['$scope', '$http', '$location', '$anchorScroll', 'usSpinnerService', 'lsfAlerts',
			function($scope, $http, $location, $anchorScroll, usSpinnerService, lsfAlerts) {
				$scope.alerts = lsfAlerts.create();
				$scope.result = ${logfn:jsonify(result)};
				$scope.alerts.buildFromMessages($scope.result.messages);
				$scope.nls = {
						on:'<spring:message code="catlogging.sniffers.scheduled.true" />',
						off:'<spring:message code="catlogging.sniffers.scheduled.false" />'
				};
			}
			]
		);
		SnifferListModule.controller(
				"SnifferController", ['$scope', '$http', '$log', 'messageCenterService',
				function($scope, $http, $log, messageCenterService) {
					$scope.snifferPath = $scope.contextPath + "/c/sniffers/" + $scope.sniffer.id;
					$scope.start = function() {
						$log.info("Starting sniffer", $scope.sniffer);
						$http({
						    url: $scope.snifferPath + "/start",
						    method: "POST"
						}).success(function(data, status, headers, config) {
							$log.info("Started sniffer", $scope.sniffer);
							$scope.sniffer.aspects.scheduleInfo.scheduled=true;
						}).error(function() {
							messageCenterService.add('danger', 'Failed to start');
						});
					};
					$scope.stop = function() {
						$log.info("Stopping sniffer", $scope.sniffer);
						$http({
						    url: $scope.snifferPath + "/stop",
						    method: "POST"
						}).success(function(data, status, headers, config) {
							$log.info("Stopped sniffer", $scope.sniffer);
							$scope.sniffer.aspects.scheduleInfo.scheduled = false;
						}).error(function() {
							messageCenterService.add('danger', 'Failed to stop');
						});
					};
				}
				]
			);
		 
		
		</script>
		<c:choose>
			<c:when test="${empty sniffers}">
				<div class="alert alert-info">
					<i class="glyphicon glyphicon-exclamation-sign"></i> <spring:message code="catlogging.common.list.text.1"/>
				</div>
			</c:when>
			<c:otherwise>
				<div ng-controller="SnifferListController">
					<div lsf-alerts alerts="alerts"></div>
					<div mc-messages></div> 
					<ul class="bordered-list sniffers">
						<li ng-repeat="sniffer in result.items">
							<div ng-controller="SnifferController">
								<h4>
									<a href="{{contextPath}}/c/sniffers/{{sniffer.id}}/events">{{sniffer.name}}</a>
								</h4>
								<div class="row">
									<div class="col-md-12">
										<span class="label label-success" ng-if="sniffer.aspects.scheduleInfo.scheduled">{{nls.on}}</span>
										<span class="label label-default" ng-if="!sniffer.aspects.scheduleInfo.scheduled">{{nls.off}}</span>
										|
										Last run: 
											<span ng-if="sniffer.aspects.scheduleInfo.lastFireTime">{{sniffer.aspects.scheduleInfo.lastFireTime | date:'medium'}}</span>
											<span ng-if="!sniffer.aspects.scheduleInfo.lastFireTime"> - never -</span>
										|
										<i class="glyphicon glyphicon-bullhorn"></i> Events: 
											<a href="{{contextPath}}/c/sniffers/{{sniffer.id}}/events">
												<span class="badge" ng-class="{'badge-warning': sniffer.aspects.eventsCount!=0}">{{sniffer.aspects.eventsCount}}</span>
											</a>
										<div class="pull-right">
											<div class="btn-group btn-group-sm" role="group" aria-label="Actions">
												<a href="#" class="btn btn-sm btn-default" ng-if="!sniffer.aspects.scheduleInfo.scheduled"
													ng-click="start()"><i class="glyphicon glyphicon-play"></i><spring:message code="catlogging.common.form.start"/></a>
												<a href="#" class="btn btn-sm btn-default" ng-if="sniffer.aspects.scheduleInfo.scheduled"
													ng-click="stop()"><i class="glyphicon glyphicon-pause"></i><spring:message code="catlogging.common.form.pause"/></a>
												<a href="{{contextPath}}/c/sniffers/{{sniffer.id}}" class="btn btn-sm btn-default"><i class="glyphicon glyphicon-edit"></i> <spring:message code="catlogging.common.form.edit"/></a>
												<a href="{{contextPath}}/c/sniffers/{{sniffer.id}}/status" class="btn btn-sm btn-default"><i class="glyphicon glyphicon-play-circle"></i> <spring:message code="catlogging.common.form.control"/></a>
											</div>
										</div>
									</div>
								</div>
							</div>
						</li>
					</ul>
				</div>
			</c:otherwise>
		</c:choose>
	</jsp:body>
</tpl:bodySidebar>