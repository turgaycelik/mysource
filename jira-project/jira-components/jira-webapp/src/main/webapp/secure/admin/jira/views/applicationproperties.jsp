<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section"/>
    <meta name="admin.active.tab" content="general_configuration"/>
	<title><ww:text name="'admin.generalconfiguration.jira.configuration'"/></title>
</head>
<%-- Use data-property-id to identify calls containing property values for testing --%>
<body>
    <ww:if test="hasErrorMessages == 'true'">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'titleText'"><ww:text name="'admin.common.words.errors'"/></aui:param>
            <aui:param name="'messageHtml'">
                <ul>
                    <ww:iterator value="errorMessages">
                        <li><ww:property /></li>
                    </ww:iterator>
                </ul>
            </aui:param>
        </aui:component>
    </ww:if>

    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'mainContent'">
            <h2><ww:text name="'admin.common.words.settings'"/></h2>
        </ui:param>
        <ui:param name="'actionsContent'">
            <ww:if test="/systemAdministrator == true">
                <div class="aui-buttons">
                    <a class="aui-button" id="edit-advanced-properties" href="AdvancedApplicationProperties.jspa"><ww:text name="'admin.advancedproperties.settings.heading'"/></a>
                </div>
            </ww:if>
            <div class="aui-buttons">
                <a class="aui-button" id="edit-app-properties" href="EditApplicationProperties!default.jspa"><ww:text name="'admin.common.phrases.edit.settings'"/></a>
            </div>
        </ui:param>
    </ui:soy>

    <table class="aui">
        <caption>
            <ww:text name="'admin.menu.general.settings'"/>
            <ui:component template="help.jsp" name="'configuration'" >
                <ww:param name="'helpURLFragment'">#settings</ww:param>
            </ui:component>
        </caption>
        <tbody>
            <tr>
                <td width="40%"><b><ww:text name="'admin.common.words.title'"/></b></td>
                <td width="60%" data-property-id="title"><ww:property value="applicationProperties/string('jira.title')"/></td>
            </tr>

            <ww:if test="unifiedUserManagementEnabled == false">
                <tr>
                    <td><b><ww:text name="'admin.common.words.mode'"/></b></td>
                    <td data-property-id="jira-mode"><ww:property value="/jiraMode"/></td>
                </tr>
            </ww:if>

            <tr id="maximumAuthenticationAttemptsAllowed">
                <td><b><ww:text name="'admin.generalconfiguration.maximum.authentication.attempts.allowed'"/></b></td>
                <ww:if test="applicationProperties/defaultBackedString('jira.maximum.authentication.attempts.allowed') != null && applicationProperties/defaultBackedString('jira.maximum.authentication.attempts.allowed')/length() > 0">
                    <td data-property-id="maximumAuthenticationAttemptsAllowed"><ww:property value="applicationProperties/defaultBackedString('jira.maximum.authentication.attempts.allowed')" /></td>
                </ww:if>
                <ww:else>
                    <td data-property-id="maximumAuthenticationAttemptsAllowed"><ww:text name="'common.words.unlimited'"/></td>
                </ww:else>
            </tr>

            <ww:if test="unifiedUserManagementEnabled == false">
                <tr>
                    <td><b><ww:text name="'admin.generalconfiguration.captcha.on.signup'"/></b></td>
                    <td>
                        <ww:if test="applicationProperties/option('jira.option.captcha.on.signup') == true">
                            <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                        </ww:if>
                        <ww:else>
                            <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                        </ww:else>
                    </td>
                </tr>
            </ww:if>

            <tr>
                <td><b><ww:text name="'admin.generalconfiguration.base.url'"/></b></td>
                <td><a href="<ww:property value="applicationProperties/string('jira.baseurl')"/>" target="_blank"><ww:property value="applicationProperties/string('jira.baseurl')"/></a></td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.generalconfiguration.email.from.header'"/></b></td>
                <td><ww:property value="applicationProperties/defaultBackedString('jira.email.fromheader.format')"/></td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.common.words.introduction'"/></b></td>
                <td><ww:property value="/introductionProperty/viewHtml" escape="false" /></td>
            </tr>
        </tbody>
    </table>

    <table class="aui" id="language-info">
        <caption>
            <ww:text name="'admin.generalconfiguration.internationalisation'"/>
            <ui:component template="help.jsp" name="'configuration'" >
                <ww:param name="'helpURLFragment'">#settings</ww:param>
            </ui:component>
        </caption>
        <tbody>
            <tr>
                <td width="40%"><b><ww:text name="'admin.generalconfiguration.indexing.language'"/></b></td>
                <td width="60%"><ww:property value="./currentIndexingLanguageDescription" escape="false" /></td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.generalconfiguration.installed.languages'"/></b></td>
                <td>
                    <ww:iterator value="/localeManager/installedLocales" status="'status'">
                        <ww:property value="/displayNameOfLocale(.)"/><ww:if test="@status/last == false"><br></ww:if>
                    </ww:iterator>
                    <ww:if test="/showPluginHints == true">
                        <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.message.info'">
                            <ui:param name="'content'">
                                <p>
                                    <ww:text name="'admin.generalconfiguration.internationalisation.description'" >
                                        <ww:param name="value0"><a href="<%= request.getContextPath() %>/plugins/servlet/upm"></ww:param>
                                        <ww:param name="value1"></a></ww:param>
                                        <ww:param name="value2"><a target='_blank' href='<ww:property value="/tacUrl()"/>'></ww:param>
                                        <ww:param name="value3"></a></ww:param>
                                    </ww:text>
                                </p>
                            </ui:param>
                        </ui:soy>
                    </ww:if>
                </td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.generalconfiguration.default.language'"/></b></td>
                <td><ww:property value="/displayNameOfLocale(/applicationProperties/defaultLocale)" /></td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.generalconfiguration.default.timezone'"/></b></td>
                <td><ww:if test="/useSystemTimeZone == true"><ww:text name="'admin.timezone.system.default'"/>: </ww:if><ww:property value="/defaultTimeZoneInfo/GMTOffset"/> <ww:property value="/defaultTimeZoneInfo/city"/></td>
            </tr>
        </tbody>
    </table>

    <table class="aui" id="options_table">
        <caption>
            <ww:text name="'admin.common.words.options'"/>
            <ui:component template="help.jsp" name="'configuration'" >
                <ww:param name="'helpURLFragment'">#settings</ww:param>
            </ui:component>
        </caption>
        <tbody>
            <tr>
                <td width="40%"><b><ww:text name="'admin.generalconfiguration.allow.users.to.vote'"/></b></td>
                <td width="60%">
                    <ww:if test="applicationProperties/option('jira.option.voting') == true">
                        <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                    </ww:if>
                    <ww:else>
                        <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                    </ww:else>
                </td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.generalconfiguration.allow.users.to.watch'"/></b></td>
                <td>
                    <ww:if test="applicationProperties/option('jira.option.watching') == true">
                        <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                    </ww:if>
                    <ww:else>
                        <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                    </ww:else>
                </td>
            </tr>
            <tr id="maximumLengthProjectNames">
                <td><b><ww:text name="'admin.generalconfiguration.maximum.length.project.names'"/></b></td>
                <ww:if test="applicationProperties/defaultBackedString('jira.projectname.maxlength') != null && applicationProperties/defaultBackedString('jira.projectname.maxlength')/length() > 0">
                    <td data-property-id="maximumLengthProjectNames"><ww:property value="applicationProperties/defaultBackedString('jira.projectname.maxlength')" /></td>
                </ww:if>
                <ww:else>
                    <td data-property-id="maximumLengthProjectNames"><ww:text name="'common.words.unlimited'"/></td>
                </ww:else>
            </tr>
            <tr id="maximumLengthProjectKeys">
                <td><b><ww:text name="'admin.generalconfiguration.maximum.length.project.keys'"/></b></td>
                <ww:if test="applicationProperties/defaultBackedString('jira.projectkey.maxlength') != null && applicationProperties/defaultBackedString('jira.projectkey.maxlength')/length() > 0">
                    <td data-property-id="maximumLengthProjectKeys"><ww:property value="applicationProperties/defaultBackedString('jira.projectkey.maxlength')" /></td>
                </ww:if>
                <ww:else>
                    <td data-property-id="maximumLengthProjectKeys"><ww:text name="'common.words.unlimited'"/></td>
                </ww:else>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.generalconfiguration.allow.unassigned.issues'"/></b></td>
                <td>
                    <ww:if test="applicationProperties/option('jira.option.allowunassigned') == true">
                        <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                    </ww:if>
                    <ww:else>
                        <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                    </ww:else>
                </td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.generalconfiguration.external.user.management'"/></b></td>
                <td>
                    <ww:if test="applicationProperties/option('jira.option.user.externalmanagement') == true">
                        <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                    </ww:if>
                    <ww:else>
                        <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                    </ww:else>
                </td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.generalconfiguration.logout.confirmation'"/></b></td>
                <td><strong>
                    <ww:if test="applicationProperties/defaultBackedString('jira.option.logoutconfirm') == 'never'">
                        <ww:text name="'admin.common.words.never'"/>
                    </ww:if>
                    <ww:elseIf test="applicationProperties/defaultBackedString('jira.option.logoutconfirm') == 'cookie'">
                        <ww:text name="'admin.common.words.cookie'"/>
                    </ww:elseIf>
                    <ww:else>
                        <ww:text name="'admin.common.words.always'"/>
                    </ww:else>
                    </strong>
                </td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.generalconfiguration.use.gzip.compression'"/></b></td>
                <td>
                    <ww:if test="/gzipCompression/enabled == true">
                        <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                    </ww:if>
                    <ww:else>
                        <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                    </ww:else>
                </td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.generalconfiguration.accept.remote.api.calls'"/></b></td>
                <td>
                    <ww:if test="applicationProperties/option('jira.option.rpc.allow') == true">
                        <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                    </ww:if>
                    <ww:else>
                        <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                    </ww:else>
                </td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.generalconfiguration.user.email.visibility'"/></b></td>
                <td><strong>
                    <ww:if test="applicationProperties/defaultBackedString('jira.option.emailvisible') == 'show'">
                        <ww:text name="'admin.generalconfiguration.public'"/>
                    </ww:if>
                    <ww:elseIf test="applicationProperties/defaultBackedString('jira.option.emailvisible') == 'hide'">
                        <ww:text name="'admin.generalconfiguration.hidden'"/>
                    </ww:elseIf>
                    <ww:elseIf test="applicationProperties/defaultBackedString('jira.option.emailvisible') == 'mask'">
                        <ww:text name="'admin.generalconfiguration.masked'"><ww:param name="value0">user at example dot com</ww:param></ww:text>
                    </ww:elseIf>
                    <ww:else>
                        <ww:text name="'admin.generalconfiguration.logged.in.only'"/>
                    </ww:else>
                    </strong>
                </td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.generalconfiguration.commentlevel.visibility'"/></b></td>
                <td>
                    <ww:if test="applicationProperties/option('jira.comment.level.visibility.groups') == true">
                        <strong><ww:text name="'admin.generalconfiguration.commentlevel.visibility.both'"/></strong>
                    </ww:if>
                    <ww:else>
                        <strong><ww:text name="'admin.generalconfiguration.commentlevel.visibility.rolesonly'"/></strong>
                    </ww:else>
                </td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.generalconfiguration.enable.email.header'">
                    <ww:param name="'value0'"></b><br></ww:param>
                    <ww:param name="'value1'">Precedence: bulk</ww:param>
                </ww:text></td>
                <td>
                    <ww:if test="applicationProperties/option('jira.option.precedence.header.exclude') == true">
                        <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                    </ww:if>
                    <ww:else>
                        <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                    </ww:else>
                </td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.generalconfiguration.enable.ajax.issue.picker'" /></b></td>
                <td>
                    <ww:if test="applicationProperties/option('jira.ajax.autocomplete.issuepicker.enabled') == true">
                        <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                    </ww:if>
                    <ww:else>
                        <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                    </ww:else>
                </td>
            </tr>
            <tr>
                 <td><b><ww:text name="'admin.generalconfiguration.enabled.jql.autocomplete'" /></b></td>
                 <td>
                     <ww:if test="applicationProperties/option('jira.jql.autocomplete.disabled') == false">
                         <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                     </ww:if>
                     <ww:else>
                         <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                     </ww:else>
                 </td>
            </tr>
            <ww:if test="/systemAdministrator == true">
                <tr>
                    <td><b><ww:text name="'admin.generalconfiguration.ie.mime.sniffing'"/></b></td>
                    <td data-property-id="ie-mime-sniffing"><strong>
                        <ww:if test="applicationProperties/defaultBackedString('jira.attachment.download.mime.sniffing.workaround') == 'workaround'">
                            <ww:text name="'admin.generalconfiguration.ie.mime.sniffing.workaround'"/>
                        </ww:if>
                        <ww:elseIf test="applicationProperties/defaultBackedString('jira.attachment.download.mime.sniffing.workaround') == 'secure'">
                            <ww:text name="'admin.generalconfiguration.ie.mime.sniffing.paranoid'"/>
                        </ww:elseIf>
                        <ww:elseIf test="applicationProperties/defaultBackedString('jira.attachment.download.mime.sniffing.workaround') == 'insecure'">
                            <ww:text name="'admin.generalconfiguration.ie.mime.sniffing.owned'"><ww:param name="value0">user at example dot com</ww:param></ww:text>
                        </ww:elseIf>
                        <ww:else>
                            <ww:text name="'admin.generalconfiguration.logged.in.only'"/>
                        </ww:else>
                        </strong>
                    </td>
                </tr>
            </ww:if>
            <tr>
                <td><b><ww:text name="'admin.generalconfiguration.show.contact.administrators.form'" /></b></td>
                <td data-property-id="contact-admin-form">
                    <ww:if test="applicationProperties/option('jira.show.contact.administrators.form') == true">
                        <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                    </ww:if>
                    <ww:else>
                        <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                    </ww:else>
                </td>
            </tr>
            <tr>
                <td><b><ww:text name="'admin.generalconfiguration.contact.administrators.message'" /></b></td>
                <td>
                    <ww:property value="/contactAdministratorsMessage" escape="false"/>
                </td>
            </tr>
            <%-- - Use Gravatar --%>
            <tr>
                <td><b><ww:text name="'admin.useravatar.gravatar.option'"></ww:text></b></td>
                <td data-property-id="use-gravatar">
                    <ww:if test="/useGravatar == true">
                        <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                        <ww:if test="/gravatarApiAddress">
                        - <ww:property value="/gravatarApiAddress"/>
                        </ww:if>
                    </ww:if>
                    <ww:else>
                        <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                    </ww:else>
                </td>
            </tr>

            <%-- Inline edit --%>
            <ww:if test="showDisableInlineEdit == 'true'">
                <tr data-property-id="disableInlineEdit">
                    <td><b><ww:text name="'admin.generalconfiguration.inline.edit'"></ww:text></b></td>
                    <td>
                        <ww:if test="/disableInlineEdit == false">
                            <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                        </ww:if>
                        <ww:else>
                            <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                        </ww:else>
                    </td>
                </tr>
            </ww:if>

            <ww:if test="showDisableCriteriaAutoUpdate == 'true'">
                <tr data-property-id="disableCriteriaAutoUpdate">
                    <td><b><ww:text name="'jira.issuenav.criteria.update.label'"></ww:text></b></td>
                    <td>
                        <ww:if test="/criteriaAutoUpdate == true">
                            <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                        </ww:if>
                        <ww:else>
                            <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                        </ww:else>
                    </td>
                </tr>
            </ww:if>
            <ww:if test="productRecommendationsAvailable == 'true'">
                <tr>
                    <td><b><ww:text name="'admin.generalconfiguration.show.product.recommendations.label'"></ww:text></b></td>
                    <td>
                        <ww:if test="productRecommendations == true">
                            <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                        </ww:if>
                        <ww:else>
                            <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                        </ww:else>
                    </td>
                </tr>
            </ww:if>
            <ww:if test="showProjectDescriptionHtmlEnabled == 'true'">
                <tr data-property-id="project-description-mode">
                    <td><b><ww:text name="'admin.generalconfiguration.project.description.html.enabled.label'"></ww:text></b></td>
                    <td><strong>
                        <ww:if test="projectDescriptionHtmlEnabled == true">
                            <strong class="status-active"><ww:text name="'admin.common.words.on'"/></strong>
                        </ww:if>
                        <ww:else>
                            <strong class="status-inactive"><ww:text name="'admin.common.words.off'"/></strong>
                        </ww:else>
                        </strong>
                    </td>
                </tr>
            </ww:if>
        </tbody>
    </table>
</body>
</html>
