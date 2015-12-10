<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib prefix="jira" uri="jiratags" %>
<html>
<head>
	<title><ww:text name="'admin.services.services'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="services"/>
    <jira:web-resource-require modules="jira.webresources:viewservices" />
</head>
<body>
    <ww:if test="services/empty == false">
        <page:applyDecorator name="jirapanel">
            <page:param name="title"><ww:text name="'admin.services.services'"/></page:param>
            <page:param name="width">100%</page:param>
            <page:param name="helpURL">services</page:param>
        </page:applyDecorator>
        <table class="aui aui-table-rowhover" id="tbl_services">
            <thead>
                <tr>
                    <th></th>
                    <th>
                        <ww:text name="'admin.common.phrases.name.class'"/>
                    </th>
                    <th>
                        <ww:text name="'admin.common.words.properties'"/>
                    </th>
                    <th class="nowrap">
                        <ww:text name="'admin.services.delay.mins'"/>
                    </th>
                    <th></th>
                </tr>
            </thead>
            <tbody>
            <ww:iterator value="services" status="'status'">
                <tr id="service-<ww:property value="./id"/>" <ww:if test="./usable == false">class="disabled"</ww:if> >
                    <td class="cell-type-icon">
                        <ww:if test="handlerUsingObsoleteSettings(.) == true">
                            <span class="svg-icon warning size-14 obsolete-settings-hover"></span>
                        </ww:if>
                    </td>
                    <td class="cell-type-key">
                        <strong id="service-name-<ww:property value="./id"/>">
                            <ww:property value="./name"/>
                        </strong>
                        <div id="service-class-<ww:property value="./id"/>" class="description secondary-text">
                            <ww:property value="./serviceClass"/>
                        </div>
                    </td>
                    <td class="cell-type-value">
                        <%-- get the property set for this service, and then get all the keys where the propertyset is of type String ('5') --%>
                        <ul>
                            <ww:iterator value="/propertyMap(.)/keySet">
                                <li><strong><ww:property value="." />:</strong> <ww:property value="/text(/propertyMap(..)/(.))" /></li>
                            </ww:iterator>
                        </ul>
                    </td>
                    <td><ww:property value="delayInMins(.)"/></td>
                    <td <ww:if test="./usable == false">class="disabled"</ww:if>>
                        <ul class="operations-list">
                            <ww:if test="./usable == true">
                                <ww:if test="editable(.) == true">
                                    <li><a id="edit_<ww:property value="id"/>" href="<ww:url page="EditService!default.jspa"><ww:param name="'id'" value="id"/></ww:url>"><ww:text name="'common.words.edit'"/></a></li>
                                </ww:if>
                                <ww:if test="./internal == false">
                                    <li><a id="del_<ww:property value="id"/>" href="<ww:url page="ViewServices.jspa"><ww:param name="'delete'" value="id"/></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                                </ww:if>
                            </ww:if>
                            <ww:else>
                                <li><a id="del_<ww:property value="id"/>" href="<ww:url page="ViewServices.jspa"><ww:param name="'delete'" value="id"/></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                            </ww:else>
                        </ul>
                    </td>
                </tr>
            </ww:iterator>
            </tbody>
        </table>
    </ww:if>
    <div id="obsolete-settings-warning"></div>
    <aui:component id="obsolete-settings-message" template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'cssClass'">hidden</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'jmp.viewservices.obsolete.options.handler'"/></p>
        </aui:param>
    </aui:component>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'contentHtml'">
            <ww:if test="systemAdministrator == true">
                <page:applyDecorator name="jiraform">
                    <page:param name="action">ViewServices.jspa</page:param>
                    <page:param name="submitId">addservice_submit</page:param>
                    <page:param name="submitName"><ww:text name="'admin.services.add.service'"/></page:param>
                    <page:param name="title"><ww:text name="'admin.services.add.service'"/></page:param>
                    <page:param name="width">100%</page:param>
                    <page:param name="helpURL">services</page:param>
                    <page:param name="helpURLFragment">#Registering+a+Service</page:param>
                <%--	<page:param name="helpDescription">with Services</page:param>--%>
                    <page:param name="description">
                        <ww:text name="'admin.services.add.service.instruction'"/>
                        <ww:text name="'admin.services.add.service.mail.handlers'">
                                <ww:param name="'value0'"><a id="mail-goto-link" href="IncomingMailServers.jspa"></ww:param>
                                <ww:param name="'value1'"></a></ww:param>
                        </ww:text>
                    </page:param>

                    <ui:textfield label="text('common.words.name')" name="'name'" size="'30'">
                        <ui:param name="'cssId'">serviceName</ui:param>
                    </ui:textfield>

                    <ui:textfield label="text('admin.services.class')" name="'clazz'" size="'60'">
                        <ui:param name="'cssId'">serviceClass</ui:param>
                        <ui:param name="'description'">
                        <ww:if test="inBuiltServiceTypes/empty == false">
                            <img id="builtinServicesArrow" src="<%= request.getContextPath() %>/images/icons/navigate_right.gif" width=8 height=8 border=0>
                            <a href="#" id="show-services"><ww:text name="'admin.services.built.in.services'"/></a>

                              <div id="builtinServices" style="display: none">
                                  <ul>
                                      <ww:iterator value="inBuiltServiceTypes">
                                          <li><a href="#" class="set-service" data-service-type="<ww:property value="./type/name"/>"><ww:text name="./i18nKey"/></a></li>
                                      </ww:iterator>
                                  </ul>
                              </div>
                        </ww:if>
                        </ui:param>
                    </ui:textfield>
                    <ui:textfield label="text('admin.services.delay')" name="'delay'" size="'30'">
                        <ui:param name="'description'"><ww:text name="'admin.services.delay.description'"/></ui:param>
                    </ui:textfield>
                </page:applyDecorator>
            </ww:if>
            <ww:else>
                <page:applyDecorator name="jiraform">
                    <page:param name="title"><ww:text name="'admin.services.add.service'"/></page:param>
                    <page:param name="width">100%</page:param>
                    <page:param name="helpURL">services</page:param>
                    <page:param name="helpURLFragment">#Registering+a+Service</page:param>
                    <page:param name="description">
                        <ww:text name="'admin.services.add.service.mail.handlers'">
                                <ww:param name="'value0'"><a id="mail-goto-link" href="IncomingMailServers.jspa"></ww:param>
                                <ww:param name="'value1'"></a></ww:param>
                        </ww:text>
                    </page:param>
                </page:applyDecorator>
            </ww:else>
        </aui:param>
    </aui:component>
</body>
</html>
