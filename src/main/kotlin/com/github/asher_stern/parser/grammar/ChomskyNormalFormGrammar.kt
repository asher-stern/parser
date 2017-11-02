package com.github.asher_stern.parser.grammar

import com.github.asher_stern.parser.utils.Table2D
import com.github.asher_stern.parser.utils.Table3D

/**
 * Created by Asher Stern on November-02 2017.
 */
data class ChomskyNormalFormGrammar<N, T>(
        val startSymbol: N,
        val terminals: Set<T>,
        val nonTerminals: Set<N>,
        val terminalRules: Table2D<T, N, Double>, // from terminal to non-terminals (to log probability)
        val nonTerminalRules: Table3D<N, N, N, Double> // from first & second in the RHS to the LHS (to log probability)
)
