package tools.subathon.timer.bot.util;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TemplateParser {

    private sealed interface Segment {
        boolean isPlaceholder();
    }

    private record Literal(String text) implements Segment {
        @Override public boolean isPlaceholder() { return false; }
    }

    private record Placeholder(String name) implements Segment {
        @Override public boolean isPlaceholder() { return true; }
    }

    private final List<Segment> segments;

    private TemplateParser(List<Segment> segments) {
        this.segments = segments;
    }

    public static Builder builder() {
        return new Builder();
    }

    public ParseResult parse(@NonNull String message) {
        Map<String, String> values = new HashMap<>();
        int cursor = 0;

        for (int i = 0; i < segments.size(); i++) {
            Segment seg = segments.get(i);

            if (seg instanceof Literal(String text)) {
                int index = message.indexOf(text, cursor);
                if (index == -1) {
                    return ParseResult.failure("Literal '" + text + "' not found at expected position");
                }
                cursor = index + text.length();
            } else if (seg instanceof Placeholder(String name)) {
                String nextLiteral = null;
                if (i + 1 < segments.size() && segments.get(i + 1) instanceof Literal(String text)) {
                    nextLiteral = text;
                }

                int varStart = cursor;
                int varEnd;

                if (nextLiteral != null) {
                    int anchor = message.indexOf(nextLiteral, varStart);
                    if (anchor == -1) {
                        return ParseResult.failure("Missing anchor '" + nextLiteral + "' for placeholder '" + name + "'");
                    }
                    varEnd = anchor;
                } else {
                    varEnd = message.length();
                }

                String rawValue = message.substring(varStart, varEnd);

                if (rawValue.isEmpty()) {
                    return ParseResult.failure("Placeholder '" + name + "' is empty");
                }

                values.put(name, rawValue);
                cursor = varEnd;
            }
        }

        return ParseResult.success(values);
    }

    public static class Builder {
        private String template;

        public Builder withTemplate(@NonNull String template) {
            this.template = template;
            return this;
        }

        public TemplateParser build() {
            if (template == null) {
                throw new IllegalArgumentException("Must provide a template");
            }
            List<Segment> segments = new ArrayList<>();
            Set<String> seenPlaceholders = new HashSet<>();
            Set<String> allowedPlaceholders = Set.of("user", "amount");

            int i = 0;
            while (i < template.length()) {
                int start = template.indexOf("{", i);
                if (start == -1) {
                    if (i < template.length()) {
                        segments.add(new Literal(template.substring(i)));
                    }
                    break;
                }

                if (start > i) {
                    segments.add(new Literal(template.substring(i, start)));
                }

                int end = template.indexOf('}', start);
                if (end == -1) {
                    throw new IllegalArgumentException("Unclosed placeholder in template: " + template);
                }

                String name = template.substring(start + 1, end);
                if (name.isBlank()) {
                    throw new IllegalArgumentException("Placeholder name cannot be empty: {}");
                }
                if (!seenPlaceholders.add(name)) {
                    throw new IllegalArgumentException("Duplicate placeholder name: " + name);
                }
                if (!allowedPlaceholders.contains(name)) {
                    throw new IllegalArgumentException("Invalid placeholder {" + name + "}, allowed placeholders are: '{user}', '{amount}'");
                }

                segments.add(new Placeholder(name));
                i = end + 1;
            }

            if (segments.isEmpty()) {
                throw new IllegalArgumentException("Template must contain at least one segment");
            }

            if (segments.stream().filter(Segment::isPlaceholder).noneMatch(p -> ((Placeholder) p).name.equals("amount"))) {
                throw new IllegalArgumentException("Template must contain at least 'amount' placeholder: " + template);
            }

            for (int j = 1; j < segments.size(); j++) {
                if (segments.get(j).isPlaceholder() && segments.get(j - 1).isPlaceholder()) {
                    throw new IllegalArgumentException("Template cannot contain adjacent placeholders: " + template);
                }
            }

            return new TemplateParser(segments);
        }
    }

    public record ParseResult(boolean success, Map<String, String> values, String errorMessage) {
        public static ParseResult success(Map<String, String> values) {
            return new ParseResult(true, values, null);
        }

        public static ParseResult failure(String errorMessage) {
            return new ParseResult(false, null, errorMessage);
        }
    }
}
