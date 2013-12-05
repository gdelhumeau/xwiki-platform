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
package org.xwiki.wiki.internal.descriptor.document;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.sheet.SheetBinder;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.internal.mandatory.AbstractMandatoryDocumentInitializer;
import com.xpn.xwiki.objects.classes.BaseClass;

/**
 * Update the XWiki.XWikiServerClass document with all required information.
 * 
 * @version $Id$
 * @since 5.0M2
 */
@Component
@Named("XWiki.XWikiServerClass")
@Singleton
public class XWikiServerClassDocumentInitializer extends AbstractMandatoryDocumentInitializer
{
    /**
     * The name of the mandatory document.
     */
    public static final String DOCUMENT_NAME = "XWikiServerClass";

    /**
     * Reference to the server class.
     */
    public static final EntityReference SERVER_CLASS =  new EntityReference(DOCUMENT_NAME, EntityType.DOCUMENT,
            new EntityReference(XWiki.SYSTEM_SPACE, EntityType.SPACE));

    /**
     * Default list separators of XWiki.XWikiServerClass fields.
     */
    public static final String DEFAULT_FIELDS_SEPARATOR = "|";

    /**
     * Name of field <code>prettyname</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELD_WIKIPRETTYNAME = "wikiprettyname";

    /**
     * Pretty name of field <code>prettyname</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELDPN_WIKIPRETTYNAME = "Wiki pretty name";

    /**
     * Name of field <code>owner</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELD_OWNER = "owner";

    /**
     * Pretty name of field <code>owner</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELDPN_OWNER = "Owner";

    /**
     * Name of field <code>description</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELD_DESCRIPTION = "description";

    /**
     * Pretty name of field <code>description</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELDPN_DESCRIPTION = "Description";

    /**
     * Name of field <code>server</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELD_SERVER = "server";

    /**
     * Pretty name of field <code>server</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELDPN_SERVER = "Server";

    /**
     * Name of field <code>visibility</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELD_VISIBILITY = "visibility";

    /**
     * First possible values for <code>visibility</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELDL_VISIBILITY_PUBLIC = "public";

    /**
     * Second possible values for <code>visibility</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELDL_VISIBILITY_PRIVATE = "private";

    /**
     * List of possible values for <code>visibility</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELDL_VISIBILITY = FIELDL_VISIBILITY_PUBLIC + DEFAULT_FIELDS_SEPARATOR
        + FIELDL_VISIBILITY_PRIVATE + DEFAULT_FIELDS_SEPARATOR;

    /**
     * Pretty name of field <code>visibility</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELDPN_VISIBILITY = "Visibility";

    /**
     * Name of field <code>state</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELD_STATE = "state";

    /**
     * First possible values for <code>state</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELDL_STATE_ACTIVE = "active";

    /**
     * Second possible values for <code>state</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELDL_STATE_INACTIVE = "inactive";

    /**
     * Third possible values for <code>state</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELDL_STATE_LOCKED = "locked";

    /**
     * List of possible values for <code>state</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELDL_STATE = FIELDL_STATE_ACTIVE + DEFAULT_FIELDS_SEPARATOR + FIELDL_STATE_INACTIVE
        + DEFAULT_FIELDS_SEPARATOR + FIELDL_STATE_LOCKED;

    /**
     * Pretty name of field <code>state</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELDPN_STATE = "State";

    /**
     * Name of field <code>language</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELD_LANGUAGE = "language";

    /**
     * List of possible values for <code>language</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELDL_LANGUAGE = "en|fr";

    /**
     * Pretty name of field <code>language</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELDPN_LANGUAGE = "Language";

    /**
     * Name of field <code>secure</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELD_SECURE = "secure";

    /**
     * Pretty name of field <code>secure</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELDPN_SECURE = "Secure";

    /**
     * Display type of field <code>secure</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELDDT_SECURE = "checkbox";

    /**
     * Default value of field <code>secure</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final Boolean DEFAULT_SECURE = Boolean.FALSE;

    /**
     * Name of field <code>homepage</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELD_HOMEPAGE = "homepage";

    /**
     * Pretty name of field <code>homepage</code> for the XWiki class XWiki.XWikiServerClass.
     */
    public static final String FIELDPN_HOMEPAGE = "Home page";

    /**
     * Page name of the sheet associated to the current document.
     */
    private static final String CLASS_SHEET_PAGE_NAME = "XWikiServerClassSheet";

    /**
     * Used to bind a class to a document sheet.
     */
    @Inject
    @Named("class")
    private SheetBinder classSheetBinder;

    /**
     * Used to access current XWikiContext.
     */
    @Inject
    private Provider<XWikiContext> xcontextProvider;

    /**
     * Overriding the abstract class' private reference.
     */
    private DocumentReference reference;

    /**
     * Default constructor.
     */
    public XWikiServerClassDocumentInitializer()
    {
        // Since we can`t get the main wiki here, this is just to be able to use the Abstract class.
        // getDocumentReference() returns the actual main wiki document reference.
        super(XWiki.SYSTEM_SPACE, DOCUMENT_NAME);
    }

    /**
     * Initialize and return the main wiki's class document reference.
     * 
     * @return {@inheritDoc}
     */
    @Override
    public EntityReference getDocumentReference()
    {
        if (this.reference == null) {
            synchronized (this) {
                if (this.reference == null) {
                    String mainWikiName = xcontextProvider.get().getMainXWiki();
                    this.reference = new DocumentReference(mainWikiName, XWiki.SYSTEM_SPACE, DOCUMENT_NAME);
                }
            }
        }

        return this.reference;
    }

    @Override
    public boolean updateDocument(XWikiDocument document)
    {
        boolean needsUpdate = false;

        // Add missing class fields
        BaseClass baseClass = document.getXClass();

        needsUpdate |= baseClass.addTextField(FIELD_WIKIPRETTYNAME, FIELDPN_WIKIPRETTYNAME, 30);
        needsUpdate |= baseClass.addUsersField(FIELD_OWNER, FIELDPN_OWNER, false);
        needsUpdate |= baseClass.addTextAreaField(FIELD_DESCRIPTION, FIELDPN_DESCRIPTION, 40, 5);
        needsUpdate |= baseClass.addTextField(FIELD_SERVER, FIELDPN_SERVER, 30);
        needsUpdate |= baseClass.addStaticListField(FIELD_VISIBILITY, FIELDPN_VISIBILITY, FIELDL_VISIBILITY);
        needsUpdate |= baseClass.addStaticListField(FIELD_STATE, FIELDPN_STATE, FIELDL_STATE);
        needsUpdate |= baseClass.addStaticListField(FIELD_LANGUAGE, FIELDPN_LANGUAGE, FIELDL_LANGUAGE);
        needsUpdate |= baseClass.addBooleanField(FIELD_SECURE, FIELDPN_SECURE, FIELDDT_SECURE);
        needsUpdate |= updateBooleanClassDefaultValue(baseClass, FIELD_SECURE, DEFAULT_SECURE);
        needsUpdate |= baseClass.addTextField(FIELD_HOMEPAGE, FIELDPN_HOMEPAGE, 30);

        // Add missing document fields
        needsUpdate |= setClassDocumentFields(document, "XWiki Server Class");

        // Remove the class sheet binding to XWiki.XWikiServerClassSheet because the sheet is now called
        // WikiManager.XWikiServerClassSheet.
        List<DocumentReference> sheets = this.classSheetBinder.getSheets(document);
        String wikiName = document.getDocumentReference().getWikiReference().getName();
        DocumentReference oldSheetReference = new DocumentReference(wikiName, XWiki.SYSTEM_SPACE,
                CLASS_SHEET_PAGE_NAME);
        Iterator<DocumentReference> itSheets = sheets.iterator();
        while (itSheets.hasNext()) {
            DocumentReference sheet = itSheets.next();
            if (oldSheetReference.equals(sheet)) {
                needsUpdate |= this.classSheetBinder.unbind(document, oldSheetReference);
                itSheets.remove();
            }
        }

        // Use XWikiServerClassSheet to display documents having XWikiServerClass objects if no other class sheet is
        // specified.
        if (sheets.isEmpty()) {
            DocumentReference sheet = new DocumentReference(wikiName, "WikiManager", CLASS_SHEET_PAGE_NAME);
            needsUpdate |= this.classSheetBinder.bind(document, sheet);
        }

        // Check if the document is hidden
        if (!document.isHidden()) {
            document.setHidden(true);
            needsUpdate = true;
        }

        return needsUpdate;
    }
}
