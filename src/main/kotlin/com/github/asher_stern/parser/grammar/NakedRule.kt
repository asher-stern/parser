package com.github.asher_stern.parser.grammar

/**
 * Created by Asher Stern on November-04 2017.
 */
data class NakedRule<N, T>(val lhs: N, val rhs: List<SyntacticItem<N, T>>)
{
    val friendlyString: String by lazy {
        "$lhs -> " + rhs.joinToString(" ") {
            if (it.symbol != null) it.symbol.toString() else "#"+it.terminal!!.toString()
        }
    }
}