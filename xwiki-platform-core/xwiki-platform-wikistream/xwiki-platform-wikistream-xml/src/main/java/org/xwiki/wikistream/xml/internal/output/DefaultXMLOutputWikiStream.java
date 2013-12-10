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
package org.xwiki.wikistream.xml.internal.output;

import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.xwiki.wikistream.WikiStreamException;
import org.xwiki.wikistream.internal.output.BeanOutputWikiStream;
import org.xwiki.wikistream.xml.output.XMLOutputProperties;

/**
 * @param <P>
 * @version $Id$
 * @since 5.2M2
 */
public class DefaultXMLOutputWikiStream<P extends XMLOutputProperties, F> extends AbstractXMLOutputWikiStream<P>
    implements BeanOutputWikiStream<P>
{
    private final AbstractXMLBeanOutputWikiStreamFactory<P, F> factory;

    public DefaultXMLOutputWikiStream(AbstractXMLBeanOutputWikiStreamFactory<P, F> factory, P properties)
        throws WikiStreamException, XMLStreamException, IOException
    {
        super(properties);

        this.factory = factory;
    }

    @Override
    protected Object createFilter(P properties) throws XMLStreamException, FactoryConfigurationError,
        WikiStreamException
    {
        return this.factory.createListener(this.result, properties);
    }

    @Override
    public void setProperties(P properties) throws WikiStreamException
    {
        // Not needed
    }
}
