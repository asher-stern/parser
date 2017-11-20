package com.github.asher_stern.parser.cyk

/**
 * Created by Asher Stern on November-02 2017.
 */

/**
 * Represents an item in a cell of the CYK table.
 * @property lhs the left-hand-side of a rule - a symbol from which the span (which corresponds to the cell) can be derived from.
 * @property rhsSingleSymbol an optional list of symbols derived from the [lhs], such that the span (which corresponds to the cell)
 * is derived from the last symbol in the list. For example, if the derivation for the sentence "I go" is  S - VP - NP VP,
 * then the VP is in this list, S is the [lhs], and NP VP are the two rhs children: [rhsFirst] and [rhsSecond].
 * @property rhsFirst the first child of the [lhs]. It spans the first part of the span which corresponds to the cell.
 * @property rhsSecond the second child of the [lhs]. It spans the second part of the span which corresponds to the cell.
 * @property secondBeginIndex the index (starting at 1) of the terminal that starts the second part of the span. The span
 * is part of the sentence, corresponding to the cell in the table. It is divided into two sub-spans (in the case where [rhsFirst]
 * and [rhsSecond] are not null), and [secondBeginIndex] is the index where the second sub-span begins.
 * @property logProbability the log (ln) of the probability of deriving the span (which corresponds to the cell) from [lhs].
 */
data class CykTableItem<N>(
        val lhs: N,
        val rhsSingleSymbol: List<N>?,
        val rhsFirst: N?,
        val rhsSecond: N?,
        val secondBeginIndex: Int?,
        val logProbability: Double
)
