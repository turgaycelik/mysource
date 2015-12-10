<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'grouppicker.title'" /></title>
    <%@ include file="/includes/js/multipickerutils.jsp" %>
</head>
<body>
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'mainContent'">
            <h1><ww:text name="'grouppicker.title'" /></h1>
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
                        opener.AJS.$('#'+AJS.$.trim(AJS.$("#openElement").text())).val(value);
                        window.close();
                    }
                </script>
                <page:applyDecorator id="group-picker-popup" name="auiform">
                    <page:param name="action">GroupPickerBrowser.jspa</page:param>
                    <page:param name="method">post</page:param>
                    <page:param name="cssClass">top-label ajs-dirty-warning-exempt</page:param>
                    <page:param name="submitButtonText"><ww:text name="'userpicker.filter'"/></page:param>
                    <ww:property value="filter">
                        <div class="aui-group">
                            <div class="aui-item">
                                <page:applyDecorator name="auifieldgroup">
                                    <aui:textfield label="text('grouppicker.namecontains')" maxlength="255" id="'nameFilter'" name="'nameFilter'" theme="'aui'">
                                        <aui:param name="'cssClass'">full-width-field</aui:param>
                                    </aui:textfield>
                                </page:applyDecorator>
                            </div>
                            <div class="aui-item">
                                <page:applyDecorator name="auifieldgroup">
                                    <aui:select label="text('grouppicker.groupsperpage')" id="'groupsPerPage'" name="'max'" list="/maxValues" listKey="'.'" listValue="'.'" theme="'aui'">
                                        <aui:param name="'cssClass'">full-width-field</aui:param>
                                    </aui:select>
                                </page:applyDecorator>
                            </div>
                        </div>
                        <aui:component template="hidden.jsp" theme="'aui'" name="'element'" />
                        <aui:component template="hidden.jsp" theme="'aui'" name="'multiSelect'" />
                        <aui:component template="hidden.jsp" theme="'aui'" name="'start'" />
                        <aui:component template="hidden.jsp" theme="'aui'" name="'previouslySelected'" />
                    </ww:property>
                </page:applyDecorator>
                <div class="aui-group count-pagination">
                    <div class="results-count aui-item">
                        <ww:text name="'grouppicker.displayinggroups'" value0="niceStart" value1="niceEnd" value2="groups/size" />
                    </div>
                    <div class="pagination aui-item">
                        <jsp:include page="userpicker_navigation.jsp"/>
                    </div>
                </div>
                <ww:if test="/multiSelect == true">
                    <form class="selectorform" name="selectorform">
                        <table class="aui aui-table-rowhover">
                            <thead>
                                <tr>
                                    <th width="1%"><input type="checkbox" name="all" onClick="setCheckboxes()"></th>
                                    <th><ww:text name="'grouppicker.groupname'" /></th>
                                    <th class="hidden"></th>
                                </tr>
                            </thead>
                            <tbody>
                            <ww:iterator value="currentPage" status="'status'">
                                <tr data-row-for="<ww:property value="name"/>" title="<ww:text name="'picker.click.to.select'"><ww:param name="'value0'"><ww:property value="name"/></ww:param></ww:text>" <ww:if test="/multiSelect == false">onclick="select(getElementById('groupname_<ww:property value="@status/index"/>').getAttribute('value'));"</ww:if> >
                                    <td><input data-group-select="true" <ww:if test="wasPreviouslySelected(.) == true"> checked="checked"</ww:if> type=checkbox name="userchecks" value="<ww:property value="name"/>" id="group_<ww:property value="@status/index"/>" onclick="processCBClick(event, this);"/></td>
                                    <td onclick="toggleCheckBox(event, 'group_<ww:property value="@status/index"/>')"><ww:property value="name"/></td>
                                    <td class="hidden">
                                        <div id="groupname_<ww:property value="@status/index"/>" value="<ww:property value="name"/>" style="visibility: hidden"></div>
                                    </td>
                                </tr>
                            </ww:iterator>
                            </tbody>
                        </table>
                    </form>
                    <div class="buttons-container">
                        <input id="multiselect-submit" class="aui-button" type="submit" value="<ww:text name="'common.words.select'"/>" onclick="selectUsers(AJS.$.trim(AJS.$('#openElement').text()), 'input[data-group-select]')">
                    </div>
                </ww:if>
                <ww:else>
                    <table class="aui aui-table-rowhover">
                        <thead>
                            <tr>
                                <th colspan="2"><ww:text name="'grouppicker.groupname'" /></th>
                            </tr>
                        </thead>
                        <tbody>
                        <ww:iterator value="currentPage" status="'status'">
                            <tr data-row-for="<ww:property value="name"/>" title="<ww:text name="'picker.click.to.select'"><ww:param name="'value0'"><ww:property value="name"/></ww:param></ww:text>" <ww:if test="/multiSelect == false">onclick="select(getElementById('groupname_<ww:property value="@status/index"/>').getAttribute('value'));"</ww:if> >
                                <td class="hidden">
                                    <div id="groupname_<ww:property value="@status/index"/>" value="<ww:property value="name"/>" style="visibility: hidden"></div>
                                </td>
                                <td><ww:property value="name"/></td>
                            </tr>
                        </ww:iterator>
                        </tbody>
                    </table>
                </ww:else>
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
