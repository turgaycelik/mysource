<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.addproject.add.a.new.project'"/></title>
    <meta name="admin.active.section" content="admin_project_menu/project_section"/>
    <meta name="admin.active.tab" content="view_projects"/>
</head>
<body>
    <page:applyDecorator id="add-project" name="auiform">
        <page:param name="action">AddProject.jspa</page:param>
        <page:param name="submitButtonName">Add</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.forms.add'"/></page:param>
        <page:param name="cancelLinkURI"><ww:url value="'/secure/project/ViewProjects.jspa'" atltoken="false" /></page:param>

        <page:param name="showHint"><ww:property value="true" /></page:param>
        <page:param name="hint">
            <ww:text name="'admin.addproject.project.import.link'">
                <ww:param name="'value0'"><a href="<ww:url atltoken="false" value="'/secure/admin/views/ExternalImport1.jspa'"/>"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </page:param>
        <page:param name="hintTooltip"><ww:text name="'admin.addproject.project.import.link.tip'"/></page:param>
        <page:param name="hideHintLabel">true</page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.addproject.add.a.new.project'"/></aui:param>
        </aui:component>

        <aui:component name="'nextAction'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'src'" template="hidden.jsp" theme="'aui'" />

        <div id="add-project-fields" class="aui-group">
            <div class="aui-item">

                <page:applyDecorator name="auifieldset">
                    <page:applyDecorator name="auifieldgroup">
                        <page:param name="description">
                            <ww:text name="'admin.addproject.name.description'">
                                <ww:param name="'value0'"><ww:property value="/maxNameLength"/></ww:param>
                            </ww:text>
                        </page:param>
                        <aui:textfield label="text('common.words.name')" name="'name'" id="'name'" theme="'aui'">
                            <aui:param name="'mandatory'" value="true" />
                        </aui:textfield>
                    </page:applyDecorator>

                    <page:applyDecorator name="auifieldgroup">
                        <page:param name="description">
                            <ww:text name="'admin.addproject.key.description.short'">
                                <ww:param name="'value0'"><ww:property value="/maxKeyLength"/></ww:param>
                            </ww:text>

                            <ww:property value="projectKeyDescription" escape="false" />
                        </page:param>
                        <aui:textfield label="text('common.concepts.key')" name="'key'" maxlength="/maxKeyLength" id="'key'" theme="'aui'">
                            <aui:param name="'iconCssClass'">icon-help</aui:param>
                            <aui:param name="'iconUri'">#</aui:param>
                            <aui:param name="'iconText'"><ww:text name="'admin.addproject.key.help'" /></aui:param>
                            <aui:param name="'iconTitle'"><ww:text name="'admin.addproject.key.help'" /></aui:param>
                            <aui:param name="'mandatory'" value="true" />
                        </aui:textfield>
                    </page:applyDecorator>

                    <aui:component name="'keyEdited'" template="hidden.jsp" theme="'aui'" />

                    <ww:if test="/showProjectSample == true">
                        <page:applyDecorator name="auifieldgroup">
                            <aui:component label="text('common.words.avatar')" name="'avatarId'" id="'avatarId'" template="inlineAvatarPicker.jsp" theme="'aui'">
                                <aui:param name="'defaultId'"><ww:property value="/defaultAvatarId"/></aui:param>
                                <aui:param name="'src'"><ww:property value="/defaultAvatarUrl"/></aui:param>
                                <aui:param name="'size'">large</aui:param>
                                <aui:param name="'isProject'">true</aui:param>
                                <aui:param name="'title'"><ww:text name="'admin.projects.edit.avatar.click.to.edit'"/></aui:param>
                            </aui:component>
                        </page:applyDecorator>
                    </ww:if>

                    <ww:if test="/shouldShowLead == true">
                        <page:applyDecorator name="auifieldgroup">
                            <page:param name="id">lead-picker</page:param>
                            <page:param name="description"><ww:text name="'admin.addproject.project.lead.description'"/></page:param>
                            <aui:component label="text('common.concepts.projectlead')" name="'lead'" id="'lead'" template="singleSelectUserPicker.jsp" theme="'aui'">
                                <aui:param name="'inputText'" value="/leadError" />
                                <aui:param name="'userName'" value="/lead"/>
                                <aui:param name="'userFullName'" value="/leadUserObj/displayName"/>
                                <aui:param name="'userAvatar'" value="/leadUserAvatarUrl"/>
                                <aui:param name="'mandatory'" value="'true'" />
                                <aui:param name="'disabled'" value="/userPickerDisabled" />
                            </aui:component>
                        </page:applyDecorator>
                    </ww:if>
                    <ww:else>
                        <aui:component label="'lead'" name="'lead'" template="hidden.jsp" theme="'aui'">
                            <aui:param name="'divId'" value="'lead-picker'" />
                        </aui:component>
                    </ww:else>

                    <aui:component label="'permissionScheme'" name="'permissionScheme'" template="hidden.jsp" theme="'aui'"/>

                    <ww:if test="/inlineDialogMode == false">
                        <ww:text name="'admin.addproject.project.import.link'">
                            <ww:param name="'value0'"><a href="<ww:url atltoken="false" value="'/secure/admin/views/ExternalImport1.jspa'"/>"></ww:param>
                            <ww:param name="'value1'"></a></ww:param>
                        </ww:text>
                    </ww:if>

                </page:applyDecorator>
            </div>
            <ww:if test="/showProjectSample == true">
                <div id="sample-project-container" class="aui-item">
                    <%-- content gets added via soy template call in ProjectSample.js --%>
                </div>
            </ww:if>
        </div>
    </page:applyDecorator>
</body>
</html>
