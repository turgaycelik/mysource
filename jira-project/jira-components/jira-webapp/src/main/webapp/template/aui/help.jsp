<%@ page import="com.atlassian.jira.web.util.HelpUtil" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%-- Provides help on a topic. Sample usage:

    <ww:component name="'navigatorviews'" template="help.jsp" theme="aui>
    </ww:component>

Code use:
    com.atlassian.jira.web.util.HelpUtil helpUtil = new com.atlassian.jira.web.util.HelpUtil(request.getRemoteUser(), request.getContextPath());
    request.setAttribute("helpUtil", helpUtil);
--%>

<%--<ww:bean name="'com.atlassian.jira.web.util.HelpUtil'" id="helpUtil" />--%>
<%
    // pico tries to find a satisfiable constructor for HelpUtil, whilst none exists.  This is quite slow for performance reasons
    HelpUtil helpUtil = new HelpUtil();
    request.setAttribute("helpUtil", helpUtil);
%>

<ww:property value="@helpUtil/helpPath(parameters['name'])">
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Links.helpLink'">
        <ui:param name="'isLocal'" value="local" />
        <ui:param name="'title'"><ww:text name="title" /></ui:param>
        <ui:param name="'description'">
            <ww:if test="parameters['linktext']"><ww:property value="parameters['linktext']"/></ww:if>
            <ww:else><ww:property value="alt"/></ww:else>
        </ui:param>
        <ui:param name="'url'"><ww:url value="url" atltoken="false"/></ui:param>
        <ui:param name="'fragmentIdentifier'"><ww:property value="parameters['helpURLFragment']" /></ui:param>
    </ui:soy>
</ww:property>
