<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/issue_attributes"/>
    <meta name="admin.active.tab" content="resolutions"/>
	<title><ww:text name="'admin.issuesettings.resolutions.view.resolutions'"/></title>
</head>

<body>

<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.issuesettings.resolutions.view.resolutions'"/></page:param>
    <page:param name="width">100%</page:param>
    <p>
    <ww:text name="'admin.issuesettings.resolutions.the.table.below'"/>
    </p>
    <ul class="optionslist">
    <ww:if test="/translatable == true">
        <li><ww:text name="'admin.issuesettings.resolutions.translations'">
            <ww:param name="'value0'"><a href="ViewTranslations!default.jspa?issueConstantType=resolution"></ww:param>
            <ww:param name="'value1'"></a></ww:param>
        </ww:text></li>
    </ww:if>
    <li><ww:text name="'admin.issuesettings.resolutions.clear.defaults'">
        <ww:param name="'value0'"><a href="MakeDefaultResolution.jspa"></ww:param>
        <ww:param name="'value1'"></a></ww:param>
    </ww:text></li>
    </ul>
</page:applyDecorator>

    <table class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th width="20%">
                    <ww:text name="'common.words.name'"/>
                </th>
                <th>
                    <ww:text name="'common.words.description'"/>
                </th>
                <th width="1%">
                    <ww:text name="'admin.issuesettings.order'"/>
                </th>
                <th width="10%">
                    <ww:text name="'common.words.operations'"/>
                </th>
            </tr>
        </thead>
        <tbody>
        <ww:iterator value="/constantsManager/resolutions" status="'status'">
            <tr>
                <td><b><ww:property value="string('name')"/></b><ww:if test="../default(.) == true"> (<ww:text name="'admin.common.words.default'"/>)</ww:if></td>
                <td><ww:property value="string('description')"/></td>
                <td>
                    <ww:if test="@status/first != true"><a href="<ww:url page="ResolutionUp.jspa"><ww:param name="'up'" value="string('id')" /></ww:url>"><img src="<%= request.getContextPath() %>/images/icons/arrow_up_blue_small.gif" class="sortArrow" alt="<ww:text name="'admin.issuesettings.resolutions.move.up'"/>"></a></ww:if>
                    <ww:else><image src="<%= request.getContextPath() %>/images/border/spacer.gif" class="sortArrow"></ww:else>
                    <ww:if test="@status/last != true"><a href="<ww:url page="ResolutionDown.jspa"><ww:param name="'down'" value="string('id')" /></ww:url>"><img src="<%= request.getContextPath() %>/images/icons/arrow_down_blue_small.gif" class="sortArrow" alt="<ww:text name="'admin.issuesettings.resolutions.move.down'"/>"></a></ww:if>
                    <ww:else><image src="<%= request.getContextPath() %>/images/border/spacer.gif" class="sortArrow"></ww:else>
                </td>
                <td>
                    <ul class="operations-list">
                        <li><a href="EditResolution!default.jspa?id=<ww:property value="string('id')"/>"><ww:text name="'common.words.edit'"/></a></li>
                        <li><a href="DeleteResolution!default.jspa?id=<ww:property value="string('id')"/>"><ww:text name="'common.words.delete'"/></a></li>
                    <ww:if test="../default(.) == false">
                        <li><a href="<ww:url page="MakeDefaultResolution.jspa"><ww:param name="'make'" value="string('id')" /></ww:url>"><ww:text name="'admin.common.words.default'"/></a></li>
                    </ww:if>
                    </ul>
                </td>
            </tr>
        </ww:iterator>
        </tbody>
    </table>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'contentHtml'">
            <page:applyDecorator name="jiraform">
                <page:param name="action">AddResolution.jspa</page:param>
                <page:param name="helpURL">resolutions</page:param>
                <page:param name="width">100%</page:param>
                <page:param name="submitId">add_submit</page:param>
                <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
                <page:param name="title"><ww:text name="'admin.issuesettings.resolutions.add.new.resolution'"/></page:param>

                <ui:textfield label="text('common.words.name')" name="'name'" size="'30'" />

                <ui:textfield label="text('common.words.description')" name="'description'" size="'60'" />
            </page:applyDecorator>
        </aui:param>
    </aui:component>
</body>
</html>
