<%@tag description="Left Navbar HTML Template" pageEncoding="UTF-8"%>
<%@attribute name="active" required="false" type="java.lang.String"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<c:set var="isAuth" value="${catloggingProps['catlogging.enable.auth'] == 'true' ? true : false}" />
<div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
    <div class="container-fluid">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="<c:url value="/" />">catlogging</a>
        </div>
        <div class="collapse navbar-collapse">
            <ul class="nav navbar-nav">
                <li class="${active=='sources'?'active':'' }">
                    <a href="<c:url value="/c/sources" />"><spring:message code="catlogging.nav.logs"/></a>
                </li>
                <li class="${active=='sniffers'?'active':'' }">
                    <a href="<c:url value="/c/sniffers" />"><spring:message code="catlogging.nav.events"/></a>
                </li>
                <!--
		<li class="${active=='reports'?'active':'' }">
			<a href="<c:url value="/c/reports" />">Dashboards</a>
		</li>
		 -->
                <li class="${active=='system'?'active':'' }">
                    <a href="<c:url value="/c/system" />"><spring:message code="catlogging.nav.system"/></a>
                </li>
            </ul>
            <ul class="nav navbar-right navbar-nav">
                <li><a href="/c/sources?lang=en" > <spring:message code="catlogging.lang.en"/> </a></li>
                <li><a href="/c/sources?lang=ko" > <spring:message code="catlogging.lang.ko"/> </a></li>
                <li><a href="/c/sources?lang=zh" > <spring:message code="catlogging.lang.zh"/> </a></li>
                <li>
                    <a href="<c:url value="/c/system?path=notifications" />" title="{{systemNotificationSummary.count}} unread notification{{systemNotificationSummary.count!=1?'s':''}}"><i class="fa fa-bell"></i>
                        <sup ng-if="systemNotificationSummary.count!=0"><span class="label" ng-class="systemNotificationSummary.worstLevel=='ERROR'?'label-danger':(systemNotificationSummary.worstLevel=='WARN'?'label-warning':'label-info')">{{systemNotificationSummary.count}}</span></sup></a>
                </li>
                <c:if test="${isAuth}">
                    <li><a href="/logout" > <i class="fa fa-sign-out"></i> </a></li>
                </c:if>
            </ul>
        </div><!--/.nav-collapse -->
    </div>
</div>