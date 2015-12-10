<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.attachmentsettings.edit.attachment.settings'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="attachments"/>
    <meta name="decorator" content="panel-admin"/>
</head>
<body>

<page:applyDecorator id="edit-attachemnts" name="auiform">
    <page:param name="action">EditAttachmentSettings.jspa</page:param>
    <page:param name="method">post</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.forms.update'"/></page:param>
    <page:param name="submitButtonName"><ww:text name="'common.forms.update'"/></page:param>
    <page:param name="cancelLinkURI">ViewAttachmentSettings.jspa</page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'admin.attachmentsettings.edit.attachment.settings'"/></aui:param>
    </aui:component>

    <ww:if test="/systemAdministrator == true">
        <page:applyDecorator name="auifieldset">
            <page:param name="type">group</page:param>
            <page:param name="legend"><ww:text name="'admin.attachmentsettings.attachment.path'"/></page:param>
            <script>
                window.onload = function(){
                    jQuery("#attachmentPathOption_CUSTOM").toggleField("#attachmentPath")
                }
            </script>
            <ww:if test="/customAttachmentPath != null">
                <fieldset class="hidden parameters">
                    <input type="hidden" id="admin.attachmentsettings.custompath.migration.confirmation" value="<ww:text name="'admin.attachmentsettings.custompath.migration.confirmation'"/>">
                </fieldset>
                <script>
                    jQuery(function()
                    {
                        var promptMsg = function(e)
                        {
                            var msg = AJS.params['admin.attachmentsettings.custompath.migration.confirmation'];
                            if (! confirm(msg))
                            {
                                e.preventDefault();
                                e.stopPropagation();
                            }
                            else
                            {
                                jQuery('#attachmentPathOption_DEFAULT').unbind('click', arguments.callee);
                                jQuery('#attachmentPathOption_DISABLED').unbind('click', arguments.callee);
                            }
                        };
                        jQuery('#attachmentPathOption_DEFAULT').bind('click', promptMsg);
                        jQuery('#attachmentPathOption_DISABLED').bind('click', promptMsg);
                    });
                </script>
                <page:applyDecorator name="auifieldgroup">
                    <page:param name="type">radio</page:param>
                    <page:param name="description">
                        <em><ww:property value="/customAttachmentPath"/>/Users/msharp/tools/jira_home/data/attachments</em>
                        <aui:component template="auimessage.jsp" theme="'aui'">
                            <aui:param name="'messageType'">warning</aui:param>
                            <aui:param name="'helpKey'">JRA21004</aui:param>
                            <aui:param name="'messageHtml'">
                                <ww:text name="'admin.attachmentsettings.custompath.migration.msg'"/>
                            </aui:param>
                        </aui:component>
                    </page:param>
                    <aui:radio checked="'true'" id="attachmentPathOption_CUSTOM" label="text('admin.attachmentsettings.usecustompath')" value="'CUSTOM'" disabled="'true'" list="null" name="'attachmentPathOption'" theme="'aui'" />
                </page:applyDecorator>
            </ww:if>

            <page:applyDecorator name="auifieldgroup">
                <page:param name="type">radio</page:param>
                <page:param name="description"><em><ww:property value="/defaultAttachmentPath"/></em></page:param>
                <%-- Set the checked state of the radio --%>
                <ww:if test="attachmentPathOption == 'DEFAULT'">
                    <ww:property id="attachmentPathOption_DEFAULT-checked" value="'true'"/>
                </ww:if>
                <aui:radio checked="@attachmentPathOption_DEFAULT-checked" id="attachmentPathOption_DEFAULT" label="text('admin.attachmentsettings.usedefaultpath')" value="'DEFAULT'" list="null" name="'attachmentPathOption'" theme="'aui'" />
            </page:applyDecorator>
            <page:applyDecorator name="auifieldgroup">
                <page:param name="type">radio</page:param>
                <%-- Set the checked state of the radio --%>
                <ww:if test="attachmentPathOption == 'DISABLED'">
                    <ww:property id="attachmentPathOption_DISABLED-checked" value="'true'"/>
                </ww:if>
                <aui:radio checked="@attachmentPathOption_DISABLED-checked" id="attachmentPathOption_DISABLED" label="text('admin.attachmentsettings.disableattachments')" value="'DISABLED'" list="null" name="'attachmentPathOption'" theme="'aui'" />
            </page:applyDecorator>
        </page:applyDecorator>
    </ww:if>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield label="text('admin.attachmentsettings.attachment.size')" id="'attachmentSize'" mandatory="false" maxlength="40" name="'attachmentSize'" theme="'aui'" />
        <page:param name="description"><ww:text name="'admin.attachmentsettings.attachment.size.description'"/></page:param>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldset">
        <page:param name="type">group</page:param>
        <page:param name="legend"><ww:text name="'admin.attachmentsettings.enable.thumbnails'"/></page:param>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">radio</page:param>
            <%-- Set the checked state of the radio --%>
            <ww:if test="thumbnailsEnabled == 'true'">
                <ww:property id="thumbnails-enabled-checked" value="'true'"/>
            </ww:if>
            <aui:radio checked="@thumbnails-enabled-checked" id="thumbnails-enabled" label="text('admin.common.words.on')" value="true" list="null" name="'thumbnailsEnabled'" theme="'aui'" />
        </page:applyDecorator>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">radio</page:param>
            <page:param name="description"><ww:text name="'admin.attachmentsettings.enable.thumbnails.description'"/></page:param>
            <%-- Set the checked state of the radio --%>
            <ww:if test="thumbnailsEnabled == 'false'">
                <ww:property id="thumbnails-disabled-checked" value="'true'"/>
            </ww:if>
            <aui:radio checked="@thumbnails-disabled-checked" id="thumbnails-disabled" label="text('admin.common.words.off')" value="false" list="null" name="'thumbnailsEnabled'" theme="'aui'" />
        </page:applyDecorator>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldset">
        <page:param name="type">group</page:param>
        <page:param name="legend"><ww:text name="'admin.attachmentsettings.enable.zipsupport'"/></page:param>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">radio</page:param>
            <%-- Set the checked state of the radio --%>
            <ww:if test="zipSupport == 'true'">
                <ww:property id="zipSupport-enabled-checked" value="'true'"/>
            </ww:if>
            <aui:radio checked="@zipSupport-enabled-checked" id="zipSupport-enabled" label="text('admin.common.words.on')" value="true" list="null" name="'zipSupport'" theme="'aui'" />
        </page:applyDecorator>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">radio</page:param>
            <page:param name="description"><ww:text name="'admin.attachmentsettings.enable.zipsupport.description'"/></page:param>
            <%-- Set the checked state of the radio --%>
            <ww:if test="zipSupport == 'false'">
                <ww:property id="zipSupport-disabled-checked" value="'true'"/>
            </ww:if>
            <aui:radio checked="@zipSupport-disabled-checked" id="zipSupport-disabled" label="text('admin.common.words.off')" value="false" list="null" name="'zipSupport'" theme="'aui'" />
        </page:applyDecorator>
    </page:applyDecorator>

</page:applyDecorator>
</body>
</html>
