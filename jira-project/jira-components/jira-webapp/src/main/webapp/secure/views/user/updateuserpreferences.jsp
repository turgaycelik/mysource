<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<%@ taglib prefix="jira" uri="jiratags" %>
<page:applyDecorator id="update-user-preferences" name="auiform">
    <page:param name="action">UpdateUserPreferences.jspa</page:param>
    <page:param name="submitButtonName">Update</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.forms.update'"/></page:param>
    <page:param name="cancelLinkURI"><ww:url value="'/secure/ViewProfile.jspa'" atltoken="false"/></page:param>

<aui:component template="formHeading.jsp" theme="'aui'">
    <aui:param name="'text'"><ww:text name="'preferences.update.title'"/></aui:param>
</aui:component>

    <ww:if test="/remoteUser == null">
        <page:param name="useCustomButtons">true</page:param>
        <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
            <aui:param name="'messageHtml'">
                <p><ww:text name="'session.timeout.message.title'"/></p>
                <p>
                    <ww:text name="'preferences.must.log.in'">
                        <ww:param name="param0"><a href="<%=request.getContextPath()%>/login.jsp?os_destination=%2Fsecure%2FViewProfile.jspa"></ww:param>
                        <ww:param name="param1"></a></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>
    </ww:if>
    <ww:elseIf test="/remoteUser/name != /username">
        <page:param name="useCustomButtons">true</page:param>
        <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
            <aui:param name="'messageHtml'">
                <p><ww:text name="'preferences.own.profile'"/></p>
                <p>
                    <ww:text name="'editprofile.logged.in.as'">
                        <ww:param name="param0"><a href="<%=request.getContextPath()%>/secure/ViewProfile.jspa"><ww:property value="/remoteUser/displayName"/></a></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>
    </ww:elseIf>
    <ww:else>
        <page:applyDecorator name="auifieldset">
            <page:param name="legend"><ww:text name="'preferences.update.user.details'"/></page:param>

            <aui:component name="'username'" template="hidden.jsp" theme="'aui'" value="/username"/>

            <page:applyDecorator name="auifieldgroup">
                <page:param name="description"><ww:text name="'preferences.issuesPerPage'"/></page:param>
                <aui:textfield id="'pagesize'" label="text('preferences.issuesPerPage.label')" mandatory="'true'" maxlength="'255'" name="'userIssuesPerPage'" size="'short'" theme="'aui'"/>
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <page:param name="description"><ww:text name="'preferences.mailType'"/></page:param>
                <aui:select id="'mailtype'" label="text('preferences.mailType.label')" list="mimeTypes" listKey="'key'" listValue="'value'" name="'userNotificationsMimeType'" theme="'aui'"/>
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                 <page:param name="description">
                     <jira:feature-check featureKey="ondemand.language.beta">
                         <ww:text name="'preferences.locale.ondemand.beta.description'">
                             <ww:param name="value0"><a target='_blank' href='https://confluence.atlassian.com/x/fTIvEw'></ww:param>
                             <ww:param name="value1"></a></ww:param>
                         </ww:text>
                     </jira:feature-check>
                     <ww:if test="/showPluginHints == true">
                     <ww:text name="'preferences.locale.description'">
                     <ww:param name="value0"><a target='_blank' href='<ww:property value="/tacUrl()"/>'></ww:param>
                     <ww:param name="value1"></a></ww:param>
                     </ww:text>
                     </ww:if>
                 </page:param>

                <aui:select id="'locale'" label="text('preferences.locale')" list="/installedLocales" listKey="'key'" listValue="'value'" name="'userLocale'" theme="'aui'"/>
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <page:param name="type">aui-field-cascadingselect</page:param>
                <page:applyDecorator name="auifieldgroup">
                    <label for="timeZoneRegion"><ww:text name="'admin.timezone.zone'"/></label>
                    <select class="select cascadingselect-parent" id="timeZoneRegion" name="timeZoneRegion">
                        <ww:iterator value="/timeZoneRegions">
                              <option class="option-group-<ww:property value="./key"/>" value="<ww:property value="./key"/>" <ww:if test="/configuredTimeZoneRegion == ./key">selected="selected"</ww:if>  ><ww:property value="./displayName"/></option>
                        </ww:iterator>
                    </select>
                </page:applyDecorator>
                <page:applyDecorator name="auifieldgroup">
                    <page:param name="description"><ww:text name="'preferences.timezone.desc'"/></page:param>
                    <select class="select cascadingselect-child" id="defaultUserTimeZone" name="defaultUserTimeZone">
                        <ww:iterator value="/timeZoneInfos">
                              <option class="option-group-<ww:property value="./regionKey"/>" value="<ww:property value="./timeZoneId"/>" <ww:if test="/configuredTimeZoneId == ./timeZoneId">selected="selected"</ww:if>><ww:property value="./GMTOffset"/> <ww:property value="./city"/> </option>
                        </ww:iterator>
                    </select>
                </page:applyDecorator>
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <page:param name="description"><ww:text name="'preferences.notification.desc'"/></page:param>
                <aui:select id="'own-notifications'" label="text('preferences.notification.label')" list="/ownChangesList" listKey="'id'" listValue="'name'" name="'notifyOwnChanges'" theme="'aui'" value="notifyOwnChanges"/>
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <page:param name="description"><ww:text name="'preferences.default.share.desc'"/></page:param>
                <aui:select id="'sharing'" label="text('preferences.default.share.title')" list="/shareList" listKey="'id'" listValue="'name'" name="'shareDefault'" theme="'aui'" value="shareValue"/>
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <page:param name="description"><ww:text name="'preferences.keyboard.shortcuts.desc'"/></page:param>
                <aui:select id="'keyboard-shortcuts'" label="text('preferences.keyboard.shortcuts.title')" list="/keyboardShortcutList" listKey="'id'" listValue="'name'" name="'keyboardShortcutsEnabled'" theme="'aui'" value="keyboardShortcutValue"/>
            </page:applyDecorator>

            <ww:if test="/showAutowatch == true">
                <page:applyDecorator name="auifieldgroup">
                    <page:param name="description"><ww:text name="'preferences.autowatch.desc'"/></page:param>
                    <aui:select id="'auto-watch-preference'" label="text('preferences.autowatch.title')" list="/autowatchList" listKey="'id'" listValue="'name'" name="'autoWatchPreference'" theme="'aui'" value="autoWatchPreference"/>
                </page:applyDecorator>
            </ww:if>

        </page:applyDecorator>
    </ww:else>
</page:applyDecorator>