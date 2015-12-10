<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<ww:if test="/contentOnly == true">
    <% response.setContentType("application/json"); %>
    {"permissionsError": true}
</ww:if>
<ww:else>
<html>
<head>
    <title><ww:text name="'perm.violation.title'"/></title>
    <meta name="decorator" content="message" />
</head>
<body>
    <div class="form-body">
        <header>
            <h1><ww:text name="'perm.violation.title'"/></h1>
        </header>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'perm.violation.desc'"/></p>
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
</ww:else>
