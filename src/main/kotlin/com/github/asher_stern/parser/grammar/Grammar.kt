package com.github.asher_stern.parser.grammar

/**
 * Created by Asher Stern on November-02 2017.
 */
data class Grammar<N, T>(
        val startSymbol: N,
        val nonTerminals: Set<N>,
        val terminals: Set<T>,
        val rules: Set<Rule<N, T>>
)
