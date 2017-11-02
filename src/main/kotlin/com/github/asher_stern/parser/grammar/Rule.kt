package com.github.asher_stern.parser.grammar

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
