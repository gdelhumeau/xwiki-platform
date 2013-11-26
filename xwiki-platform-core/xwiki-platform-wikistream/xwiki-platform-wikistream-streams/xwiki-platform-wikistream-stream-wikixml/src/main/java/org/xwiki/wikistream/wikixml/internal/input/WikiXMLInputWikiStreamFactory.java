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
package org.xwiki.wikistream.wikixml.internal.input;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.stream.XMLEventWriter;

import org.xwiki.component.annotation.Component;
import org.xwiki.filter.xml.parser.XMLParserFactory;
import org.xwiki.wikistream.WikiStreamException;
import org.xwiki.wikistream.type.WikiStreamType;
import org.xwiki.wikistream.wikixml.input.WikiXMLInputProperties;
import org.xwiki.wikistream.xml.internal.input.AbstractXMLBeanInputWikiStreamFactory;

/**
 * A generic xml output wikistream implementation. This class can be used as a test bench to validate various
 * XMLInputStream wiki parsers.
 * 
 * @version $Id$
 * @since 5.2M2
 */
@Component
@Named("wiki+xml")
@Singleton
public class WikiXMLInputWikiStreamFactory extends
    AbstractXMLBeanInputWikiStreamFactory<WikiXMLInputProperties, Object>
{
    @Inject
    private XMLParserFactory parserFactory;

    /**
     * Default constructor.
     */
    public WikiXMLInputWikiStreamFactory()
    {
        super(WikiStreamType.WIKI_XML);

        setName("Generic XML output stream");
        setDescription("Generates wiki events from generic XML file.");
    }

    @Override
    public Collection<Class< ? >> getFilterInterfaces() throws WikiStreamException
    {
        return Collections.emptyList();
    }

    @Override
    protected XMLEventWriter createXMLEventWriter(Object filter, WikiXMLInputProperties parameters)
    {
        return this.parserFactory.createXMLEventWriter(filter, null);
    }
}
