<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<spring:message code="catlogging.confirms.delete" var="confirmDelMsg" text=""/>
<form action="<c:url value="/c/sources/${activeSource.id}/delete" />" method="post">
	<ul class="breadcrumb">
		<li><a href="<c:url value="/c/sources" />"><spring:message code="catlogging.breadcrumb.sources" text=""/></a></li>
		<li class="active"><a href="<c:url value="/c/sources/${activeSource.id}/logs" />">${activeSource.name}</a></li>
		<li class="dropdown">
			<button data-toggle="dropdown" href="#" class="btn btn-default btn-xs"><i class="glyphicon glyphicon-cog"></i> <span class="caret"></span></button>
			<ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
				<li><a href="<c:url value="/c/sources/${activeSource.id}/logs" />"><i class="glyphicon glyphicon-list"></i> <spring:message code="catlogging.nav.logs" text=""/></a></li>
				<li><a href="<c:url value="/c/sources/${activeSource.id}" />"><i class="glyphicon glyphicon-edit"></i> <spring:message code="catlogging.common.form.edit" text=""/></a></li>
				<li class="divider"></li>
				<li role="presentation">			
					<a role="menuitem" href="#" onclick="if (confirm('${confirmDelMsg}')) {$(this).parents('form').submit()}">
							<i class="glyphicon glyphicon-trash"></i> <text ng-controller="LocaleMessageController" ng-init="localeMessageKey='catlogging.common.delete'">{{localeMessage}}</text> <text ng-controller="LocaleMessageController" ng-init="localeMessageKey='catlogging.type.com.catlogging.model.LogSource'">{{localeMessage}}</text></a>
				</li>
			</ul>
		</li>
	</ul>
</form>