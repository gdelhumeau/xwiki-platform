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
package com.xpn.xwiki.web;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.DeletedDocument;
import com.xpn.xwiki.doc.XWikiDeletedDocument;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Action for delete document to recycle bin and for delete documents from recycle bin.
 *
 * @version $Id$
 */
public class DeleteAction extends XWikiAction
{
    /** confirm parameter name. */
    private static final String CONFIRM_PARAM = "confirm";
    
    private QueryManager queryManager = null;
    
    private QueryManager getQueryManager()
    {
        if (queryManager == null) {
            queryManager = Utils.getComponent(QueryManager.class);
        }
        return queryManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean action(XWikiContext context) throws XWikiException
    {
        XWiki xwiki = context.getWiki();
        XWikiRequest request = context.getRequest();
        XWikiResponse response = context.getResponse();
        XWikiDocument doc = context.getDoc();
        boolean redirected = false;
        // If confirm=1 then delete the page. If not, the render action will go to the "delete" page so that the
        // user can confirm. That "delete" page will then call the delete action again with confirm=1.
        if (!"1".equals(request.getParameter(CONFIRM_PARAM))) {
            return true;
        }
        // CSRF prevention
        if (!csrfTokenCheck(context)) {
            return false;
        }

        String sindex = request.getParameter("id");
        if (sindex != null && xwiki.hasRecycleBin(context)) {
            deleteFromRecycleBin(sindex, context);
            redirected = true;
        } else if (doc.isNew()) {
            // Redirect the user to the view template so that he gets the "document doesn't exist" dialog box.
            sendRedirect(response, Utils.getRedirect("view", context));
            redirected = true;
        } else {
            deleteToRecycleBin(context);
        }
        if (!redirected) {
            // If a xredirect param is passed then redirect to the page specified instead of going to the default
            // confirmation page.
            String redirect = Utils.getRedirect(request, null);
            if (redirect != null) {
                sendRedirect(response, redirect);
                redirected = true;
            }
        }
        return !redirected;
    }
    
    private void deleteToRecycleBin(XWikiContext context) throws XWikiException
    {
        XWiki xwiki = context.getWiki();
        XWikiRequest request = context.getRequest();
        XWikiDocument doc = context.getDoc();
        
        // Delete children of the current document or no
        String deleteChildren = request.getParameter("deleteChildren");
        if (StringUtils.isNotEmpty(deleteChildren) && !"0".equals(deleteChildren)) {
            try {
                // Get the children of the document
                String xwql = "where doc.fullName like :parent";
                Query query = getQueryManager().createQuery(xwql, Query.XWQL);
                query.bindValue("parent", String.format("%s.%", doc.getSpace()));
                List<String> children = query.execute();
                for (String child : children) {
                    xwiki.deleteAllDocuments(xwiki.getDocument(child, context), context);
                }
            } catch (QueryException e) {
                throw new XWikiException(
                        String.format("Unable to get the children of document [%s]", doc.toString()), e);
            }
        }
        
        String language = xwiki.getLanguagePreference(context);
        if (StringUtils.isEmpty(language) || language.equals(doc.getDefaultLanguage())) {
            xwiki.deleteAllDocuments(doc, context);
        } else {
            // Only delete the translation
            XWikiDocument tdoc = doc.getTranslatedDocument(language, context);
            xwiki.deleteDocument(tdoc, context);
        }
    }
    
    private void deleteFromRecycleBin(String sindex, XWikiContext context) throws XWikiException
    {
        XWiki xwiki = context.getWiki();
        XWikiDocument doc = context.getDoc();
        XWikiResponse response = context.getResponse();
     
        long index = Long.parseLong(sindex);
        XWikiDeletedDocument dd = xwiki.getRecycleBinStore().getDeletedDocument(doc, index, context, true);
        // If the document hasn't been previously deleted (i.e. it's not in the deleted document store) then
        // don't try to delete it and instead redirect to the view page.
        if (dd != null) {
            DeletedDocument ddapi = new DeletedDocument(dd, context);
            if (!ddapi.canDelete()) {
                throw new XWikiException(XWikiException.MODULE_XWIKI_ACCESS,
                        XWikiException.ERROR_XWIKI_ACCESS_DENIED,
                        "You are not allowed to delete a document from the trash "
                                + "immediately after it has been deleted from the wiki");
            }
            if (!dd.getFullName().equals(doc.getFullName())) {
                throw new XWikiException(XWikiException.MODULE_XWIKI_APP,
                        XWikiException.ERROR_XWIKI_APP_URL_EXCEPTION,
                        "The specified trash entry does not match the current document");
            }
            xwiki.getRecycleBinStore().deleteFromRecycleBin(doc, index, context, true);
        }
        sendRedirect(response, Utils.getRedirect("view", context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String render(XWikiContext context) throws XWikiException
    {
        XWikiRequest request = context.getRequest();
        XWikiDocument doc = context.getDoc();
        String sindex = request.getParameter("id");
        boolean recycleIdIsValid = false;
        if (sindex != null) {
            long index = Long.parseLong(sindex);
            if (context.getWiki().getRecycleBinStore().getDeletedDocument(doc, index, context, true) != null) {
                recycleIdIsValid = true;
            }
        }
        String result = "delete";
        if ("1".equals(request.getParameter(CONFIRM_PARAM))) {
            result = "deleted";
        } else if (doc.isNew() && !recycleIdIsValid) {
            result = Utils.getPage(request, "docdoesnotexist");
        }
        return result;
    }
}
