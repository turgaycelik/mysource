<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<html>
<head>
    <title><ww:text name="'common.concepts.dashboard'" /></title>
    <meta name="decorator" content="message" />
</head>
<body>
    <div class="form-body">
        <header>
            <h1><ww:text name="'common.words.error'"/></h1>
        </header>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'iconText'"><ww:text name="'admin.common.words.error'"/></aui:param>
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
    </div>
</body>
</html>