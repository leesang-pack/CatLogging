<%@tag description="Wrapper for a NG form field" pageEncoding="UTF-8"%>
<%@attribute name="cssClass" required="false" type="java.lang.String"%>
<%@attribute name="fieldName" required="true" type="java.lang.String"%>
<%@attribute name="bindErrorsPath" required="false" type="java.lang.String"%>
<%@attribute name="isValidationPath" required="false" type="java.lang.Boolean"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<div class="${cssClass}" ng-class="
						{
							'has-error':
								form.${fieldName}.$invalid
							&& !form.${fieldName}.$pristine
							|| form.${fieldName}.$error.pattern
							|| bindErrors['${empty bindErrorsPath?fieldName:bindErrorsPath}']
							&& form.${fieldName}.$pristine
						}">

	<p class="help-block" ng-show="
								form.${fieldName}.$invalid
							&& !form.${fieldName}.$pristine
							&& form.${empty isValidationPath?false:isValidationPath}
							|| form.${fieldName}.$error.pattern"> <spring:message code="catlogging.common.absolutePath" arguments="${catloggingProps['catlogging.validationPath']}" text=""/></p>

	<jsp:doBody />
	<div class="help-block" ng-if="bindErrors['${empty bindErrorsPath?fieldName:bindErrorsPath}']
						&& form.${fieldName}.$pristine"> {{bindErrors['${empty bindErrorsPath?fieldName:bindErrorsPath}']}}
	</div>
</div>