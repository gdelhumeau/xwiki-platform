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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.job.Job;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.refactoring.job.EntityRequest;
import org.xwiki.refactoring.script.RefactoringScriptService;
import org.xwiki.script.service.ScriptService;

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
    protected static final String CONFIRM_PARAM = "confirm";
    
    protected static final String ACTION_NAME = "delete";
    
    protected boolean isJobLaunched(XWikiRequest request)
    {
        // If the jobId is given, then the deletion is already processing, and we let the UI display a progress bar
        return StringUtils.isNotEmpty(request.getParameter("jobId"));
    }

    @Override
    public boolean action(XWikiContext context) throws XWikiException
    {
        XWikiRequest request = context.getRequest();

        // If confirm=1 then delete the page. If not, the render action will go to the "delete" page so that the
        // user can confirm. That "delete" page will then call the delete action again with confirm=1.
        if (!"1".equals(request.getParameter(CONFIRM_PARAM))) {
            return true;
        }

        if (isJobLaunched(request)) {
            return true;
        }

        // CSRF prevention
        if (!csrfTokenCheck(context)) {
            return false;
        }

        boolean redirected = delete(context);

        if (!redirected) {
            // If a xredirect param is passed then redirect to the page specified instead of going to the default
            // confirmation page.
            String redirect = Utils.getRedirect(request, null);
            if (redirect != null) {
                sendRedirect(context.getResponse(), redirect);
                redirected = true;
            }
        }

        return !redirected;
    }

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
        
        if (isJobLaunched(request)) {
            return ACTION_NAME;
        }        
        if ("1".equals(request.getParameter(CONFIRM_PARAM))) {
            return "deleted";
        }
        if (doc.isNew() && !recycleIdIsValid) {
            return Utils.getPage(request, "docdoesnotexist");
        }
        
        return ACTION_NAME;
    }

    protected boolean delete(XWikiContext context) throws XWikiException
    {
        XWiki xwiki = context.getWiki();
        XWikiRequest request = context.getRequest();
        XWikiResponse response = context.getResponse();
        XWikiDocument doc = context.getDoc();
 
        if (isJobLaunched(request)) {
            return false;
        }
        
        String sindex = request.getParameter("id");
        if (sindex != null && xwiki.hasRecycleBin(context)) {
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
        } else if (doc.isNew()) {
            // Redirect the user to the view template so that he gets the "document doesn't exist" dialog box.
            sendRedirect(response, Utils.getRedirect("view", context));
        } else {
            // Delete to recycle bin.
            List<String> jobId = delete(doc.getTranslatedDocument(context).getDocumentReferenceWithLocale(),
                    StringUtils.isNotEmpty(request.getParameter("affectChildren")));
            sendRedirect(response, 
                    Utils.getRedirect("delete", String.format("jobId=%s", serializeJobId(jobId)), context));
        }

        return true;
    }
    
    protected String serializeJobId(List<String> jobId)
    {
        return StringUtils.join(jobId, "/");
    }

    protected List<String> delete(EntityReference entityReference, boolean deep) throws XWikiException
    {
        RefactoringScriptService refactoring =
            (RefactoringScriptService) Utils.getComponent(ScriptService.class, "refactoring");
        EntityRequest deleteRequest = refactoring.createDeleteRequest(Arrays.asList(entityReference));
        deleteRequest.setDeep(deep);
        Job deleteJob = refactoring.delete(deleteRequest);
        if (deleteJob != null) {
            return deleteRequest.getId(); 
        } else {
            throw new XWikiException(String.format("Failed to schedule the delete job for [%s]", entityReference),
                refactoring.getLastError());
        }
    }
}
