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
package org.xwiki.mentions.internal.rendering;

import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.phase.Initializable;
import org.xwiki.mentions.DisplayStyle;
import org.xwiki.mentions.internal.MentionsFormatter;
import org.xwiki.rendering.internal.renderer.plain.PlainTextRenderer;

/**
 * Plain text rendered with a specialization to display well formatted user mentions.  
 *
 * @version $Id$
 * @since 12.6RC1
 */
@Component
@Named("plainmentions/1.0")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class PlainTextMentionsRenderer extends PlainTextRenderer implements Initializable
{
    @Inject
    private MentionsFormatter formatter;

    @Override
    public void onMacro(String id, Map<String, String> parameters, String contentP, boolean inline)
    {
        String userReference = parameters.get("reference");
        String style = parameters.get("style");
        if (Objects.equals(id, "mention")) {
            this.getPrinter().println(this.formatter.formatMention(userReference, DisplayStyle.valueOf(style)));
        } else {
            super.onMacro(id, parameters, contentP, inline);
        }
    }
}
