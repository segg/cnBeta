package gg.cnbeta.activity;

import gg.cnbeta.data.Const;

public class Log {
    public static void d(String s) {
        if (Const.DEBUG) {
            android.util.Log.d("GG", s);
        }
    }
}
