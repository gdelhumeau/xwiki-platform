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
package org.xwiki.mentions.internal.jmx;

import java.util.Collection;
import java.util.function.Supplier;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.mentions.internal.async.MentionsData;

/**
 * Implementation of the Mentions JXM MBean.
 *
 * @version $Id$
 * @since 12.6RC1
 */
public class JMXMentions implements JMXMentionsMBean
{
    private final Collection<MentionsData> queue;

    private final Supplier<Integer> threadNumber;

    /**
     * Default construct.
     * @param queue The mentions analysis task queue.
     * @param threadNumber The current number of threads
     */
    public JMXMentions(Collection<MentionsData> queue,
        Supplier<Integer> threadNumber)
    {
        this.queue = queue;

        this.threadNumber = threadNumber;
    }

    @Override
    public int getQueueSize()
    {
        return this.queue.size();
    }

    @Override
    public void clearQueue()
    {
        this.queue.clear();
    }

    @Override
    public Integer getThreadNumber()
    {
        return this.threadNumber.get();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JMXMentions that = (JMXMentions) o;

        return new EqualsBuilder()
                   .append(this.queue, that.queue)
                   .append(this.threadNumber, that.threadNumber)
                   .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                   .append(this.queue)
                   .append(this.threadNumber)
                   .toHashCode();
    }
}
