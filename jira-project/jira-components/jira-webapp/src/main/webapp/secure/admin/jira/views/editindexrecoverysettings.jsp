<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.indexing.jira.indexing'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="indexing"/>
    <meta name="decorator" content="panel-admin"/>
</head>
<body>

<page:applyDecorator id="edit-index-recovery" name="auiform">
    <page:param name="action">EditIndexRecoverySettings.jspa</page:param>
    <page:param name="method">post</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.forms.update'"/></page:param>
    <page:param name="submitButtonName"><ww:text name="'common.forms.update'"/></page:param>
    <page:param name="cancelLinkURI">IndexAdmin.jspa</page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'admin.index.recovery.edit.settings'"/></aui:param>
    </aui:component>

    <page:applyDecorator name="auifieldset">
        <page:param name="type">group</page:param>
        <page:param name="legend"><ww:text name="'admin.index.recovery.enable'"/></page:param>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">radio</page:param>
            <%-- Set the checked state of the radio --%>
            <ww:if test="recoveryEnabled == 'true'">
                <ww:property id="recovery-enabled-checked" value="'true'"/>
            </ww:if>
            <aui:radio checked="@recovery-enabled-checked" id="recovery-enabled" label="text('admin.common.words.on')" value="true" list="null" name="'recoveryEnabled'" theme="'aui'" />
        </page:applyDecorator>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">radio</page:param>
            <page:param name="description"><ww:text name="'admin.index.recovery.enable.description'"/></page:param>
            <%-- Set the checked state of the radio --%>
            <ww:if test="recoveryEnabled == 'false'">
                <ww:property id="recovery-disabled-checked" value="'true'"/>
            </ww:if>
            <aui:radio checked="@recovery-disabled-checked" id="recovery-disabled" label="text('admin.common.words.off')" value="false" list="null" name="'recoveryEnabled'" theme="'aui'" />
        </page:applyDecorator>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldset">
        <page:param name="type">group</page:param>
        <page:param name="legend"><ww:text name="'admin.index.recovery.snapshot.interval'"/></page:param>
        <aui:radio id="snapshot-" label="" list="intervalOptions" listKey="'id'" listValue="'name'" name="'snapshotInterval'" value="snapshotInterval" theme="'aui'" />
    </page:applyDecorator>


</page:applyDecorator>
</body>
</html>
