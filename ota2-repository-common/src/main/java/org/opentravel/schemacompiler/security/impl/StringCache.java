/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemacompiler.security.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * This class implements a String cache for ByteChunk and CharChunk.
 * 
 * @author Remy Maucherat
 */
public class StringCache {

    /**
     * Enabled ?
     */
    protected static boolean byteEnabled = ("true".equals(System.getProperty(
            "tomcat.util.buf.StringCache.byte.enabled", "false")));

    protected static boolean charEnabled = ("true".equals(System.getProperty(
            "tomcat.util.buf.StringCache.char.enabled", "false")));

    protected static int trainThreshold = Integer.parseInt(System.getProperty(
            "tomcat.util.buf.StringCache.trainThreshold", "20000"));

    protected static int cacheSize = Integer.parseInt(System.getProperty(
            "tomcat.util.buf.StringCache.cacheSize", "200"));

    /**
     * Statistics hash map for byte chunk.
     */
    protected static final HashMap<ByteEntry,int[]> bcStats = new HashMap<>(cacheSize);

    /**
     * toString count for byte chunk.
     */
    protected static int bcCount = 0;

    /**
     * Cache for byte chunk.
     */
    protected static ByteEntry[] bcCache = null;

    /**
     * Statistics hash map for char chunk.
     */
    protected static final HashMap<CharEntry,int[]> ccStats = new HashMap<>(cacheSize);

    /**
     * toString count for char chunk.
     */
    protected static int ccCount = 0;

    /**
     * Cache for char chunk.
     */
    protected static CharEntry[] ccCache = null;

    /**
     * Access count.
     */
    protected static int accessCount = 0;

    /**
     * Hit count.
     */
    protected static int hitCount = 0;

    // ------------------------------------------------------------ Properties
    
    /**
     * Private constructor to prevent instantiation.
     */
    private StringCache() {}
    
    /**
     * @return Returns the cacheSize.
     */
    public static int getCacheSize() {
        return cacheSize;
    }

    /**
     * @param cacheSize
     *            The cacheSize to set.
     */
    public static void setCacheSize(int cacheSize) {
        StringCache.cacheSize = cacheSize;
    }

    /**
     * @return Returns the enabled.
     */
    public static boolean getByteEnabled() {
        return byteEnabled;
    }

    /**
     * @param enabled
     *            The enabled to set.
     */
    public static void setByteEnabled(boolean byteEnabled) {
        StringCache.byteEnabled = byteEnabled;
    }

    /**
     * @return Returns the enabled.
     */
    public static boolean getCharEnabled() {
        return charEnabled;
    }

    /**
     * @param enabled
     *            The enabled to set.
     */
    public static void setCharEnabled(boolean charEnabled) {
        StringCache.charEnabled = charEnabled;
    }

    /**
     * @return Returns the trainThreshold.
     */
    public static int getTrainThreshold() {
        return trainThreshold;
    }

    /**
     * @param trainThreshold
     *            The trainThreshold to set.
     */
    public static void setTrainThreshold(int trainThreshold) {
        StringCache.trainThreshold = trainThreshold;
    }

    /**
     * @return Returns the accessCount.
     */
    public static int getAccessCount() {
        return accessCount;
    }

    /**
     * @return Returns the hitCount.
     */
    public static int getHitCount() {
        return hitCount;
    }

    // -------------------------------------------------- Public Static Methods

    public static void reset() {
        hitCount = 0;
        accessCount = 0;
        synchronized (bcStats) {
            bcCache = null;
            bcCount = 0;
        }
        synchronized (ccStats) {
            ccCache = null;
            ccCount = 0;
        }
    }

    public static String toString(ByteChunk bc) {

        // If the cache is null, then either caching is disabled, or we're
        // still training
        if (bcCache == null) {
            String value = bc.toStringInternal();
            if (byteEnabled) {
                // If training, everything is synced
                synchronized (bcStats) {
                    // If the cache has been generated on a previous invocation
                    // while waiting fot the lock, just return the toString
                    // value
                    // we just calculated
                    if (bcCache != null) {
                        return value;
                    }
                    // Two cases: either we just exceeded the train count, in
                    // which
                    // case the cache must be created, or we just update the
                    // count for
                    // the string
                    if (bcCount > trainThreshold) {
                        // Sort the entries according to occurrence
                        TreeMap<Integer,ArrayList<ByteEntry>> tempMap = new TreeMap<>();
                        Iterator<ByteEntry> entries = bcStats.keySet().iterator();
                        while (entries.hasNext()) {
                            ByteEntry entry = entries.next();
                            int[] countA = bcStats.get(entry);
                            Integer count = Integer.valueOf(countA[0]);
                            // Add to the list for that count
                            ArrayList<ByteEntry> list = tempMap.get(count);
                            if (list == null) {
                                // Create list
                                list = new ArrayList<>();
                                tempMap.put(count, list);
                            }
                            list.add(entry);
                        }
                        // Allocate array of the right size
                        int size = bcStats.size();
                        if (size > cacheSize) {
                            size = cacheSize;
                        }
                        ByteEntry[] tempbcCache = new ByteEntry[size];
                        // Fill it up using an alphabetical order
                        // and a dumb insert sort
                        ByteChunk tempChunk = new ByteChunk();
                        int n = 0;
                        while (n < size) {
                            Object key = tempMap.lastKey();
                            ArrayList<ByteEntry> list = tempMap.get(key);
                            for (int i = 0; i < list.size() && n < size; i++, n++) {
                                ByteEntry entry = list.get(i);
                                tempChunk.setBytes(entry.getName(), 0, entry.getName().length);
                                int insertPos = findClosest(tempChunk, tempbcCache, n);
                                if (insertPos == n) {
                                    tempbcCache[n + 1] = entry;
                                } else {
                                    System.arraycopy(tempbcCache, insertPos + 1, tempbcCache,
                                            insertPos + 2, n - insertPos - 1);
                                    tempbcCache[insertPos + 1] = entry;
                                }
                            }
                            tempMap.remove(key);
                        }
                        bcCount = 0;
                        bcStats.clear();
                        bcCache = tempbcCache;
                    } else {
                        bcCount++;
                        // Allocate new ByteEntry for the lookup
                        ByteEntry entry = new ByteEntry();
                        entry.setValue(value);
                        int[] count = bcStats.get(entry);
                        if (count == null) {
                            int end = bc.getEnd();
                            int start = bc.getStart();
                            // Create byte array and copy bytes
                            entry.setName(new byte[bc.getLength()]);
                            System.arraycopy(bc.getBuffer(), start, entry.getName(), 0, end - start);
                            // Set encoding
                            entry.setEnc(bc.getEncoding());
                            // Initialize occurrence count to one
                            count = new int[1];
                            count[0] = 1;
                            // Set in the stats hash map
                            bcStats.put(entry, count);
                        } else {
                            count[0] = count[0] + 1;
                        }
                    }
                }
            }
            return value;
        } else {
            accessCount++;
            // Find the corresponding String
            String result = find(bc);
            if (result == null) {
                return bc.toStringInternal();
            }
            // Note: We don't care about safety for the stats
            hitCount++;
            return result;
        }

    }

    public static String toString(CharChunk cc) {

        // If the cache is null, then either caching is disabled, or we're
        // still training
        if (ccCache == null) {
            String value = cc.toStringInternal();
            if (charEnabled) {
                // If training, everything is synced
                synchronized (ccStats) {
                    // If the cache has been generated on a previous invocation
                    // while waiting fot the lock, just return the toString
                    // value
                    // we just calculated
                    if (ccCache != null) {
                        return value;
                    }
                    // Two cases: either we just exceeded the train count, in
                    // which
                    // case the cache must be created, or we just update the
                    // count for
                    // the string
                    if (ccCount > trainThreshold) {
                        // Sort the entries according to occurrence
                        TreeMap<Integer,ArrayList<CharEntry>> tempMap = new TreeMap<>();
                        Iterator<CharEntry> entries = ccStats.keySet().iterator();
                        while (entries.hasNext()) {
                            CharEntry entry = entries.next();
                            int[] countA = ccStats.get(entry);
                            Integer count = Integer.valueOf(countA[0]);
                            // Add to the list for that count
                            ArrayList<CharEntry> list = tempMap.get(count);
                            if (list == null) {
                                // Create list
                                list = new ArrayList<>();
                                tempMap.put(count, list);
                            }
                            list.add(entry);
                        }
                        // Allocate array of the right size
                        int size = ccStats.size();
                        if (size > cacheSize) {
                            size = cacheSize;
                        }
                        CharEntry[] tempccCache = new CharEntry[size];
                        // Fill it up using an alphabetical order
                        // and a dumb insert sort
                        CharChunk tempChunk = new CharChunk();
                        int n = 0;
                        while (n < size) {
                            Object key = tempMap.lastKey();
                            ArrayList<CharEntry> list = tempMap.get(key);
                            for (int i = 0; i < list.size() && n < size; i++, n++) {
                                CharEntry entry = list.get(i);
                                tempChunk.setChars(entry.getName(), 0, entry.getName().length);
                                int insertPos = findClosest(tempChunk, tempccCache, n);
                                if (insertPos == n) {
                                    tempccCache[n + 1] = entry;
                                } else {
                                    System.arraycopy(tempccCache, insertPos + 1, tempccCache,
                                            insertPos + 2, n - insertPos - 1);
                                    tempccCache[insertPos + 1] = entry;
                                }
                            }
                            tempMap.remove(key);
                        }
                        ccCount = 0;
                        ccStats.clear();
                        ccCache = tempccCache;
                    } else {
                        ccCount++;
                        // Allocate new CharEntry for the lookup
                        CharEntry entry = new CharEntry();
                        entry.setValue(value);
                        int[] count = ccStats.get(entry);
                        if (count == null) {
                            int end = cc.getEnd();
                            int start = cc.getStart();
                            // Create char array and copy chars
                            entry.setName(new char[cc.getLength()]);
                            System.arraycopy(cc.getBuffer(), start, entry.getName(), 0, end - start);
                            // Initialize occurrence count to one
                            count = new int[1];
                            count[0] = 1;
                            // Set in the stats hash map
                            ccStats.put(entry, count);
                        } else {
                            count[0] = count[0] + 1;
                        }
                    }
                }
            }
            return value;
        } else {
            accessCount++;
            // Find the corresponding String
            String result = find(cc);
            if (result == null) {
                return cc.toStringInternal();
            }
            // Note: We don't care about safety for the stats
            hitCount++;
            return result;
        }

    }

    // ----------------------------------------------------- Protected Methods

    /**
     * Compare given byte chunk with byte array. Return -1, 0 or +1 if inferior, equal, or superior
     * to the String.
     */
    protected static final int compare(ByteChunk name, byte[] compareTo) {
        int result = 0;

        byte[] b = name.getBuffer();
        int start = name.getStart();
        int end = name.getEnd();
        int len = compareTo.length;

        if ((end - start) < len) {
            len = end - start;
        }
        for (int i = 0; (i < len) && (result == 0); i++) {
            if (b[i + start] > compareTo[i]) {
                result = 1;
            } else if (b[i + start] < compareTo[i]) {
                result = -1;
            }
        }
        if (result == 0) {
            if (compareTo.length > (end - start)) {
                result = -1;
            } else if (compareTo.length < (end - start)) {
                result = 1;
            }
        }
        return result;
    }

    /**
     * Find an entry given its name in the cache and return the associated String.
     */
    protected static final String find(ByteChunk name) {
        int pos = findClosest(name, bcCache, bcCache.length);
        if ((pos < 0) || (compare(name, bcCache[pos].getName()) != 0)
                || !(name.getEncoding().equals(bcCache[pos].getEnc()))) {
            return null;
        } else {
            return bcCache[pos].getValue();
        }
    }

    /**
     * Find an entry given its name in a sorted array of map elements. This will return the index
     * for the closest inferior or equal item in the given array.
     */
    protected static final int findClosest(ByteChunk name, ByteEntry[] array, int len) {

        int a = 0;
        int b = len - 1;

        // Special cases: -1 and 0
        if (b == -1) {
            return -1;
        }

        if (compare(name, array[0].getName()) < 0) {
            return -1;
        }
        if (b == 0) {
            return 0;
        }

        int i = 0;
        while (true) {
            i = (b + a) / 2;
            int result = compare(name, array[i].getName());
            if (result == 1) {
                a = i;
            } else if (result == 0) {
                return i;
            } else {
                b = i;
            }
            if ((b - a) == 1) {
                int result2 = compare(name, array[b].getName());
                if (result2 < 0) {
                    return a;
                } else {
                    return b;
                }
            }
        }

    }

    /**
     * Compare given char chunk with char array. Return -1, 0 or +1 if inferior, equal, or superior
     * to the String.
     */
    protected static final int compare(CharChunk name, char[] compareTo) {
        int result = 0;

        char[] c = name.getBuffer();
        int start = name.getStart();
        int end = name.getEnd();
        int len = compareTo.length;

        if ((end - start) < len) {
            len = end - start;
        }
        for (int i = 0; (i < len) && (result == 0); i++) {
            if (c[i + start] > compareTo[i]) {
                result = 1;
            } else if (c[i + start] < compareTo[i]) {
                result = -1;
            }
        }
        if (result == 0) {
            if (compareTo.length > (end - start)) {
                result = -1;
            } else if (compareTo.length < (end - start)) {
                result = 1;
            }
        }
        return result;
    }

    /**
     * Find an entry given its name in the cache and return the associated String.
     */
    protected static final String find(CharChunk name) {
        int pos = findClosest(name, ccCache, ccCache.length);
        if ((pos < 0) || (compare(name, ccCache[pos].getName()) != 0)) {
            return null;
        } else {
            return ccCache[pos].getValue();
        }
    }

    /**
     * Find an entry given its name in a sorted array of map elements. This will return the index
     * for the closest inferior or equal item in the given array.
     */
    protected static final int findClosest(CharChunk name, CharEntry[] array, int len) {

        int a = 0;
        int b = len - 1;

        // Special cases: -1 and 0
        if (b == -1) {
            return -1;
        }

        if (compare(name, array[0].getName()) < 0) {
            return -1;
        }
        if (b == 0) {
            return 0;
        }

        int i = 0;
        while (true) {
            i = (b + a) / 2;
            int result = compare(name, array[i].getName());
            if (result == 1) {
                a = i;
            } else if (result == 0) {
                return i;
            } else {
                b = i;
            }
            if ((b - a) == 1) {
                int result2 = compare(name, array[b].getName());
                if (result2 < 0) {
                    return a;
                } else {
                    return b;
                }
            }
        }

    }

    // -------------------------------------------------- ByteEntry Inner Class

    protected static class ByteEntry {

        private byte[] name = null;
        private String enc = null;
        private String value = null;

		public byte[] getName() {
			return name;
		}

		public void setName(byte[] name) {
			this.name = name;
		}

		public String getEnc() {
			return enc;
		}

		public void setEnc(String enc) {
			this.enc = enc;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

        public String toString() {
            return getValue();
        }

        public int hashCode() {
            return getValue().hashCode();
        }

        public boolean equals(Object obj) {
            if (obj instanceof ByteEntry) {
                return getValue().equals(((ByteEntry) obj).getValue());
            }
            return false;
        }

    }

    // -------------------------------------------------- CharEntry Inner Class

    protected static class CharEntry {

        private char[] name = null;
        private String value = null;

		public char[] getName() {
			return name;
		}

		public void setName(char[] name) {
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

        public String toString() {
            return getValue();
        }

        public int hashCode() {
            return getValue().hashCode();
        }

        public boolean equals(Object obj) {
            if (obj instanceof CharEntry) {
                return getValue().equals(((CharEntry) obj).getValue());
            }
            return false;
        }

    }

}
