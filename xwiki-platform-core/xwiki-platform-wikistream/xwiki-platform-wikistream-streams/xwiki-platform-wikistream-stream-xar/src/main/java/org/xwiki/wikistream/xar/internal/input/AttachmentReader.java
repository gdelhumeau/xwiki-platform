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
package org.xwiki.wikistream.xar.internal.input;

import java.io.ByteArrayInputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.codec.binary.Base64;
import org.xwiki.filter.FilterEventParameters;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.wikistream.WikiStreamException;
import org.xwiki.wikistream.xar.input.XARInputProperties;
import org.xwiki.wikistream.xar.internal.XARAttachmentModel;
import org.xwiki.wikistream.xar.internal.XARFilter;
import org.xwiki.wikistream.xar.internal.XARUtils.EventParameter;

/**
 * @version $Id$
 * @since 5.2RC1
 */
public class AttachmentReader extends AbstractReader
{
    public static class WikiAttachment
    {
        public String name;

        public byte[] content;

        public FilterEventParameters parameters = new FilterEventParameters();

        public void send(XARFilter proxyFilter) throws WikiStreamException
        {
            proxyFilter.onWikiAttachment(this.name, new ByteArrayInputStream(this.content),
                Long.valueOf(this.content.length), this.parameters);
        }
    }

    public AttachmentReader(XARInputProperties properties)
    {
        super(properties);
    }

    public WikiAttachment read(XMLStreamReader xmlReader) throws XMLStreamException, WikiStreamException,
        ParseException
    {
        WikiAttachment wikiAttachment = new WikiAttachment();

        for (xmlReader.nextTag(); xmlReader.isStartElement(); xmlReader.nextTag()) {
            String elementName = xmlReader.getLocalName();

            String value = xmlReader.getElementText();

            EventParameter parameter = XARAttachmentModel.ATTACHMENT_PARAMETERS.get(elementName);

            if (parameter != null) {
                wikiAttachment.parameters.put(parameter.name, convert(parameter.type, value));
            } else {
                if (XARAttachmentModel.ELEMENT_NAME.equals(elementName)) {
                    wikiAttachment.name = value;
                } else if (XARAttachmentModel.ELEMENT_CONTENT.equals(elementName)) {
                    wikiAttachment.content = Base64.decodeBase64(value.getBytes());
                }
            }
        }

        return wikiAttachment;
    }
}
