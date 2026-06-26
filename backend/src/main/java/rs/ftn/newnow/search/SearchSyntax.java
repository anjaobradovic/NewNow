package rs.ftn.newnow.search;

/**
 * Detects the query-type syntax embedded in a single text input.
 *
 * Rules (first match wins):
 *  - "…"  fully wrapped in double quotes → PHRASE  (strip the quotes)
 *  - …*…  contains an asterisk          → PREFIX  (strip the * characters)
 *  - ~…   starts with a tilde           → FUZZY   (strip the leading ~)
 *  - anything else                      → MATCH
 *
 * Blank input or input that becomes empty after stripping → EMPTY (no clause).
 */
public final class SearchSyntax {

    public enum Mode { EMPTY, MATCH, PHRASE, PREFIX, FUZZY }

    public record Parsed(Mode mode, String term) {}

    private static final Parsed EMPTY = new Parsed(Mode.EMPTY, "");

    private SearchSyntax() {}

    public static Parsed parse(String raw) {
        if (raw == null) return EMPTY;
        String s = raw.trim();
        if (s.isEmpty()) return EMPTY;

        // PHRASE: must start AND end with a double quote, and have content between.
        if (s.length() >= 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
            String inner = s.substring(1, s.length() - 1).trim();
            return inner.isEmpty() ? EMPTY : new Parsed(Mode.PHRASE, inner);
        }

        // PREFIX: any asterisk anywhere. Strip the asterisks; the remaining text is
        // passed to match_bool_prefix which prefix-matches the last token.
        if (s.indexOf('*') >= 0) {
            String stripped = s.replace("*", "").trim();
            return stripped.isEmpty() ? EMPTY : new Parsed(Mode.PREFIX, stripped);
        }

        // FUZZY: leading tilde.
        if (s.charAt(0) == '~') {
            String inner = s.substring(1).trim();
            return inner.isEmpty() ? EMPTY : new Parsed(Mode.FUZZY, inner);
        }

        return new Parsed(Mode.MATCH, s);
    }
}
