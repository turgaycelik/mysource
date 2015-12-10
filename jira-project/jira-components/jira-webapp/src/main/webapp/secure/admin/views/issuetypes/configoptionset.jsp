<%@ page import="com.atlassian.jira.ComponentManager"%>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<ww:if test="/schemeId != null">
<ww:property id="command" value="'Modify'" />
</ww:if>
<ww:else>
<ww:property id="command" value="'Add'" />
</ww:else>

<%-- The page is used for the manageable option object --%>
<ww:property value="/manageableOption" >
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/issue_types_section"/>
    <meta name="admin.active.tab" content="issue_type_schemes"/>
	<title>
            <ww:property value="@command" /> <ww:text name="'admin.projects.issue.type.scheme'" />
    </title>
<%
    WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
    webResourceManager.requireResource("jira.webresources:editissuetypescheme");
%>

</head>
<body>
<header class="aui-page-header">
    <div class="aui-page-header-inner">
        <div class="aui-page-header-main">
            <h2>
                <ww:property value="@command" /> <ww:text name="'admin.projects.issue.type.scheme'" />
                <ww:if test="/schemeId != null">&mdash; <ww:property value="/configScheme/name"/></ww:if>
            </h2>
            <ui:component theme="'raw'" template="projectshare.jsp" name="'name'" value="'value'" label="'label'">
                <ui:param name="'projects'" value="/usedIn"/>
            </ui:component>
        </div>
        <ww:if test="/allowEditOptions == true">
        <div class="aui-page-header-actions">
            <nav class="aui-toolbar">
                <div class="toolbar-split toolbar-split-right">
                    <ul class="toolbar-group">
                        <li class="toolbar-item">
                            <a id="add-new-issue-type-to-scheme" class="toolbar-trigger" href="AddNewIssueTypeToScheme!input.jspa">
                                <span class="icon jira-icon-add"></span>&nbsp;<ww:text name="'admin.issuesettings.issuetypes.add.new.button.label'"/>
                            </a>
                        </li>
                    </ul>
                </div>
            </nav>
        </div>
        </ww:if>
    </div>
</header>

<ww:if test="/configScheme/global == true">
    <aui:component template="auimessage.jsp" theme="'aui'" name="'name'" value="'value'" label="'label'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'messageHtml'">
            <p>
                <ww:text name="'admin.issuesettings.note.editing.global.scheme'" />
            </p>
        </aui:param>
    </aui:component>
</ww:if>

<ww:if test="/projectId && /schemeId == null">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'">
            <p>
                <ww:text name="'admin.issuesettings.this.scheme.will.be.automatically.selected'">
                    <ww:param name="'value0'"><strong><ww:property value="/project/string('name')" /></strong></ww:param>
                </ww:text>
            </p>
        </aui:param>
    </aui:component>
</ww:if>

<fieldset class="hidden parameters">
    <input type="hidden" title="fieldId" value="<ww:property value="fieldId"/>"/>
    <input type="hidden" title="resetUrl" value="<ww:url value="'ConfigureOptionSchemes!default.jspa'" atltoken="false"><ww:param name="'fieldId'" value="/fieldId" /><ww:param name="'schemeId'" value="/schemeId" /><ww:param name="'returnUrl'" value="/returnUrl" /></ww:url>"/>
    <input type="hidden" title="allowEditOptions" value="<ww:property value="/allowEditOptions"/>"/>
</fieldset>

<page:applyDecorator name="auiform">
    <page:param name="useCustomButtons">true</page:param>
    <page:param name="method">post</page:param>
    <page:param name="id">edit-issue-type-scheme-form</page:param>
    <page:param name="action">ConfigureOptionSchemes.jspa</page:param>

    <aui:component template="multihidden.jsp" theme="'aui'">
        <aui:param name="'fields'">schemeId,fieldId,projectId</aui:param>
    </aui:component>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield theme="'aui'" label="text('admin.issuesettings.scheme.name')" name="'name'" mandatory="'true'" id="'issue-type-scheme-name'"/>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield theme="'aui'" label="text('common.words.description')" name="'description'" size="'long'" id="'issue-type-scheme-description'"/>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:select theme="'aui'" label="text('admin.issuesettings.default.issue.type')" name="'defaultOption'" list="/allOptions"
                    listKey="'id'" listValue="'name'" id="'default-issue-type-select'">
            <aui:param name="'defaultOptionText'"><ww:text name="'common.words.none'"/></aui:param>
            <aui:param name="'defaultOptionValue'" value="''"/>
        </aui:select>
    </page:applyDecorator>

    <div>
        <ww:text name="'admin.issuesettings.change.order.by.drag.drop'">
            <ww:param name="'value0'"><strong></ww:param>
            <ww:param name="'value1'"></strong></ww:param>
        </ww:text>
        <ww:if test="/allowEditOptions == true">
            <ww:text name="'admin.issuesettings.similarly.drag.drop.to.remove'">
                <ww:param name="'value0'"><strong></ww:param>
                <ww:param name="'value1'"></strong></ww:param>
            </ww:text>
        </ww:if>
    </div>

    <div id="optionsContainer" class="ab-drag-wrap">
        
        <div id="left" class="ab-drag-container">
            <h4>
                <ww:text name="'admin.issuesettings.issuetypes.for.current.scheme'">
                    <ww:param name="'value0'"><ww:property value="title" /></ww:param>
                </ww:text>
            </h4>
            <div class="ab-items">
                <ww:if test="/allowEditOptions == true">
                    <a class="ab-all" href="#" id="selectedOptionsRemoveAll">
                        <ww:text name="'admin.issuesettings.remove.all'"/>
                    </a>
                </ww:if>
                <ul id="selectedOptions" class="grabable" style="min-height:<ww:property value="/maxHeight" />px;">
                    <ww:iterator value="/optionsForScheme" status="'status'">
                        <li id="selectedOptions_<ww:property value="./id" />">
                            <span class="icon icon-vgrabber"></span>
                            <img class="icon jira-icon-image" src="<ww:url value="./imagePath" />" alt="" />
                            <span class="issue-type-name"><ww:property value="./name" /></span><ww:if test="./subTask == true"> <span class="smallgrey">(<ww:text name="'admin.issuesettings.sub.task'"/>)</span></ww:if>
                        </li>
                    </ww:iterator>
                </ul>
            </div>
        </div>

        <ww:if test="/allowEditOptions == true">
        <div id="right" class="ab-drag-container">
            <h4>
                <ww:text name="'admin.issuesettings.available.issue.types'">
                    <ww:param name="'value0'"><ww:property value="title" /></ww:param>
                </ww:text>
            </h4>
            <div class="ab-items">
                <a class="ab-all" href="#" id="selectedOptionsAddAll">
                    <ww:text name="'admin.issuesettings.add.all'"/>
                </a>
                <ul id="availableOptions" class="grabable" style="min-height:<ww:property value="/maxHeight" />px;">
                    <ww:iterator value="/availableOptions" status="'status'">
                        <li id="availableOptions_<ww:property value="./id" />">
                            <span class="icon icon-vgrabber"></span>
                            <img class="icon jira-icon-image" src="<ww:url value="./imagePath" />" alt="" />
                            <span class="issue-type-name"><ww:property value="./name" /></span><ww:if test="./subTask == true"> <span class="smallgrey">(<ww:text name="'admin.issuesettings.sub.task'"/>)</span></ww:if>
                        </li>
                    </ww:iterator>
                </ul>
            </div>
        </div>
        </ww:if>
    </div>

    <page:applyDecorator name="auifieldgroup">
        <page:param name="type">buttons-container</page:param>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">buttons</page:param>
            <aui:component template="formSubmit.jsp" theme="'aui'">
                <aui:param name="'submitButtonName'">save</aui:param>
                <aui:param name="'submitButtonText'"><ww:text name="'common.words.save'"/></aui:param>
                <aui:param name="'id'">submitSave</aui:param>
            </aui:component>
            <ww:if test="/schemeId">
                <aui:component name="'reset'" template="formButton.jsp" theme="'aui'">
                    <aui:param name="'id'">submitReset</aui:param>
                    <aui:param name="'text'"><ww:text name="'admin.common.words.reset'"/></aui:param>
                </aui:component>
            </ww:if>
            <aui:component template="formCancel.jsp" theme="'aui'">
                <aui:param name="'cancelLinkURI'">ManageIssueTypeSchemes!default.jspa</aui:param>
            </aui:component>
        </page:applyDecorator>
    </page:applyDecorator>

</page:applyDecorator>

<ui:component theme="'raw'" template="projectsharedialog.jsp" name="'name'" value="'value'" label="'label'">
    <ui:param name="'projects'" value="/usedIn"/>
    <ui:param name="'title'"><ww:text name="'admin.project.shared.list.heading.scheme'"/></ui:param>
</ui:component>

</body>
</html>
</ww:property>
