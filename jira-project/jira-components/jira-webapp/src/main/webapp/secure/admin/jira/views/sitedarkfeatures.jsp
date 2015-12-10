<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section"/>
    <meta name="admin.active.tab" content="dark_features"/>
    <meta name="decorator" content="admin"/>
    <title><ww:text name="'admin.darkfeatures.manage.heading'"/></title>
</head>
<body>
<div class="aui-group">
    <div id="site-dark-features" class="aui-item">
        <h2><ww:text name="'admin.darkfeatures.site.property'"/></h2>
        <ww:if test="/siteEnabledFeatures/size > 0">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'admin.darkfeatures.site.property.warning'"/></p>
                </aui:param>
            </aui:component>
            <ul id="site-enabled-features">
                <ww:iterator value="/siteEnabledFeatures" status="'status'">
                    <li>
                        <ww:property value="." /> (<a href="<ww:url page="SiteDarkFeatures!remove.jspa"><ww:param name="'featureKey'" value="." /></ww:url>"><ww:text name="'admin.common.words.disable'"/></a>)
                    </li>
                </ww:iterator>
            </ul>
        </ww:if>
        <ww:if test="/enabled('jira.site.darkfeature.admin') == false || /enabled('jira.user.darkfeature.admin') == false">
            <p><strong><ww:text name="'admin.darkfeatures.site.admin.add'"/></strong></p>
            <ul id="site-disabled-features">
                <ww:if test="/enabled('jira.site.darkfeature.admin') == false">
                    <li>
                        <ww:property value="'jira.site.darkfeature.admin'" /> (<a href="<ww:url page="SiteDarkFeatures.jspa"><ww:param name="'featureKey'" value="'jira.site.darkfeature.admin'" /></ww:url>"><ww:text name="'admin.common.words.enable'"/></a>)
                    </li>
                </ww:if>
                <ww:if test="/enabled('jira.user.darkfeature.admin') == false">
                    <li>
                        <ww:property value="'jira.user.darkfeature.admin'" /> (<a href="<ww:url page="SiteDarkFeatures.jspa"><ww:param name="'featureKey'" value="'jira.user.darkfeature.admin'" /></ww:url>"><ww:text name="'admin.common.words.enable'"/></a>)
                    </li>
                </ww:if>
            </ul>
        </ww:if>

        <page:applyDecorator id="dark-features" name="auiform">
            <page:param name="action">SiteDarkFeatures.jspa</page:param>
            <page:param name="method">post</page:param>
            <page:param name="cssClass">xtop-label</page:param>
            <page:param name="useCustomButtons">true</page:param>

            <page:applyDecorator name="auifieldset">
                <page:param name="cssClass">inline</page:param>
                <aui:textfield label="text('admin.darkfeatures.enable')" maxlength="255" id="'featureKey'" name="'featureKey'" theme="'aui'" />
                <aui:component template="formSubmit.jsp" theme="'aui'">
                    <aui:param name="'submitButtonName'">Add</aui:param>
                    <aui:param name="'submitButtonText'"><ww:text name="'common.forms.add'"/></aui:param>
                    <aui:param name="'id'">enable-dark-feature</aui:param>
                </aui:component>
            </page:applyDecorator>
        </page:applyDecorator>
    </div>
    <div id="system-dark-features" class="aui-item">
        <h2><ww:text name="'admin.darkfeatures.system.property'"/></h2>
        <ww:if test="/systemEnabledFeatures/size > 0">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'admin.darkfeatures.system.property.warning'"/></p>
                </aui:param>
            </aui:component>

            <ul>
                <ww:iterator value="/systemEnabledFeatures" status="'status'">
                    <li>
                        <ww:property value="." />
                    </li>
                </ww:iterator>
            </ul>
        </ww:if>
        <ww:else>
            <p><ww:text name="'admin.darkfeatures.no.system'"/></p>
        </ww:else>
    </div>
</div>
</body>
</html>