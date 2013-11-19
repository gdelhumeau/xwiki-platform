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
package org.xwiki.wiki.test.po;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class CreateWikiPageStepUser extends ExtendedViewPage
{
    @FindBy(name = "wikiprettyname")
    private WebElement prettyNameField;

    @FindBy(name = "wikiname")
    private WebElement wikiNameField;

    @FindBy(name = "description")
    private WebElement descriptionField;

    @FindBy(name = "template")
    private WebElement templateField;

    @FindBy(name = "set_as_template")
    private WebElement setAsTemplateField;

    @FindBy(id = "wizard-next")
    private WebElement nextStepButton;

    @FindBy(id = "wizard-create")
    private WebElement createButton;

    public static String getSpace()
    {
        return "WikiManager";
    }

    public static String getPage()
    {
        return "CreateNewWiki";
    }

    public void setPrettyName(String prettyName)
    {
        prettyNameField.clear();
        prettyNameField.sendKeys(prettyName);
    }

    public String getName()
    {
        return wikiNameField.getText();
    }

    public void setDescription(String description)
    {
        descriptionField.clear();
        descriptionField.sendKeys(description);
    }

    public void setIsTemplate(boolean template)
    {
        if (template != setAsTemplateField.isSelected()) {
            setAsTemplateField.click();
        }
    }

    public void setTemplate(String templateId)
    {
        List<WebElement> elements = templateField.findElements(By.tagName("option"));
        for (WebElement element : elements) {
            if (element.getAttribute("value").equals(templateId)) {
                element.click();
            }
        }
    }

    public boolean isNextStepEnabled()
    {
        return nextStepButton.isEnabled();
    }

    public void goNextStep()
    {
        nextStepButton.click();
    }

    public void create()
    {
        createButton.click();
    }
}
