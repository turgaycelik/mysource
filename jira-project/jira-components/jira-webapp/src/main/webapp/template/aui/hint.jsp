<%--
  -- WebWork, Web Application Framework
  --
  -- Distributable under LGPL license.
  -- See terms of license at opensource.org
  --
  --
  -- hint.jsp
  --
  -- Required Parameters:
  --   * hint       - The name of the attribute to put and pull the hint from.
  --   * tooltip    - The name of the attribute to put and pull the tooltip from. If not specified, hint will be used
  --%>
<%@ taglib uri="webwork" prefix="ww" %>

<ww:if test="parameters['hint']">
    <p
        <ww:if test="parameters['tooltip']">title="<ww:property value="parameters['tooltip']/trim" />"</ww:if>
        <ww:else>title="<ww:property value="parameters['hint']/trim" />"</ww:else>            
        class="hint-container overflow-ellipsis"
        id="<ww:property value="@jira.sitemesh.decorator.computed.id"/>hint">
        <ww:if test="parameters['hideLabel'] != true"><a class="shortcut-tip-trigger" href="#" title="<ww:text name="'usage.hints.title'"/>"><ww:text name="'usage.hints.label'"/></a></ww:if> <ww:property value="parameters['hint']" escape="false" />
    </p>
</ww:if>
<ww:else>
    <ww:property value="/randomHint">
        <ww:if test=". != null">
            <p title="<ww:property value="./tooltip"  escape="false" />"
               class="hint-container overflow-ellipsis"
               id="<ww:property value="@jira.sitemesh.decorator.computed.id"/>hint">
                <ww:if test="parameters['hideLabel'] != true"><a class="shortcut-tip-trigger" href="#" title="<ww:text name="'usage.hints.title'"/>"><ww:text name="'usage.hints.label'"/></a></ww:if> <ww:property value="./text" escape="false" />
            </p>
        </ww:if>
    </ww:property>
</ww:else>
