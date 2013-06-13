package com.projectsexception.myapplist.view;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import com.projectsexception.myapplist.util.CustomLog;

import java.util.WeakHashMap;

public class TypefaceProvider {

    public static final String FONT_REGULAR = "Roboto-Light";
    public static final String FONT_BOLD = "Roboto-BoldCondensed";

    static final String TYPEFACE_FOLDER = "fonts";
    static final String TYPEFACE_EXTENSION = ".ttf";

    static WeakHashMap<String, Typeface> sTypeFaces = new WeakHashMap<String, Typeface>();

    static Typeface getTypeFace(Context context, String font) {
        Typeface tempTypeface = sTypeFaces.get(font);

        if (tempTypeface == null) {
            String fontPath = TYPEFACE_FOLDER + '/' + font + TYPEFACE_EXTENSION;
            try {
                tempTypeface = Typeface.createFromAsset(context.getAssets(), fontPath);
                sTypeFaces.put(font, tempTypeface);
            } catch (java.lang.RuntimeException e) {
                CustomLog.warn("TypefaceProvider", "Cannot load custom typeface " + fontPath, e);
            }
        }

        return tempTypeface;
    }

    public static void setTypeFace(Context context, TextView textView, String font) {
        if (textView.getVisibility() == View.VISIBLE) {
            Typeface typeface = getTypeFace(context, font);
            if (typeface != null) {
                textView.setTypeface(typeface);
                //For making the font anti-aliased.
                textView.setPaintFlags(textView.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
            }
        }
    }
}