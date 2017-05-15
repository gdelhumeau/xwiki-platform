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
package org.xwiki.notifications.internal;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.eventstream.Event;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.notifications.NotificationException;
import org.xwiki.notifications.NotificationFilter;
import org.xwiki.notifications.NotificationPreferenceScope;

/**
 * Notification filter that handle the generic {@link NotificationPreferenceScope}.
 *
 * @version $Id$
 * @since 9.4RC1
 */
@Component
@Named("scope")
public class ScopeNotificationFilter implements NotificationFilter
{
    @Inject
    @Named("cached")
    private ModelBridge modelBridge;

    @Inject
    @Named("local")
    private EntityReferenceSerializer<String> serializer;

    @Inject
    private Logger logger;

    @Override
    public boolean filterEvent(Event event, DocumentReference user)
    {
        // Indicate if a restriction exist concerning this type of event
        boolean hasRestriction = false;
        // Indicate if a restriction matches the document of the event
        boolean matchRestriction = false;

        try {
            for (NotificationPreferenceScope scope : modelBridge.getNotificationPreferenceScopes(user)) {
                if (scope.getEventType().equals(event.getType())) {
                    hasRestriction = true;

                    if (event.getDocument().equals(scope.getScopeReference())
                            || event.getDocument().hasParent(scope.getScopeReference())) {
                        matchRestriction = true;
                        break;
                    }
                }
            }
        } catch (NotificationException e) {
            logger.warn("Failed to filter the notifications.", e);
        }

        return hasRestriction && !matchRestriction;
    }

    @Override
    public String queryFilterOR(DocumentReference user)
    {
        StringBuilder stringBuilder = new StringBuilder();

        String separator = "";

        try {
            for (NotificationPreferenceScope scope : modelBridge.getNotificationPreferenceScopes(user)) {
                stringBuilder.append(separator);
                stringBuilder.append("(");
                stringBuilder.append(String.format("event.type = '%s'", scope.getEventType()));

                final String scopeHash = getScopeHash(scope);

                switch (scope.getScopeReference().getType()) {
                    case DOCUMENT:
                        stringBuilder.append(String.format(" AND event.wiki = :wiki_%s AND event.page = :page_%s",
                                scopeHash, scopeHash));
                        break;
                    case SPACE:
                        stringBuilder.append(String.format(" AND event.wiki = :wiki_%s AND event.space LIKE :space_%s",
                                scopeHash, scopeHash));
                        break;
                    case WIKI:
                        stringBuilder.append(String.format(" AND event.wiki = :wiki_%s", scopeHash));
                        break;
                }

                stringBuilder.append(")");
                separator = " OR ";
            }
        } catch (NotificationException e) {
            logger.warn("Failed to filter the notifications.", e);
        }

        return stringBuilder.toString();
    }

    @Override
    public String queryFilterAND(DocumentReference user)
    {
        return "";
    }

    @Override
    public Map<String, Object> queryFilterParams(DocumentReference user)
    {
        Map<String, Object> params = new HashedMap();

        try {
            for (NotificationPreferenceScope scope : modelBridge.getNotificationPreferenceScopes(user)) {

                final String scopeHash = getScopeHash(scope);

                switch (scope.getScopeReference().getType()) {
                    case DOCUMENT:
                        params.put(String.format("wiki_%s", scopeHash), scope.getScopeReference().extractReference(
                                EntityType.WIKI).getName());
                        params.put(String.format("page_%s", scopeHash),
                                serializer.serialize(scope.getScopeReference()));
                        break;
                    case SPACE:
                        params.put(String.format("wiki_%s", scopeHash), scope.getScopeReference().extractReference(
                                EntityType.WIKI).getName());
                        params.put(String.format("space_%s", scopeHash),
                                String.format("%s.", serializer.serialize(scope.getScopeReference())));
                        break;
                    case WIKI:
                        params.put(String.format("wiki_%s", scopeHash), scope.getScopeReference().extractReference(
                                EntityType.WIKI).getName());
                        break;
                }
            }
        } catch (NotificationException e) {
            logger.warn("Failed to filter the notifications.", e);
        }

        return params;
    }

    private String getScopeHash(NotificationPreferenceScope scope)
    {
        // I first used hashCode(), but the result changes from one execution to an other.
        // See: http://eclipsesource.com/blogs/2012/09/04/the-3-things-you-should-know-about-hashcode/
        // Because of that, it was harder to write unit tests.
        // Instead, I have chosen to rely on a determinist algorithm such as MD2.
        return DigestUtils.md5Hex(
                String.format("%s_%s", scope.getEventType(), scope.getScopeReference())
            ).substring(0, 7);
    }
}
