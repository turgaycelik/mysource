<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'keyboard.shortcuts.title'"/></title>
    <meta name="decorator" content="panel-general" />
</head>
<body>
    <page:applyDecorator id="update-keyboard-shortcuts" name="auiform">
        <page:param name="action">ViewKeyboardShortcuts.jspa</page:param>
        <%--
            This cancelLinkURI is kinda irrelevant really because when we display this as a dialog we attach an onClick
            handler that closes the current window. For that see formpopup.js, decorateContent method. 
        --%>
        <ww:if test="/inlineDialogMode == true">
            <page:param name="cancelLinkURI"><ww:url value="'#'" atltoken="false"/></page:param>
            <page:param name="cancelLinkText"><ww:text name="'admin.common.words.close'" /></page:param>
            <page:param name="showHint">true</page:param>
            <page:param name="hideHintLabel">true</page:param>
            <page:param name="hint">
                <ww:if test="/userLoggedIn == true">
                    <ww:if test = "/keyboardShortcutsEnabled == true">
                        <ww:text name="'keyboard.shortcuts.enabled'">
                            <ww:param name="'value0'"><strong></ww:param>
                            <ww:param name="'value1'"></strong></ww:param>
                            <ww:param name="'value2'"><a class="submit-link" href="#"></ww:param>
                            <ww:param name="'value3'"></a></ww:param>
                        </ww:text>
                    </ww:if>
                    <ww:else>
                        <ww:text name="'keyboard.shortcuts.disabled'">
                            <ww:param name="'value0'"><strong></ww:param>
                            <ww:param name="'value1'"></strong></ww:param>
                            <ww:param name="'value2'"><a class="submit-link" href="#"></ww:param>
                            <ww:param name="'value3'"></a></ww:param>
                        </ww:text>
                    </ww:else>
                </ww:if>
            </page:param>
            <page:param name="hintTooltip">
                <ww:if test="/userLoggedIn == true">
                    <ww:if test = "/keyboardShortcutsEnabled == true"><ww:text name="'keyboard.shortcuts.enabled.tooltip'"/></ww:if>
                    <ww:else><ww:text name="'keyboard.shortcuts.disabled.tooltip'"/></ww:else>
                </ww:if>
            </page:param>
        </ww:if>
        <ww:else>
            <page:param name="useCustomButtons">true</page:param>
        </ww:else>

    <fieldset class="hidden parameters">
            <input type="hidden" id="shortcutsTitle" value="<ww:text name="'keyboard.shortcuts.title'"/>">
        </fieldset>
        <h2><ww:text name="'keyboard.shortcuts.title'"/></h2>
        <div id="shortcutsmenu">
        <ww:iterator value="/shortcutsForContext/keySet" status="'status'">
            <ww:if test="/shortcutsForContext/(.)/size > 0">
            <ww:if test="@status/odd == true">
                <div class="module module-alternate">
            </ww:if>
            <ww:else>
                <div class="module">
            </ww:else>
                    <div class="mod-header">
                       <h3><ww:property value="/contextName(.)"/></h3>
                    </div>
                    <div class="mod-content">
                        <ul class="item-details">
                            <ww:iterator value="/shortcutsForContext/(.)">
                                <ww:if test="./hidden == false">
                                    <li>
                                        <dl>
                                            <dt><ww:property value="/i18nHelper/text(./descriptionI18nKey)"/>:</dt>
                                            <%-- ww:text doesn't work here, since the translated text may be coming from the plugins system.
                                                The webwork stack isn't smart enough for that.  The 18nHelper is. --%>
                                            <dd>
                                                <ww:if test="./descriptionI18nKey == 'admin.keyboard.shortcut.navigator.view.issue.desc'"><ww:property value="./prettyShortcut(/i18nHelper)" escape="false" /> <ww:text name="'common.words.or'"/> <kbd><ww:text name="'common.words.enter'"/></kbd></ww:if>
                                                <ww:else><ww:property value="./prettyShortcut(/i18nHelper)" escape="false" /></ww:else>
                                            </dd>
                                        </dl>
                                    </li>
                                </ww:if>
                            </ww:iterator>
                            <ww:if test="./toString() == 'global'">
                                <li>
                                    <dl>
                                        <dt><ww:text name="'common.forms.submit.help.label'"/>:</dt>
                                        <dd>
                                            <ww:iterator value="/formSubmitKeys" status="'status'">
                                                <kbd><ww:property value="."/></kbd><ww:if test="@status/last == false"> + </ww:if>
                                            </ww:iterator>
                                        </dd>
                                    </dl>
                                </li>
                            </ww:if>
                        </ul>
                    </div>
                </div>
            </ww:if>
        </ww:iterator>
        <ww:if test = "/keyboardShortcutsEnabled == true">
            <input type="hidden" name="keyboardShortcutsEnabled" value="false">
        </ww:if>
        <ww:else>
            <input type="hidden" name="keyboardShortcutsEnabled" value="true">
        </ww:else>
        </div>
    </page:applyDecorator>
</body>
</html>
