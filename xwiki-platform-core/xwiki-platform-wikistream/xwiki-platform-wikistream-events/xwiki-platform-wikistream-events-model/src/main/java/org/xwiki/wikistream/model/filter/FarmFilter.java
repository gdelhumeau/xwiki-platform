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
package org.xwiki.wikistream.model.filter;

import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.annotation.Default;
import org.xwiki.filter.annotation.Name;
import org.xwiki.stability.Unstable;
import org.xwiki.wikistream.WikiStreamException;

/**
 * Farm related events.
 * 
 * @version $Id$
 * @since 5.2M2
 */
@Unstable
public interface FarmFilter
{
    /**
     * @param parameters the farm parameters
     * @throws WikiStreamException when failing to send event
     */
    void beginFarm(
        @Default(FilterEventParameters.DEFAULT) @Name(FilterEventParameters.NAME) FilterEventParameters parameters)
        throws WikiStreamException;

    /**
     * @param parameters the farm parameters
     * @throws WikiStreamException when failing to send event
     */
    void endFarm(
        @Default(FilterEventParameters.DEFAULT) @Name(FilterEventParameters.NAME) FilterEventParameters parameters)
        throws WikiStreamException;
}
