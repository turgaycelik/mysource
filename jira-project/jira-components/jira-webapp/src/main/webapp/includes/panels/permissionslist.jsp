<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<h3>
    <ww:component name="'permissions'" template="help.jsp"/>
    <ww:text name="'admin.globalpermissions.jira.permissions'"/>
</h3>

<jsp:include page="permission/list_table.jsp"/>
<aui:component template="module.jsp" theme="'aui'">
    <aui:param name="'contentHtml'">
        <page:applyDecorator name="jiraform">
            <page:param name="action">GlobalPermissions.jspa</page:param>
            <page:param name="submitId">addpermission_submit</page:param>
            <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
            <page:param name="width">100%</page:param>
            <page:param name="title"><ww:text name="'admin.globalpermissions.add.permission'"/></page:param>
            <page:param name="autoSelectFirst">false</page:param>
            <%--<page:param name="description"><ww:text name="'admin.globalpermissions.add.a.new.permission'"/></page:param>--%>

            <ui:select label="text('admin.common.words.permission')" name="'globalPermType'" list="globalPermTypes" listKey="'key'" listValue="'text(value)'">
                <ui:param name="'headerrow'" value="text('admin.globalpermissions.please.select.a.permission')" />
                <ui:param name="'headervalue'" value="''" />
            </ui:select>
            <ui:select label="text('admin.common.words.group')" name="'groupName'" list="groups" listKey="'name'" listValue="'name'">
                <ui:param name="'headerrow'" value="text('admin.common.words.anyone')" />
                <ui:param name="'headervalue'" value="''" />
            </ui:select>

            <ui:component name="'action'" value="'add'" template="hidden.jsp" theme="'single'" />
        </page:applyDecorator>
    </aui:param>
</aui:component>