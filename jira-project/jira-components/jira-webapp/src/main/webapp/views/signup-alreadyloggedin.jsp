<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<html>
<head>
	<title><ww:text name="'signup.heading.already.logged.in'"/></title>
</head>
<body class="page-type-message">
    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                <ui:param name="'content'">
                    <h1><ww:text name="'signup.heading.already.logged.in'"/></h1>
                    <aui:component template="auimessage.jsp" theme="'aui'">
                        <aui:param name="'messageType'">warning</aui:param>
                        <aui:param name="'messageHtml'">
                            <p>
                                <ww:text name="'signup.already.logged.in'">
                                    <ww:param name="'value0'"><a id="log_out" href="<ww:url value="'/secure/Logout.jspa'" />"></ww:param>
                                    <ww:param name="'value1'"></a></ww:param>
                                </ww:text>
                            </p>
                        </aui:param>
                    </aui:component>
                    <p>
                        <ww:text name="'signup.return.to.dashboard'">
                            <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/Dashboard.jspa"></ww:param>
                            <ww:param name="'value1'"></a></ww:param>
                        </ww:text>
                    </p>
                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</body>
</html>
