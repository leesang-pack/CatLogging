<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://catlogging.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>


<spring:message code="catlogging.common.form.edit" var="paramEdit"/>
<tpl:bodyFull title="${activeSniffer.name } - ${paramEdit}" activeNavbar="sniffers" ngModules="'SnifferEditorModule'">
	
	<jsp:attribute name="htmlHead">
		<script type="text/javascript" src="<c:url value="/ng/sniffer/snifferTest.js?v=${catloggingProps['catlogging.version']}" />"></script>
	</jsp:attribute>
	<jsp:body>
		<script type="text/javascript">
			var SnifferEditorModule = angular.module('SnifferEditorModule', ['ui.bootstrap', 'angularSpinner', 'lsfSnifferTestModule']);
			SnifferEditorModule.controller(
				"SnifferEditController", ['$scope', '$http', '$location', '$anchorScroll', 'usSpinnerService', 'messageCenterService',
				function($scope, $http, $location, $anchorScroll, usSpinnerService, messageCenterService) {
					$scope.catlogging = catlogging;
					$scope.bindErrors={<spring:hasBindErrors name="snifferForm"><c:forEach items="${errors.allErrors}" var="error">'${error.field }':'<spring:message code="${error.code}" text="${error.defaultMessage}" javaScriptEscape="true" />',</c:forEach></spring:hasBindErrors>};
					$scope.bean = ${logfn:jsonify(activeSniffer)};
					$scope.beanWrapper = [$scope.bean];
					$scope.scheduleInfo = {
						scheduled: ${scheduled}
					};

					$scope.submit = function(form) {
						$(".backdrop-overlay").show();
						usSpinnerService.spin('update');
						form.$setPristine();
						var data=$scope.beanWrapper[0];
						var always = function() {
							$(".backdrop-overlay").hide();
							$location.hash('top');
						    $anchorScroll();
						    usSpinnerService.stop('update');									
						};
						$(".alert-success").hide();
						$http({
						    url: "<c:url value="/c/sniffers/${activeSniffer.id}" />",
						    method: "PUT",
						    data: data
						}).success(function(data, status, headers, config) {
							$("form#refresh").submit();
						}).error(function(data, status, headers, config) {
							always();
						    if (data && data.bindErrors) {
						    	$scope.bindErrors = data.bindErrors;
						    } else {
							 	messageCenterService.add('danger', 'Failed to save changes: ' + status);
						    }
						});
					};	
				}
			]);
		</script>
		<div ng-controller="SnifferEditController" ng-cloak >
			<jsp:include page="sniffer.breadcrumb.jsp">
				<jsp:param value="catlogging.common.form.edit" name="context"/>
			</jsp:include>
	
			<c:if test="${param.created}">
				<div class="alert alert-success">
					<button type="button" class="close" data-dismiss="alert">&times;</button>
					<h4>Successfully created</h4>
					Complete the settings if not yet happened and then start monitoring.
				</div>
			</c:if>

			<form id="refresh" method="post"></form>
			<form name="form" method="post" action="/c/sniffers/${activeSniffer.id}" id="snifferForm"
				role="form" novalidate="novalidate">
	
				<jsp:include page="sniffer.form.jsp" />
				
				<hr>
				<div class="row">
					<div class="col-md-12" us-spinner spinner-key="update">
						<button type="button" class="btn btn-primary btn-sm" ng-disabled="${scheduled} || form.$invalid" ng-click="submit(form)">Save</button>
					</div>
				</div>
			</form>
		</div>
	</jsp:body>
</tpl:bodyFull>