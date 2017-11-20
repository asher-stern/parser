package com.github.asher_stern.parser.grammar

/**
 * Created by Asher Stern on November-02 2017.
 */

/**
 * Data-structure representing a grammar.
 * @property startSymbol the start symbol: the root of every parse-tree is this symbol.
 * @property nonTerminals all the symbols in the grammar.
 * @property terminals all the terminals in the grammar.
 * @property rules all the rules in the grammer.
 */
data class Grammar<N, T>(
        val startSymbol: N,
        val nonTerminals: Set<N>,
        val terminals: Set<T>,
        val rules: Set<Rule<N, T>>
)
