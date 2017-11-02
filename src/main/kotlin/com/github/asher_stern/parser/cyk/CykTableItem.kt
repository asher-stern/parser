package com.github.asher_stern.parser.cyk

/**
 * Created by Asher Stern on November-02 2017.
 */
data class CykTableItem<N>(
        val lhs: N,
        val rhsFirst: N?,
        val rhsSecond: N?,
        val secondBeginIndex: Int?,
        val logProbability: Double
)
