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
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.xwiki.filter.FilterEventParameters;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.syntax.SyntaxFactory;
import org.xwiki.wikistream.WikiStreamException;
import org.xwiki.wikistream.filter.xwiki.XWikiWikiDocumentFilter;
import org.xwiki.wikistream.xar.input.XARInputProperties;
import org.xwiki.wikistream.xar.internal.XARAttachmentModel;
import org.xwiki.wikistream.xar.internal.XARClassModel;
import org.xwiki.wikistream.xar.internal.XARDocumentModel;
import org.xwiki.wikistream.xar.internal.XARFilter;
import org.xwiki.wikistream.xar.internal.XARObjectModel;
import org.xwiki.wikistream.xar.internal.XARUtils.EventParameter;
import org.xwiki.wikistream.xar.internal.input.AttachmentReader.WikiAttachment;
import org.xwiki.wikistream.xar.internal.input.ClassReader.WikiClass;
import org.xwiki.wikistream.xar.internal.input.WikiObjectReader.WikiObject;
import org.xwiki.wikistream.xml.internal.input.XMLInputWikiStreamUtils;

/**
 * @version $Id$
 * @since 5.2RC1
 */
public class DocumentLocaleReader extends AbstractReader
{
    private EntityReferenceResolver<String> relativeResolver;

    private String currentSpace;

    private FilterEventParameters currentSpaceParameters = FilterEventParameters.EMPTY;

    private String currentDocument;

    private Locale currentDocumentLocale;

    private String currentDocumentRevision;

    private FilterEventParameters currentDocumentParameters = new FilterEventParameters();

    private FilterEventParameters currentDocumentLocaleParameters = new FilterEventParameters();

    private FilterEventParameters currentDocumentRevisionParameters = new FilterEventParameters();

    private boolean sentBeginWikiDocument;

    private boolean sentBeginWikiDocumentLocale;

    private boolean sentBeginWikiDocumentRevision;

    private WikiClass currentClass = new WikiClass();

    private Queue<WikiObject> currentObjects = new LinkedList<WikiObject>();

    private Queue<WikiAttachment> currentAttachments = new LinkedList<WikiAttachment>();

    public DocumentLocaleReader(SyntaxFactory syntaxFactory, EntityReferenceResolver<String> relativeResolver)
    {
        super(syntaxFactory);

        this.relativeResolver = relativeResolver;
    }

    public String getCurrentSpace()
    {
        return this.currentSpace;
    }

    public FilterEventParameters getCurrentSpaceParameters()
    {
        return this.currentSpaceParameters;
    }

    private void reset()
    {
        this.currentDocument = null;
        this.currentDocumentLocale = null;
        this.currentDocumentRevision = null;

        this.currentDocumentParameters = new FilterEventParameters();
        this.currentDocumentLocaleParameters = new FilterEventParameters();
        this.currentDocumentRevisionParameters = new FilterEventParameters();

        this.sentBeginWikiDocument = false;
        this.sentBeginWikiDocumentLocale = false;
        this.sentBeginWikiDocumentRevision = false;
    }

    private void sendBeginWikiDocument(XARFilter proxyFilter, boolean force, XARInputProperties properties)
        throws WikiStreamException
    {
        if (!this.sentBeginWikiDocument
            && (force || (this.currentDocument != null && this.currentDocumentParameters.size() == XARDocumentModel.DOCUMENT_PARAMETERS
                .size()))) {
            proxyFilter.beginWikiDocument(this.currentDocument, this.currentDocumentParameters);
            this.sentBeginWikiDocument = true;
        }
    }

    private void sendEndWikiDocument(XARFilter proxyFilter, XARInputProperties properties) throws WikiStreamException
    {
        sendBeginWikiDocumentLocale(proxyFilter, true, properties);
        sendBeginWikiDocument(proxyFilter, true, properties);

        proxyFilter.endWikiDocument(this.currentDocument, this.currentDocumentParameters);
    }

    private void sendBeginWikiDocumentLocale(XARFilter proxyFilter, boolean force, XARInputProperties properties)
        throws WikiStreamException
    {
        if (this.sentBeginWikiDocument
            && !this.sentBeginWikiDocumentLocale
            && (force || (this.currentDocumentLocale != null && this.currentDocumentLocaleParameters.size() == XARDocumentModel.DOCUMENTLOCALE_PARAMETERS
                .size()))) {
            if (!properties.isWithHistory()) {
                this.currentDocumentLocaleParameters.remove(XWikiWikiDocumentFilter.PARAMETER_JRCSREVISIONS);
            }

            proxyFilter.beginWikiDocumentLocale(this.currentDocumentLocale, this.currentDocumentLocaleParameters);
            this.sentBeginWikiDocumentLocale = true;
        }
    }

    private void sendEndWikiDocumentLocale(XARFilter proxyFilter, XARInputProperties properties)
        throws WikiStreamException
    {
        sendBeginWikiDocumentRevision(proxyFilter, true, properties);
        sendBeginWikiDocumentLocale(proxyFilter, true, properties);

        proxyFilter.endWikiDocumentLocale(this.currentDocumentLocale, this.currentDocumentLocaleParameters);
    }

    private void sendBeginWikiDocumentRevision(XARFilter proxyFilter, boolean force, XARInputProperties properties)
        throws WikiStreamException
    {
        if (this.sentBeginWikiDocumentLocale
            && !this.sentBeginWikiDocumentRevision
            && (force || (this.currentDocumentRevision != null && this.currentDocumentRevisionParameters.size() == XARDocumentModel.DOCUMENTREVISION_PARAMETERS
                .size()))) {
            proxyFilter.beginWikiDocumentRevision(this.currentDocumentRevision, this.currentDocumentRevisionParameters);
            this.sentBeginWikiDocumentRevision = true;
        }
    }

    private void sendEndWikiDocumentRevision(XARFilter proxyFilter, XARInputProperties properties)
        throws WikiStreamException
    {
        sendBeginWikiDocumentRevision(proxyFilter, true, properties);

        proxyFilter.endWikiDocumentRevision(this.currentDocumentRevision, this.currentDocumentRevisionParameters);
    }

    public void read(Object filter, XARFilter proxyFilter, XARInputProperties properties) throws XMLStreamException,
        IOException, WikiStreamException, ParseException
    {
        XMLStreamReader xmlReader = XMLInputWikiStreamUtils.createXMLStreamReader(properties);

        try {
            read(xmlReader, filter, proxyFilter, properties);
        } finally {
            properties.getSource().close();
        }
    }

    public void read(InputStream stream, Object filter, XARFilter proxyFilter, XARInputProperties properties)
        throws XMLStreamException, IOException, WikiStreamException, ParseException
    {
        XMLStreamReader xmlReader =
            properties.getEncoding() != null ? XMLInputFactory.newInstance().createXMLStreamReader(stream,
                properties.getEncoding()) : XMLInputFactory.newInstance().createXMLStreamReader(stream);

        read(xmlReader, filter, proxyFilter, properties);
    }

    public void read(XMLStreamReader xmlReader, Object filter, XARFilter proxyFilter, XARInputProperties properties)
        throws XMLStreamException, IOException, WikiStreamException, ParseException
    {
        reset();

        // <xwikidoc>

        xmlReader.nextTag();

        xmlReader.require(XMLStreamReader.START_ELEMENT, null, XARDocumentModel.ELEMENT_DOCUMENT);

        readDocument(xmlReader, filter, proxyFilter, properties);
    }

    private void readDocument(XMLStreamReader xmlReader, Object filter, XARFilter proxyFilter,
        XARInputProperties properties) throws XMLStreamException, WikiStreamException, ParseException, IOException
    {
        for (xmlReader.nextTag(); xmlReader.isStartElement(); xmlReader.nextTag()) {
            String elementName = xmlReader.getLocalName();

            if (elementName.equals(XARAttachmentModel.ELEMENT_ATTACHMENT)) {
                readAttachment(xmlReader, filter, proxyFilter, properties);
            } else if (elementName.equals(XARObjectModel.ELEMENT_OBJECT)) {
                readObject(xmlReader, filter, proxyFilter, properties);
            } else if (elementName.equals(XARClassModel.ELEMENT_CLASS)) {
                readClass(xmlReader, filter, proxyFilter, properties);
            } else {
                String value = xmlReader.getElementText();

                if (XARDocumentModel.ELEMENT_SPACE.equals(elementName)) {
                    if (!value.equals(this.currentSpace)) {
                        if (this.currentSpace != null) {
                            proxyFilter.endWikiSpace(this.currentSpace, this.currentSpaceParameters);
                        }
                        this.currentSpace = value;
                        proxyFilter.beginWikiSpace(this.currentSpace, this.currentSpaceParameters);
                    }
                } else if (XARDocumentModel.ELEMENT_NAME.equals(elementName)) {
                    this.currentDocument = value;
                    sendBeginWikiDocument(proxyFilter, false, properties);
                } else if (XARDocumentModel.ELEMENT_LOCALE.equals(elementName)) {
                    this.currentDocumentLocale = (Locale) convert(Locale.class, value);
                    sendBeginWikiDocumentLocale(proxyFilter, false, properties);
                } else if (XARDocumentModel.ELEMENT_REVISION.equals(elementName)) {
                    this.currentDocumentRevision = value;
                    sendBeginWikiDocumentRevision(proxyFilter, false, properties);
                } else {
                    EventParameter parameter = XARDocumentModel.DOCUMENT_PARAMETERS.get(elementName);

                    if (parameter != null) {
                        this.currentDocumentParameters.put(parameter.name, convert(parameter.type, value));

                        sendBeginWikiDocument(proxyFilter, false, properties);
                    } else {
                        parameter = XARDocumentModel.DOCUMENTLOCALE_PARAMETERS.get(elementName);

                        if (parameter != null) {
                            this.currentDocumentLocaleParameters.put(parameter.name, convert(parameter.type, value));

                            sendBeginWikiDocumentLocale(proxyFilter, false, properties);
                        } else {
                            parameter = XARDocumentModel.DOCUMENTREVISION_PARAMETERS.get(elementName);

                            if (parameter != null) {
                                Object objectValue;
                                if (parameter.type == EntityReference.class) {
                                    objectValue = this.relativeResolver.resolve(value, EntityType.DOCUMENT);
                                } else {
                                    objectValue = convert(parameter.type, value);
                                }
                                this.currentDocumentRevisionParameters.put(parameter.name, objectValue);

                                sendBeginWikiDocumentRevision(proxyFilter, false, properties);
                            } else {
                                // Unknown property
                                // TODO: log something ?
                            }
                        }
                    }
                }
            }
        }

        sendBeginWikiDocument(proxyFilter, true, properties);
        sendBeginWikiDocumentLocale(proxyFilter, true, properties);
        sendBeginWikiDocumentRevision(proxyFilter, true, properties);

        sendWikiAttachments(proxyFilter);
        sendWikiClass(proxyFilter);
        sendWikiObjects(proxyFilter);

        sendEndWikiDocumentRevision(proxyFilter, properties);
        sendEndWikiDocumentLocale(proxyFilter, properties);
        sendEndWikiDocument(proxyFilter, properties);
    }

    private void readObject(XMLStreamReader xmlReader, Object filter, XARFilter proxyFilter,
        XARInputProperties properties) throws XMLStreamException, WikiStreamException, IOException, ParseException
    {
        sendBeginWikiDocumentRevision(proxyFilter, false, properties);

        WikiObjectReader reader = new WikiObjectReader();

        WikiObject wikiObject = reader.readObject(xmlReader, properties);

        if (this.sentBeginWikiDocumentRevision) {
            wikiObject.send(proxyFilter);
        } else {
            this.currentObjects.offer(wikiObject);
        }
    }

    private void readClass(XMLStreamReader xmlReader, Object filter, XARFilter proxyFilter,
        XARInputProperties properties) throws XMLStreamException, WikiStreamException, IOException, ParseException
    {
        sendBeginWikiDocumentRevision(proxyFilter, false, properties);

        ClassReader reader = new ClassReader();

        this.currentClass = reader.read(xmlReader, properties);

        if (this.sentBeginWikiDocumentRevision) {
            sendWikiClass(proxyFilter);
        }
    }

    private void readAttachment(XMLStreamReader xmlReader, Object filter, XARFilter proxyFilter,
        XARInputProperties properties) throws XMLStreamException, WikiStreamException, ParseException
    {
        sendBeginWikiDocumentRevision(proxyFilter, false, properties);

        AttachmentReader reader = new AttachmentReader();

        WikiAttachment wikiAttachment = reader.read(xmlReader, properties);

        if (this.sentBeginWikiDocumentRevision) {
            wikiAttachment.send(proxyFilter);
        } else {
            this.currentAttachments.offer(wikiAttachment);
        }
    }

    private void sendWikiClass(XARFilter proxyFilter) throws WikiStreamException
    {
        if (this.currentClass != null) {
            this.currentClass.send(proxyFilter);
            this.currentClass = null;
        }
    }

    private void sendWikiObjects(XARFilter proxyFilter) throws WikiStreamException
    {
        while (this.currentObjects.size() > 0) {
            this.currentObjects.poll().send(proxyFilter);
        }
    }

    private void sendWikiAttachments(XARFilter proxyFilter) throws WikiStreamException
    {
        while (this.currentAttachments.size() > 0) {
            this.currentAttachments.poll().send(proxyFilter);
        }
    }
}
