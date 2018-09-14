package org.aksw.defacto.util;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.aksw.defacto.Defacto;
import org.apache.log4j.Logger;

/**
 * PageRank queries google toolbar webservice and returns a
 * google page rank retrieved
 */
public class PageRank {
	
	private static final PageRank INSTANCE = new PageRank();
    private static Map<String,Integer> pageRankCache = new HashMap<String,Integer>();
    
    public static PageRank getInstance() {
    	
    	return INSTANCE;
    }
    
    /**
     * Must receive a domain in form of: "http://www.domain.com"
     * @param domain - (String)
     * @return PR rating (int) or -1 if unavailable or internal error happened.
     */
    public synchronized int getPageRank(String domain) {

        if ( pageRankCache.containsKey(domain.trim()) ) return pageRankCache.get(domain);
        else {
            
            int result = -1;

            String googlePrResult = "";

            String query = "http://toolbarqueries.google.com/tbr?client=navclient-auto&ie=UTF-8&oe=UTF-8&"+
                    "ch=6"+new JenkinsHash().hash(("info:" + domain).getBytes())+"&features=Rank&q=info:" + domain;

            try {
                
                URLConnection con = new URL(query).openConnection();
                InputStream is = con.getInputStream();
                byte [] buff = new byte[1024];
                int read = is.read(buff);
                while (read > 0) {
                    googlePrResult = new String(buff, 0, read);
                    read = is.read(buff);
                }
                if ( !googlePrResult.trim().isEmpty() )
                    result = new Long(googlePrResult.split(":")[2].trim()).intValue();
                else 
                    result = 0;

                Thread.sleep(Defacto.DEFACTO_CONFIG.getIntegerSetting("crawl", "GOOGLE_WAIT_TIME"));
            }
            catch (Exception e) {
                
                Logger.getLogger(PageRank.class).warn("Could not get PageRank for: " + domain, e);
            }
            
            return result;
        }
    }

    /**
     * <b>This is a Bob Jenkins hashing algorithm implementation</b>
     * <br> 
     * These are functions for producing 32-bit hashes for hash table lookup.
     * hashword(), hashlittle(), hashlittle2(), hashbig(), mix(), and final()
     * are externally useful functions.  Routines to test the hash are included
     * if SELF_TEST is defined.  You can use this free for any purpose.  It's in
     * the public domain.  It has no warranty.
     */
    private static class JenkinsHash {

        // max value to limit it to 4 bytes
        private static final long MAX_VALUE = 0xFFFFFFFFL;

        // internal variables used in the various calculations
        long a;
        long b;
        long c;

        /**
         * Convert a byte into a long value without making it negative.
         * @param b
         * @return
         */
        private long byteToLong(byte b) {
            long val = b & 0x7F;
            if ((b & 0x80) != 0) {
                val += 128;
            }
            return val;
        }

        /**
         * Do addition and turn into 4 bytes.
         * @param val
         * @param add
         * @return
         */
        private long add(long val, long add) {
            return (val + add) & MAX_VALUE;
        }

        /**
         * Do subtraction and turn into 4 bytes.
         * @param val
         * @param subtract
         * @return
         */
        private long subtract(long val, long subtract) {
            return (val - subtract) & MAX_VALUE;
        }

        /**
         * Left shift val by shift bits and turn in 4 bytes.
         * @param val
         * @param xor
         * @return
         */
        private long xor(long val, long xor) {
            return (val ^ xor) & MAX_VALUE;
        }

        /**
         * Left shift val by shift bits.  Cut down to 4 bytes.
         * @param val
         * @param shift
         * @return
         */
        private long leftShift(long val, int shift) {
            return (val << shift) & MAX_VALUE;
        }

        /**
         * Convert 4 bytes from the buffer at offset into a long value.
         * @param bytes
         * @param offset
         * @return
         */
        private long fourByteToLong(byte[] bytes, int offset) {
            return (byteToLong(bytes[offset + 0])
                    + (byteToLong(bytes[offset + 1]) << 8)
                    + (byteToLong(bytes[offset + 2]) << 16)
                    + (byteToLong(bytes[offset + 3]) << 24));
        }

        /**
         * Mix up the values in the hash function.
         */
        private void hashMix() {
            a = subtract(a, b);
            a = subtract(a, c);
            a = xor(a, c >> 13);
            b = subtract(b, c);
            b = subtract(b, a);
            b = xor(b, leftShift(a, 8));
            c = subtract(c, a);
            c = subtract(c, b);
            c = xor(c, (b >> 13));
            a = subtract(a, b);
            a = subtract(a, c);
            a = xor(a, (c >> 12));
            b = subtract(b, c);
            b = subtract(b, a);
            b = xor(b, leftShift(a, 16));
            c = subtract(c, a);
            c = subtract(c, b);
            c = xor(c, (b >> 5));
            a = subtract(a, b);
            a = subtract(a, c);
            a = xor(a, (c >> 3));
            b = subtract(b, c);
            b = subtract(b, a);
            b = xor(b, leftShift(a, 10));
            c = subtract(c, a);
            c = subtract(c, b);
            c = xor(c, (b >> 15));
        }

        /**
         * Hash a variable-length key into a 32-bit value.  Every bit of the
         * key affects every bit of the return value.  Every 1-bit and 2-bit
         * delta achieves avalanche.  The best hash table sizes are powers of 2.
         *
         * @param buffer       Byte array that we are hashing on.
         * @param initialValue Initial value of the hash if we are continuing from
         *                     a previous run.  0 if none.
         * @return Hash value for the buffer.
         */
        public long hash(byte[] buffer, long initialValue) {
            int len, pos;

            // set up the internal state
            // the golden ratio; an arbitrary value
            a = 0x09e3779b9L;
            // the golden ratio; an arbitrary value
            b = 0x09e3779b9L;
            // the previous hash value
            c = 0x0E6359A60L;

            // handle most of the key
            pos = 0;
            for (len = buffer.length; len >= 12; len -= 12) {
                a = add(a, fourByteToLong(buffer, pos));
                b = add(b, fourByteToLong(buffer, pos + 4));
                c = add(c, fourByteToLong(buffer, pos + 8));
                hashMix();
                pos += 12;
            }

            c += buffer.length;

            // all the case statements fall through to the next on purpose
            switch (len) {
                case 11:
                    c = add(c, leftShift(byteToLong(buffer[pos + 10]), 24));
                case 10:
                    c = add(c, leftShift(byteToLong(buffer[pos + 9]), 16));
                case 9:
                    c = add(c, leftShift(byteToLong(buffer[pos + 8]), 8));
                    // the first byte of c is reserved for the length
                case 8:
                    b = add(b, leftShift(byteToLong(buffer[pos + 7]), 24));
                case 7:
                    b = add(b, leftShift(byteToLong(buffer[pos + 6]), 16));
                case 6:
                    b = add(b, leftShift(byteToLong(buffer[pos + 5]), 8));
                case 5:
                    b = add(b, byteToLong(buffer[pos + 4]));
                case 4:
                    a = add(a, leftShift(byteToLong(buffer[pos + 3]), 24));
                case 3:
                    a = add(a, leftShift(byteToLong(buffer[pos + 2]), 16));
                case 2:
                    a = add(a, leftShift(byteToLong(buffer[pos + 1]), 8));
                case 1:
                    a = add(a, byteToLong(buffer[pos + 0]));
                    // case 0: nothing left to add
            }
            hashMix();

            return c;
        }

        /**
         * See hash(byte[] buffer, long initialValue)
         *
         * @param buffer Byte array that we are hashing on.
         * @return Hash value for the buffer.
         */
        public long hash(byte[] buffer) {
            return hash(buffer, 0);
        }
    }
}