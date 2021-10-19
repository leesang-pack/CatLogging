<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://catlogging.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<div id="source-wildcard-file-wizard">
	<span class="text-muted">Source for simple log files matching a file name pattern</span>
	<div class="row">
		<%--
			ngFormFieldWrapper에서 필수 인자 fieldName이 필요하다.
			그것을 가지고 validation을 확인 후에 div에 has-error를 넣을지 말지 결정
        --%>
		<t:ngFormFieldWrapper cssClass="form-group col-md-12 required" fieldName="pattern">

		<%--
			툴팁
        --%>
			<lsf-info-label label="File path pattern:" for="pattern">
				<div ng-include="contextPath + '/ng/help/logsByAntPathExpression.html?v='+version"></div>
			</lsf-info-label>

		<%--
			NG-model은 객체의 고유이름 인듯
			NG의 fieldName은 input의 name에 해당 된다.
			input에 의해 regex 검사를 ngFormFieldWrapper의 fieldName에 의해 하고 스타일 체인지
		--%>
			<input type="text" ng-model="bean.pattern" name="pattern" id="pattern" ng-pattern="/${catloggingProps['catlogging.validationPath']}/" class="form-control pattern" required>
		</t:ngFormFieldWrapper>
	</div>
</div>