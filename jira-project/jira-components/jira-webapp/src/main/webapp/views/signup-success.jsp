<%@ page import="java.util.*"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<html>
<head>
	<title><ww:text name="'signup.heading.success'"/></title>
</head>
<body class="aui-page-focused aui-page-focused-medium">
    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                <ui:param name="'content'">
                    <h1><ww:text name="'signup.heading.success'"/></h1>
                    <aui:component template="auimessage.jsp" theme="'aui'">
                        <aui:param name="'messageType'">success</aui:param>
                        <aui:param name="'messageHtml'">
                            <p><ww:text name="'signup.success'"/></p>
                        </aui:param>
                    </aui:component>
                    <p><a href="<%= request.getContextPath() %>/login.jsp"><ww:text name="'login.click'"/></a>.</p>
                    <p>
                        <ww:text name="'signup.stay.in.touch'">
                            <ww:param name="'value0'"><a href='<ww:property value="/externalLinkUtils/property('external.link.atlassian.news')"/>'></ww:param>
                            <ww:param name="'value1'"></a></ww:param>
                        </ww:text>
                    </p>
                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</body>
</html>
