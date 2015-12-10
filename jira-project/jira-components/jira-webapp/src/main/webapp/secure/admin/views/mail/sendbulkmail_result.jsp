<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/mail_section"/>
    <meta name="admin.active.tab" content="send_email"/>
    <title><ww:text name="'admin.email.send.email'"/></title>
</head>

<body>

<page:applyDecorator name="jiraform">
    <page:param name="action">ViewProjects.jspa</page:param>
    <page:param name="submitId">ok_submit</page:param>
    <page:param name="submitName"> <ww:text name="'admin.common.words.ok'"/> </page:param>
    <page:param name="autoSelectFirst">false</page:param>
    <page:param name="title"><ww:text name="'admin.email.send.email'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="description"><p><ww:property value="/status" escape="'false'" /></p></page:param>
</page:applyDecorator>
    <table class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th>
                    <ww:text name="'common.words.username'"/>
                </th>
                <th>
                    <ww:text name="'common.words.email'"/>
                </th>
                <th>
                    <ww:text name="'common.words.fullname'"/>
                </th>
            </tr>
        </thead>
        <tbody>
        <ww:iterator value="users" status="'status'">
            <tr>
                <td>
                    <a id="<ww:property value="name"/>" href="<ww:url page="ViewUser.jspa"><ww:param name="'name'" value="name"/></ww:url>"><ww:property value="name"/></a>
                </td>
                <td>
                    <a href="<ww:url page="ViewUser.jspa"><ww:param name="'name'" value="name"/></ww:url>"><ww:property value="emailAddress"/></a>
                </td>
                <td>
                    <ww:property value="displayName"/>
                </td>
            </tr>
        </ww:iterator>
        </tbody>
    </table>
</body>
</html>
