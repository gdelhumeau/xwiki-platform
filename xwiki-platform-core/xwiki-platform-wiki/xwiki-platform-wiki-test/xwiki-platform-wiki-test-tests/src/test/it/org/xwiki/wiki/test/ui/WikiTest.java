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
package org.xwiki.wiki.test.ui;

import org.junit.Rule;
import org.junit.Test;
import org.xwiki.test.ui.AbstractTest;
import org.xwiki.test.ui.SuperAdminAuthenticationRule;
import org.xwiki.test.ui.po.editor.WikiEditPage;
import org.xwiki.wiki.test.po.CreateWikiPage;
import org.xwiki.wiki.test.po.WikiHomePage;
import org.xwiki.wiki.test.po.WikiIndexPage;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * UI tests for the Wiki application.
 *
 * @version $Id$
 * @since 5.3RC1
 */
public class WikiTest extends AbstractTest
{
    @Rule
    public SuperAdminAuthenticationRule superAdminAuthenticationRule =
            new SuperAdminAuthenticationRule(getUtil(), getDriver());

    private String TEMPLATE_CONTENT = "Content of the template";

    public void createTemplateWiki() throws Exception
    {
        WikiIndexPage wikiIndexPage = WikiIndexPage.gotoPage();
        CreateWikiPage createWikiPage = wikiIndexPage.createWiki();
        createWikiPage.setPrettyName("My new template");
        // Let 100 ms to the javascript code to be executed
        Thread.sleep(100);
        // test that the wiki identifier is correctly computed
        //assertEquals(createWikiPage.getName(), "mynewtemplate");

        createWikiPage.setDescription("This is the template I do for the tests");
        createWikiPage.setIsTemplate(true);

        assertTrue(createWikiPage.isNextStepEnabled());

        createWikiPage.goNextStep();
        createWikiPage.create();

        // Modify the template content
        WikiHomePage wikiHomePage = new WikiHomePage();
        WikiEditPage wikiEditPage = wikiHomePage.editWiki();
        wikiEditPage.setContent(TEMPLATE_CONTENT);
        wikiEditPage.clickSaveAndView();

        // Verify the template is in the list of templates in the wizard
        CreateWikiPage createWikiPage2 = wikiHomePage.createWiki();
        assertTrue(createWikiPage2.getTemplateList().contains("mynewtemplate"));
    }

    @Test
    public void createWikiFromTemplate() throws Exception
    {
        createTemplateWiki();

        WikiIndexPage wikiIndexPage = WikiIndexPage.gotoPage();
        CreateWikiPage createWikiPage = wikiIndexPage.createWiki();
        createWikiPage.setPrettyName("My new wiki");
        createWikiPage.setTemplate("mynewtemplate");
        createWikiPage.setIsTemplate(false);
        createWikiPage.setDescription("My first wiki");
        createWikiPage.goNextStep();
        createWikiPage.create();
        assertTrue(createWikiPage.isProvisioningStep());
        // Wait during the provisioning step
        long timeout = System.currentTimeMillis() + 5000;
        while (!createWikiPage.isFinalizeButtonEnabled() && System.currentTimeMillis() < timeout) {
            Thread.sleep(100);
        }
        // Verify that the provisioning step is over
        assertTrue(createWikiPage.isFinalizeButtonEnabled());
        // Finalize
        createWikiPage.finalize();

        // Verify the content is the same than in the template
        WikiHomePage wikiHomePage = new WikiHomePage();
        WikiEditPage wikiEditPage = wikiHomePage.editWiki();
        assertEquals(wikiEditPage.getContent(), TEMPLATE_CONTENT);

    }
}
