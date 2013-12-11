/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.wiki.rest.internal;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.rest.XWikiRestComponent;
import org.xwiki.rest.model.jaxb.Wiki;
import org.xwiki.test.mockito.MockitoComponentMockingRule;
import org.xwiki.wiki.descriptor.WikiDescriptor;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;
import org.xwiki.wiki.manager.WikiManager;
import org.xwiki.wiki.provisioning.WikiProvisioningJob;
import org.xwiki.wiki.rest.WikiManagerREST;
import org.xwiki.wiki.template.WikiTemplateManager;

import com.xpn.xwiki.XWikiContext;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultWikiManagerRESTTest
{
    @Rule
    public MockitoComponentMockingRule<WikiManagerREST> mocker =
            new MockitoComponentMockingRule(DefaultWikiManagerREST.class, XWikiRestComponent.class, "wikimanager");

    private WikiManager wikiManager;

    private WikiDescriptorManager wikiDescriptorManager;

    private WikiTemplateManager wikiTemplateManager;

    private EntityReferenceSerializer<String> entityReferenceSerializer;

    private Execution execution;

    private XWikiContext xcontext;

    @Before
    public void setUp() throws Exception
    {
        execution = mock(Execution.class);
        mocker.registerComponent(Execution.class, execution);
        xcontext = mock(XWikiContext.class);
        ExecutionContext executionContext = mock(ExecutionContext.class);
        when(execution.getContext()).thenReturn(executionContext);
        when(executionContext.getProperty("xwikicontext")).thenReturn(xcontext);
        wikiManager = mocker.getInstance(WikiManager.class);
        wikiDescriptorManager = mocker.getInstance(WikiDescriptorManager.class);
        wikiTemplateManager = mocker.getInstance(WikiTemplateManager.class);
        entityReferenceSerializer = mocker.getInstance(new DefaultParameterizedType(null,
                EntityReferenceSerializer.class, String.class));
    }

    @Test
    public void createWiki() throws Exception
    {
        // Mocks
        WikiDescriptor descriptor = new WikiDescriptor("newwiki", "newwiki");
        when(wikiManager.create("newwiki", "template", true)).thenReturn(descriptor);
        WikiProvisioningJob job = mock(WikiProvisioningJob.class);
        when(wikiTemplateManager.applyTemplate("newwiki", "template")).thenReturn(job);

        // Call the method
        Wiki wiki = new Wiki();
        wiki.setDescription("My new wiki");
        wiki.setId("newwiki");
        wiki.setName("New Wiki");
        wiki.setOwner("xwiki:XWiki.Admin");
        Response response = mocker.getComponentUnderTest().createWiki("template", wiki);

        // Verify
        WikiDescriptor expectedDescriptor = new WikiDescriptor("newwiki", "newwiki");
        expectedDescriptor.setPrettyName("New Wiki");
        expectedDescriptor.setOwnerId("xwiki:XWiki.Admin");
        verify(job).join();
        verify(wikiDescriptorManager).saveDescriptor(eq(expectedDescriptor));
        verify(wikiTemplateManager).applyTemplate("newwiki", "template");
    }
}
