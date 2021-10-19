<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://catlogging.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<div id="source-wildcard-rolling-file-wizard">
	<lsf-info-label label="Source for rolled log files with a live file named dynamically" class="text-muted">
		This source concats a live actively written log file with older rolled over files into a single continuous log.
		The live log is the single file which can change size. After it's rolled the size should be fix and no longer got changed to
		enable exact navigation in the concated logs. Due to the name of the live file is dynamic e.g. containing
		the current day timestamp, it's determined as the first file of the matching file collection regarding
		the set order criteria.
		<strong>Important:</strong> This source supports rolling only based on a timestamp name pattern, because this guarantees 
		static names of rolled over files, which is the key for navigating in the concated files.
	</lsf-info-label>
	<div class="row">
<%--		<div class="col-md-8">--%>
		<lsf-form-group class="form-group required" field-name="pattern" field-path="pattern" bind-errors="bindErrors">
			<t:ngFormFieldWrapper cssClass="form-group col-md-12 required" fieldName="pattern">
				<lsf-info-label label="Path pattern for the live and rolled over files:" for="pattern">
					Reference here the live and the rolled over files using an
					<a href="http://ant.apache.org/manual/dirtasks.html#patterns" target="_blank">Ant-style pattern</a> expression.
					The live file is determined as first entry of the matching file collection regarding
					the set order criteria. This source supports exposing of only one rolling log.
				</lsf-info-label>
				<input type="text" ng-model="bean.pattern" name="pattern" id="pattern" ng-pattern="/${catloggingProps['catlogging.validationPath']}/" class="form-control" required>
			</t:ngFormFieldWrapper>
		</lsf-form-group>
<%--		</div>--%>
		<div class="col-md-4">
			<lsf-form-group class="form-group required" field-name="pastLogsType" field-path="pastLogsType" bind-errors="bindErrors">
				<lsf-info-label label="Order criteria matching files:" for="pastLogsType">
					Defines the order criteria of the matching files which should correlate to the sequence the log files
					have been written over time.
				</lsf-info-label>
				<select ng-model="bean.pastLogsType" name="pastLogsType" id="pastLogsType" class="form-control" required>
					<option value="NAME">File name</option>
					<option value="LAST_MODIFIED">Time of last modification </option>
				</select>
			</lsf-form-group>
		</div>
	</div>
</div>
