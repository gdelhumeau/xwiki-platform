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
package org.xwiki.rest.internal.resources.classes;

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryFilter;
import org.xwiki.rest.model.jaxb.PropertyValue;
import org.xwiki.rest.model.jaxb.PropertyValues;
import org.xwiki.rest.resources.classes.ClassPropertyValuesProvider;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.classes.ListClass;

/**
 * Base class for {@link ClassPropertyValuesProvider} implementations that work with list properties whose values are
 * document references.
 * 
 * @param <T> the property type
 * @version $Id$
 * @since 10.6RC1
 */
public abstract class AbstractDocumentListClassPropertyValuesProvider<T extends ListClass>
    extends AbstractListClassPropertyValuesProvider<T>
{
    @Inject
    @Named("compact")
    private EntityReferenceSerializer<String> compactSerializer;

    @Inject
    @Named("document")
    private QueryFilter documentFilter;

    @Inject
    @Named("viewable")
    private QueryFilter viewableFilter;

    @Override
    protected PropertyValues getUsedValues(T propertyDefinition, int limit, String filter) throws QueryException
    {
        Query query = this.usedValuesQueryBuilder.build(propertyDefinition);
        // We know the used values are document references so we can check view access in a better way than what the
        // used values query builder does by default.
        query.getFilters().clear();
        if (propertyDefinition.isMultiSelect() && !propertyDefinition.isRelationalStorage()) {
            query.addFilter(new SplitValueQueryFilter(propertyDefinition.getSeparators(), limit, filter));
        }
        query.addFilter(this.documentFilter);
        query.addFilter(this.viewableFilter);
        return getValues(query, limit, filter, propertyDefinition);
    }

    @Override
    protected PropertyValue getValueFromQueryResult(Object result, T propertyDefinition)
    {
        PropertyValue value = super.getValueFromQueryResult(result, propertyDefinition);
        if (value != null && value.getValue() instanceof DocumentReference) {
            DocumentReference documentReference = (DocumentReference) value.getValue();
            WikiReference wikiReference =
                propertyDefinition.getOwnerDocument().getDocumentReference().getWikiReference();
            // Serialize the document reference relative to the wiki were the property is defined.
            value.setValue(this.compactSerializer.serialize(documentReference, wikiReference));
            value.getMetaData().put(META_DATA_LABEL,
                getLabel(documentReference, value.getMetaData().get(META_DATA_LABEL)));
            value.getMetaData().put(META_DATA_ICON, getIcon(documentReference));
            value.getMetaData().put("url", getURL(documentReference));
        }
        return value;
    }

    protected String getLabel(DocumentReference documentReference, Object currentLabel)
    {
        String label = currentLabel == null ? "" : currentLabel.toString().trim();
        return label.isEmpty() ? documentReference.getName() : label;
    }

    protected abstract String getIcon(DocumentReference documentReference);

    protected String getURL(DocumentReference documentReference)
    {
        XWikiContext xcontext = this.xcontextProvider.get();
        return xcontext.getWiki().getURL(documentReference, xcontext);
    }
}
