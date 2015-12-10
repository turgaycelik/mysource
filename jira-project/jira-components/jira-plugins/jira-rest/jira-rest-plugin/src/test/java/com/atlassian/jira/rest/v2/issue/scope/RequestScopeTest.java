package com.atlassian.jira.rest.v2.issue.scope;


import com.atlassian.plugins.rest.common.interceptor.MethodInvocation;
import org.easymock.classextension.EasyMock;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;

import static org.easymock.classextension.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test our custom RequestScope.
 */
public class RequestScopeTest
{
   @Test
   public void scopedBeanCacheShouldNotLeak() throws Exception
   {
       MethodInvocation methodInvocation = EasyMock.createNiceMock(MethodInvocation.class);
       replay(methodInvocation);

       RequestScope scope = new RequestScope();
       RequestScope.Request request = scope.beginRequest(methodInvocation);
       try
       {
           // this is what Spring calls when it needs a bean created
           scope.get("testBean", new ObjectFactory()
           {
               public Object getObject() throws BeansException
               {
                   return new Object();
               }
           });
       }
       finally
       {
           request.destroy();
       }

       // everything lives under the request, so if there are no references to it there are no leaks
       assertThat(scope.currentRequest(), equalTo(null));
   }
}
