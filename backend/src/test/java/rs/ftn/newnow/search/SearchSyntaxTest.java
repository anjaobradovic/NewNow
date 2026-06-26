package rs.ftn.newnow.search;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class SearchSyntaxTest {

    @Test
    void plainInputBecomesMatch() {
        SearchSyntax.Parsed p = SearchSyntax.parse("arena");
        assertSame(SearchSyntax.Mode.MATCH, p.mode());
        assertEquals("arena", p.term());
    }

    @Test
    void quotedInputBecomesPhrase() {
        SearchSyntax.Parsed p = SearchSyntax.parse("\"Ivan Marić\"");
        assertSame(SearchSyntax.Mode.PHRASE, p.mode());
        assertEquals("Ivan Marić", p.term());
    }

    @Test
    void singleQuoteCharactersAreLeftAsPlainMatch() {
        // Only fully wrapped values qualify; a stray quote is treated as plain.
        SearchSyntax.Parsed p = SearchSyntax.parse("\"Ivan");
        assertSame(SearchSyntax.Mode.MATCH, p.mode());
        assertEquals("\"Ivan", p.term());
    }

    @Test
    void starInputBecomesPrefix() {
        SearchSyntax.Parsed p = SearchSyntax.parse("Ivan M*");
        assertSame(SearchSyntax.Mode.PREFIX, p.mode());
        // We strip the trailing star but keep the rest intact; match_bool_prefix runs the
        // text through the field's analyzer and prefix-matches the last token.
        assertEquals("Ivan M", p.term());
    }

    @Test
    void tildePrefixBecomesFuzzy() {
        SearchSyntax.Parsed p = SearchSyntax.parse("~rizika");
        assertSame(SearchSyntax.Mode.FUZZY, p.mode());
        assertEquals("rizika", p.term());
    }

    @Test
    void whitespaceIsTrimmedAndEmptyInputReturnsEmpty() {
        SearchSyntax.Parsed p = SearchSyntax.parse("   ");
        assertSame(SearchSyntax.Mode.EMPTY, p.mode());
        SearchSyntax.Parsed q = SearchSyntax.parse(null);
        assertSame(SearchSyntax.Mode.EMPTY, q.mode());
    }

    @Test
    void emptyAfterStrippingTildeIsEmpty() {
        SearchSyntax.Parsed p = SearchSyntax.parse("~");
        assertSame(SearchSyntax.Mode.EMPTY, p.mode());
    }

    @Test
    void emptyQuotedStringIsEmpty() {
        SearchSyntax.Parsed p = SearchSyntax.parse("\"\"");
        assertSame(SearchSyntax.Mode.EMPTY, p.mode());
    }
}
