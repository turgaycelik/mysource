<%@ page import="com.atlassian.core.util.FileSize"%>
<%@ page import="com.atlassian.jira.config.properties.APKeys"%>
<%@ page import="webwork.config.Configuration" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ include file="/template/standard/controlheader.jsp" %>

<input type="file"
       name="<ww:property value="parameters['name']"/>"
       value="<ww:property value="parameters['nameValue']"/>" <%-- NB - this will only work in opera.  IE & Mozilla both ignore it for security reasons --%>
       size="30"
>
<br><font size="1">
<ww:text name="'attachfile.filebrowser.warning'">
    <ww:param name="'value0'"><%= FileSize.format(new Long(Configuration.getString(APKeys.JIRA_ATTACHMENT_SIZE))) %></ww:param>
</ww:text></font>

<%@ include file="/template/standard/controlfooter.jsp" %>
