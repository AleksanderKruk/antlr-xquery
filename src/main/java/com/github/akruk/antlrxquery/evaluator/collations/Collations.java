package com.github.akruk.antlrxquery.evaluator.collations;

import java.text.Collator;
import java.util.Locale;

public final class Collations {
    /** URI standardowej Unicode‐codepoint collation wg XPath-Functions. */
    public static final String CODEPOINT_URI =
      "http://www.w3.org/2005/xpath-functions/collation/codepoint";

    public static final Collator DEFAULT_COLLATOR;

    static {
        DEFAULT_COLLATOR = Collator.getInstance(Locale.ROOT);
        DEFAULT_COLLATOR.setStrength(Collator.IDENTICAL);
        DEFAULT_COLLATOR.setDecomposition(Collator.NO_DECOMPOSITION);
    }

    private Collations() {
        // tylko statyczne pola
    }
}
