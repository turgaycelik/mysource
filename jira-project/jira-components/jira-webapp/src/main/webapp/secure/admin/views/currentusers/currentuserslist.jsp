<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.currentusers.title'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/security_section"/>
    <meta name="admin.active.tab" content="usersessions"/>
</head>
<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.currentusers.title'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">currentusers</page:param>
    <page:param name="helpURLFragment">#CurrentUsers</page:param>
    <page:param name="description">
        <p><ww:text name="'admin.currentusers.desc'"/></p>
        <p><ww:text name="'admin.currentusers.current.server.time'">
            <ww:param value="/serverTime"/>
        </ww:text></p>
    </page:param>
</page:applyDecorator>

<jsp:include page="currentuserslistnav.jsp"/>

<table class="aui aui-table-rowhover">
    <thead>
        <tr>
            <th><ww:text name="'common.words.sessionid'"/></th>
            <th><ww:text name="'common.words.user'"/></th>
            <th><ww:text name="'common.words.type'"/></th>
            <th><ww:text name="'common.words.ipaddress'"/></th>
            <th><ww:text name="'common.words.requests'"/></th>
            <th><ww:text name="'common.words.lastaccessed'"/></th>
            <th><ww:text name="'admin.currentusers.creation'"/></th>
        </tr>
    </thead>
    <tbody>
    <ww:iterator value="/pager/list" status="'status'">
        <tr class="<ww:if test="@status/odd == true">rowNormal</ww:if><ww:else>rowAlternate</ww:else>">
            <td>
                <ww:property value="./ASessionId"/>
            </td>
            <td>
                <ww:if test="./validUser == true">
                    <a href="<ww:url page="secure/ViewProfile.jspa"><ww:param name="'name'" value="./userName"/></ww:url>"><ww:property value="./userName"/></a>
                </ww:if>
                <ww:else>
                    <ww:property value="./userName"/>
                </ww:else>
            </td>
            <td>
                <ww:property value="./type"/>
            </td>
            <td>
                <ww:property value="./ipAddress"/>
            </td>
            <td style="text-align:center;">
                <ww:property value="./requestCount"/>
            </td>
            <td>
                <ww:property value="./lastAccessTime"/>
            </td>
            <td>
                <ww:property value="./creationTime"/>
            </td>
        </tr>
    </ww:iterator>
    </tbody>
</table>

<jsp:include page="currentuserslistnav.jsp"/>

</body>
</html>
