<%@ page import="com.atlassian.jira.web.util.ExternalLinkUtilImpl,com.atlassian.jira.web.util.ExternalLinkUtil"%><%@ taglib uri="webwork" prefix="ww" %><%--
    Allows lookup of a simple external link. Sample usage:
    <ww:component name="'external.link.confluence.product.site'" template="externallink.jsp" >
        <ww:param name="'value0'">xxx</ww:param>
        <ww:param name="'value1'">yyy</ww:param>
        <ww:param name="'value2'">zzz</ww:param>
        <ww:param name="'value3'">aaa</ww:param>
    </ww:component>">

    Sample in code usage:
      com.atlassian.jira.web.util.ExternalLinkUtil externalLinkUtil = com.atlassian.jira.web.util.ExternalLinkUtilImpl.getInstance();
      request.setAttribute("externalLinkUtil", externalLinkUtil);
      externalLinkUtil.getProperty("external.link.confluence.product.site");

    The params, value0-3, allow you to put replacement tokens into the string with the syntax {0} - {3}.
--%><%
    ExternalLinkUtil externalLinkUtil = ExternalLinkUtilImpl.getInstance();
    request.setAttribute("externalLinkUtil", externalLinkUtil);
%><ww:if test="parameters['value0'] != true"><ww:if test="parameters['value1'] != true"><ww:if test="parameters['value2'] != true"><ww:if test="parameters['value3'] != true"><ww:property value="@externalLinkUtil/property(parameters['name'], parameters['value0'], parameters['value1'], parameters['value2'], parameters['value3'])" /></ww:if><ww:else><ww:property value="@externalLinkUtil/property(parameters['name'], parameters['value0'], parameters['value1'], parameters['value2'])" /></ww:else></ww:if><ww:else><ww:property value="@externalLinkUtil/property(parameters['name'], parameters['value0'], parameters['value1'])" /></ww:else></ww:if><ww:else><ww:property value="@externalLinkUtil/property(parameters['name'], parameters['value0'])" /></ww:else></ww:if><ww:else><ww:property value="@externalLinkUtil/property(parameters['name'])" /></ww:else>
