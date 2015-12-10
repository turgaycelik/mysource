<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.util.UnsupportedBrowserManager" %>
<ww:bean name="'com.atlassian.jira.web.util.HelpUtil'" id="helpUtil" />
<%@ taglib uri="webwork" prefix="aui" %>
<%
    final UnsupportedBrowserManager browserManager =  ComponentManager.getComponentInstanceOfType(UnsupportedBrowserManager.class);
    if (browserManager.isCheckEnabled() && !browserManager.isHandledCookiePresent(request) && browserManager.isUnsupportedBrowser(request))
    {
       request.setAttribute("messageKey", browserManager.getMessageKey(request));
%>
<aui:component id="'browser-warning'" template="auimessage.jsp" theme="'aui'">
    <aui:param name="'messageType'">error</aui:param>
    <aui:param name="'cssClass'">closeable</aui:param>
    <aui:param name="'messageHtml'">
        <p>
            <ww:text name="@messageKey">
                <ww:param name="'value0'"><a href='<ww:property value="@helpUtil/helpPath('platforms.supported')/url" />'></ww:param>
                <ww:param name="'value1'"><ww:property value="@helpUtil/helpPath('platforms.supported')/title" /></ww:param>
                <ww:param name="'value2'"></a></ww:param>
            </ww:text>
        </p>
    </aui:param>
</aui:component>
<% } %>