package com.sharefable.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ESQueryable {
    // The match query will apply the same standard analyzer to the search term
    // and will therefore match what is stored in the index.
    // The term query does not apply any analyzers to the search term,
    // so will only look for that exact term in the inverted index.
    enum SearchType {
        Match,
        Term
    }

    boolean exclude() default false;

    SearchType type() default SearchType.Match;

    boolean isAlsoKeyword() default false;
}
