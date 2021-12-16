<%@page import="java.nio.charset.Charset"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://catlogging.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<script type="text/javascript">
	$(function() {
		$("#reader-log4j-wizard .help-popup").popover({placement:"top"});
	});
	catloggingNgApp.controllerProvider.register(
		"Log4jWizardController", ['$scope', function ($scope) {
		$scope.placeHolders = {
			c: "category",
			t: "thread",
			m: "message",
			p: "priority",
			d: "date"
		};
		$scope.getUsedSpecifiers = function(formatPattern) {
			var specifiers=[];
			var re = /%[\d-\.]*([a-z]+)/gi;
			var m;
			do {
			    m = re.exec(formatPattern);
			    if (m) {
			    	specifiers.push(m[1]);
			    }
			} while (m);
			return specifiers;
		};
	}]);
</script>
<div id="reader-log4j-wizard" class="wizard" ng-controller="Log4jWizardController">
	<div class="row">
		<t:ngFormFieldWrapper cssClass="form-group col-md-10" fieldName="formatPattern">
			<label class="control-label" for="formatPattern"><spring:message code="catlogging.wizard.reader.conversion" />*:
				<i class="glyphicon glyphicon-info-sign help-popup" data-container="body" data-html="true" data-content="
				<spring:message code="catlogging.wizard.reader.conversion.text.1" />"
				 data-title="<spring:message code="catlogging.wizard.reader.conversion.label.1" />"></i>
			</label>
			<div class="controls controls-row">
				<input type="text" class="form-control pattern" ng-model="bean.formatPattern" name="formatPattern" id="formatPattern" required/>
			</div>
		</t:ngFormFieldWrapper>
		<t:ngFormFieldWrapper cssClass="form-group col-md-2" fieldName="charset">
			<label for="charset" class="control-label"><spring:message code="catlogging.common.form.characterSet" />*:</label>
			<select name="charset" id="charset" ng-model="bean.charset" class="form-control" required>
				<option value=""><spring:message code="catlogging.common.pleaseSelect" /></option>
				<c:forEach items="<%=Charset.availableCharsets() %>" var="entry">
					<option value="${entry.key}">${entry.key}</option>
				</c:forEach>
			</select>
		</t:ngFormFieldWrapper>
	</div>
	<div class="row">
		<div ng-include="contextPath + '/ng/wizards/filter/util/localeTimeZoneFields.html?v='+version"></div>
	</div>
	<div class="row">
		<div class="form-group col-md-12">
			<label class="control-label"><spring:message code="catlogging.wizard.reader.conversion.mapping" />:
				<i class="glyphicon glyphicon-info-sign help-popup" data-container="body" data-html="true" data-content="
				<spring:message code="catlogging.wizard.reader.conversion.mapping.text.1" />"
					 data-title="<spring:message code="catlogging.wizard.reader.conversion.mapping.label.1" />"></i></label>
			<div class="row form-group form-horizontal" ng-repeat="c in getUsedSpecifiers(bean.formatPattern)" ng-if="c!='n'">
				<div class="col-md-1 cc"><label class="control-label">%{{c}}:</label></div>				
				<div class="col-md-5">
					<div class="controls controls-row">
						<input type="text" class="form-control" ng-model="bean.specifiersFieldMapping[c]" placeholder="{{placeHolders[c]}}" />
					</div>
				</div>
			</div>
			<div class="help-block" ng-if="getUsedSpecifiers(bean.formatPattern).length==0"><spring:message code="catlogging.wizard.reader.conversion.mapping.text.2" /></div>
		</div>
	</div>
</div>