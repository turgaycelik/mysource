<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.event.types.view'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="eventtypes"/>
</head>
<body>
    <page:applyDecorator name="jirapanel">
        <page:param name="title"><ww:text name="'admin.event.types.view'"/></page:param>
        <page:param name="width">100%</page:param>
            <p>
                <ww:text name="'admin.event.types.table.desc'"/><br />
                <ww:text name="'admin.event.types.table.columns.desc'" />
            </p>
            <p><ww:text name="'admin.event.types.type'" /></p>
            <ul>
                <li>
                    <ww:text name="'admin.event.types.system.desc'">
                        <ww:param name="'value0'"><b></ww:param>
                        <ww:param name="'value1'"></b></ww:param>
                    </ww:text>
                </li>
                <li>
                    <ww:text name="'admin.event.types.custsom.desc'">
                        <ww:param name="'value0'"><b></ww:param>
                        <ww:param name="'value1'"></b></ww:param>
                    </ww:text>
                </li>
            </ul>
            <p><ww:text name="'admin.event.types.state.desc'" /></p>
            <ul>
                <li><ww:text name="'admin.event.types.active.desc'">
                        <ww:param name="'value0'"><span class="green-highlight"></ww:param>
                        <ww:param name="'value1'"></span></ww:param>
                    </ww:text>
                </li>
                <li><ww:text name="'admin.event.types.inactive.desc'">
                        <ww:param name="'value0'"><span class="red-highlight"></ww:param>
                        <ww:param name="'value1'"></span></ww:param>
                    </ww:text>
                </li>
            </ul>
            <p><ww:text name="'admin.event.types.delete.inactive'" /></p>
    </page:applyDecorator>

    <ww:property value="/eventTypeManager/eventTypes">
        <jsp:include page="/includes/admin/eventtype/event-type-details.jsp"/>
    </ww:property>

    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'contentHtml'">
            <page:applyDecorator name="jiraform">
                <page:param name="action">AddEventType.jspa</page:param>
                <page:param name="submitId">add_submit</page:param>
                <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
                <page:param name="title"><ww:text name="'admin.event.types.addnew'"/></page:param>
                <page:param name="width">100%</page:param>
                <page:param name="autoSelectFirst">false</page:param>
                <page:param name="description"><ww:text name="'admin.event.types.addnew.desc'"/></page:param>

                <ui:textfield label="text('common.words.name')" name="'name'" size="'30'" />
                <ui:textfield label="text('common.words.description')" name="'description'" size="'60'" />
                <ui:select label="text('admin.notifications.template')" name="'templateId'" template="selectmap.jsp"
                    list="/templateManager/templatesMap('issueevent')" listKey="'key'" listValue="'value/name'">
                    <ui:param name="'headerrow'" value="text('admin.event.types.select.template')" />
                    <ui:param name="'headervalue'" value="'-1'" />
                    <ui:param name="'description'">
                        <ww:text name="'admin.event.types.template.desc'"/>
                    </ui:param>
                </ui:select>
            </page:applyDecorator>
        </aui:param>
    </aui:component>
</body>
</html>
