<%@ taglib uri="webwork" prefix="ww" %> <%@ taglib uri="webwork" prefix="ui" %>
<html>
<head>
    <title><ww:text name="'issuepicker.name'" /></title>
    <meta content="popup" name="decorator"/>
    <%@ include file="/includes/js/multipickerutils.jsp" %>
    <script type="text/javascript">

    function populateForm(val)
    {
        callbackToMainWindow([{
            value: val,
            label: AJS.$("a[rel=" + val + "]").attr("data-label")
        }]);

        return false;
    }

    // populate the parent form with multiple values
    function populateFormMultiple()
    {
        var val, value = [], counter;
        if (document.issues.issuekey.length > 0)
        {
            // get all the selected checkboxes
            for (counter=0; counter<document.issues.issuekey.length; counter++)
            {
                if (document.issues.issuekey[counter].checked)
                {
                    val = document.issues.issuekey[counter].value;
                    value.push({
                        value: val,
                        label: AJS.$("a[rel=" + val + "]").attr("data-label")
                    });
                }
            }
        }
        else if (document.issues.issuekey.checked)
        {
            val = document.issues.issuekey.value;

            value.push({
                value: val,
                label: AJS.$("a[rel=" + val + "]").attr("data-label")
            });

        }

        callbackToMainWindow(value);
        return false;
    }

    function callbackToMainWindow(value)
    {
        var path, callback, i;

        path = ["jira", "issuepicker", "callback"];

        callback = window.opener;
        for (i = 0; callback && i < path.length; i++) {
            callback = callback[path[i]];
        }

        value = JSON.stringify(value);

        if (callback){
            callback(value);
        }

        window.close();
    }
    AJS.$(function(){
        AJS.$('#searchRequestId').attr('disabled', AJS.$('input[name="mode"]:checked').val() === "recent");
        AJS.$('#issuepicker-source').find(':radio').change(function(){
            AJS.$('#searchRequestId').attr('disabled', AJS.$('input[name="mode"]:checked').val() === "recent");
            if (AJS.$('input[name="currentMode"]').val() === 'search' && AJS.$('input[name="mode"]:checked').val() === 'recent') {
                AJS.$('#issuepicker-source').submit();
            }
        });
    });
    </script>
</head>
<body>
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'mainContent'">
            <h1><ww:text name="'issuepicker.name'" /></h1>
        </ui:param>
        <ww:if test="/singleSelectOnly == 'false' && /selectMode != 'multiple'">
            <ui:param name="'actionsContent'">
                            <a class="aui-button" href="<ww:url>
                                    <ww:param name="'selectMode'" value="'multiple'" />
                                    <ww:param name="'mode'" value="/mode" />
                                    <ww:param name="'callbackMode'" value="/callbackMode" />
                                    <ww:param name="'fieldId'" value="/fieldId" />
                                    <ww:param name="'currentIssue'" value="/currentIssue"/>
                                    <ww:param name="'singleSelectOnly'" value="/singleSelectOnly"/>
                                    <ww:param name="'showSubTasks'" value="/showSubTasks"/>
                                    <ww:param name="'showSubTasksParent'" value="/showSubTasksParent"/>
                                    <ww:param name="'searchRequestId'" value="/searchRequestId" />
                                    <ww:if test="/selectedProjectId">
                                        <ww:param name="'selectedProjectId'" value="/selectedProjectId"/>
                                    </ww:if>
                                    </ww:url>">
                                <ww:text name="'issuedisplayer.select.multiple.issues'" />
                            </a>
            </ui:param>
        </ww:if>
    </ui:soy>

    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                <ui:param name="'content'">

            <form id="issuepicker-source" name="selectFilter" class="aui top-label" action="IssuePicker.jspa">
                <div class="group">
                    <div class="radio">
                        <input type="radio" id="issue-source-recent" class="radio" name="mode" value="recent" <ww:if test="mode == 'recent'">checked</ww:if>/>
                        <label for="issue-source-recent" title="<ww:text name="'issuepicker.recent.issues.desc'" />">
                            <ww:text name="'issuepicker.recent.issues'" />
                        </label>
                    </div>
                    <div class="radio">
                        <input type="radio" id="issue-source-search" class="radio" name="mode" value="search" <ww:if test="mode == 'search'">checked</ww:if>/>
                        <label for="issue-source-search" title="<ww:text name="'issuepicker.search.filter.desc'" />">
                            <ww:text name="'issuepicker.search.filter'" />
                        </label>
                    </div>
                </div>
                <div class="field-group">
                    <select id="searchRequestId" name="searchRequestId" onChange="submit()" class="select">
                        <option value="-1"><ww:text name="'issuepicker.select.value'"/></option>
                        <ww:iterator value="/availableFilters">
                            <option value="<ww:property value="./id"/>"<ww:if test="./id == /searchRequestId && mode=='search'">selected</ww:if>><ww:property value="./name"/></option>
                        </ww:iterator>
                    </select>
                </div>
                <div class="hidden">
                    <input type="hidden" name="currentMode" value="<ww:property value="/mode"/>">
                    <input type="hidden" name="currentIssue" value="<ww:property value="/currentIssue"/>">
                    <input type="hidden" name="singleSelectOnly" value="<ww:property value="/singleSelectOnly"/>">
                    <input type="hidden" name="showSubTasks" value="<ww:property value="/showSubTasks"/>">
                    <input type="hidden" name="showSubTasksParent" value="<ww:property value="/showSubTasksParent"/>">
                    <ww:if test="/selectedProjectId">
                        <input type="hidden" name="selectedProjectId" value="<ww:property value="/selectedProjectId"/>">
                    </ww:if>
                </div>
            </form>
            <hr/>
            <form name="issues">
                <ww:if test="mode == 'recent'">
                    <div class="module toggle-wrap twixi-block" id="recent-issues">
                        <ww:property value="/userHistoryIssues" >
                                <div class="mod-header"><h3 class="toggle-title twixi-trigger"><ww:text name="'issuepicker.issues.viewed'"/></h3></div>
                                <div class="mod-content twixi-content">
                                    <ww:if test=". && size > 0">
                                        <%@ include file="/includes/issue/issuedisplayer.jsp" %>
                                    </ww:if>
                                    <ww:else>
                                        <ww:text name="'issuepicker.noissues.viewed'"/>
                                    </ww:else>
                                </div>
                        </ww:property>
                    </div>
                <%-- See if the user has a current search.  If they do - show them the first 50 issues in that search --%>
                    <div class="module toggle-wrap twixi-block" id="current-issues">
                        <ww:property value="/browsableIssues" >
                            <div class="mod-header"><h3 class="toggle-title twixi-trigger"><ww:text name="'issuepicker.current.search.issues'"/></h3></div>
                            <div class="mod-content twixi-content">
                                <ww:if test=". && size > 0">
                                    <%@ include file="/includes/issue/issuedisplayer.jsp" %>
                                </ww:if>
                                <ww:else>
                                    <ww:text name="'issuepicker.current.search.noissues'"/>
                                </ww:else>
                            </div>
                        </ww:property>
                    </div>
                </ww:if>
                <ww:elseIf test="mode == 'search'">
                    <%-- Return the first 50 results from the selected filter. --%>
                    <div class="module toggle-wrap twixi-block" id="filter-issues">
                        <ww:property value="/searchRequestIssues" >
                            <div class="mod-header"><h3 class="toggle-title twixi-trigger"><ww:text name="'issuepicker.search.issues'"/></h3></div>
                            <div class="mod-content twixi-content">
                                <ww:if test=". && size > 0">
                                    <%@ include file="/includes/issue/issuedisplayer.jsp" %>
                                </ww:if>
                                <ww:else>
                                    <ww:text name="'issuepicker.search.noissues'"/> <ww:property value="/searchRequestName" />
                                </ww:else>
                            </div>
                        </ww:property>
                    </div>
                </ww:elseIf>
            </form>

                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</body>
</html>
