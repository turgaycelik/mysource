<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.schemes.issuesecurity.add.user.group.to.issue.security.level'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/misc_schemes_section"/>
    <meta name="admin.active.tab" content="security_schemes"/>
</head>

<body>
<page:applyDecorator name="jiraform">
    <page:param name="action">AddIssueSecurity.jspa</page:param>
    <page:param name="columns">1</page:param>
    <page:param name="submitId">add_submit</page:param>
    <page:param name="submitName"> <ww:text name="'common.forms.add'"/> </page:param>
    <page:param name="cancelURI"><ww:url page="EditIssueSecurities!default.jspa"><ww:param name="'schemeId'" value="schemeId"/></ww:url></page:param>
    <page:param name="title"><ww:text name="'admin.schemes.issuesecurity.add.user.group.to.issue.security.level'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="description">
        <ww:property value="securityLevel(security)/string('name')">
            <p>
            <ww:text name="'admin.schemes.issuesecurity.issue.security.scheme'"/>: <b> <ww:property value="scheme/string('name')"/>  </b> <br>
            <ww:text name="'admin.schemes.issuesecurity.issue.security.level'"/>: <b> <ww:property value="."/> </b>
            </p>
            <p><ww:text name="'admin.schemes.issuesecurity.add.instruction'"/></p>
            <ww:text name="'admin.schemes.issuesecurity.add.description'"/>:
            <ul>
                <li><ww:text name="'admin.schemes.issuesecurity.add.description.dotpoint1'"/></li>
                <li><ww:text name="'admin.schemes.issuesecurity.add.description.dotpoint2'"><ww:param name="'value0'"><b><ww:property value="."/></b></ww:param></ww:text></li>
            </ul>
        </ww:property>
    </page:param>

    <tr>
        <td>
            <table>
                <ww:iterator value="types/keySet" status="'status'">
                <tr>

                    <td width="5%" align="center"><input type="radio" name="type" value="<ww:property value="."/>" id="<ww:property value="."/>_id" <ww:if test="type==.">checked</ww:if>></td>
                    <td width="25%" align="left"><label for="<ww:property value="."/>_id"><ww:property value="types/(.)/displayName"/></label></td>
                    <ww:if test="types/(.)/type == 'email'">
                        <td><input type="text" name="<ww:property value="."/>" value="<ww:property value="../parameter(.)"/>"/></td>
                    </ww:if>
                    <ww:elseIf test="types/(.)/type == 'user'">
                        <td>
                            <ui:component label="''" name="." template="userselect.jsp" theme="'raw'">
                                <ui:param name="'formname'" value="'jiraform'" />
                                <ui:param name="'imageName'" value="'userImage'"/>
                                <ui:param name="'onchange'">document.forms['jiraform'].type[<ww:property value="@status/index"/>].checked = true;</ui:param>
                            </ui:component>
                        </td>
<%--                        <td><input type="text" name="<ww:property value="."/>" value="<ww:property value="../parameter(.)"/>"></td>--%>
                    </ww:elseIf>
                    <ww:elseIf test="types/(.)/type == 'group'">
                        <td>
                        <select name="<ww:property value="."/>" onClick="document.forms['jiraform'].type[<ww:property value="@status/index"/>].checked = true;">
                            <option value=""><ww:text name="'admin.common.words.anyone'"/></option>
                            <ww:iterator value="types/(.)/groups" >
                                <option value="<ww:property value="name"/>" <ww:if test="../../parameter(..) == name" >selected</ww:if>><ww:property value="name"/></option>
                            </ww:iterator>
                        </select>
                        </td>
                    </ww:elseIf>
                    <ww:elseIf test="types/(.)/type == 'projectrole'">
                        <td>
                        <select name="<ww:property value="."/>" onClick="document.forms['jiraform'].type[<ww:property value="@status/index"/>].checked = true;">
                            <option value=""><ww:text name="'admin.notifications.choose.a.projectrole'"/></option>
                            <ww:iterator value="types/(.)/projectRoles" >
                                <option value="<ww:property value="id"/>" <ww:if test="../../parameter(..) == name" >selected</ww:if>><ww:property value="name"/></option>
                            </ww:iterator>
                        </select>
                        </td>
                    </ww:elseIf>
                    <ww:elseIf test="types/(.)/type == 'userCF'">
                        <td>
                        <select name="<ww:property value="."/>" onClick="document.forms['jiraform'].type[<ww:property value="@status/index"/>].checked = true;">
                            <option value=""><ww:text name="'admin.schemes.issuesecurity.choose.a.custom.field'"/></option>
                            <ww:iterator value="types/(.)/displayFields" >
                                <option value="<ww:property value="id"/>" <ww:if test="../../parameter(..) == id" >selected</ww:if>><ww:property value="name"/></option>
                            </ww:iterator>
                        </select>
                        </td>
                    </ww:elseIf>
                    <ww:elseIf test="types/(.)/type == 'groupCF'">
                        <td>
                        <select name="<ww:property value="."/>" onClick="document.forms['jiraform'].type[<ww:property value="@status/index"/>].checked = true;">
                            <option value=""><ww:text name="'admin.notifications.choose.a.custom.field'"/></option>
                            <ww:iterator value="types/(.)/displayFields" >
                                <option value="<ww:property value="id"/>" <ww:if test="../../parameter(..) == id" >selected</ww:if>><ww:property value="name"/></option>
                            </ww:iterator>
                        </select>
                        </td>
                    </ww:elseIf>
                    <ww:else><td>&nbsp;</td></ww:else>
                </tr>
                </ww:iterator>
                <ui:component name="'schemeId'" template="hidden.jsp"/>
                <ui:component name="'security'" template="hidden.jsp"/>
            </table>
        </td>
    </tr>

</page:applyDecorator>
</body>
</html>
