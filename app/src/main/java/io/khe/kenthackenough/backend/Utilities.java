package io.khe.kenthackenough.backend;

import android.text.Html;
import android.text.SpannableString;
import android.text.style.URLSpan;
import android.util.Log;

import com.github.rjeschke.txtmark.Processor;

import java.net.MalformedURLException;
import java.net.URL;

import io.khe.kenthackenough.Config;

/**
 * A class to hold small pieces of code used between many functions
 */
public class Utilities {

    /**
     * getSpannableFromMarkdown converts markdown into a SpannableString suitable for use with TextViews
     * @param markdown the markdown to convert
     * @return a SpannableString corresponding to the information in the markdown
     */
    static SpannableString getSpannableFromMarkdown(String markdown) {
        return getSpannableFromHTML(Processor.process(markdown));
    }


    /**
     * getSpannableFromMarkdown converts markdown into a SpannableString suitable for use with TextViews
     * @param html the html to convert
     * @return a SpannableString corresponding to the information in the markdown
     */
    static SpannableString getSpannableFromHTML(String html) {
        SpannableString result = new SpannableString(Html.fromHtml(html));
        URLSpan[] links = result.getSpans(0,result.length(), URLSpan.class);

        for (URLSpan link : links) {

            // remove and re-add if it's valid
            int start = result.getSpanStart(link);
            int end = result.getSpanEnd(link);
            int flags = result.getSpanFlags(link);
            result.removeSpan(link);
            try {
                URL url = new URL(link.getURL());
                link = new URLSpan(url.toString());
                result.setSpan(link, start, end, flags);
            } catch (MalformedURLException e) {
                try {
                    // make an attempt to fix simple cases of missing http://
                    URL url = new URL("http://" + link.getURL());
                    link = new URLSpan(url.toString());
                    result.setSpan(link, start, end, flags);
                } catch (MalformedURLException e1) {
                    Log.e(Config.DEBUG_TAG, "Bad URL: " + link.getURL() + " in " + html);
                }
            }
        }
        return result;
    }

}
