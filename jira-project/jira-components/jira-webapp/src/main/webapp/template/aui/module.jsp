<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%--
    PARAMETERS: (required)
    contentHtml - html content which will be wrapped inside the content div

    PARAMETERS: (optional)
    id			- id for module
    cssClass    - CSS classes added after "module". No spaces, all lowercase, hyphens
                  instead of underscores, must start with a-z)
    headingText - text to go in the <h3>. This is required for the operations to show or for the Twixi functionality
    operations  - markup for the operations icons like on View Issue. The markup should match what we use there.
    twixi       - set to true if you want to trigger generic twixi functionality on this module
--%>
<%-- Define variables up here to keep logic separate from the markup --%>
<ww:declare id="varId" value="parameters['id']"><ww:if test="."> id="<ww:property value="."/>"</ww:if></ww:declare>
<ww:declare id="varCssClass" value="parameters['cssClass']"><ww:if test="."> <ww:property value="."/></ww:if></ww:declare>
<ww:if test="parameters['headingText'] && parameters['headingText'] != ''">
    <ww:declare id="varOperations" value="parameters['operations']"><ww:if test="."><ww:property value="." /></ww:if></ww:declare>
    <ww:declare id="varHeadingText" value="parameters['headingText']"><ww:if test="."><ww:property value="." /></ww:if></ww:declare>
    <ww:property id="@varOperations" value="parameters['operations']" />
    <ww:property id="@varHeadingText" value="parameters['headingText']" />
    <ww:if test="parameters['twixi'] == 'true'">
        <ww:declare id="varTwixiBlockClass"> toggle-wrap twixi-block</ww:declare>
        <ww:declare id="varTwixiTriggerClass"> class="toggle-title twixi-trigger"</ww:declare>
        <ww:declare id="varTwixiContentClass"> twixi-content</ww:declare>
    </ww:if>
</ww:if>
<%-- Template starts here --%>
<div<ww:property value="@varId" escape="false" /> class="module<ww:property value="@varCssClass" /><ww:property value="@varTwixiBlockClass" />">
    <ww:if test="parameters['headingText'] && parameters['headingText'] != ''">
        <div class="mod-header">
            <ww:property value="@varOperations" escape="false" />
            <h3<ww:property value="@varTwixiTriggerClass" escape="false" />><ww:property value="@varHeadingText" escape="false" /></h3>
        </div>
    </ww:if>
    <div class="mod-content<ww:property value="@varTwixiContentClass" />">
        <ww:property value="parameters['contentHtml']" escape="false" />
    </div>
</div>
<%-- Clear out the variables so they don't leak to other components--%>
<ww:property id="varId" value="''"/>
<ww:property id="varCssClass" value="''"/>
<ww:property id="varOperations" value="''"/>
<ww:property id="varHeadingText" value="''"/>
<ww:property id="varContentHtml" value="''"/>
<ww:property id="varTwixiBlockClass" value="''"/>
<ww:property id="varTwixiTriggerClass" value="''"/>
<ww:property id="varTwixiContentClass" value="''"/>