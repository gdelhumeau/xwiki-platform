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

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

/**
 * Action for deleting an entire space, optionally saving all the deleted documents to the document trash, if enabled.
 *
 * @version $Id$
 * @since 3.4M1
 */
public class DeleteSpaceAction extends DeleteAction
{
    @Override
    protected boolean delete(XWikiContext context) throws XWikiException
    {
        XWikiResponse response = context.getResponse();
        
        // Delete to recycle bin.
        List<String> jobId = delete(context.getDoc().getDocumentReference().getLastSpaceReference(), true);
        sendRedirect(response,
                Utils.getRedirect("delete", String.format("jobId=%s", serializeJobId(jobId)), context));

        // A redirect has been performed.
        return true;
    }

    @Override
    public String render(XWikiContext context) throws XWikiException
    {
        XWikiRequest request = context.getRequest();
        String result = "deletespace";
        if ("1".equals(request.getParameter(CONFIRM_PARAM))) {
            result = "deletedspace";
        }
        return result;
    }
}
