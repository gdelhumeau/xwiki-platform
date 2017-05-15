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

import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.notifications.NotificationException;
import org.xwiki.notifications.NotificationPreferenceScope;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @version $Id$
 */
public class ScopeNotificationFilterTest
{
    public static final WikiReference SCOPE_REFERENCE_1 = new WikiReference("wiki1");

    public static final SpaceReference SCOPE_REFERENCE_2 = new SpaceReference("space2", new WikiReference("wiki2"));

    public static final DocumentReference SCOPE_REFERENCE_3 = new DocumentReference("wiki3", "space3", "page3");

    @Rule
    public final MockitoComponentMockingRule<ScopeNotificationFilter> mocker =
            new MockitoComponentMockingRule<>(ScopeNotificationFilter.class);

    private ModelBridge modelBridge;
    private EntityReferenceSerializer<String> serializer;
    @Before
    public void setUp() throws Exception
    {
        modelBridge = mocker.getInstance(ModelBridge.class, "cached");
        serializer = mocker.getInstance(EntityReferenceSerializer.TYPE_STRING, "local");
    }

    @Test
    public void filterEvent() throws Exception
    {

    }

    @Test
    public void queryFilterOR() throws Exception
    {
        // Mocks
        createPreferenceScopeMocks();

        // Test
        String result = mocker.getComponentUnderTest().queryFilterOR(
                new DocumentReference("xwiki", "XWiki", "User")
        );

        // Verify
        assertEquals(
                "(event.type = 'event1' AND event.wiki = :wiki_c6887a9)" +
                " OR " +
                "(event.type = 'event2' AND event.wiki = :wiki_5bfb50d AND event.space LIKE :space_5bfb50d)" +
                " OR " +
                "(event.type = 'event3' AND event.wiki = :wiki_8d3f251 AND event.page = :page_8d3f251)" ,
                result);
    }

    private void createPreferenceScopeMocks() throws NotificationException
    {
        NotificationPreferenceScope scope1 = mock(NotificationPreferenceScope.class);
        when(scope1.getScopeReference()).thenReturn(
                SCOPE_REFERENCE_1
        );
        when(scope1.getEventType()).thenReturn("event1");

        NotificationPreferenceScope scope2 = mock(NotificationPreferenceScope.class);
        when(scope2.getScopeReference()).thenReturn(
                SCOPE_REFERENCE_2
        );
        when(scope2.getEventType()).thenReturn("event2");

        NotificationPreferenceScope scope3 = mock(NotificationPreferenceScope.class);
        when(scope3.getScopeReference()).thenReturn(
                SCOPE_REFERENCE_3
        );
        when(scope3.getEventType()).thenReturn("event3");

        when(modelBridge.getNotificationPreferenceScopes(any(DocumentReference.class))).thenReturn(
                Arrays.asList(scope1, scope2, scope3)
        );
    }

    @Test
    public void queryFilterAND() throws Exception
    {
        assertEquals("",
                mocker.getComponentUnderTest().queryFilterAND(
                        new DocumentReference("xwiki", "XWiki", "User")
                )
        );
    }

    @Test
    public void queryFilterParams() throws Exception
    {
        // Mocks
        createPreferenceScopeMocks();
        when(serializer.serialize(SCOPE_REFERENCE_1)).thenReturn("wiki1");
        when(serializer.serialize(SCOPE_REFERENCE_2)).thenReturn("space2");
        when(serializer.serialize(SCOPE_REFERENCE_3)).thenReturn("space3.page3");

        // Test
        Map<String, Object> results = mocker.getComponentUnderTest().queryFilterParams(
                new DocumentReference("xwiki", "XWiki", "User")
        );

        // Verify
        assertEquals("wiki1", results.get("wiki_c6887a9"));
        assertEquals("wiki2", results.get("wiki_5bfb50d"));
        assertEquals("space2.", results.get("space_5bfb50d"));
        assertEquals("wiki3", results.get("wiki_8d3f251"));
        assertEquals("space3.page3", results.get("page_8d3f251"));
    }
}
