/*
 * Copyright (c) 2013 IRCCloud, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.irccloud.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class BuffersDataSource {
	public class Buffer {
		int bid;
		int cid;
		long min_eid;
		long last_seen_eid;
		String name;
		String type;
		int archived;
		int deferred;
		int timeout;
		String away_msg;
	}

	public class comparator implements Comparator<Buffer> {
	    public int compare(Buffer b1, Buffer b2) {
	    	int joined1 = 1, joined2 = 1;
	    	ChannelsDataSource.Channel c = ChannelsDataSource.getInstance().getChannelForBuffer(b1.bid);
			if(c == null)
				joined1 = 0;
	    	c = ChannelsDataSource.getInstance().getChannelForBuffer(b2.bid);
			if(c == null)
				joined2 = 0;
	    	if(b1.type.equals("conversation") && b2.type.equals("channel"))
	    		return 1;
	    	else if(b1.type.equals("channel") && b2.type.equals("conversation"))
	    		return -1;
	    	else if(joined1 != joined2)
	    		return joined2 - joined1;
	    	else
	    		return b1.name.compareToIgnoreCase(b2.name);
	    }
	}
	
	private ArrayList<Buffer> buffers;
	
	private static BuffersDataSource instance = null;
	
	public static BuffersDataSource getInstance() {
		if(instance == null)
			instance = new BuffersDataSource();
		return instance;
	}

	public BuffersDataSource() {
		buffers = new ArrayList<Buffer>();
	}

	public void clear() {
		buffers.clear();
	}
	
	public int count() {
		return buffers.size();
	}
	
	public int firstBid() {
		if(buffers.size() > 0)
			return buffers.get(0).bid;
		else
			return -1;
	}
	
	public synchronized Buffer createBuffer(int bid, int cid, long min_eid, long last_seen_eid, String name, String type, int archived, int deferred, int timeout) {
		Buffer b = getBuffer(bid);
		if(b == null) {
			b = new Buffer();
			buffers.add(b);
		}
		b.bid = bid;
		b.cid = cid;
		b.min_eid = min_eid;
		b.last_seen_eid = last_seen_eid;
		b.name = name;
		b.type = type;
		b.archived = archived;
		b.deferred = deferred;
		b.timeout = timeout;
		return b;
	}

	public synchronized void updateLastSeenEid(int bid, long last_seen_eid) {
		Buffer b = getBuffer(bid);
		if(b != null && b.last_seen_eid < last_seen_eid)
			b.last_seen_eid = last_seen_eid;
	}
	
	public synchronized void updateArchived(int bid, int archived) {
		Buffer b = getBuffer(bid);
		if(b != null)
			b.archived = archived;
	}
	
	public synchronized void updateTimeout(int bid, int timeout) {
		Buffer b = getBuffer(bid);
		if(b != null)
			b.timeout = timeout;
	}
	
	public synchronized void updateName(int bid, String name) {
		Buffer b = getBuffer(bid);
		if(b != null)
			b.name = name;
	}
	
	public synchronized void updateAway(int bid, String away_msg) {
		Buffer b = getBuffer(bid);
		if(b != null) {
			b.away_msg = away_msg;
		}
	}
	
	public synchronized void deleteBuffer(int bid) {
		Buffer b = getBuffer(bid);
		if(b != null)
			buffers.remove(b);
	}

	public synchronized void deleteAllDataForBuffer(int bid) {
		Buffer b = getBuffer(bid);
		if(b != null) {
			if(b.type.equalsIgnoreCase("channel")) {
				ChannelsDataSource.getInstance().deleteChannel(bid);
				UsersDataSource.getInstance().deleteUsersForBuffer(b.cid, b.bid);
			}
			EventsDataSource.getInstance().deleteEventsForBuffer(bid);
		}
		buffers.remove(b);
	}

	public synchronized Buffer getBuffer(int bid) {
		Iterator<Buffer> i = buffers.iterator();
		while(i.hasNext()) {
			Buffer b = i.next();
			if(b.bid == bid)
				return b;
		}
		return null;
	}
	
	public synchronized Buffer getBufferByName(int cid, String name) {
		Iterator<Buffer> i = buffers.iterator();
		while(i.hasNext()) {
			Buffer b = i.next();
			if(b.cid == cid && b.name.equalsIgnoreCase(name))
				return b;
		}
		return null;
	}
	
	public synchronized ArrayList<Buffer> getBuffersForServer(int cid) {
		ArrayList<Buffer> list = new ArrayList<Buffer>();
		Iterator<Buffer> i = buffers.iterator();
		while(i.hasNext()) {
			Buffer b = i.next();
			if(b.cid == cid)
				list.add(b);
		}
		Collections.sort(list, new comparator());
		return list;
	}
	
	public synchronized ArrayList<Buffer> getBuffers() {
		ArrayList<Buffer> list = new ArrayList<Buffer>();
		Iterator<Buffer> i = buffers.iterator();
		while(i.hasNext()) {
			list.add(i.next());
		}
		return list;
	}
}
