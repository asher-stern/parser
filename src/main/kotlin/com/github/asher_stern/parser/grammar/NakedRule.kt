package com.github.asher_stern.parser.grammar

/**
 * Created by Asher Stern on November-04 2017.
 */

/**
 * A minimalist representation of a syntactic-rule: only the left-hand-side and the right-hand-side.
 * A more detailed class is [Rule], which contains also the probability of the rule, its name, etc.
 */
data class NakedRule<N, T>(val lhs: N, val rhs: List<SyntacticItem<N, T>>)
{
    val friendlyString: String by lazy {
        "$lhs -> " + rhs.joinToString(" ") {
            if (it.symbol != null) it.symbol.toString() else "#"+it.terminal!!.toString()
        }
    }
}