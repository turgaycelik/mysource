<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.notifications.add.notification'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/misc_schemes_section"/>
    <meta name="admin.active.tab" content="notification_schemes"/>
</head>

<body>
<page:applyDecorator name="jiraform">
    <page:param name="action">AddNotification.jspa</page:param>
    <page:param name="submitId">add_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
    <page:param name="cancelURI"><ww:url page="EditNotifications!default.jspa"><ww:param name="'schemeId'" value="schemeId"/></ww:url></page:param>
    <page:param name="title"><ww:text name="'admin.notifications.add.notification'"/></page:param>
    <page:param name="helpURL">notification_schemes</page:param>
    <page:param name="width">100%</page:param>
    <page:param name="columns">3</page:param>
    <page:param name="instructions">
    <ww:text name="'admin.projects.notification.scheme'"/>: <strong> <ww:property value="scheme/string('name')"/>  </strong>
        <p><ww:text name="'admin.notifications.instruction'"/></p>
    </page:param>

    <ui:select label="text('admin.notifications.events')" name="'eventTypeIds'" template="selectmultiple.jsp" list="events" listKey="'key'" listValue="'value/translatedName(/remoteUser)'" size="7">
        <ui:param name="'valueColSpan'" value="2"/>
        <ui:param name="'description'">
            <ww:text name="'admin.notifications.events.description'"/>
        </ui:param>
    </ui:select>

    <ww:iterator value="types/keySet" status="'itStatus'">
    <tr>
        <td>&nbsp;</td>
        <td class="fieldValueArea" width="25%">
            <input class="radio" type="radio" name="type" id="label_<ww:property/>" value="<ww:property value="."/>" <ww:if test="type==.">checked="checked"</ww:if>/>
            <label for="label_<ww:property/>"><ww:property value="types/(.)/displayName"/></label>
        </td>
        <td class="fieldValueArea">
            <ww:if test="types/(.)/type == 'email'">
                <input type="text" name="<ww:property value="."/>" value="<ww:property value="../parameter(.)"/>" onkeydown="document.forms['jiraform'].type[<ww:property value="@itStatus/index"/>].checked = true;" />
                <div class="fieldDescription"><ww:text name="'admin.notifications.only.public.issues'">
                    <ww:param name="'value0'"><strong></ww:param>
                    <ww:param name="'value1'"></strong></ww:param>
                </ww:text></div>
            </ww:if>
            <ww:elseIf test="types/(.)/type == 'user'">
                <ui:component label="''" name="." template="userselect.jsp" theme="'raw'">
                    <ui:param name="'formname'" value="'jiraform'" />
                    <ui:param name="'imageName'" value="'userImage'"/>
                    <ui:param name="'onchange'">document.forms['jiraform'].type[<ww:property value="@itStatus/index"/>].checked = true;</ui:param>
                </ui:component>
            </ww:elseIf>
            <ww:elseIf test="types/(.)/type == 'group'">
                <select name="<ww:property value="."/>" onClick="document.forms['jiraform'].type[<ww:property value="@itStatus/index"/>].checked = true;">
                    <option value=""><ww:text name="'admin.notifications.choose.a.group'"/></option>
                    <ww:iterator value="types/(.)/groups" >
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
                <select name="<ww:property value="."/>" onClick="document.forms['jiraform'].type[<ww:property value="@itStatus/index"/>].checked = true;">
                    <option value=""><ww:text name="'admin.notifications.choose.a.custom.field'"/></option>
                    <ww:iterator value="types/(.)/fields" >
                        <option value="<ww:property value="id"/>" <ww:if test="../../parameter(..) == id" >selected</ww:if>><ww:property value="name"/></option>
                    </ww:iterator>
                </select>
            </ww:elseIf>
            <ww:elseIf test="types/(.)/type == 'groupCF'">
                <select name="<ww:property value="."/>" onClick="document.forms['jiraform'].type[<ww:property value="@itStatus/index"/>].checked = true;">
                    <option value=""><ww:text name="'admin.notifications.choose.a.custom.field'"/></option>
                    <ww:iterator value="types/(.)/fields" >
                        <option value="<ww:property value="id"/>" <ww:if test="../../parameter(..) == id" >selected</ww:if>><ww:property value="name"/></option>
                    </ww:iterator>
                </select>
            </ww:elseIf>
            <ww:else>&nbsp;</ww:else>
        </td>
    </tr>
    </ww:iterator>
    <tr>
        <td colspan="3">
            <ui:component name="'schemeId'" template="hidden.jsp"/>
            <ui:component name="'event'" template="hidden.jsp"/>
        </td>
    </tr>
</page:applyDecorator>
</body>
</html>