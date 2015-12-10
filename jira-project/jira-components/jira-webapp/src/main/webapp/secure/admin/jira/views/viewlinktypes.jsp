
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/issue_features"/>
    <meta name="admin.active.tab" content="linking"/>
	<title><ww:text name="'admin.issuelinking.view.issue.link.types'"/></title>
</head>

<body>

<ww:if test="issueLinking == true">
    <page:applyDecorator name="jiraform">
        <page:param name="action">IssueLinkingDeActivate.jspa</page:param>
        <page:param name="submitId">deactivate_submit</page:param>
        <page:param name="submitName"><ww:text name="'admin.common.words.deactivate'"/></page:param>
        <page:param name="title">
            <ww:text name="'admin.issuelinking.status'">
                <ww:param name="'value0'"><span class="status-active"><ww:text name="'admin.common.words.on'"/></span></ww:param>
            </ww:text>
        </page:param>
        <page:param name="width">100%</page:param>
        <page:param name="autoSelectFirst">false</page:param>
        <page:param name="description">
            <p><ww:text name="'admin.issuelinking.instruction3'"/></p>
        </page:param>
        <page:param name="helpURL">issuelinking</page:param>
    </page:applyDecorator>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'">
            <p>
            <ww:text name="'admin.issuelinking.instruction2'">
                <ww:param name="'value0'"><b><ww:text name="'admin.permissions.LINK_ISSUE'"/></b></ww:param>
            </ww:text>
            <p>
        </aui:param>
    </aui:component>
    <table class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th>
                    <ww:text name="'common.words.name'"/>
                </th>
                <th>
                    <ww:text name="'admin.issuelinking.outward.description'"/>
                </th>
                <th>
                    <ww:text name="'admin.issuelinking.inward.description'"/>
                </th>
                <th width="10%">
                   <ww:text name="'common.words.operations'"/>
                </th>
            </tr>
        </thead>
        <tbody>
        <ww:iterator value="linkTypes">
            <tr>
                <td><b><ww:property value="./name"/></b></td>
                <td><ww:property value="./outward"/></td>
                <td><ww:property value="./inward"/></td>
                <td>
                    <ul class="operations-list">
                        <li><a id="edit_<ww:property value="./name"/>" href="EditLinkType!default.jspa?id=<ww:property value="./id"/>"><ww:text name="'common.words.edit'"/></a></li>
                        <li><a id="del_<ww:property value="./name"/>" href="DeleteLinkType!default.jspa?id=<ww:property value="./id"/>"><ww:text name="'common.words.delete'"/></a></li>
                    </ul>
                </td>
            </tr>
        </ww:iterator>
        </tbody>
    </table>

    <page:applyDecorator name="jiraform">
        <page:param name="action">ViewLinkTypes.jspa</page:param>
        <page:param name="submitId">add_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="title"><ww:text name="'admin.issuelinking.add.new.link.type'"/></page:param>
        <page:param name="description"><ww:text name="'admin.issuelinking.add.a.new.link.type'"/></page:param>
        <page:param name="autoSelectFirst">false</page:param>

        <ui:textfield label="text('common.words.name')" name="'name'" size="'30'">
            <ui:param name="'description'"><ww:text name="'admin.issuelinking.name.description'"/></ui:param>
        </ui:textfield>

        <ui:textfield label="text('admin.issuelinking.outward.link.description')" name="'outward'" size="'30'">
            <ui:param name="'description'"><ww:text name="'admin.issuelinking.outward.link.example'"/></ui:param>
        </ui:textfield>

        <ui:textfield label="text('admin.issuelinking.inward.link.description')" name="'inward'" size="'30'">
            <ui:param name="'description'"><ww:text name="'admin.issuelinking.inward.link.example'"/></ui:param>
        </ui:textfield>
    </page:applyDecorator>

</ww:if>
<ww:else>

    <page:applyDecorator name="jiraform">
        <page:param name="action">IssueLinkingActivate.jspa</page:param>
        <page:param name="submitId">activate_submit</page:param>
        <page:param name="submitName"><ww:text name="'admin.common.words.activate'"/></page:param>
        <page:param name="title">
            <ww:text name="'admin.issuelinking.status'">
                <ww:param name="'value0'"><span class="status-inactive"><ww:text name="'admin.common.words.off'"/></span></ww:param>
            </ww:text>
        </page:param>
        <page:param name="width">100%</page:param>
        <page:param name="description">
            <p>
            <ww:text name="'admin.issuelinking.instruction'"/>
            </p>
        </page:param>
    </page:applyDecorator>

</ww:else>

</body>
</html>
