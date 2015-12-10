<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
        <title><ww:text name="'voters.view.title'"/></title>
        <meta name="decorator" content="issueaction" />
        <%
            WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
            webResourceManager.requireResource("jira.webresources:jquery-flot");

            KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
            keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
        %>
        <link rel="index" href="<ww:url value="/issuePath" atltoken="false" />" />
</head>
<body>
            <div class="command-bar">
                <div class="ops-cont">
                    <ul class="ops">
                        <li id="back-lnk-section" class="last">
                            <a id="back-lnk" class="button first last" href="<%= request.getContextPath() %>/browse/<ww:property value="/issueObject/key" />"><span class="icon icon-back"><span><ww:text name="'opsbar.back.to.issue'"/></span></span><ww:text name="'opsbar.back.to.issue'"/></a>
                        </li>
                    </ul>
                    <ul class="ops">
                        <ww:if test="/issue/string('resolution') == null">
                            <ww:if test="issueReportedByMe == false">
                                <li>
                                    <ww:if test="canAddVote == true">
                                        <a class="button first last" id="vote" href="<ww:url value="'ViewVoters!addVote.jspa'"><ww:param name="'key'" value="/issue/string('key')"/></ww:url>"><ww:text name="'voters.vote.add'" /></a>
                                    </ww:if>
                                    <ww:elseIf test="canRemoveVote == true">
                                        <a class="button first last" id="unvote" href="<ww:url value="'ViewVoters!removeVote.jspa'"><ww:param name="'key'" value="/issue/string('key')"/></ww:url>"><ww:text name="'voters.vote.remove'" /></a>
                                    </ww:elseIf>
                                </li>
                            </ww:if>
                            <ww:else>
                                <li class="info">
                                    <span class="warning"><ww:text name="'common.words.note'"/></span>: <ww:text name="'issue.operations.novote'"/>                                    
                                </li>
                            </ww:else>
                        </ww:if>
                        <ww:else>
                            <li class="info">
                                <span class="warning"><ww:text name="'common.words.note'"/></span>: <ww:text name="'issue.operations.voting.resolved'"/>                                
                            </li>
                        </ww:else>
                    </ul>
                </div>
            </div>
            <ww:if test="/votingEnabled == true && /voteHistory/voteHistory/size > 2">
                <h2><ww:text name="'voters.history.title'"/></h2>
                            <div id="vote-history-graph" style="width:900px;height:175px;"></div>
            </ww:if>
            <h2><ww:text name="'voters.view.title'"/></h2>
                        <ww:if test="/votingEnabled == false">
                            <p><span class="warning"><ww:text name="'issue.operations.voting.disabled'"/></span></p>
                        </ww:if>
                        <%--We know when this is false the Issue doesn't exist and has no voters.
                        All exception handling and error rendering is done by the issuesummary decorator.--%>
                        <ww:elseIf test="/issueValid == true">
                            <ww:property value="voters">
                                <ww:if test=". != null && ./empty == false">
                                    <table id="voter-list" class="aui" cellpadding="0" cellspacing="0" width="100%">
                                        <thead>
                                            <th><ww:text name="'common.words.fullname'"/> (<ww:text name="'common.words.username'"/>)</th>
                                        </thead>
                                        <tbody>
                                            <ww:iterator status="'status'">
                                                <tr>
                                                    <td>
                                                        <jira:formatuser userName="./name" type="'profileLink'" id="'voter_link'"/> (<ww:property value="./name" />)
                                                    </td>
                                                </tr>
                                            </ww:iterator>
                                        </tbody>
                                    </table>
                                </ww:if>
                                <ww:else>
                                    <aui:component template="auimessage.jsp" theme="'aui'">
                                        <aui:param name="'messageType'">info</aui:param>
                                        <aui:param name="'messageHtml'">
                                            <p><ww:text name="'voters.novoters'"/></p>
                                        </aui:param>
                                    </aui:component>
                                </ww:else>
                            </ww:property>
                        </ww:elseIf>
    <script type="text/javascript">
        AJS.$(function () {
    <ww:if test="/votingEnabled == true && /voteHistory/voteHistory/size > 2">
        var data = [];
        <ww:iterator  value="/voteHistory/voteHistory">data.push([new Date(<ww:property value="/commaSeperatedDateParts(./timestamp)"/>), <ww:property value="./votes"/>]);
        </ww:iterator>
                var t = 10;
        var n = <ww:property value="/voteHistory/numberOfDays"/>;
        if (n < 11) t = n - 1;
        if (t < 1) t = 1;
        var options = {
          series: {
            lines: { show: true },
            points: { show: false }
          },
          xaxis: {
            mode: "time",
            timeformat: "%y/%0m/%0d",
            ticks : t,
            labelWidth: 80
          },
          yaxis: {
            tickDecimals: 0,
            labelWidth: 30
          }

        };
        AJS.$.plot(AJS.$("#vote-history-graph"), [{color: "#3C78B5",  data : data}], options);
    </ww:if>
        });
    </script>
</body>
</html>
