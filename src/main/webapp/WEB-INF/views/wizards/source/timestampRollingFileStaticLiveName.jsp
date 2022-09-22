<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://catlogging.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<div id="source-wildcard-rolling-file-wizard">
	<lsf-info-label label="<spring:message code="catlogging.common.timestampStatic.label.1" text=""/>" class="text-muted">
		<p ng-controller="LocaleMessageController" ng-init="localeMessageKey='catlogging.common.timestampStatic.text.1'" ng-bind-html="sanitizeLocalMessage()"></p>
	</lsf-info-label>
	<div class="row">
<%--		<div class="col-md-12">--%>
		<lsf-form-group class="form-group required" field-name="pattern" field-path="pattern" bind-errors="bindErrors">
			<t:ngFormFieldWrapper cssClass="form-group col-md-12 required" fieldName="pattern">
				<lsf-info-label label="<spring:message code="catlogging.common.timestampStatic.label.4" text=""/>:" for="pattern">
					<p ng-controller="LocaleMessageController" ng-init="localeMessageKey='catlogging.common.timestampStatic.text.2'" ng-bind-html="sanitizeLocalMessage()"></p>
					<p ng-controller="LocaleMessageController" ng-init="localeMessageKey='catlogging.common.fileWildcard.text.3'" ng-bind-html="sanitizeLocalMessage()"></p>
				</lsf-info-label>
				<input type="text" ng-model="bean.pattern" name="pattern" id="pattern" ng-pattern="/${catloggingProps['catlogging.validationPath']}/" class="form-control" required>
			</t:ngFormFieldWrapper>
		</lsf-form-group>
<%--		</div>--%>
	</div>
	<div class="row">
		<div class="col-md-8">
			<lsf-form-group class="form-group required" field-name="pastLogsSuffixPattern" field-path="pastLogsSuffixPattern" bind-errors="bindErrors">
				<lsf-info-label label="<spring:message code="catlogging.common.timestampStatic.label.2" text=""/>:" for="pastLogsSuffixPattern">
					<p ng-controller="LocaleMessageController" ng-init="localeMessageKey='catlogging.common.timestampStatic.text.3'" ng-bind-html="sanitizeLocalMessage()"></p>
				</lsf-info-label>
				<input type="text" ng-model="bean.pastLogsSuffixPattern" name="pastLogsSuffixPattern" id="pastLogsSuffixPattern" class="form-control" required>
			</lsf-form-group>
		</div>
		<div class="col-md-4">
			<lsf-form-group class="form-group required" field-name="pastLogsType" field-path="pastLogsType" bind-errors="bindErrors">
				<lsf-info-label label="<spring:message code="catlogging.common.orderRollFile" text=""/>:" for="pastLogsType">
					<p ng-controller="LocaleMessageController" ng-init="localeMessageKey='catlogging.common.orderRollFile.text'" ng-bind-html="sanitizeLocalMessage()"></p>
				</lsf-info-label>
				<select ng-model="bean.pastLogsType" name="pastLogsType" id="pastLogsType" class="form-control" required>
					<option value="NAME"><spring:message code="catlogging.common.filename" text=""/></option>
					<option value="LAST_MODIFIED"><spring:message code="catlogging.common.lastofmodify" text=""/> </option>
				</select>
			</lsf-form-group>
		</div>
	</div>
</div>
