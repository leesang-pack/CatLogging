<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://catlogging.com/jstl/fn"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:message code="catlogging.breadcrumb.settings" var="settingsLabel" />
<spring:message code="${activeNode.title}" var="activeNodeTitle" />
<spring:message code="${rootNode.title}" var="rootNodeTitle" />
<c:set var="isNgPage" value="${activeNode.pageContext.typeName=='ngPage'}" />
<c:set var="isNgTemplate" value="${activeNode.pageContext.typeName=='ngTemplate'}" />
<tpl:bodySidebar title="${activeNodeTitle} - ${rootNodeTitle}" activeNavbar="system" ngModules="'SystemRootModule'">
	<jsp:attribute name="htmlHead">
		<c:if test="${isNgPage}">
			<!-- NG page -->
			<c:forEach var="jsFile" items="${activeNode.pageContext.jsFiles}">
			    <script type="text/javascript" src="<c:url value="/${jsFile}" />?v=${catloggingProps['catlogging.version']}"></script>
			</c:forEach>
		</c:if>
		<script type="text/javascript">
			angular.module('SystemRootModule',
				[<c:if test="${isNgPage}">'${activeNode.pageContext.module}'</c:if>]
			)
			.controller(
					"SystemAbstractController",
					[
					 '$scope',
					 '$http',
					 '$log',
					 'lsfAlerts',
					 function($scope, $http, $log, lsfAlerts) {
						 $scope.loadSettings = function(childScope) {
							 var callbacks = {};
					    	 childScope.state.busy = true;
					    	 $http({
							     url : childScope.settingsRessource,
							     method : "GET"
							 })
							.success(
								function(data, status, headers, config) {
								    $log.info("Settings loaded from", $scope.settingsRessource, data);
								    childScope.state.busy = false;
								    if (callbacks.success) {
								    	callbacks.success(data, status, headers, config);
								    }
								}
							)
							.error(
								function(data, status, headers, config, statusText) {
									childScope.state.busy = false;
									childScope.alerts.httpError("Failed to load settings", data, status, headers, config, statusText);
								    if (callbacks.error) {
								    	callbacks.error(data, status, headers, config, statusText);
								    }
								}
							);
					    	
					    	return {
					    		success: function(callback) {
					    			callbacks.success = callback;
					    		},
					    		error: function(callback) {
					    			callbacks.error = callback;
					    		}
					    	};
					     };
					     
					     $scope.saveSettings = function(childScope, settings) {
							var callbacks = {};
					    	childScope.state.busy = true;
					    	childScope.alerts.clear();
							$log.info("Saving settings", settings);
							 $http({
							     url : childScope.settingsRessource,
							     method : "POST",
							     data: settings
							 })
							.success(
								function(data, status, headers, config) {
								    $log.info("Settings saved", childScope.settingsRessource);
								    childScope.state.busy = false;
								    childScope.alerts.success("Changes applied successfully");
								    if (callbacks.success) {
								    	callbacks.success(data, status, headers, config);
								    }
								}
							)
							.error(
								function(data, status, headers, config, statusText) {
									childScope.state.busy = false;
									childScope.alerts.httpError("Failed to save settings", data, status, headers, config, statusText);
								    if (data && data.bindErrors) {
								    	childScope.bindErrors = data.bindErrors;
								    }
								    if (callbacks.error) {
								    	callbacks.error(data, status, headers, config, statusText);
								    }
								}
							);
					    	return {
					    		success: function(callback) {
					    			callbacks.success = callback;
					    		},
					    		error: function(callback) {
					    			callbacks.error = callback;
					    		}
					    	};

					     };					     
					 }
					]
				);
			
			;
		</script>
    </jsp:attribute>
	<jsp:attribute name="sidebar">
		<ul class="nav nav-sidebar">
			<c:forEach var="node1" items="${rootNode.subNodes}">
				<c:url value="/c/system" var="url">
					<c:if test="${node1!=rootNode}"><c:param name="path" value="${node1.path}"/></c:if>
				</c:url>
				<spring:message code="${node1.title}" var="Node1Title" />
				<li class="${logfn:contains(breadcrumbNodes, node1) || node1 == activeNode ? 'active':''}"><a href="${url}">${Node1Title}</a>
				</li>
			</c:forEach>
		</ul>
	</jsp:attribute>
	
	<jsp:body>
		<ul class="breadcrumb">
			<c:forEach var="node" items="${breadcrumbNodes}">
				<c:url value="/c/system" var="url">
					<c:if test="${node!=rootNode}"><c:param name="path" value="${node.path}"/></c:if>
				</c:url>
				<spring:message code="${node.title}" var="NodeTitle" />
				<li><a href="${url}">${NodeTitle}</a></li>
			</c:forEach>
			<li class="active">${activeNodeTitle}</li>
		</ul>
		
		<div ng-controller="SystemAbstractController">
			<c:choose>
				<c:when test="${isNgPage}">
					<div ng-controller="${activeNode.pageContext.controller}" ng-include="'<c:url value="/${activeNode.pageContext.template}" />?v=${catloggingProps['catlogging.version']}'"></div>
				</c:when>
				<c:when test="${isNgTemplate}">
					<div ng-include="'<c:url value="/${activeNode.pageContext.template}" />?v=${catloggingProps['catlogging.version']}'"></div>
				</c:when>
			</c:choose>
		</div>
	</jsp:body>
</tpl:bodySidebar>