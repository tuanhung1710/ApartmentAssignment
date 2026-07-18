package apartmentmanagement.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight whitelist sanitizer for Quill comment HTML (no external deps).
 * Allows: p, br, strong/b, em/i, u, s, ol, ul, li, a[href], blockquote.
 */
public final class HtmlSanitizer {

    private static final Pattern TAG_PATTERN = Pattern.compile(
            "<(/?)([a-zA-Z0-9]+)([^>]*)>", Pattern.DOTALL);
    private static final Pattern ATTR_PATTERN = Pattern.compile(
            "([a-zA-Z_:][-a-zA-Z0-9_:.]*?)\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s\"'=<>`]+)");
    private static final Pattern STRIP_TAGS = Pattern.compile("<[^>]+>");

    private HtmlSanitizer() {
    }

    public static String sanitize(String html) {
        if (html == null) {
            return null;
        }
        String input = html.trim();
        if (input.isEmpty()) {
            return "";
        }

        StringBuilder out = new StringBuilder(input.length());
        Matcher m = TAG_PATTERN.matcher(input);
        int last = 0;
        while (m.find()) {
            out.append(escapeText(input.substring(last, m.start())));
            String slash = m.group(1);
            String tag = m.group(2).toLowerCase(Locale.ROOT);
            String attrs = m.group(3) == null ? "" : m.group(3);

            if ("br".equals(tag)) {
                if (slash.isEmpty()) {
                    out.append("<br>");
                }
            } else if (isSimpleTag(tag)) {
                if (slash.isEmpty()) {
                    out.append('<').append(tag).append('>');
                } else {
                    out.append("</").append(tag).append('>');
                }
            } else if ("a".equals(tag)) {
                if (slash.isEmpty()) {
                    String href = extractHref(attrs);
                    if (href != null) {
                        out.append("<a href=\"").append(escapeAttr(href))
                                .append("\" rel=\"noopener noreferrer\" target=\"_blank\">");
                    } else {
                        // drop unsafe/empty link open tag
                    }
                } else {
                    out.append("</a>");
                }
            } else {
                // drop unknown tags (keep text later via last index)
            }
            last = m.end();
        }
        out.append(escapeText(input.substring(last)));
        return out.toString().trim();
    }

    /** True when HTML has no visible text (e.g. empty Quill &lt;p&gt;&lt;br&gt;&lt;/p&gt;). */
    public static boolean isBlankHtml(String html) {
        if (html == null) {
            return true;
        }
        String plain = stripTags(html).replace(' ', ' ').trim();
        return plain.isEmpty();
    }

    public static String stripTags(String html) {
        if (html == null) {
            return "";
        }
        return STRIP_TAGS.matcher(html).replaceAll("").trim();
    }

    private static boolean isSimpleTag(String tag) {
        return "p".equals(tag)
                || "strong".equals(tag) || "b".equals(tag)
                || "em".equals(tag) || "i".equals(tag)
                || "u".equals(tag) || "s".equals(tag)
                || "ol".equals(tag) || "ul".equals(tag) || "li".equals(tag)
                || "blockquote".equals(tag);
    }

    private static String extractHref(String attrs) {
        if (attrs == null || attrs.isEmpty()) {
            return null;
        }
        Matcher am = ATTR_PATTERN.matcher(attrs);
        while (am.find()) {
            String name = am.group(1).toLowerCase(Locale.ROOT);
            if (!"href".equals(name)) {
                continue;
            }
            String raw = am.group(2);
            if (raw.length() >= 2
                    && ((raw.charAt(0) == '"' && raw.charAt(raw.length() - 1) == '"')
                    || (raw.charAt(0) == '\'' && raw.charAt(raw.length() - 1) == '\''))) {
                raw = raw.substring(1, raw.length() - 1);
            }
            raw = raw.trim();
            String lower = raw.toLowerCase(Locale.ROOT);
            if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("mailto:")) {
                return raw;
            }
            return null;
        }
        return null;
    }

    private static String escapeText(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                default:
                    sb.append(ch);
            }
        }
        return sb.toString();
    }

    private static String escapeAttr(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                default:
                    sb.append(ch);
            }
        }
        return sb.toString();
    }
}
