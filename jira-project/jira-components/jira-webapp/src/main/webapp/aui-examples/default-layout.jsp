<%@ page import="com.atlassian.jira.action.ActionContextKit" %>
<%@ page import="com.atlassian.jira.web.action.JiraWebActionSupport" %>
<%@ page import="webwork.action.ActionContext" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="ui" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <title>Default Layout</title>
</head>
<%
    ActionContextKit.setContext(request,response,request.getContextPath());
    JiraWebActionSupport fakeAction = new JiraWebActionSupport()
    {
//        public Map<String,String> getSelectOptionsMap() {
//            return MapBuilder.newBuilder("opt1","Name1").add("opt2","Name2").toMap();
////        }
//        public List<String> getSelectOptionsList() {
//            return EasyList.build("ListItem1", "ListItem2", "ListItem3");
//        }
    };
    fakeAction.addError("lname", "Error message here");
    ActionContext.getValueStack().pushValue(fakeAction);

%>
<body>
    <ww:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ww:param name="'mainContent'">
            <h1>Default Layout</h1>
        </ww:param>
    </ww:soy>

    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                <ui:param name="'content'">

            <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
                <ui:param name="'mainContent'">
                    <h2>Example content heading</h2>
                </ui:param>
                <ui:param name="'actionsContent'">
                    <div class="aui-buttons">
                        <button class="aui-button">With a page action!</button>
                        <button class="aui-button aui-dropdown2-trigger" aria-haspopup="true" aria-owns="actions-menu">And some more...</button>
                    </div>
                </ui:param>
            </ui:soy>

            <blockquote>
                <p>Lorem ipsum</p>
            </blockquote>
            <aui:component id="'customWarning'" template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">warning</aui:param>
                <aui:param name="'cssClass'">my-class</aui:param>
                <aui:param name="'iconText'">This is a warning</aui:param>
                <aui:param name="'titleText'" >You have no memory left</aui:param>
                <aui:param name="'messageHtml'"><p>To rectify this issue, please allocate more memory.</p></aui:param>
            </aui:component>
            <page:applyDecorator id="create-user" name="auiform">
                <page:param name="action"><ww:url value="'secure/MyAction.jspa'" /></page:param>
                <page:param name="submitButtonName">Submit</page:param>
                <%-- This is always html escaped for you inside the template, so please don't escape it yourself to
                avoid double-escaping issues. --%>
                <page:param name="submitButtonText"><ww:text name="'AUI.form.submit.button.text'"/></page:param>
                <%-- This is always html escaped for you inside the template as well, so please don't escape it yourself to
                avoid double-escaping issues.
                    This is also set to AUI.form.cancel.link.text if you don't specify it, so please omit it if you want
                that key to be printed. --%>
                <page:param name="cancelLinkText"><ww:text name="'AUI.form.cancel.link.text'"/></page:param>
                <%-- You need to url escape this value even if it's "safe" so always use <ww:url> for the value of
                this parameter as shown below. --%>
                <page:param name="cancelLinkURI"><ww:url value="'/browse/HSP-1'" atltoken="false"/></page:param>

                <aui:component template="formHeading.jsp" theme="'aui'">
                    <aui:param name="'text'">Default - Edit State</aui:param>
                </aui:component>

                <page:applyDecorator name="auifieldset">
                    <page:param name="legend">Text Entry</page:param>

                    <page:applyDecorator name="auifieldgroup">
                        <page:param name="description">Default width input.</page:param>
                        <aui:textfield id="'fname'" label="'First Name'" mandatory="true" name="'fname'" theme="'aui'" />
                    </page:applyDecorator>

                    <page:applyDecorator name="auifieldgroup">
                        <page:param name="description">Long width input</page:param>
                        <aui:textfield id="'lname'" label="'Last Name'" mandatory="'true'" name="'lname'" size="'long'" theme="'aui'" title="'last name'"/>
                    </page:applyDecorator>

                    <page:applyDecorator name="auifieldgroup">
                        <page:param name="description">Medium width input</page:param>
                        <aui:textfield id="'email1'" label="'Email'" name="'email'" size="'medium'" theme="'aui'">
                            <aui:param name="'iconCssClass'">icon-help</aui:param>
                            <aui:param name="'iconURI'">http://example.com</aui:param>
                            <aui:param name="'iconText'">help</aui:param>
                            <aui:param name="'iconTitle'">what's this "and all that"</aui:param>
                        </aui:textfield>
                    </page:applyDecorator>

                    <page:applyDecorator name="auifieldgroup">
                        <aui:password id="'password1'" label="'Password'" name="'new-password'" theme="'aui'" title="'password'" value="'test'" />
                    </page:applyDecorator>

                    <page:applyDecorator name="auifieldgroup">
                        <aui:password id="'password2'" label="'Confirm Password'" name="'confirm-password'" theme="'aui'" title="'confirm password'" />
                    </page:applyDecorator>

                    <page:applyDecorator name="auifieldgroup">
                        <page:param name="description">Long width input</page:param>
                        <aui:textarea id="'textarea1'" label="'Textarea1'" mandatory="'true'" name="'textarea1'" rows="'10'" size="'long'" theme="'aui'" title="'last name'"/>
                    </page:applyDecorator>

                    <page:applyDecorator name="auifieldgroup">
                        <page:param name="description">Long width inputLong width inputLong width inputLong width inputLong width inputLong width inputLong width inputLong width inputLong width inputLong width inputLong width inputLong width inputLong width inputLong width inputLong width inputLong width input</page:param>
                        <aui:textarea id="'textarea2'" label="'Textarea2'" mandatory="'true'" name="'textarea2'" rows="'10'" size="'long'" theme="'aui'" title="'last name'"/>
                    </page:applyDecorator>

                    <page:applyDecorator name="auifieldset">
                        <page:param name="type">group</page:param>
                        <page:param name="legend">Radio list</page:param>
                        <page:param name="mandatory">true</page:param>
                        <div class="radio">
                            <input type="radio" name="radio-list" id="radio-list-1" /> <label for="radio-list-1">Red</label>
                        </div>
                        <div class="radio">
                            <input type="radio" name="radio-list" id="radio-list-2" /> <label for="radio-list-2">Green</label>
                        </div>
                        <div class="radio">
                            <input type="radio" name="radio-list" id="radio-list-3" /> <label for="radio-list-3">Blue</label>
                        </div>
                    </page:applyDecorator>

                    <page:applyDecorator name="auifieldset">
                        <page:param name="type">group</page:param>
                        <page:param name="legend">Checkbox list</page:param>
                        <page:applyDecorator name="auifieldgroup">
                            <page:param name="type">checkbox</page:param>
                            <aui:checkbox id="'colour-red'" label="'Red'" name="'colour'" fieldValue="'red'" theme="'aui'" />
                        </page:applyDecorator>
                        <page:applyDecorator name="auifieldgroup">
                            <page:param name="type">checkbox</page:param>
                            <aui:checkbox id="'colour-blue'" label="'Blue'" name="'colour'" fieldValue="'blue'" theme="'aui'" />
                        </page:applyDecorator>
                    </page:applyDecorator>
                </page:applyDecorator>

            </page:applyDecorator>

            <h2>Snippets</h2>
            <div style="margin-bottom:2em;">
                <h3>Module Basic</h3>
                <div class="module">
                    <div class="mod-header">
                        <h3>Module Header</h3>
                    </div>
                    <div class="mod-content">
                        Module content
                    </div>
                </div>
            </div>
            <div style="margin-bottom:2em;">
                <h3>Module Advanced</h3>
                <%--would need to hook up the toggling of this - app.js--%>
                <div class="module toggle-wrap">
                    <div class="mod-header">
                        <ul class="ops">
                            <li><a class="issueaction-attach-file icon jira-icon-add" href="#"><span>Link text (hidden)</span></a></li>
                            <li id="attachment-sorting-options" class="drop">
                                <div class="aui-dd-parent">
                                    <a class="icon drop-menu aui-dropdown-trigger" href="#"><span>More Options (hidden)</span></a>
                                    <div class="aui-dropdown-content aui-list aui-list-checked">
                                        <ul class="aui-list-section">
                                            <li class="aui-list-item  aui-checked">
                                                <a href="#" class="aui-list-item-link">Link</a>
                                            </li>
                                        </ul>
                                        <ul class="aui-list-section aui-last">
                                            <li class="aui-list-item">
                                                <a href="#" class="aui-list-item-link">Link</a>
                                            </li>
                                        </ul>
                                    </div>
                                </div>
                            </li>
                        </ul>
                        <h3 class="toggle-title">Module Header</h3>
                    </div>
                    <div class="mod-content">
                        Module content
                    </div>
                </div>
            </div>
            <div style="margin-bottom:2em;">
                <a name="tool-butts"></a>
                <h3>Toolbar</h3>
                <div class="aui-toolbar">
                    <div class="toolbar-group">
                        <span class="toolbar-item">
                            <input class="toolbar-trigger" type="submit" name="update" value="Toolbar Text"/>
                        </span>
                        <span class="toolbar-item">
                            <input class="toolbar-trigger" type="submit" name="update" value="Toolbar Text"/>
                        </span>
                    </div>
                    <div class="toolbar-group">
                        <span class="toolbar-item active">
                            <input class="toolbar-trigger" type="submit" name="update" value="Active Toolbar"/>
                        </span>
                    </div>
                    <%--input styled like a link--%>
                    <div class="toolbar-group">
                        <span class="toolbar-item toolbar-item-link">
                            <input class="toolbar-trigger cancel" type="button" name="cancel" value="Cancel"/>
                        </span>
                    </div>
                    <%--or--%>
                    <div class="toolbar-group">
                        <span class="toolbar-item toolbar-item-link">
                            <a class="toolbar-trigger cancel" href="#<%-- cancel link --%>">Cancel</a>
                        </span>
                    </div>
                </div>
                <h3>Split Toolbar</h3>
                <div class="aui-toolbar">
                    <div class="toolbar-split toolbar-split-left">
                        <div class="toolbar-group">
                            <span class="toolbar-item">
                                <input class="toolbar-trigger" type="submit" name="update" value="Toolbar Text"/>
                            </span>
                            <span class="toolbar-item">
                                <input class="toolbar-trigger" type="submit" name="update" value="Toolbar Text"/>
                            </span>
                        </div>
                        <div class="toolbar-group">
                            <span class="toolbar-item active">
                                <input class="toolbar-trigger" type="submit" name="update" value="Active Toolbar"/>
                            </span>
                        </div>
                    </div>
                    <div class="toolbar-split toolbar-split-right">
                        <div class="toolbar-group">
                            <span class="toolbar-item">
                                <input class="toolbar-trigger" type="submit" name="update" value="Toolbar Text"/>
                            </span>
                            <span class="toolbar-item">
                                <input class="toolbar-trigger" type="submit" name="update" value="Toolbar Text"/>
                            </span>
                        </div>
                        <div class="toolbar-group">
                            <span class="toolbar-item active">
                                <input class="toolbar-trigger" type="submit" name="update" value="Active Toolbar"/>
                            </span>
                        </div>
                    </div>
                </div>
            </div>

            <div style="margin-bottom:2em;">
                <a name="new-form-butts"></a>
                <h3>New Form buttons</h3>
                <h5>Standalone class="aui-button"</h5>
                <button class="aui-button">Button</button>
                <input class="aui-button" value="Input Button" type="button" />
                <input class="aui-button" value="Input Submit" type="submit" />
                <a href="#" class="aui-button aui-button-link">Cancel</a>
                <br>
                <br>
                <button class="aui-button" disabled="disabled">Disabled Button</button>
                <input class="aui-button" value="Disabled Input Button" type="button" disabled="disabled" />
                <input class="aui-button" value="Disabled Input Submit" type="submit" disabled="disabled" />
                <h5>Within buttons-container (provides padding)</h5>
                <div class="buttons-container">
                    <button class="aui-button">Button</button>
                    <input class="aui-button" value="Input Button" type="button" />
                    <input class="aui-button" value="Input Submit" type="submit" />
                    <a href="#" class="aui-button aui-button-link">Cancel</a>
                </div>
            </div>

            <div style="margin-bottom:2em;">
                <h3>twixi</h3>
                <div id="<%--use id if you want the state saved to a cookie--%>" class="twixi-block collapsed">
                    <div class="twixi-trigger">
                        <h5><span class="icon icon-twixi"></span>Twixi trigger</h5>
                    </div>
                    <div class="twixi-content">
                        Twixi content
                    </div>
                </div>
            </div>
            <div style="margin-bottom:2em;">
                <h3>Options List (admin)</h3>
                <ul class="optionslist">
                    <li>Options list</li>
                    <li>Options list</li>
                    <li>Options list</li>
                </ul>
            </div>
            <div style="margin-bottom:2em;">
                <h3>Table</h3>
                <table class="aui aui-table-rowhover">
                    <thead>
                        <tr>
                            <th>Heading</th>
                            <th>Heading</th>
                            <th>Heading</th>
                            <th>Operations</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>Content</td>
                            <td>Content</td>
                            <td>Content</td>
                            <td>
                                <ul class="operations-list">
                                    <li><a href="#">Operations list</a></li>
                                    <li><a href="#">Operations list</a></li>
                                    <li><a href="#">Operations list</a></li>
                                    <li><a href="#">Operations list</a></li>
                                </ul>
                            </td>
                        </tr>
                        <tr>
                            <td>Content</td>
                            <td>Content</td>
                            <td>Content</td>
                            <td>
                                <ul class="operations-list">
                                    <li><a href="#">Operations list</a></li>
                                    <li><a href="#">Operations list</a></li>
                                    <li><a href="#">Operations list</a></li>
                                    <li><a href="#">Operations list</a></li>
                                </ul>
                            </td>
                        </tr>
                        <tr>
                            <td>Content</td>
                            <td>Content</td>
                            <td>Content</td>
                            <td>
                                <ul class="operations-list">
                                    <li><a href="#">Operations list</a></li>
                                    <li><a href="#">Operations list</a></li>
                                    <li><a href="#">Operations list</a></li>
                                    <li><a href="#">Operations list</a></li>
                                </ul>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>

    <div class="aui-dropdown2 aui-style-default" id="actions-menu">
        <ul>
            <li><a>Note that it doesn't matter where in the DOM this menu is defined...</a></li>
            <li><a>It only matters that its ID is used by an element with the <code>aria-owns</code> attribute.</a></li>
        </ul>
    </div>
</body>
</html>
<%
    ActionContextKit.resetContext();
%>
