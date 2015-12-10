<%--
  -- WebWork, Web Application Framework
  --
  -- Distributable under LGPL license.
  -- See terms of license at opensource.org
  --
  --
  -- issuelabel.jsp
  --
  -- Make the 'tag text' link to the Issue via /browse/{issue key}
  --
  -- Required Parameters:
  --   * label      - The description that will be used to identfy the control.
  --   * name       - The name of the attribute to put and pull the result from.
  --
  -- Optional Parameters:
  --   * labelposition   - determines were the label will be place in relation
  --                       to the control.  Default is to the left of the control.
  --
  --%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ include file="/template/standard/controlheader.jsp" %>

      <ww:property value="parameters['nameValue']">
         <ww:if test="."><a href="<%=request.getContextPath()%>/browse/<ww:property value="."/>"><ww:property value="."/></a></ww:if>
      </ww:property>

	  <ww:property value="parameters['texthtml']">
         <ww:if test="."><ww:property value="."/></ww:if>
      </ww:property>

<%@ include file="/template/standard/controlfooter.jsp" %>
