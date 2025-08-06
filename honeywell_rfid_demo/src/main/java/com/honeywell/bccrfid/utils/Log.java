package com.honeywell.bccrfid.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Log {
    public static int level = android.util.Log.INFO;

    public static void v(String TAG, String msg) {
        if (level <= android.util.Log.VERBOSE) {
            android.util.Log.v(TAG, msg);
        }
    }

    public static void v(String TAG, String msg, Throwable tr) {
        if (level <= android.util.Log.VERBOSE) {
            android.util.Log.v(TAG, msg, tr);
        }
    }

    public static void d(String TAG, String msg) {
        if (level <= android.util.Log.DEBUG) {
            android.util.Log.d(TAG, msg);
        }
    }

    public static void d(String TAG, String msg, Throwable tr) {
        if (level <= android.util.Log.DEBUG) {
            android.util.Log.d(TAG, msg, tr);
        }
    }

    public static void i(String TAG, String msg) {
        if (level <= android.util.Log.INFO) {
            android.util.Log.i(TAG, msg);
        }
    }

    public static void i(String TAG, String msg, Throwable tr) {
        if (level <= android.util.Log.INFO) {
            android.util.Log.i(TAG, msg, tr);
        }
    }

    public static void w(String TAG, String msg) {
        if (level <= android.util.Log.WARN) {
            android.util.Log.w(TAG, msg);
        }
    }

    public static void w(String TAG, String msg, Throwable tr) {
        if (level <= android.util.Log.WARN) {
            android.util.Log.w(TAG, msg, tr);
        }
    }

    public static void w(String TAG, Throwable tr) {
        if (level <= android.util.Log.WARN) {
            android.util.Log.w(TAG, tr);
        }
    }

    public static void e(String TAG, String msg) {
        if (level <= android.util.Log.ERROR) {
            android.util.Log.e(TAG, msg);
        }
    }

    public static void e(String TAG, String msg, Throwable tr) {
        if (level <= android.util.Log.ERROR) {
            android.util.Log.e(TAG, msg, tr);
        }
    }

    public static void e(String TAG, Exception e) {
        if (level <= android.util.Log.ERROR) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            android.util.Log.e(TAG, sw.toString());
        }
    }

    public static void wtf(String TAG, String msg) {
        if (level <= android.util.Log.ASSERT) {
            android.util.Log.wtf(TAG, msg);
        }
    }

    public static void wtf(String TAG, Throwable tr) {
        if (level <= android.util.Log.ASSERT) {
            android.util.Log.wtf(TAG, tr);
        }
    }

    public static void wtf(String TAG, String msg, Throwable tr) {
        if (level <= android.util.Log.ASSERT) {
            android.util.Log.wtf(TAG, msg, tr);
        }
    }

    public static void b(String TAG, byte[] b, int offset, int len) {
        if (level <= android.util.Log.INFO) {
            if (b == null) {
                Log.e(TAG, "Log.b() null pointer!");
                return;
            } else if (b.length < offset + len) {
                Log.e(TAG, "Log.b() overflow,    b.length=" + b.length
                        + ", offset=" + offset + ", len=" + len);
                return;
            }

            StringBuffer sb = new StringBuffer();

            for (int i = offset; i < len; i++) {
                int c = b[i] & 0xff;

                if (c < 0x10) {
                    sb.append("0");
                }

                sb.append(Integer.toHexString(c));
                sb.append(" ");
            }

            Log.i(TAG, sb.toString());
        }
    }

    private final static boolean SHOW_UNPRINTABLE_CHAR_AS_POINT = true;
    private final static int ADDR_BAR_WIDTH = 6;
    private final static int LINE_BYTE_NUMBER = 16;
    private final static char[] CHAR_TABLE = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private static String getHexViewerStringLine(byte[] b, int start, int len) {
        StringBuilder sb = new StringBuilder();
        sb.append('|');
        String addrStr = String.format("%x", start);

        for (int i = 0; i < ADDR_BAR_WIDTH - addrStr.length(); i++) {
            sb.append('0');
        }

        sb.append(addrStr);
        sb.append("| ");

        // show hex
        for (int i = 0; i < 16; i++) {
            if (i < len) {
                sb.append(CHAR_TABLE[(b[i + start] & 0xf0) >> 4]);
                sb.append(CHAR_TABLE[b[i + start] & 0x0f]);
                sb.append(' ');
            } else {
                sb.append("   ");
            }
        }

        sb.append('|');

        // show ASCII
        for (int i = 0; i < 16; i++) {
            if (i < len) {
                int value = b[i + start] & 0xff;

                if (value < 0x20 || value > 0x7f) {
                    if (SHOW_UNPRINTABLE_CHAR_AS_POINT) {
                        sb.append('.');
                    } else {
                        sb.append((char) value);
                    }
                } else {
                    sb.append((char) value);
                }
            } else {
                sb.append(" ");
            }
        }

        sb.append('|');
        // sb.append('\n');
        return sb.toString();
    }

    public static void hexViewer(String TAG, byte[] b) {
        hexViewer(TAG, b, 0, b.length);
    }

    public static void hexViewer(String TAG, byte[] b, int start, int len) {
        if (level > android.util.Log.INFO) {
            return;
        }

        if (b.length < start + len) {
            Log.e(TAG, "hexViewer() overflow,    b.length=" + b.length
                    + ", start=" + start + ", len=" + len);
            return;
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < ADDR_BAR_WIDTH + 1; i++) {
            sb.append(" ");
        }

        sb.append("+-------------------------------------------------+                 ");
        sb.append('\n');

        for (int i = 0; i < ADDR_BAR_WIDTH + 1; i++) {
            sb.append(" ");
        }

        sb.append("|  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |                 ");
        sb.append('\n');
        sb.append('+');

        for (int i = 0; i < ADDR_BAR_WIDTH; i++) {
            sb.append("-");
        }

        sb.append("+-------------------------------------------------+----------------+");
        // sb.append('\n');
        Log.i(TAG, sb.toString());

        int lineNum = len / LINE_BYTE_NUMBER;

        if (lineNum * LINE_BYTE_NUMBER < len) {
            lineNum++;
        }

        int offset = start;
        int end = start + len;
        int lineLen = 0;

        for (int i = 0; i < lineNum; i++) {
            if (offset + LINE_BYTE_NUMBER > end) {
                lineLen = end - offset;
            } else {
                lineLen = LINE_BYTE_NUMBER;
            }

            Log.i(TAG, getHexViewerStringLine(b, offset, lineLen));
            offset += LINE_BYTE_NUMBER;
        }

        sb.setLength(0);
        sb.append('+');

        for (int i = 0; i < ADDR_BAR_WIDTH; i++) {
            sb.append("-");
        }

        sb.append("+-------------------------------------------------+----------------+");
        Log.i(TAG, sb.toString());
    }
}
