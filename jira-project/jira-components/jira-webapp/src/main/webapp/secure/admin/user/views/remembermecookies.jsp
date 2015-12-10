<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.rememberme.cookies'"><ww:param name="value0"><ww:property value="/user/displayName"/></ww:param></ww:text></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>
<body>
    <page:applyDecorator id="rememberme_cookies_form" name="auiform">
        <page:param name="action">UserRememberMeCookies.jspa</page:param>
        <page:param name="submitButtonName">Submit</page:param>
        <page:param name="submitButtonText"><ww:text name="'admin.rememberme.clear.cookies'"/></page:param>
        <page:param name="cancelLinkURI"><ww:url value="'ViewUser.jspa'"><ww:param name="'name'" value="/name"/></ww:url></page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'escape'" value="'false'"/>
            <aui:param name="'text'"><ww:text name="'admin.rememberme.cookies'"><ww:param name="value0"><ww:property value="/user/displayName"/></ww:param></ww:text></aui:param>
        </aui:component>

        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.rememberme.instruction'"/></p>
            </aui:param>
        </aui:component>

        <table id="rememberme_cookies_table" class="aui">
            <thead>
                <tr>
                    <th><ww:text name="'admin.rememberme.id'"/></th>
                    <ww:if test="/tokens/empty == false">
                        <th><ww:text name="'admin.rememberme.created'"/></th>
                        <th><ww:text name="'admin.rememberme.expires'"/></th>
                    </ww:if>
                </tr>
            </thead>
            <tbody>
                <ww:if test="/tokens/empty == true">
                    <tr>
                        <td><ww:text name="'admin.rememberme.there.are.none'"><ww:param name="'value0'"><ww:property value="/user/displayName"/></ww:param></ww:text></td>
                    </tr>
                </ww:if>
                <ww:else>
                    <ww:iterator value="/tokens" status="''">
                        <tr>
                            <td><ww:property value="./id"/></td>
                            <td><ww:property value="/formattedDate(./createdTime)"/></td>
                            <td><ww:property value="/formattedDate(./expiryTime)"/></td>
                        </tr>
                    </ww:iterator>
                </ww:else>
            </tbody>
        </table>

        <aui:component template="hidden.jsp" name="'name'" theme="'aui'"/>
    </page:applyDecorator>
</body>
</html>
