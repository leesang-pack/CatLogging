<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="tpl" tagdir="/WEB-INF/tags/templates"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="logfn" uri="http://catlogging.com/jstl/fn"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:message var="typeLabel" code="catlogging.type.${ex.resourceType.name}" text=""/>
<spring:message var="errorShort" code="catlogging.exception.refintg.short" arguments="${typeLabel}" text=""/>
<spring:message var="errorDetail" code="catlogging.exception.refintg.detail" arguments="${typeLabel}" text=""/>

<tpl:main title="${errorShort}">
	<tpl:navbar />
	<div class="container-fluid">
		<div class="alert alert-danger">
			<h1>${errorShort}</h1>
			<p>${errorDetail}</p>
		</div>
	</div>
</tpl:main>