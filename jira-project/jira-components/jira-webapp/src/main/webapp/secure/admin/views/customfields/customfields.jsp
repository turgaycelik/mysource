<%@ page import="com.atlassian.jira.component.ComponentAccessor" %>
<%@ page import="com.atlassian.plugin.webresource.UrlMode" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="view_custom_fields"/>
	<title><ww:text name="'admin.menu.issuefields.custom.fields'"/></title>
</head>
<body>
    <header class="aui-page-header">
        <div class="aui-page-header-inner">
            <div class="aui-page-header-main">
                <h2><ww:text name="'admin.menu.issuefields.custom.fields'"/></h2>
            </div>
            <div class="aui-page-header-actions">
                <div class="aui-buttons">
                    <ww:if test="/customFieldTypesExist == true">
                            <a id="add_custom_fields" class="aui-button" href="<ww:url value="'/secure/admin/CreateCustomField!default.jspa'" atltoken="false" />">
                                <span class="icon jira-icon-add"></span>
                                <ww:text name="'admin.issuefields.customfields.add.custom.field'"/>
                            </a>
                    </ww:if>
                    <ww:if test="/onDemand == false">
                            <a id="custom_fields_marketplace" class="aui-button" href="<ww:url value="'/plugins/servlet/upm/marketplace/popular?category=Custom+Fields&source=custom_fields_view'" atltoken="false" />">
                                <img class="icon jira-icon-image" src="<%= ComponentAccessor.getWebResourceUrlProvider().getStaticResourcePrefix(UrlMode.AUTO) %>/images/icons/marketplace-ico.png" alt="" />
                                <ww:text name="'admin.issuefields.customfields.extentions.button'"/>
                            </a>
                    </ww:if>
                </div>
                <aui:component name="'customfields'" template="help.jsp" theme="'aui'" />
            </div>
        </div>
    </header>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'contentHtml'">
            <ww:if test="/customFieldTypesExist == false">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">warning</aui:param>
                    <aui:param name="'messageHtml'">
                        <p><ww:text name="'admin.issuefields.customfields.no.plugins.configured'"/></p>
                    </aui:param>
                </aui:component>
            </ww:if>
            <ww:if test="customFields/size > 0">
                <table id="custom-fields" class="aui aui-table-rowhover">
                    <thead>
                        <tr>
                            <th>
                                <ww:text name="'common.words.name'"/>
                            </th>
                            <th>
                                <ww:text name="'admin.common.words.type'"/>
                            </th>
                            <th>
                                <ww:text name="'admin.issuefields.available.contexts'"/>
                            </th>
                            <th>
                                <ww:text name="'admin.issuefields.screens'"/>
                            </th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        <ww:iterator value="customFields" status="'status'">
                        <tr <ww:if test="@status/modulus(2) != 1">class="rowAlternate"</ww:if>>
                            <td id="custom-fields-<ww:property value="./id" />-name">
                                <strong><ww:property value="untranslatedName"/></strong>
                                <ww:if test="/fieldLocked(.) == true">
                                    <span class="aui-lozenge status-locked" title="<ww:text name="/managedFieldDescriptionKey(.)"/>"><ww:text name="'admin.managed.configuration.items.locked'"/></span>
                                </ww:if>
                                <ww:elseIf test="/fieldManaged(.) == true">
                                    <span class="aui-lozenge status-managed" title="<ww:text name="/managedFieldDescriptionKey(.)"/>"><ww:text name="'admin.managed.configuration.items.managed'"/></span>
                                </ww:elseIf>
                                <div class="secondary-text description"><ww:property value="untranslatedDescriptionProperty/viewHtml" escape="false"/></div>
                            </td>
                            <td id="custom-fields-<ww:property value="./id" />-type" class="nowrap">
                              <ww:property value='customFieldType/name'/>
                            </td>
                            <td>
                              <jsp:include page="contexts.jsp" flush="true"/>
                            </td>
                            <td>
                                <ww:if test="/fieldScreenTabs(.)/empty == false">
                                    <ul>
                                        <ww:iterator value="/fieldScreenTabs(.)" status="'tabStatus'">
                                            <li>
                                                <ww:if test="./fieldScreen/tabs/size > 1">
                                                    <a href="<ww:url value="'/secure/admin/ConfigureFieldScreen.jspa'" atltoken="false"><ww:param name="'id'" value="./fieldScreen/id" /><ww:param name="'tabPosition'" value="./position" /></ww:url>"><ww:property value="./fieldScreen/name" /></a> (<ww:property value="./name" />)
                                                </ww:if>
                                                <ww:else>
                                                    <a href="<ww:url value="'/secure/admin/ConfigureFieldScreen.jspa'" atltoken="false"><ww:param name="'id'" value="./fieldScreen/id" /></ww:url>"><ww:property value="./fieldScreen/name" /></a>
                                                </ww:else>
                                            </li>
                                        </ww:iterator>
                                    </ul>
                                </ww:if>
                            </td>
                            <td class="cell-type-actions">
                                <div class="aui-dd-parent">
                                    <a class="aui-dd-link cog-dd js-default-dropdown" href="#"><span></span></a>
                                    <div class="aui-list hidden">
                                        <ul class="aui-list-section aui-first aui-last">
                                            <ww:if test="/fieldLocked(.) == false">
                                                <li class="aui-list-item"><a class="aui-list-item-link" id="config_<ww:property value="./id" />" href="<ww:url value="'ConfigureCustomField!default.jspa'" atltoken="false"><ww:param name="'customFieldId'" value="genericValue/long('id')" /></ww:url>"><ww:text name="'admin.common.words.configure'"/></a></li>
                                            </ww:if>
                                            <ww:else>
                                                <li class="aui-list-item"><a class="aui-list-item-link" id="config_<ww:property value="./id" />" href="<ww:url value="'ConfigureCustomField!default.jspa'" atltoken="false"><ww:param name="'customFieldId'" value="genericValue/long('id')" /></ww:url>"><ww:text name="'common.words.view'"/></a></li>
                                            </ww:else>
                                            <ww:if test="/fieldLocked(.) == false">
                                                <li class="aui-list-item"><a class="aui-list-item-link" id="edit_<ww:property value="./name" />" href="<ww:url value="'EditCustomField!default.jspa'" atltoken="false"><ww:param name="'id'" value="genericValue/long('id')" /></ww:url>"><ww:text name="'common.words.edit'"/></a></li>
                                                <ww:if test="/multiLingual() == true">
                                                    <li class="aui-list-item"><a class="aui-list-item-link" id="translate_<ww:property value="./id" />" href="<ww:url value="'TranslateCustomField!default.jspa'" atltoken="false"><ww:param name="'id'" value="genericValue/long('id')" /></ww:url>"><ww:text name="'admin.issuefields.customfields.translate'"/></a></li>
                                                </ww:if>
                                            </ww:if>
                                            <li class="aui-list-item"><a class="aui-list-item-link" id="associate_<ww:property value="./id" />" href="<ww:url value="'AssociateFieldToScreens!default.jspa'" atltoken="false"><ww:param name="'fieldId'" value="id" /><ww:param name="'returnUrl'" value="'ViewCustomFields.jspa'" /></ww:url>"><ww:text name="'admin.issuefields.screens'"/></a></li>
                                            <ww:if test="/fieldLocked(.) == false">
                                                <li class="aui-list-item"><a class="aui-list-item-link" id="del_<ww:property value="./id" />" href="<ww:url value="'DeleteCustomField!default.jspa'" atltoken="false"><ww:param name="'id'" value="genericValue/long('id')" /></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                                            </ww:if>
                                        </ul>
                                    </div>
                                </div>
                            </td>
                        </tr>
                        </ww:iterator>
                    </tbody>
                </table>
            </ww:if>
            <ww:else>
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'messageHtml'"><ww:text name="'admin.issuefields.customfields.no.custom.fields.defined'"/></aui:param>
                </aui:component>
            </ww:else>
        </aui:param>
    </aui:component>
</body>
</html>
