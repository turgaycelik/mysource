<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/user_interface"/>
    <meta name="admin.active.tab" content="edit_default_dashboard"/>
    <title><ww:property value="applicationTitle" /></title>
</head>
<body>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">error</aui:param>
        <aui:param name="'titleText'"><ww:text name="'admin.common.words.errors'"/></aui:param>
        <aui:param name="'messageHtml'">
            <ww:if test="/errorMessages && /errorMessages/empty == false">
                <ww:iterator value="/errorMessages">
                    <p><ww:property value="."/></p>
                </ww:iterator>
            </ww:if>
            <ww:if test="remoteUser == null">
                <p>
                    <ww:text name="'dashboard.page.login'">
                        <ww:param name="'value0'"><jira:loginlink><ww:text name="'login.required.login'"/></jira:loginlink></ww:param>
                    </ww:text>
                </p>
            </ww:if>
            <p>
                <ww:text name="'contact.admin.for.perm'">
                    <ww:param name="'value0'"><ww:property value="administratorContactLink" escape="'false'"/></ww:param>
                </ww:text>
            </p>
        </aui:param>
    </aui:component>
</body>
</html>
