<%@tag import="org.apache.commons.lang3.StringUtils"%>
<%@tag description="Abbreviates a String using ellipses" pageEncoding="UTF-8"%>
<%@attribute name="value" required="true" type="java.lang.String"%>
<%@attribute name="maxWidth" required="true" type="java.lang.Integer"%><%= StringUtils.abbreviate((String)jspContext.getAttribute("value"),(Integer)jspContext.getAttribute("maxWidth")) %>