<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.permissions.add.title'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/misc_schemes_section"/>
    <meta name="admin.active.tab" content="permission_schemes"/>
</head>

<body>
<page:applyDecorator name="jiraform">
    <page:param name="action">AddPermission.jspa</page:param>
    <page:param name="submitId">add_submit</page:param>
    <page:param name="submitName"> <ww:text name="'common.forms.add'"/> </page:param>
    <page:param name="cancelURI"><ww:url page="EditPermissions!default.jspa"><ww:param name="'schemeId'" value="schemeId"/></ww:url></page:param>
    <page:param name="title"><ww:text name="'admin.permissions.add.title'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="columns">3</page:param>
    <page:param name="helpURL">permissionsHelp</page:param>
    <page:param name="helpURLFragment">#adding_to_permission</page:param>
    <page:param name="instructions">
    <ww:text name="'admin.projects.permission.scheme'"/>: <strong><ww:property value="scheme/string('name')"/></strong>
    <p><ww:text name="'admin.permissions.add.longdesc'"/></p>
    </page:param>
    <ui:select label="text('admin.common.words.permissions')" name="'permissions'" template="selectmultiple.jsp" list="allPermissions" listKey="'key'" listValue="'text(nameI18nKey)'" size="7" >
        <ui:param name="'valueColSpan'" value="2"/>
        <ui:param name="'description'">
            (<ww:text name="'admin.permissions.add.short.instruction'"/>).
        </ui:param>
    </ui:select>

    <%-- todo - this iterator status gets buggered up on Resin.  Not sure why --%>
    <ww:iterator value="types/keySet" status="'itStatus'">
    <%-- rows with different types of permissions (eg group / single user) --%>
    <tr>
        <td>&nbsp;</td>
        <td class="fieldValueArea" width="25%">
            <input class="radio" type="radio" name="type" value="<ww:property value="."/>" id="type_<ww:property />" <ww:if test="type==.">checked="checked"</ww:if>/>
            <label for="type_<ww:property />"><ww:property value="types/(.)/displayName"/></label>
        </td>
        <%-- For types that need a select value (eg which group), display the custom values --%>
        <td class="fieldValueArea">
            <%-- For users, show the user picker --%>
            <ww:if test="types/(.)/type == 'user'">
                <ui:component label="''" name="." template="userselect.jsp" theme="'common'">
                    <ui:param name="'formname'" value="'jiraform'" />
                    <ui:param name="'imageName'" value="'userImage'"/>
                    <ui:param name="'onchange'">document.forms['jiraform'].type[<ww:property value="@itStatus/index"/>].checked = true;</ui:param>
                </ui:component>
            </ww:if>
            <%-- For groups, show a dropdown with a list of groups --%>
            <ww:elseIf test="types/(.)/type == 'group'">
                <!-- Only set the 'onChange' if there is more than one option available -->
                <select name="<ww:property value="."/>" <ww:if test="types/size > 1">onClick="document.forms['jiraform'].type[<ww:property value="@itStatus/index"/>].checked = true;"</ww:if>>
                    <option value=""><ww:text name="'admin.common.words.anyone'"/></option>
                    <ww:iterator value="types/(.)/groups" status="'a'">
                        <option value="<ww:property value="name"/>" <ww:if test="../../parameter(..) == name" >selected</ww:if>><ww:property value="name"/></option>
                    </ww:iterator>
                </select>
            </ww:elseIf>
            <ww:elseIf test="types/(.)/type == 'projectrole'">
                <select name="<ww:property value="."/>" onClick="document.forms['jiraform'].type[<ww:property value="@itStatus/index"/>].checked = true;">
                    <option value=""><ww:text name="'admin.notifications.choose.a.projectrole'"/></option>
                    <ww:iterator value="types/(.)/projectRoles" >
                        <option value="<ww:property value="id"/>" <ww:if test="../../parameter(..) == name" >selected</ww:if>><ww:property value="name"/></option>
                    </ww:iterator>
                </select>
            </ww:elseIf>
            <ww:elseIf test="types/(.)/type == 'userCF'">
                <select name="<ww:property value="."/>" <ww:if test="types/size > 1">onClick="document.forms['jiraform'].type[<ww:property value="@itStatus/index"/>].checked = true;"</ww:if>>
                    <option value=""><ww:text name="'admin.notifications.choose.a.custom.field'"/></option>
                    <ww:iterator value="types/(.)/displayFields" >
                        <option value="<ww:property value="id"/>" <ww:if test="../../parameter(..) == id" >selected</ww:if>><ww:property value="name"/></option>
                    </ww:iterator>
                </select>
            </ww:elseIf>
            <ww:elseIf test="types/(.)/type == 'groupCF'">
                <select name="<ww:property value="."/>" <ww:if test="types/size > 1">onClick="document.forms['jiraform'].type[<ww:property value="@itStatus/index"/>].checked = true;"</ww:if>>
                    <option value=""><ww:text name="'admin.notifications.choose.a.custom.field'"/></option>
                    <ww:iterator value="types/(.)/displayFields" >
                        <option value="<ww:property value="id"/>" <ww:if test="../../parameter(..) == id" >selected</ww:if>><ww:property value="name"/></option>
                    </ww:iterator>
                </select>
            </ww:elseIf>
            <%-- Other inputs do not require a text box --%>
            <ww:else>&nbsp;</ww:else>
        </td>
    </tr>
    </ww:iterator>
    <tr>
        <td colspan="3">
            <ui:component name="'schemeId'" template="hidden.jsp"/>
            <ui:component name="'permission'" template="hidden.jsp"/>
        </td>
    </tr>
</page:applyDecorator>
</body>
</html>
