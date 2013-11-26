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
package org.xwiki.wikistream.xml.internal.input;

import javax.xml.stream.XMLEventWriter;

import org.xwiki.wikistream.WikiStreamException;
import org.xwiki.wikistream.internal.input.AbstractBeanInputWikiStreamFactory;
import org.xwiki.wikistream.internal.input.BeanInputWikiStream;
import org.xwiki.wikistream.type.WikiStreamType;
import org.xwiki.wikistream.xml.input.XMLInputProperties;

/**
 * @param <P>
 * @version $Id$
 * @since 5.2M2
 */
public abstract class AbstractXMLBeanInputWikiStreamFactory<P extends XMLInputProperties, F> extends
    AbstractBeanInputWikiStreamFactory<P, F>
{
    public AbstractXMLBeanInputWikiStreamFactory(WikiStreamType type)
    {
        super(type);
    }

    @Override
    public BeanInputWikiStream<P> createInputWikiStream(P properties) throws WikiStreamException
    {
        return new DefaultXMLInputWikiStream<P, F>(this, properties);
    }

    protected abstract XMLEventWriter createXMLEventWriter(Object filter, P parameters);
}
