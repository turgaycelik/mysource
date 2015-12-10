
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.import.restore.jira.data'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/import_export_section"/>
    <meta name="admin.active.tab" content="restore_data"/>
</head>

<body>

<page:applyDecorator id="restore-xml-data-backup" name="auiform">
    <page:param name="action">XmlRestore.jspa</page:param>
    <page:param name="method">post</page:param>
    <page:param name="submitButtonText"><ww:text name="'admin.import.restore'"/></page:param>
    <page:param name="submitButtonName">Restore</page:param>
    <page:param name="cancelLinkURI">default.jsp</page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'admin.import.restore.jira.data.from.backup'"/></aui:param>
    </aui:component>

    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'admin.import.note1'"/></p>
            <p><ww:text name="'admin.import.note2'"/></p>
        </aui:param>
    </aui:component>

    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'messageHtml'">
            <p>
                <ww:text name="'admin.import.warning'">
                    <ww:param name="'value0'"><a href="XmlBackup!default.jspa"><b></ww:param>
                    <ww:param name="'value1'"></b></a></ww:param>
                </ww:text>
            </p>
        </aui:param>
    </aui:component>

    <ww:if test="/hasSpecificErrors == true && /specificErrors/errorMessages/empty == false">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'messageHtml'">
                <ww:iterator value="specificErrors/errorMessages">
                    <p><ww:property escape="false"/></p>
                </ww:iterator>
            </aui:param>
        </aui:component>
    </ww:if>

    <%--This is here for our functests so we can do a quick import where we just clear the cache--%>
    <input id="quickImport" type="checkbox" name="quickImport" value="true" style="display:none;" />

    <page:applyDecorator name="auifieldset">
        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="'admin.import.file.name.description'"/> <span id="default-import-path"><ww:property value="/defaultImportPath"/></span></page:param>
            <aui:textfield id="'file-name'" size="'long'" label="text('admin.export.file.name')" mandatory="true" name="'filename'" theme="'aui'" />
        </page:applyDecorator>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <ww:component template="formFieldValue.jsp" label="text('setup.indexpath.label')" theme="'aui'">
            <aui:param name="'texthtml'">
                <ww:text name="'restore.index.path.msg'"/> <ww:property value="/defaultIndexPath"/>
            </aui:param>
        </ww:component>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <page:param name="description"><ww:text name="'admin.import.enter.a.license'"/></page:param>
        <aui:textarea id="'license'" label="text('admin.import.license.if.required')" name="'license'" cols="50"
                      rows="'5'" size="'long'" theme="'aui'"/>
    </page:applyDecorator>

    <ww:if test="/outgoingMailModifiable == true">
        <page:applyDecorator name="auifieldset">
            <page:param name="type">group</page:param>
            <page:param name="legend"><ww:text name="'admin.import.outgoing.mail.setting.label'"/></page:param>
            <aui:radio id="'outgoing-mail'" label="text('admin.import.outgoing.mail.setting.label')" list="/outgoingMailOptions"
                       listKey="'key'" listValue="'value'" name="'outgoingEmail'" theme="'aui'" />
        </page:applyDecorator>
    </ww:if>
    <ww:else>
        <page:applyDecorator name="auifieldgroup">
            <ww:component template="formFieldValue.jsp" label="text('admin.import.outgoing.mail.setting.label')" theme="'aui'">
                <aui:param name="'texthtml'">
                    <ww:text name="'admin.import.outgoing.mail.setting.set.on.jira.start'">
                        <ww:param name="'value0'"><code></ww:param>
                        <ww:param name="'value1'"></code></ww:param>
                    </ww:text>
                </aui:param>
            </ww:component>
        </page:applyDecorator>
    </ww:else>

    <aui:component name="'saxParser'" template="hidden.jsp" theme="'aui'"  />
    <aui:component name="'downgradeAnyway'" value="/downgradeAnyway" template="hidden.jsp" theme="'aui'" />
</page:applyDecorator>
<script type="text/javascript">
    jQuery(function() {
        jQuery("#reimport").click(function(e) {
            e.preventDefault();
            //set the form to import with default paths
            jQuery("#restore-xml-data-backup-submit,#restore-xml-data-backup-cancel").attr("disabled", "true");
            jQuery(this).closest("form").append("<input type='hidden' name='useDefaultPaths' value='true' />").submit();
        });
        // Acknowledge downgrade error
        jQuery("#acknowledgeDowngradeError").click(function(e) {
            e.preventDefault();
            // hide the warning. then set the 'downgradeAnyway' form param
            jQuery('#acknowledgeDowngradeError').parent().parent().fadeOut();
            jQuery('input[name=downgradeAnyway]').val("true");
        });

    });
</script>
</body>
</html>
