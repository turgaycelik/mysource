<%@ page import="com.atlassian.jira.security.Permissions,
                 com.atlassian.jira.ManagerFactory,
                 com.atlassian.crowd.embedded.api.User,
                 com.atlassian.seraph.auth.DefaultAuthenticator"%>
 <%--
  -- WebWork, Web Application Framework
  --
  -- Distributable under LGPL license.
  -- See terms of license at opensource.org
  --
  --
  -- userselect.jsp
  --
  -- Required Parameters:
  --   * label      - The description that will be used to identfy the control.
  --   * name       - The name of the attribute to put and pull the result from.
  --   * formname   - The name of the form on which the control is to be placed. This is so the value can be returned
  --   * userMode   - What mode of users should be returned. 1 = All users 2= Assignable Users etc

  -- Optional Parameters:
  --   * imageName   - determines what the image of the userselect will be called

  --   * labelposition   - determines were the label will be place in relation
  --                       to the control.  Default is to the left of the control.
  --   * size       - SIZE parameter of the HTML INPUT tag.
  --   * maxlength  - MAXLENGTH parameter of the HTML INPUT tag.
  --   * disabled   - DISABLED parameter of the HTML INPUT tag.
  --   * readonly   - READONLY parameter of the HTML INPUT tag.
  --   * onkeyup    - onkeyup parameter of the HTML INPUT tag.
  --   * tabindex  - tabindex parameter of the HTML INPUT tag.
  --   * onchange  - onkeyup parameter of the HTML INPUT tag.
  --%>
<%@ taglib uri="webwork" prefix="ww" %>
<jsp:include page="/template/standard/controlheader.jsp" />
<jsp:include page="/template/standard/textimagedisablingnoheaders.jsp" />
<jsp:include page="/template/standard/controlfooter.jsp" />
