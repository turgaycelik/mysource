<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'userpicker.title'" /></title>
    <%@ include file="/includes/js/multipickerutils.jsp" %>
</head>
<body>
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'mainContent'">
            <h1><ww:text name="'userpicker.title'" /></h1>
        </ui:param>
    </ui:soy>

    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                <ui:param name="'content'">

            <ww:if test="permission == true">
                <script type="text/javascript">
                    function select(value)
                    {
                        opener.AJS.$('#'+AJS.$.trim(AJS.$("#openElement").text())).val(value).change();
                        window.close();
                    }
                </script>
                <page:applyDecorator id="user-picker-popup" name="auiform">
                    <page:param name="action">UserPickerBrowser.jspa</page:param>
                    <page:param name="method">post</page:param>
                    <page:param name="cssClass">top-label ajs-dirty-warning-exempt</page:param>
                    <page:param name="submitButtonText"><ww:text name="'userpicker.filter'"/></page:param>
                    <ww:property value="filter">
                        <div class="aui-group">
                            <div class="aui-item">
                                <page:applyDecorator name="auifieldgroup">
                                    <aui:textfield label="text('userpicker.fullnamecontains')" maxlength="255" id="'nameFilter'" name="'nameFilter'" theme="'aui'">
                                        <aui:param name="'cssClass'">full-width-field</aui:param>
                                    </aui:textfield>
                                </page:applyDecorator>
                            </div>
                            <div class="aui-item">
                                <ww:if test="/emailColumnVisible == true">
                                    <page:applyDecorator name="auifieldgroup">
                                        <aui:textfield label="text('userpicker.emailcontains')" maxlength="255" id="'emailFilter'" name="'emailFilter'" theme="'aui'">
                                            <aui:param name="'cssClass'">full-width-field</aui:param>
                                        </aui:textfield>
                                    </page:applyDecorator>
                                </ww:if>
                            </div>
                        </div>
                        <div class="aui-group">
                            <div class="aui-item">
                                <page:applyDecorator name="auifieldgroup">
                                    <aui:select label="text('userpicker.ingroup')" id="'groups'" name="'group'" list="/groups" listKey="'name'" listValue="'name'" theme="'aui'">
                                        <aui:param name="'cssClass'">full-width-field</aui:param>
                                        <aui:param name="'defaultOptionText'" value="text('common.filters.any')" />
                                        <aui:param name="'defaultOptionValue'" value="''" />
                                    </aui:select>
                                </page:applyDecorator>
                            </div>
                            <div class="aui-item">
                                <page:applyDecorator name="auifieldgroup">
                                    <aui:select label="text('userpicker.usersperpage')" id="'usersPerPage'" name="'max'" list="/maxValues" listKey="'.'" listValue="'.'" theme="'aui'">
                                        <aui:param name="'cssClass'">full-width-field</aui:param>
                                    </aui:select>
                                </page:applyDecorator>
                            </div>
                        </div>
                        <aui:component template="hidden.jsp" theme="'aui'" name="'formName'" />
                        <aui:component template="hidden.jsp" theme="'aui'" name="'element'" />
                        <aui:component template="hidden.jsp" theme="'aui'" name="'multiSelect'" />
                        <aui:component template="hidden.jsp" theme="'aui'" name="'start'" />
                        <aui:component template="hidden.jsp" theme="'aui'" name="'previouslySelected'" />
                        <ww:if test="fieldConfigId">
                            <aui:component template="hidden.jsp" theme="'aui'" name="'fieldConfigId'" />
                        </ww:if>
                        <ww:if test="projectIds">
                            <ww:iterator value="projectIds">
                                <aui:component template="hidden.jsp" theme="'aui'" name="'projectId'" value="." />
                            </ww:iterator>
                        </ww:if>
                    </ww:property>
                </page:applyDecorator>
                <form class="selectorform" name="selectorform">
                    <div class="aui-group count-pagination">
                        <div class="results-count aui-item">
                            <ww:text name="'userpicker.displayingusers'" value0="niceStart" value1="niceEnd" value2="users/size" />
                        </div>
                        <div class="pagination aui-item">
                            <jsp:include page="userpicker_navigation.jsp"/>
                        </div>
                    </div>
                    <table class="aui aui-table-rowhover">
                        <thead>
                            <tr>
                                <th class="hidden"></th>
                            <ww:if test="multiSelect == true">
                                <th width="1%"><input type="checkbox" name="all" onClick="setCheckboxes()"></th>
                            </ww:if>
                                <th><ww:text name="'common.words.username'" /></th>
                                <th><ww:text name="'common.words.fullname'" /></th>
                            <ww:if test="/emailColumnVisible == true">
                                <th><ww:text name="'common.words.email'" /></th>
                            </ww:if>
                            </tr>
                        </thead>
                        <tbody>
                        <ww:iterator value="currentPage" status="'status'">
                            <tr data-row-for="<ww:property value="name"/>" id="username_row_<ww:property value="@status/index"/>" title="<ww:text name="'picker.click.to.select'"><ww:param name="'value0'"><ww:property value="displayName"/></ww:param></ww:text>" <ww:if test="/multiSelect == false">onclick="select(getElementById('username_<ww:property value="@status/index"/>').getAttribute('value'));"</ww:if> >
                                <td class="hidden" data-cell-type="user-info-hidden">
                                    <div id="username_<ww:property value="@status/index"/>" value="<ww:property value="name"/>" style="visibility: hidden"></div>
                                </td>
                            <ww:if test="/multiSelect == true">
                                <td data-cell-type="user-select"><input data-user-select="true" <ww:if test="wasPreviouslySelected(.) == true"> checked="checked"</ww:if> type=checkbox name="userchecks" value="<ww:property value="name"/>" id="user_<ww:property value="@status/index"/>" onclick="processCBClick(event, this);"/></td>
                                <td data-cell-type="name" class="user-name" onclick="toggleCheckBox(event, 'user_<ww:property value="@status/index"/>')"><ww:property value="name"/></td>
                                <td data-cell-type="fullname" onclick="toggleCheckBox(event, 'user_<ww:property value="@status/index"/>')"><span id="user_fullname_<ww:property value="@status/index"/>"><ww:property value="displayName"/></span></td>
                                <ww:if test="/emailColumnVisible == true">
                                    <td data-cell-type="email" class="cell-type-email" onclick="toggleCheckBox(event, 'user_<ww:property value="@status/index"/>')"><ww:property value="displayEmail(emailAddress)"/></td>
                                </ww:if>
                            </ww:if>
                            <ww:else>
                                <td data-cell-type="name" class="user-name"><ww:property value="name"/></td>
                                <td data-cell-type="fullname" ><ww:property value="displayName"/></td>
                                <ww:if test="/emailColumnVisible == true">
                                    <td data-cell-type="email" class="cell-type-email"><ww:property value="displayEmail(emailAddress)"/></td>
                                </ww:if>
                            </ww:else>
                            </tr>
                        </ww:iterator>
                        </tbody>
                    </table>
                    <ww:if test="/multiSelect == true">
                        <div class="buttons-container">
                            <input id="multiselect-submit" class="aui-button" type="submit" value="<ww:text name="'common.words.select'"/>" onclick="selectUsers(AJS.$.trim(AJS.$('#openElement').text()), 'input[data-user-select]')">
                        </div>
                    </ww:if>
                </form>
                <ul id="params" style="display:none">
                    <li id="openElement"><ww:property value="$element" /></li>
                </ul>
            </ww:if>
            <ww:else>
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">warning</aui:param>
                    <aui:param name="'messageHtml'">
                        <p><ww:text name="'userpicker.nopermissions'" /></p>
                    </aui:param>
                </aui:component>
            </ww:else>

                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</body>
</html>
