<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib prefix="jira" uri="jiratags" %>
<html>
<head>
    <title><ww:text name="'admin.projects.edit.project'"/>: <ww:property value="project/string('name')" /></title>
    <jira:web-resource-require modules="jira.webresources:autocomplete"/>
    <meta name="admin.active.section" content="admin_project_menu/project_section"/>
    <meta name="admin.active.tab" content="view_projects"/>
</head>
<body>

<fieldset class="hidden parameters">
    <input type="hidden" id="uploadImage" value="<ww:text name="'avatarpicker.upload.image'"/>">
</fieldset>

<page:applyDecorator id="project-edit" name="auiform">

    <page:param name="action">EditProject.jspa</page:param>

    <page:param name="cancelLinkURI"><%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="/project/string('key')"/>/summary</page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'admin.projects.edit.project'"/>: <ww:text name="project/string('name')" /></aui:param>
    </aui:component>

    <ww:if test="/hasInvalidLead == 'true'">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'messageHtml'">
                <ww:text name="'admin.errors.not.a.valid.lead'"/>
            </aui:param>
        </aui:component>
    </ww:if>
    <ww:else>

        <page:param name="submitButtonText"><ww:text name="'common.forms.update'"/></page:param>
        <page:param name="submitButtonName">Update</page:param>

        <ww:bean name="'com.atlassian.jira.web.util.HelpUtil'" id="helpUtil" />
        <div id="edit-project-fields" class="aui-group">
            <ui:soy moduleKey="'jira.webresources:action-soy-templates'" template="'aui.message.warning'">
                <ui:param name="'id'">edit-project-warning-message</ui:param>
                <ui:param name="'content'"><p>
                    <ww:text name="'admin.projects.edit.project.key.warning'">
                        <ww:param name="'value0'"><a href='<ww:property  value="@helpUtil/helpPath('editing_project_key')/url" />' target="_blank"></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                </p></ui:param>
                <ui:param name="'extraAttributes'">style="display: none"</ui:param>
            </ui:soy>

            <page:applyDecorator name="auifieldgroup">
                <aui:textfield label="/text('common.words.name')" name="'name'" size="'50'" maxlength="/maxNameLength" mandatory="'true'" theme="'aui'"/>
            </page:applyDecorator>

            <ww:if test="/projectKeyRenameAllowed == 'true'">
                <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.EditProject.textFieldWithSubTemplate'">
                    <ui:param name="'id'">project-edit-key</ui:param>
                    <ui:param name="'name'">key</ui:param>
                    <ui:param name="'isRequired'">true</ui:param>
                    <ww:if test="/keyEdited == 'false'">
                        <ui:param name="'isDisabled'">true</ui:param>
                    </ww:if>
                    <ui:param name="'maxLength'"><ww:property value="/maxKeyLength" escape="false"/></ui:param>
                    <ui:param name="'labelContent'"><ww:property value="/text('common.concepts.key')" escape="false"/></ui:param>
                    <ui:param name="'value'"><ww:property value="/key" escape="false"/></ui:param>
                    <ui:param name="'errorTexts'"><ww:property value="/errors['key']" escape="false"/></ui:param>
                    <ui:param name="'subTemplateHtml'">
                        <a href="#" id="edit-project-key-toggle" class="aui-button aui-button-link">
                            <ww:if test="/keyEdited == 'true'">
                                <ww:property value="/text('admin.projects.edit.project.link.toggle.revert')" />
                            </ww:if>
                            <ww:else>
                                <ww:property value="/text('admin.projects.edit.project.link.toggle.edit.key')" />
                            </ww:else>
                        </a>
                    </ui:param>
                </ui:soy>
                <input type="hidden" id="edit-project-original-key" name="originalKey" value="<ww:property value="/originalKey"/>" />
                <input type="hidden" id="edit-project-key-edited" name="keyEdited" value="<ww:property value="/keyEdited"/>" />
            </ww:if>

            <page:applyDecorator name="auifieldgroup">
                <aui:textfield label="/text('common.concepts.url')" name="'url'" size="'50'" maxlength="255" theme="'aui'"/>
            </page:applyDecorator>

            <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Avatar.picker'">
                <ui:param name="'labelContent'"><ww:property value="/text('common.concepts.project.avatar')"/></ui:param>
                <ui:param name="'defaultId'"><ww:property value="/defaultAvatar"/></ui:param>
                <ui:param name="'avatarId'"><ww:property value="/avatarId"/></ui:param>
                <ui:param name="'src'"><ww:property value="/avatarUrl"/></ui:param>
                <ui:param name="'size'">large</ui:param>
                <ui:param name="'isProject'">true</ui:param>
                <ui:param name="'title'"><ww:text name="'admin.projects.edit.avatar.click.to.edit'"/></ui:param>
                <ui:param name="'avatarOwnerId'"><ww:property value="/pid"/></ui:param>
                <ui:param name="'avatarOwnerKey'"><ww:property value="/originalKey"/></ui:param>
                <ui:param name="'mandatory'">true</ui:param>
                <ui:param name="'fieldId'">avatarId</ui:param>
                <ui:param name="'avatarType'">project</ui:param>
            </ui:soy>

            <page:applyDecorator name="auifieldgroup">
                <page:param name="description">
                    <ww:text name="/projectDescriptionRenderer/descriptionI18nKey"/>
                </page:param>
                <label><ww:text name="'common.words.description'"/></label>
                <ww:property value="/projectDescriptionEditHtml" escape="false"/>
            </page:applyDecorator>

            <aui:component name="'pid'" template="hidden.jsp" theme="'aui'"/>
            <aui:component name="'avatarId'" template="hidden.jsp" theme="'aui'"/>
        </div>

    </ww:else>

</page:applyDecorator>
</body>
</html>
