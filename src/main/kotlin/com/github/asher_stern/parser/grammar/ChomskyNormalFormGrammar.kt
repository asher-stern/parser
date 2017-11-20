package com.github.asher_stern.parser.grammar

import com.github.asher_stern.parser.utils.Table2D
import com.github.asher_stern.parser.utils.Table3D

/**
 * Created by Asher Stern on November-02 2017.
 */

/**
 * A data-structure for grammar in Chomsky-normal-form (with the addition of also single-symbol to single-symbol rules).
 * It holds the same information as [Grammar], but is more convenient to work with in CYK algorithm (see [com.github.asher_stern.parser.cyk.CykAlgorithm]).
 */
data class ChomskyNormalFormGrammar<N, T>(
        val startSymbol: N,
        val terminals: Set<T>,
        val nonTerminals: Set<N>,
        val terminalRules: Table2D<T, N, Double>, // from terminal to non-terminals (to log probability)
        val singleToSingleRules: Table2D<N, N, Double>, // from rhs to lhs (to log probability)
        val nonTerminalRules: Table3D<N, N, N, Double> // from first & second in the RHS to the LHS (to log probability)
)
{
    val listNonTerminalRules: List<RawChomskyNormalFormRule<N>>
    init
    {
        val _list = mutableListOf<RawChomskyNormalFormRule<N>>()
        for (rhsFirst in nonTerminalRules.firstIndexes)
        {
            for ( (rhsSecond, lhsMap) in nonTerminalRules[rhsFirst]!! )
            {
                for ( (lhs, logProbability) in lhsMap )
                {
                    _list.add(RawChomskyNormalFormRule(lhs, rhsFirst, rhsSecond, logProbability!!))
                }
            }
        }
        listNonTerminalRules = _list
    }

    val listSingleToSingleRules: List<RawSingleToSingleRule<N>>
    init
    {
        val _list = mutableListOf<RawSingleToSingleRule<N>>()
        for (rhs in singleToSingleRules.firstIndexes)
        {
            for ((lhs, logProbability) in singleToSingleRules[rhs]!!)
            {
                _list.add(RawSingleToSingleRule(lhs, rhs, logProbability!!))
            }
        }
        listSingleToSingleRules = _list
    }
}

data class RawChomskyNormalFormRule<N>(val lhs: N, val rhsFirst: N, val rhsSecond: N, val logProbability: Double)

data class RawSingleToSingleRule<N>(val lhs: N, val rhs: N, val logProbability: Double)
