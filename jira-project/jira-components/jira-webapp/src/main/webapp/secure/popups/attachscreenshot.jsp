<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib prefix="webwork" uri="webwork" %>
<% ComponentManager.getInstance().getWebResourceManager().requireResource("jira.webresources:jira-global"); %>
<html>
<head>
    <%-- TODO: SEAN raised http://jira.atlassian.com/browse/JRA-25378 for the poor security check here --%>
    <ww:if test="/issueExists == true">
        <title><ww:text name="'attachscreenshot.title'"/></title>
        <meta name="x.ua.compatible" content="requiresActiveX=true" />
    </ww:if>
    <ww:else>
        <title><ww:text name="'common.words.error'"/></title>
        <meta name="decorator" content="message" />
    </ww:else>
</head>
<body>
<ww:if test="/issueExists==true">
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'mainContent'">
            <h1><ww:text name="'attachscreenshot.title'"/></h1>
        </ui:param>
    </ui:soy>

    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                <ui:param name="'content'">

            <p>
                <ww:text name="'attachscreenshot.description.line1'"/><br>
                <ww:text name="'attachscreenshot.description.line2'"/>
            </p>

            <ww:if test="/hasErrorMessages == 'true'">
                <ul>
                    <ww:iterator value="/flushedErrorMessages">
                        <li><ww:property value="." /></li>
                    </ww:iterator>
                </ul>
            </ww:if>

            <ul id="applet-params" style="display:none">
                <li id="user-agent"><%= TextUtils.htmlEncode(request.getHeader("User-Agent")) %></li>

                <ww:iterator value="/groupLevels" status="'paramStatus'">
                    <li id="comment-group-name-<ww:property value="@paramStatus/index"/>"><ww:text name="./name" /></li>
                </ww:iterator>

                <ww:iterator value="/roleLevels" status="'paramStatus'">
                <li id="comment-role-<ww:property value="@paramStatus/index"/>"><ww:text name="./name" /></li>
                </ww:iterator>

            </ul>

            <script type="text/javascript" src="<%= request.getContextPath() %>/includes/deployJava.js"></script>
            <script type="text/javascript">
                var version = '1.6';
                var attributes = {
                    codebase:"<%= request.getContextPath() %>/secure/",
                    code:"com.atlassian.jira.screenshot.applet.ScreenshotApplet.class",
                    archive:"applet/screenshot.jar",
                    width:710,
                    height:540
                };
                var parameters = {
                    scriptable:"false",
                    post:"AttachScreenshot.jspa?secureToken=<ww:property value="/newUserToken"/>",
                    issue:<ww:property value="id" />,
                    screenshotname:"<ww:property value="nextScreenshotName"/>",
                    encoding:"<ww:property value="/applicationProperties/encoding" />",
                    useragent: jQuery("#user-agent").text(),
                    <ww:iterator value="/groupLevels" status="'paramStatus'">
                    'comment.group.name.<ww:property value="@paramStatus/index"/>': jQuery("#comment-group-name-<ww:property value="@paramStatus/index"/>").text().replace(/"/g, '&quot;'),
                    </ww:iterator>
                    <ww:iterator value="/roleLevels" status="'paramStatus'">
                    'comment.role.<ww:property value="@paramStatus/index"/>':"<ww:text name="./id/toString()" />|" + jQuery("#comment-role-<ww:property value="@paramStatus/index"/>").text().replace(/"/g, '&quot;'),
                    </ww:iterator>
                    'paste.text':"<ww:property value="/encode(/text('attachfile.paste.label'))" />",
                    'filename.text':"<ww:property value="/encode(/text('attachfile.filename.label'))" />",
                    'errormsg.filename.text':"<ww:property value="/encode(/text('attachfile.applet.filename.error'))" />",
                    'comment.text':"<ww:property value="/encode(/text('attachfile.comment.update.label'))" />",
                    'attach.text':"<ww:property value="/encode(/text('attachfile.submitname'))" />",
                    'cancel.text':"<ww:property value="/encode(/text('common.words.cancel'))" />",
                    'badconfiguration.text':"<ww:property value="/encode(/text('attachfile.applet.configuration.error'))" />",
                    'comment.level.text':"<ww:property value="/encode(/text('comment.update.viewableby.label'))" />",
                    'allusers.text':"<ww:property value="/encode(/text('comment.constants.allusers'))" />",
                    'projectroles.text':"<ww:property value="/encode(/text('common.words.project.roles'))" />",
                    'groups.text':"<ww:property value="/encode(/text('common.words.groups'))" />",
                    'security.text':"<ww:property value="/encode(/text('attachfile.applet.security.problem'))" />"
                };

                //window.name will be set when we call window.open(), so we can use it here to detect if it's a popup or
                //if the user opened the screenshot applet in a new tab (JRADEV-3511,JRADEV-3512)
                var isPopup = (window.name === "screenshot");

                var returnUrl = "<ww:property value="returnUrl" />";

                var issueKey = "<ww:property value="key" />";
                var issueId = "<ww:property value="id" />";

                // JRA-27514 When the applet calls getAppletContext().showDocument() on Windows OS,
                // window.opener changes to be the popup window
                var realOpener = window.opener;

                function submit() {
                    if (isPopup) {
                        if (realOpener && !realOpener.closed) {
                            if (realOpener.JIRA && realOpener.JIRA.Issues && realOpener.JIRA.Issues.Api && realOpener.JIRA.Issues.Api.updateIssue) {
                                realOpener.JIRA.Issues.Api.updateIssue({ key: issueKey, id: issueId }, 'thanks_issue_attached');
                            } else {
                                realOpener.open(returnUrl, '_top');
                            }
                        }
                        window.close();
                    } else {
                        window.location = returnUrl;
                    }
                }

                function cancel() {
                    if (isPopup) {
                        window.close();
                    } else {
                        window.location = returnUrl;
                    }
                }

                function isMetroStyle() {
                    var result;
                    try {
                        new ActiveXObject("htmlfile");
                        result = true;
                    } catch (e) {
                        result = false;
                    }
                    return !result;
                }

                var isIE10Metro = jQuery.browser.msie && jQuery.browser.version == "10.0" && isMetroStyle() ;
                if ( !isIE10Metro ) {
                    //Not IE10, or IE10 in desktop mode
                    deployJava.runApplet(attributes, parameters, version);
                } else {
                    var pageAlreadyRefreshed = document.location.search.match(/redirect=1/);
                    //IE10 metro mode
                    if (!pageAlreadyRefreshed) {
                        //IE10 does not ask the user to switch to desktop mode when opening a new window.
                        //This redirect is a workaround for it until Microsoft fixes the real bug
                        //https://connect.microsoft.com/IE/feedback/details/776564/metro-ui-ie10-requiresactivex-true-does-not-work-in-redirected-page-or-page-opened-in-a-new-window
                        //Without the timeout,the user don't get the switch-to-desktop message. Odd.
                        var glue = document.location.search.length?"&":"?";
                        setTimeout(function() {
                            document.location.assign(document.location.href + glue + "redirect=1");
                        },0)

                    } else {
                        //Do nothing, the user is being asked to switch to desktop mode
                    }
                }
            </script>
            <input type="submit" accesskey="<ww:text name="'common.forms.cancel.accesskey'" />" onclick="window.close();" class="hiddenButton" name="randombutton" />

                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</ww:if>
<ww:else>
    <div class="form-body">
        <header>
            <h1><ww:text name="'common.words.error'"/></h1>
        </header>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.errors.issues.current.issue.null'"/></p>
            </aui:param>
        </aui:component>
    </div>
</ww:else>
</body>
</html>
