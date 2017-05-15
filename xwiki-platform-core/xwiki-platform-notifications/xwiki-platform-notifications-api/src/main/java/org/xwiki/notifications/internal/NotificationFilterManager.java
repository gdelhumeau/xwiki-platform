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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.ModelContext;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.notifications.NotificationException;
import org.xwiki.notifications.NotificationFilter;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;

/**
 * @version $Id$
 * @since 9.4RC1
 */
@Component(roles = NotificationFilterManager.class)
@Singleton
public class NotificationFilterManager
{
    @Inject
    private ComponentManager componentManager;

    @Inject
    private WikiDescriptorManager wikiDescriptorManager;

    @Inject
    private ModelContext modelContext;

    public Collection<NotificationFilter> getAllNotificationFilters() throws NotificationException
    {
        String currentWikiId = wikiDescriptorManager.getCurrentWikiId();

        Map<String, NotificationFilter> filters = new HashMap<>();
        try {
            for (String wikiId : wikiDescriptorManager.getAllIds()) {
                modelContext.setCurrentEntityReference(new WikiReference(wikiId));

                filters.putAll(componentManager.getInstanceMap(NotificationFilter.class));
            }
        } catch (Exception e) {
            throw new NotificationException("Failed to get all the notification filters.", e);
        } finally {
            modelContext.setCurrentEntityReference(new WikiReference(currentWikiId));
        }

        return filters.values();
    }
}
