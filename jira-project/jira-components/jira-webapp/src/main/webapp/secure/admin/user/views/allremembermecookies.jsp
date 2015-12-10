<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title><ww:text name="'admin.rememberme.all.cookies'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/security_section"/>
    <meta name="admin.active.tab" content="rememberme"/>
</head>

<body>

<page:applyDecorator id="rememberme_cookies_form" name="auiform">
    <page:param name="action">AllUsersRememberMeCookies.jspa</page:param>
    <page:param name="submitName"/>
    <page:param name="submitButtonName">Submit</page:param>
    <page:param name="submitButtonText"><ww:text name="'admin.rememberme.clear.cookies'"/></page:param>
    <page:param name="cancelLinkURI"><ww:url value="'/secure/project/ViewProjects.jspa'"/></page:param>

    <ww:if test="/cleared == true">
       <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">success</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.rememberme.all.cleared'"/></p>
            </aui:param>
        </aui:component>
    </ww:if>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'admin.rememberme.all.cookies'"/></aui:param>
    </aui:component>

    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'admin.rememberme.all.instruction'"/></p>
        </aui:param>
    </aui:component>

    <p><ww:text name="'admin.rememberme.all.count'"><ww:param name='value0'><strong><ww:property value="/totalCountString"/></strong></ww:param></ww:text></p>

</page:applyDecorator>
</body>
</html>