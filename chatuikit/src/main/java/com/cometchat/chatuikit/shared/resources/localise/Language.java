package com.cometchat.chatuikit.shared.resources.localise;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The Language class provides constants and annotations for supported language
 * codes.
 *
 * <p>
 * It defines a set of language codes as String constants and provides a
 * StringDef annotation for validation.
 */
public class Language {
    private static final String TAG = Language.class.getSimpleName();


    /**
     * Annotation for defining valid language codes.
     */
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({Code.de, Code.ar, Code.zh_tw, Code.en, Code.fr, Code.es, Code.hi, Code.hu, Code.lt, Code.ms, Code.ru, Code.zh, Code.sv, Code.pt, Code.it, Code.ja, Code.ko, Code.nl, Code.tr})
    public @interface Code {
        String de = "de";
        String ar = "ar";
        String zh_tw = "zh-TW";
        String en = "en";
        String es = "es";
        String ms = "ms";
        String sv = "sv";
        String hu = "hu";
        String lt = "lt";
        String ru = "ru";
        String fr = "fr";
        String pt = "pt";
        String hi = "hi";
        String zh = "zh";
        String it = "it";
        String ja = "ja";
        String ko = "ko";
        String nl = "nl";
        String tr = "tr";
    }
}
