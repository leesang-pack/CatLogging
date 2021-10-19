<%@page import="java.nio.charset.Charset"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://catlogging.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>


<div id="reader-grok-wizard" class="wizard">
	<lsf-info-label label="Regex / Grok pattern reader">
		This reader reads the content of a log and parses each line using the specified regular or grok expression.
		The below pattern is evaluated against each log line. <span ng-include="contextPath + '/ng/help/regexGrokPattern.html?v='+version"></span>
	</lsf-info-label>
	<div class="row">
		<t:ngFormFieldWrapper cssClass="form-group col-md-10 required" fieldName="grokPattern" bindErrorsPath="grokBean.pattern">
			<label for="grokPattern" class="control-label">Pattern:</label>
			<div class="controls controls-row">
				<input type="text" class="form-control pattern" ng-model="bean.grokBean.pattern" name="grokPattern" id="grokPattern" required/>
			</div>
		</t:ngFormFieldWrapper>
		<t:ngFormFieldWrapper cssClass="form-group col-md-2" fieldName="charset">
			<label for="charset" class="control-label">Character set*:</label>
			<select name="charset" id="charset" ng-model="bean.charset" class="form-control" required>
				<option value=""><spring:message code="catlogging.common.pleaseSelect" /></option>
				<c:forEach items="<%=Charset.availableCharsets() %>" var="entry">
					<option value="${entry.key}">${entry.key}</option>
				</c:forEach>
			</select>
		</t:ngFormFieldWrapper>
	</div>
	
	<div class="row">
		<div class="form-group col-md-6">
			<label class="control-label">Pattern flags:</label>
			<div class="controls controls-row">
				<label class="checkbox-inline">
					<input type="checkbox" ng-model="bean.grokBean.subStringSearch" class="subStringSearch"/> Sub string search
				</label>
				<label class="checkbox-inline">
					<input type="checkbox" ng-model="bean.grokBean.caseInsensitive" class="caseInsensitive"/> Case-insensitive matching
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
			<lsf-info-label label="Overflow attribute:" for="overflowAttribute">
				In case of lines not matching the pattern these can be attached to a field of last well parsed log line.
				 The overflow field can reference an existing or a new field. If not set, the not matching lines will be attached (as default)
				 to the <code>lf_raw</code> field of the last well parsed log entry.
			</lsf-info-label>
			<input type="text" class="form-control pattern" ng-model="bean.overflowAttribute" id="overflowAttribute" name="overflowAttribute" />
		</t:ngFormFieldWrapper>		
	</div>

</div>