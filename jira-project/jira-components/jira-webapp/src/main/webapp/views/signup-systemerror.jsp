<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<html>
<head>
    <title><ww:text name="'signup.title'"/></title>
</head>
<body class="page-type-message">
    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                <ui:param name="'content'">
                    <h1><ww:text name="'signup.heading'"/></h1>
                    <aui:component template="auimessage.jsp" theme="'aui'">
                        <aui:param name="'messageType'">error</aui:param>
                        <aui:param name="'messageHtml'">
                            <p><ww:text name="'signup.error.creating.user'"/></p>
                        </aui:param>
                    </aui:component>
                    <p>
                        <ww:text name="'signup.contact.admin'">
                            <ww:param name="'value0'"><ww:property value="administratorContactLink" escape="'false'"/></ww:param>
                        </ww:text>
                    </p>
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
