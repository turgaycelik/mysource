<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<html>
<head>
    <title><ww:text name="'showconstantshelp.title'"/></title>
    <style>
        dl {
            overflow: hidden;
            padding-left: 14em;
        }

        dt {
            -webkit-box-sizing: border-box;
            -moz-box-sizing: border-box;
            box-sizing: border-box;
            clear: left;
            float: left;
            margin-left: -14em;
            margin-top: 10px;
            padding-right: 10px;
            text-align: right;
            width: 10em;
        }

        dt:first-child,
        dt:first-child + dd {
            margin-top: 0 !important;
        }

        dd {
            float: left;
            margin-top: 10px;
            width: 100%;
        }

        dd + dd {
            margin-top: 5px;
        }

        dl dt {
            margin-top: 5px;
            text-align: left;
        }

        dl dd {
            margin-top: 5px;
        }
    </style>
</head>
<body>
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'mainContent'">
            <h1><ww:text name="'showconstantshelp.heading'"/></h1>
        </ui:param>
    </ui:soy>
    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                <ui:param name="'content'">

            <ww:bean name="'com.atlassian.jira.web.util.HelpUtil'" id="helpUtil" />
            <p><ww:text name="'showconstantshelp.tracking.issues'"/></p>
            <p><ww:text name="'showconstantshelp.associated.information'"/></p>
            <ul>
                <li>
                    <ww:text name="'showconstantshelp.associated.issuetype'">
                        <ww:param name="'value0'"><a href="#IssueTypes"></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                </li>
                <li><ww:text name="'showconstantshelp.associated.summary'"/></li>
                <li><ww:text name="'showconstantshelp.associated.description'"/></li>
                <li><ww:text name="'showconstantshelp.associated.project'"/></li>
                <li>
                    <ww:text name="'showconstantshelp.associated.components'">
                        <ww:param name="'value0'"><a href=<ww:property value="@helpUtil/helpPath('component_management')/url"/>></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                </li>
                <li>
                    <ww:text name="'showconstantshelp.associated.affectedversions'">
                        <ww:param name="'value0'"><a href=<ww:property value="@helpUtil/helpPath('version_management')/url"/>></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                </li>
                <li><ww:text name="'showconstantshelp.associated.fixversions'"/></li>
                <li><ww:text name="'showconstantshelp.associated.environment'"/></li>
                <li>
                    <ww:text name="'showconstantshelp.associated.priority'">
                        <ww:param name="'value0'"><a href="#PriorityLevels"></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                </li>
                <li><ww:text name="'showconstantshelp.associated.assignee'"/></li>
                <li><ww:text name="'showconstantshelp.associated.reporter'"/></li>
                <li>
                    <ww:text name="'showconstantshelp.associated.status'">
                        <ww:param name="'value0'"><a href="#StatusTypes"></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                </li>
                <li><ww:text name="'showconstantshelp.associated.changehistory'"/></li>
                <li><ww:text name="'showconstantshelp.associated.comments'"/></li>
                <li>
                    <ww:text name="'showconstantshelp.associated.resolution'">
                        <ww:param name="'value0'"><a href="#ResolutionTypes"></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                </li>
            </ul>

            <a name="IssueTypes"></a>
            <h4><ww:text name="'showconstantshelp.issuetypes.title'"/></h4>
            <div class="section">

                <p><ww:text name="'showconstantshelp.issuetypes.description'"/></p>

                <ww:if test="/subTasksEnabled == true">
                    <h5><ww:text name="'showconstantshelp.issuetypes.regular'"/></h5>
                </ww:if>

                <dl>
                <ww:iterator value="issueTypes">
                    <%-- Get the Issue Type Object Instead --%>
                    <ww:property value="/issueType(./string('id'))">
                    <dt>
                        <ww:component name="'issuetype'" template="constanticon.jsp">
                            <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                            <ww:param name="'iconurl'" value="./iconUrl" />
                            <ww:param name="'title'"><ww:property value="./descTranslation()"/></ww:param>
                        </ww:component>
                        <ww:property value="./nameTranslation()"/>
                    </dt>
                    <dd><ww:property value="./descTranslation()"/></dd>
                    </ww:property>
                </ww:iterator>
                </dl>

                <ww:if test="/subTasksEnabled == true">
                    <h5><ww:text name="'showconstantshelp.issuetypes.subtasks'"/></h5>

                    <dl>
                    <ww:iterator value="/subTaskIssueTypes">
                        <%-- Get the Issue Type Object Instead --%>
                        <ww:property value="/issueType(./string('id'))">
                        <dt>
                            <ww:component name="'issuetype'" template="constanticon.jsp">
                                <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                                <ww:param name="'iconurl'" value="./iconUrl" />
                                <ww:param name="'title'"><ww:property value="./descTranslation()"/></ww:param>
                            </ww:component>
                            <ww:property value="./nameTranslation()"/>
                        </dt>
                        <dd><ww:property value="./descTranslation()"/></dd>
                        </ww:property>
                    </ww:iterator>
                    </dl>
                </ww:if>
            </div>

            <a name="PriorityLevels"></a>
            <h4><ww:text name="'showconstantshelp.priority.title'"/></h4>
            <div class="section">

                <p><ww:text name="'showconstantshelp.priority.description'"/></p>

                <dl>
                <ww:iterator value="priorities">
                    <dt>
                        <ww:component name="'priority'" template="constanticon.jsp">
                            <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                            <ww:param name="'iconurl'" value="./iconUrl" />
                            <ww:param name="'title'"><ww:property value="./descTranslation()"/></ww:param>
                        </ww:component>
                        <ww:property value="./nameTranslation()"/>
                    </dt>
                    <dd><ww:property value="./descTranslation()"/></dd>
                </ww:iterator>
                </dl>

            </div>

            <a name="StatusTypes"></a>
            <h4><ww:text name="'showconstantshelp.statuses.title'"/></h4>
            <ww:if test="/statusCategoriesEnabled == true">
                <h5><ww:text name="'showconstantshelp.statuscategory.title'"/></h5>
                <div class="section">
                    <p>
                        <ww:text name="'showconstantshelp.statuscategory.description'">
                            <ww:param name="'param0'"><b><ww:text name="'common.statuscategory.new'" /></b></ww:param>
                            <ww:param name="'param1'"><b><ww:text name="'common.statuscategory.indeterminate'" /></b></ww:param>
                            <ww:param name="'param2'"><b><ww:text name="'common.statuscategory.done'" /></b></ww:param>
                        </ww:text>
                    </p>

                    <dl>
                    <ww:iterator value="statusCategories">
                        <dt>
                            <ui:soy moduleKey="'jira.webresources:issue-statuses'" template="'JIRA.Template.Util.Issue.Status.issueStatusResolver'">
                                <ui:param name="'issueStatus'" value="."/>
                            </ui:soy>
                        </dt>
                        <dd>
                            <p><ww:property value="./description"/></p>
                        </dd>
                    </ww:iterator>
                    </dl>
                </div>

                <h5><ww:text name="'showconstantshelp.status.title'"/></h5>
            </ww:if>
            <div class="section">
                <p><ww:text name="'showconstantshelp.status.description'"/></p>

                <dl>
                <ww:iterator value="statuses">
                    <dt class="show-constants-help-status">
                        <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
                            <ww:param name="'issueStatus'" value="."/>
                            <ww:param name="'isSubtle'" value="false"/>
                            <ww:param name="'isCompact'" value="false"/>
                        </ww:component>
                    </dt>
                    <dd><ww:property value="./descTranslation()"/></dd>
                </ww:iterator>
                </dl>

            </div>

            <a name="ResolutionTypes"></a>
            <h4><ww:text name="'showconstantshelp.resolution.title'"/></h4>
            <div class="section">

                <p><ww:text name="'showconstantshelp.resolution.description'"/></p>

                <dl>
                <ww:iterator value="resolutions">
                    <dt><strong><ww:property value="./nameTranslation()"/></strong></dt>
                    <dd><ww:property value="./descTranslation()"/> </dd>
                </ww:iterator>
                </dl>

            </div>

            <ww:if test="securityLevels != null && securityLevels/size > 0">
            <a name="SecurityLevels"></a>
            <h4><ww:text name="'showconstantshelp.securitylevel.title'"/></h4>
            <div class="section">

                <p><ww:text name="'showconstantshelp.securitylevel.description'"/></p>

                <dl>
                <ww:iterator value="securityLevels">
                    <dt><strong><ww:property value="./name"/></strong></dt>
                    <dd><ww:property value="./description"/> </dd>
                </ww:iterator>
                </dl>

            </div>
            </ww:if>

                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</body>
</html>
