<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.event.types.edit'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="eventtypes"/>
</head>

<body>

    <p>
    <table width=100% cellpadding=10 cellspacing=0 border=0>
    <page:applyDecorator name="jiraform">
        <page:param name="action">EditEventType.jspa</page:param>
        <page:param name="submitId">update_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.words.update'"/></page:param>
    	<page:param name="cancelURI">ListEventTypes.jspa</page:param>
        <page:param name="title"><ww:text name="'admin.event.types.edit'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="description">
            <p>
                <ww:text name="'admin.event.types.edit.desc'">
                    <ww:param><b></ww:param>
                    <ww:param><ww:property value="/eventTypeManager/eventType(eventTypeId)/name"/></ww:param>
                    <ww:param></b></ww:param>
                </ww:text>
            </p>
        </page:param>

        <ww:property value="/eventTypeManager/eventType(eventTypeId)">
            <ui:textfield label="text('common.words.name')" name="'name'" value="./name" size="'30'" />
            <ui:textfield label="text('common.words.description')" name="'description'" value="./description" size="'30'" />
            <ui:select label="text('admin.notifications.template')" name="'templateId'" template="selectmap.jsp"
                    list="/templateManager/templatesMap('issueevent')" listKey="'key'" listValue="'value/name'">
                    <ui:param name="'description'">
                        <ww:text name="'admin.event.types.template.desc'"/>
                    </ui:param>
                </ui:select>
        </ww:property>

        <ui:component name="'eventTypeId'" template="hidden.jsp" theme="'single'"  />
        <ui:component name="'confirmed'" value="'true'" template="hidden.jsp" theme="'single'"  />

    </page:applyDecorator>
    </table>
    </p>

</body>
</html>
