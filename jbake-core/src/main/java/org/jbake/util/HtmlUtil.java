package org.jbake.util;

import org.jbake.app.Crawler.Attributes;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Manik Magar
 */
public class HtmlUtil {

    private static final Map<String, String> TAG_ATTR;
    private static final char SLASH_CHAR = '/';
    private static final String SLASH = Character.toString(SLASH_CHAR);
    private static final String EMPTY = "";
    private static final String REL_START_REGEX = "\\./";

    static {
        Map<String, String> map = new HashMap<>();
        map.put("a", "href");
        map.put("img", "src");
        TAG_ATTR = Collections.unmodifiableMap(map);
    }

    private HtmlUtil() {
    }

    /**
     * Image paths are specified as w.r.t. assets folder. This function prefix site host to all img src except
     * the ones that starts with http://, https://.
     * <p>
     * If image path starts with "./", i.e. relative to the source file, then it first replace that with output file directory and the add site host.
     *
     * @param fileContents  Map representing file contents
     * @param configuration Configuration object
     * @see #fixRelativeSourceUrls(Map, JBakeConfiguration)
     * @deprecated use {@link #fixRelativeSourceUrls(Map, JBakeConfiguration)} instead.
     */
    public static void fixImageSourceUrls(Map<String, Object> fileContents, JBakeConfiguration configuration) {
        fixRelativeSourceUrls(fileContents, configuration);
    }

    /**
     * Image or link paths are specified as w.r.t. assets folder. This function prefix site host to all img src or a href
     * except the ones that starts with http://, https://.
     * <p>
     * If image path starts with "./", i.e. relative to the source file, then it first replace that with output file directory and the add site host.
     *
     * @param fileContents  Map representing file contents
     * @param configuration Configuration object
     */
    public static void fixRelativeSourceUrls(Map<String, Object> fileContents, JBakeConfiguration configuration) {
        String htmlContent = fileContents.get(Attributes.BODY).toString();
        boolean prependSiteHost = configuration.getRelativePathPrependHost();
        String siteHost = configuration.getSiteHost();
        String uri = getDocumentUri(fileContents);

        Document document = Jsoup.parseBodyFragment(htmlContent);
        for (Map.Entry<String, String> entry : TAG_ATTR.entrySet()) {
            String tagName = entry.getKey();
            String attrKey = entry.getValue();
            Elements tags = document.getElementsByTag(tagName);
            for (Element tag : tags) {
                transformRelativeSource(tag, attrKey, uri, siteHost, prependSiteHost);
            }
        }

        //Use body().html() to prevent adding <body></body> from parsed fragment.
        fileContents.put(Attributes.BODY, document.body().html());
    }

    private static String getDocumentUri(Map<String, Object> fileContents) {
        String uri = fileContents.get(Attributes.URI).toString();
        Object noExtUri;
        if ((noExtUri = fileContents.get(Attributes.NO_EXTENSION_URI)) != null) {
            uri = noExtUri.toString();
            uri = removeTrailingSlash(uri);
        }

        if (uri.contains(SLASH)) {
            uri = removeFilename(uri);
        }
        return uri;
    }

    private static void transformRelativeSource(Element element, String attributeKey, String uri, String siteHost, boolean prependSiteHost) {
        String source = element.attr(attributeKey);

        // Now add the root path
        if (!isHttpURL(source)) {
            if (isRelative(source)) {
                source = uri + source.replaceFirst(REL_START_REGEX, EMPTY);
            }

            if (prependSiteHost) {
                if (!siteHost.endsWith(SLASH) && isRelative(source)) {
                    siteHost = siteHost.concat(SLASH);
                }
                source = siteHost + source;
            }
            element.attr(attributeKey, source);
        }
    }

    /**
     * Remove filename and keep the last slash.
     *
     * @param uri the uri
     * @return parent uri
     */
    private static String removeFilename(String uri) {
        uri = uri.substring(0, uri.lastIndexOf(SLASH_CHAR) + 1);
        return uri;
    }

    private static String removeTrailingSlash(String uri) {
        if (uri.endsWith(SLASH)) {
            uri = uri.substring(0, uri.length() - 1);
        }
        return uri;
    }

    private static boolean isHttpURL(String source) {
        return source.startsWith("http://") || source.startsWith("https://");
    }

    private static boolean isRelative(String source) {
        return !source.startsWith(SLASH);
    }
}
