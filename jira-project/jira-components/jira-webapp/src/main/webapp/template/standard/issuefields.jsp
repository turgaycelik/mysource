<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.web.action.util.FieldsResourceIncluder" %>
<%--
--  Parameters:
--   * issue        - an issue object to work on
--   * tabs         - a collection of field screen tabs
--   * errortabs    - a collection of tabs with fields that have errors
--   * selectedtab  - the index of the tab to select starting with 1
--   * ignorefields - a collection of field ids that should not be shown no matter what
--   * create       - a boolean value indicating whether issue is being created
--
--%>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<ww:bean name="'com.atlassian.jira.web.bean.FieldScreenBean'" id="fieldHelper" />
<%
    final FieldsResourceIncluder fieldResourceIncluder = ComponentManager.getComponentInstanceOfType(FieldsResourceIncluder.class);
    fieldResourceIncluder.includeFieldResourcesForCurrentUser();
%>
<ww:if test="parameters['tabs']/size > 1">
    <div class="aui-tabs horizontal-tabs">
        <%-- Show tab headings --%>
        <ul class="tabs-menu">
            <ww:iterator value="parameters['tabs']" status="'status'">
            <li class="menu-item<ww:if test="@status/count == parameters['selectedtab']"> active-tab</ww:if><ww:if test="@status/first == true"> first</ww:if>">
                <a href="#tab<ww:property value="@status/count"/>"<ww:if test="parameters['errortabs']/contains(.) == true"> class="has-errors"</ww:if>><strong><ww:property value="./name"/></strong></a>
            </li>
            </ww:iterator>
        </ul>
        <%-- Show the actual tabs with their fields --%>
        <ww:iterator value="parameters['tabs']" status="'status'">
            <div id="tab<ww:property value="@status/count"/>" class="tabs-pane<ww:if test="@status/count == parameters['selectedtab']"> active-pane</ww:if>">
            <%-- Show tab's fields --%>
            <ww:iterator value="./fieldScreenRenderLayoutItems" status="'itemStatus'">
                <%-- Hack to not show issue type as it has been shown on a previous page --%>
                <ww:if test="parameters['ignorefields'] == null || parameters['ignorefields']/contains(./orderableField/id) == false">
                    <ww:if test="parameters['create'] == true">
                        <ww:property value="./createHtml(/, /, parameters['issue'], @fieldHelper/computeDisplayParams(@itemStatus, parameters['displayParams']))" escape="'false'" />
                    </ww:if>
                    <ww:else>
                        <ww:property value="./editHtml(/, /, parameters['issue'], @fieldHelper/computeDisplayParams(@itemStatus, parameters['displayParams']))" escape="'false'" />
                    </ww:else>
                </ww:if>
            </ww:iterator>
            </div>
        </ww:iterator>
    </div>
</ww:if>
<ww:else>
    <ww:iterator value="parameters['tabs']" status="'status'">
        <ww:iterator value="./fieldScreenRenderLayoutItems" status="'itemStatus'">
            <%-- Hack to not show issue type as it has been shown on a previous page --%>
            <ww:if test="parameters['ignorefields'] == null || parameters['ignorefields']/contains(./orderableField/id) == false">
                <ww:if test="parameters['create'] == true">
                    <ww:property value="./createHtml(/, /, parameters['issue'], @fieldHelper/computeDisplayParams(@itemStatus, parameters['displayParams']))" escape="'false'" />
                </ww:if>
                <ww:else>
                    <ww:property value="./editHtml(/, /, parameters['issue'], @fieldHelper/computeDisplayParams(@itemStatus, parameters['displayParams']))" escape="'false'" />
                </ww:else>
            </ww:if>
        </ww:iterator>
    </ww:iterator>
</ww:else>
