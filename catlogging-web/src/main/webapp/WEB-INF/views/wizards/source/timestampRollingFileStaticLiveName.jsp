<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://catlogging.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<div id="source-wildcard-rolling-file-wizard">
	<lsf-info-label label="Source for rolled log files with a static named live log file" class="text-muted">
		This source concats a live actively written log file with older rolled over files into a single continuous log.
		The live log has a static name and it's the single file which can change size. After it's rolled, the file got renamed to
		a fix timestamp based name and no longer changed to enable exact navigation in the concated logs. 
		<strong>Important:</strong> This source supports rolling only based on a timestamp name pattern, because this guarantees 
		static names of rolled over files, which is the key for navigating in the concated files.
	</lsf-info-label>
	<div class="row">
<%--		<div class="col-md-12">--%>
		<lsf-form-group class="form-group required" field-name="pattern" field-path="pattern" bind-errors="bindErrors">
			<t:ngFormFieldWrapper cssClass="form-group col-md-12 required" fieldName="pattern">
				<lsf-info-label label="Path pattern for the live log:" for="pattern">
					Reference here the path pattern for the live log file being actively written and rolled over once a certain condition is met.
					This source supports exposing of multiple dedicated rolling logs in
					the case of matching several live files.
					<div ng-include="contextPath + '/ng/help/logsByAntPathExpression.html?v='+version"></div>
				</lsf-info-label>
				<input type="text" ng-model="bean.pattern" name="pattern" id="pattern" ng-pattern="/${catloggingProps['catlogging.validationPath']}/" class="form-control" required>
			</t:ngFormFieldWrapper>
		</lsf-form-group>
<%--		</div>--%>
	</div>
	<div class="row">
		<div class="col-md-8">
			<lsf-form-group class="form-group required" field-name="pastLogsSuffixPattern" field-path="pastLogsSuffixPattern" bind-errors="bindErrors">
				<lsf-info-label label="Suffix for rolled over files:" for="pastLogsSuffixPattern">
					This suffix pattern is used to detect older rolled over files in dependency to the name and location of the live log file.
					Example: In case of a daily rolling file with the live log <code>server.log</code> and rolled over files like
					<code>server.log.2015-07-02</code>, <code>server.log.2015-07-01</code> etc. the pattern <code>.*</code>
					will match the old rolled over files and combine all together into a continuous log file.
				</lsf-info-label>
				<input type="text" ng-model="bean.pastLogsSuffixPattern" name="pastLogsSuffixPattern" id="pastLogsSuffixPattern" class="form-control" required>
			</lsf-form-group>
		</div>
		<div class="col-md-4">
			<lsf-form-group class="form-group required" field-name="pastLogsType" field-path="pastLogsType" bind-errors="bindErrors">
				<lsf-info-label label="Order criteria for rolled files:" for="pastLogsType">
					Defines the order criteria for rolled over files which should correlate to the sequence the log files
					were written over time.
				</lsf-info-label>
				<select ng-model="bean.pastLogsType" name="pastLogsType" id="pastLogsType" class="form-control" required>
					<option value="NAME">File name</option>
					<option value="LAST_MODIFIED">Time of last modification </option>
				</select>
			</lsf-form-group>
		</div>
	</div>
</div>
