<%@tag import="com.catlogging.model.Log"%>
<%@tag import="org.apache.commons.io.FilenameUtils"%>
<%@tag description="Outputs the name of a file path" pageEncoding="UTF-8"%>
<%@attribute name="path" required="true" type="java.lang.String"%><%= FilenameUtils.getName((String)jspContext.getAttribute("path")) %>