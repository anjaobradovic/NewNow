package rs.ftn.newnow.search;

/**
 * How to combine multiple search clauses across fields.
 * AND  -> every supplied clause goes into bool.must     (default; "all must match")
 * OR   -> every supplied clause goes into bool.should   ("at least one matches", with
 *         minimum_should_match=1)
 *
 * The deleted=false filter always stays in bool.filter regardless of operator.
 */
public enum BoolOperator {
    AND,
    OR;

    public static BoolOperator from(String raw) {
        if (raw == null) return AND;
        return "or".equalsIgnoreCase(raw.trim()) ? OR : AND;
    }
}
