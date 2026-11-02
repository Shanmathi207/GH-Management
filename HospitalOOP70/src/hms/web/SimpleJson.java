package hms.web;

import java.util.*;

/**
 * Lightweight, zero-dependency JSON serializer and parser for HospitalOOP70 REST APIs.
 */
public class SimpleJson {

    public static String stringify(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) {
            return "\"" + escape((String) obj) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escape(String.valueOf(entry.getKey()))).append("\":");
                sb.append(stringify(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof Collection) {
            Collection<?> col = (Collection<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : col) {
                if (!first) sb.append(",");
                sb.append(stringify(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj instanceof Object[]) {
            Object[] arr = (Object[]) obj;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : arr) {
                if (!first) sb.append(",");
                sb.append(stringify(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + escape(obj.toString()) + "\"";
    }

    private static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (ch < ' ') {
                        String t = "000" + Integer.toHexString(ch);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(ch);
                    }
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * Parses a flat or nested JSON object into a Map of key-value pairs.
     */
    public static Map<String, Object> parseObject(String json) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (json == null) return map;
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) return map;

        int len = json.length();
        int i = 1;

        while (i < len - 1) {
            // Skip whitespace
            while (i < len - 1 && Character.isWhitespace(json.charAt(i))) i++;
            if (i >= len - 1 || json.charAt(i) == '}') break;

            // Key must be a string
            if (json.charAt(i) != '"') break;
            i++;
            int keyStart = i;
            while (i < len && json.charAt(i) != '"') {
                if (json.charAt(i) == '\\') i++;
                i++;
            }
            String key = unescape(json.substring(keyStart, i));
            i++; // skip closing quote

            // Skip colon and whitespace
            while (i < len && (Character.isWhitespace(json.charAt(i)) || json.charAt(i) == ':')) i++;

            // Parse value
            int valStart = i;
            if (json.charAt(i) == '"') {
                i++;
                int strStart = i;
                while (i < len && json.charAt(i) != '"') {
                    if (json.charAt(i) == '\\') i++;
                    i++;
                }
                String val = unescape(json.substring(strStart, i));
                map.put(key, val);
                i++; // skip quote
            } else if (json.charAt(i) == '{') {
                int depth = 1;
                i++;
                while (i < len && depth > 0) {
                    if (json.charAt(i) == '{') depth++;
                    else if (json.charAt(i) == '}') depth--;
                    i++;
                }
                map.put(key, parseObject(json.substring(valStart, i)));
            } else if (json.charAt(i) == '[') {
                int depth = 1;
                i++;
                while (i < len && depth > 0) {
                    if (json.charAt(i) == '[') depth++;
                    else if (json.charAt(i) == ']') depth--;
                    i++;
                }
                map.put(key, json.substring(valStart, i));
            } else {
                while (i < len && json.charAt(i) != ',' && json.charAt(i) != '}') i++;
                String raw = json.substring(valStart, i).trim();
                if ("true".equalsIgnoreCase(raw)) map.put(key, Boolean.TRUE);
                else if ("false".equalsIgnoreCase(raw)) map.put(key, Boolean.FALSE);
                else if ("null".equalsIgnoreCase(raw)) map.put(key, null);
                else {
                    try {
                        if (raw.contains(".")) {
                            map.put(key, Double.parseDouble(raw));
                        } else {
                            map.put(key, Long.parseLong(raw));
                        }
                    } catch (Exception ex) {
                        map.put(key, raw);
                    }
                }
            }

            // Skip whitespace and optional comma
            while (i < len && (Character.isWhitespace(json.charAt(i)) || json.charAt(i) == ',')) i++;
        }

        return map;
    }

    private static String unescape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            if (ch == '\\' && i + 1 < len) {
                char next = s.charAt(++i);
                switch (next) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    default: sb.append(next); break;
                }
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}

