<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title><spring:message code="site.title" text="스프링 테스트 사이트 - default" /></title>
</head>

<body>
<p>
<a href="<c:url value='/main/i18n.do?lang=ko' />">한국어</a>
<a href="<c:url value='/main/i18n.do?lang=en' />">English</a>
</p>
<p>site.title : <spring:message code="site.title" text="default text" /></p>
<p>site.count : <spring:message code="site.count" arguments="첫번째" text="default text" /></p>
<p>site.count using EL : <spring:message code="site.count" arguments="${siteCount}" text="default text" /></p>
<p>not.exist : <spring:message code="not.exist" /></p>
<p>Client Locale : <c:out value="${clientLocale}" /></p>
<p>Session Locale : <c:out value="${sessionLocale}" /></p>
</body>
</html>