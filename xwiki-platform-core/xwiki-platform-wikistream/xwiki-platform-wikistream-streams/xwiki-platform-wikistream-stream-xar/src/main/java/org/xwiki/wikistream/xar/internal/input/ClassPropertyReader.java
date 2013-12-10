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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.xwiki.filter.FilterEventParameters;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.wikistream.WikiStreamException;
import org.xwiki.wikistream.xar.input.XARInputProperties;
import org.xwiki.wikistream.xar.internal.XARClassPropertyModel;
import org.xwiki.wikistream.xar.internal.XARFilter;

/**
 * @version $Id$
 * @since 5.2RC1
 */
public class ClassPropertyReader extends AbstractReader
{
    public static class WikiClassProperty
    {
        public String name;

        public String type;

        public FilterEventParameters parameters = new FilterEventParameters();

        public Map<String, String> fields = new LinkedHashMap<String, String>();

        public void send(XARFilter proxyFilter) throws WikiStreamException
        {
            proxyFilter.beginWikiClassProperty(this.name, this.type, this.parameters);

            for (Map.Entry<String, String> entry : this.fields.entrySet()) {
                proxyFilter.onWikiClassPropertyField(entry.getKey(), entry.getValue(), FilterEventParameters.EMPTY);
            }

            proxyFilter.endWikiClassProperty(this.name, this.type, this.parameters);

        }
    }

    public ClassPropertyReader(XARInputProperties properties)
    {
        super(properties);
    }

    public WikiClassProperty read(XMLStreamReader xmlReader) throws XMLStreamException, IOException,
        WikiStreamException, ParseException
    {
        WikiClassProperty wikiClassProperty = new WikiClassProperty();

        wikiClassProperty.name = xmlReader.getLocalName();

        for (xmlReader.nextTag(); xmlReader.isStartElement(); xmlReader.nextTag()) {
            String elementName = xmlReader.getLocalName();
            String value = xmlReader.getElementText();

            if (elementName.equals(XARClassPropertyModel.ELEMENT_CLASSTYPE)) {
                wikiClassProperty.type = value;
            } else {
                wikiClassProperty.fields.put(elementName, value);
            }
        }

        if (wikiClassProperty.type == null) {
            throw new WikiStreamException(String.format("No <classType> element found for property [%s]",
                wikiClassProperty.name));
        }

        return wikiClassProperty;
    }
}
