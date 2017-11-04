package com.github.asher_stern.parser.grammar

import com.github.asher_stern.parser.tree.PosAndWord

/**
 * Created by Asher Stern on November-02 2017.
 */
data class Rule<N, T>(
        val name: String,
        val lhs: N,
        val rhs: List<SyntacticItem<N, T>>,
        val auxiliary: Boolean,
        val logProbability: Double
)
{
    val friendlyString: String by lazy {
        "$lhs -> " + rhs.joinToString(" ") {
            if (it.symbol != null) it.symbol.toString() else "#" + it.terminal!!
        } + " ($logProbability)"
    }

}




