<%@ page import="webwork.action.CoreActionContext" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%--
    Renders the JIRA logo in a single HTML element; a DIV or an IMG depending on whether the browser requires the
    opacity fix.
    <ww:component name="'logo.name'" template="logoWithOpacity.jsp" >
        <ww:param name="'needsOpacityFix'"><%= logoNeedsOpacityFix %></ww:param>
        <ww:param name="'logoTitle'"><%= jiraTitle %></ww:param>
        <ww:param name="'logoUrl'"><%= jiraLogo %></ww:param>
        <ww:param name="'logoWidth'"><%= lookAndFeelBean.getLogoWidth() %></ww:param>
        <ww:param name="'logoHeight'"><%= lookAndFeelBean.getLogoHeight() %></ww:param>
    </ww:component>
--%>
<ww:if test="parameters['needsOpacityFix'] == 'true'">
    <div style="height:<ww:property value="parameters['logoHeight'])"/>px;width:<ww:property value="parameters['logoWidth'])"/>px;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='<ww:property value="parameters['logoUrl'])"/>', sizingMethod='scale')" alt="<ww:property value="parameters['logoTitle'])"/>"></div>
</ww:if>
<ww:else>
    <img class="logo" src="<ww:property value="parameters['logoUrl'])"/>" width="<ww:property value="parameters['logoWidth'])"/>" height="<ww:property value="parameters['logoHeight'])"/>" border="0" alt="<ww:property value="parameters['logoTitle'])"/>">
</ww:else>
