<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.event.types.delete'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="eventtypes"/>
</head>

<body>

    <p>
    <table width=100% cellpadding=10 cellspacing=0 border=0>
    <page:applyDecorator name="jiraform">
        <page:param name="action">DeleteEventType.jspa</page:param>
        <page:param name="submitId">delete_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
        <page:param name="cancelURI">ListEventTypes.jspa</page:param>
        <page:param name="title"><ww:text name="'admin.event.types.delete'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="description">
            <p>
                <ww:text name="'admin.event.types.delete.confirmation'">
                    <ww:param><b></ww:param>
                    <ww:param><ww:property value="/eventTypeManager/eventType(eventTypeId)/translatedName(/remoteUser)"/></ww:param>
                    <ww:param></b></ww:param>
                </ww:text>
            </p>
        </page:param>

        <ui:component name="'eventTypeId'" template="hidden.jsp" theme="'single'"  />
        <ui:component name="'confirmed'" value="'true'" template="hidden.jsp" theme="'single'"  />

    </page:applyDecorator>
    </table>
    </p>

</body>
</html>
