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

import javax.inject.Inject;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.wikistream.WikiStreamException;
import org.xwiki.wikistream.input.InputSource;
import org.xwiki.wikistream.input.InputStreamInputSource;
import org.xwiki.wikistream.xar.input.XARInputProperties;
import org.xwiki.wikistream.xar.internal.XARFilter;
import org.xwiki.xar.internal.XarPackage;
import org.xwiki.xar.internal.model.XarModel;

/**
 * @version $Id$
 * @since 5.2RC1
 */
@Component(roles = WikiReader.class)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class WikiReader
{
    @Inject
    private DocumentLocaleReader documentReader;

    private XARInputProperties properties;

    private XarPackage xarPackage = new XarPackage();

    public void setProperties(XARInputProperties properties)
    {
        this.properties = properties;

        this.documentReader.setProperties(properties);
    }

    public XarPackage getXarPackage()
    {
        return this.xarPackage;
    }

    public void read(Object filter, XARFilter proxyFilter) throws XMLStreamException, IOException, WikiStreamException
    {
        InputStream stream;

        InputSource source = this.properties.getSource();
        if (source instanceof InputStreamInputSource) {
            stream = ((InputStreamInputSource) source).getInputStream();
        } else {
            throw new WikiStreamException("Unsupported source type [" + source.getClass() + "]");
        }

        read(stream, filter, proxyFilter);

        // Close last space
        if (this.documentReader.getCurrentSpace() != null) {
            proxyFilter.endWikiSpace(this.documentReader.getCurrentSpace(),
                this.documentReader.getCurrentSpaceParameters());
        }

        // TODO: send extension event
        if (this.xarPackage.getPackageExtensionId() != null) {

        }
    }

    public void read(InputStream stream, Object filter, XARFilter proxyFilter) throws XMLStreamException, IOException,
        WikiStreamException
    {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(stream, "UTF-8", false);

        for (ZipArchiveEntry entry = zis.getNextZipEntry(); entry != null; entry = zis.getNextZipEntry()) {
            if (entry.isDirectory() || entry.getName().startsWith("META-INF")) {
                // The entry is either a directory or is something inside of the META-INF dir.
                // (we use that directory to put meta data such as LICENSE/NOTICE files.)
                continue;
            } else if (entry.getName().equals(XarModel.PATH_PACKAGE)) {
                // The entry is the manifest (package.xml). Read this differently.
                try {
                    this.xarPackage.readDescriptor(zis);
                } catch (Exception e) {
                    // TODO: LOG warning
                }
            } else {
                try {
                    this.documentReader.read(zis, filter, proxyFilter);
                } catch (SkipEntityException skip) {
                    // TODO: put it in some status
                } catch (Exception e) {
                    throw new WikiStreamException(String.format("Failed to read XAR XML document from entry [%s]",
                        entry.getName()), e);
                }
            }
        }
    }
}
