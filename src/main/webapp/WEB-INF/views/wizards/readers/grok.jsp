<%@page import="java.nio.charset.Charset"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://catlogging.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>


<div id="reader-grok-wizard" class="wizard">
	<lsf-info-label label="<spring:message code="catlogging.wizard.reader.grok" text=""/>">
		<p ng-controller="LocaleMessageController" ng-init="localeMessageKey='catlogging.wizard.reader.grok.text.1'" ng-bind-html="sanitizeLocalMessage()"></p>
	</lsf-info-label>
	<div class="row">
		<t:ngFormFieldWrapper cssClass="form-group col-md-10 required" fieldName="grokPattern" bindErrorsPath="grokBean.pattern">
			<label for="grokPattern" class="control-label"><spring:message code="catlogging.common.form.pattern" text=""/>:</label>
			<div class="controls controls-row">
				<input type="text" class="form-control pattern" ng-model="bean.grokBean.pattern" name="grokPattern" id="grokPattern" required/>
			</div>
		</t:ngFormFieldWrapper>
		<t:ngFormFieldWrapper cssClass="form-group col-md-2" fieldName="charset">
			<label for="charset" class="control-label"><spring:message code="catlogging.common.form.characterSet" text=""/>*:</label>
			<select name="charset" id="charset" ng-model="bean.charset" class="form-control" required>
				<option value=""><spring:message code="catlogging.common.pleaseSelect" text=""/></option>
				<c:forEach items="<%=Charset.availableCharsets() %>" var="entry">
					<option value="${entry.key}">${entry.key}</option>
				</c:forEach>
			</select>
		</t:ngFormFieldWrapper>
	</div>
	
	<div class="row">
		<div class="form-group col-md-6">
			<label class="control-label"><spring:message code="catlogging.common.form.patternFlags" text=""/>:</label>
			<div class="controls controls-row">
				<label class="checkbox-inline">
					<input type="checkbox" ng-model="bean.grokBean.subStringSearch" class="subStringSearch"/> <spring:message code="catlogging.common.form.patternSubString" text=""/>
				</label>
				<label class="checkbox-inline">
					<input type="checkbox" ng-model="bean.grokBean.caseInsensitive" class="caseInsensitive"/> <spring:message code="catlogging.common.form.patternCaseMatch" text=""/>
				</label>
				<!-- 
				<label class="checkbox-inline">
					<input type="checkbox" ng-model="bean.grokBean.multiLine" class="multiLine"/> Multiline mode
				</label>
				<label class="checkbox-inline">
					<input type="checkbox" ng-model="bean.grokBean.dotAll" class="dotAll"/> Dotall mode
				</label> -->
			</div>
		</div>
		<t:ngFormFieldWrapper cssClass="form-group col-md-6" fieldName="overflowAttribute">
			<lsf-info-label label="<spring:message code="catlogging.common.form.overflowAttr" text=""/>:" for="overflowAttribute">
				<p ng-controller="LocaleMessageController" ng-init="localeMessageKey='catlogging.common.form.overflowAttr.text'" ng-bind-html="sanitizeLocalMessage()"></p>
			</lsf-info-label>
			<input type="text" class="form-control pattern" ng-model="bean.overflowAttribute" id="overflowAttribute" name="overflowAttribute" />
		</t:ngFormFieldWrapper>		
	</div>

</div>