package com.github.asher_stern.parser.grammar

import com.github.asher_stern.parser.utils.Table2D
import com.github.asher_stern.parser.utils.Table3D

/**
 * Created by Asher Stern on November-04 2017.
 */
class ChomskyNormalFormGrammarConstructor<N, T>(
        private val grammar: Grammar<N, T>
)
{
    fun construct(): ChomskyNormalFormGrammar<N, T>
    {
        for (rule in grammar.rules)
        {
            if (isTerminalRule(rule))
            {
                terminalRules[rule.rhs.first().terminal!!, rule.lhs] = rule.logProbability
            }
            else if (isNonTerminalRule(rule))
            {
                val (first, second) = rule.rhs.map { it.symbol!! }
                nonTerminalRules[first, second, rule.lhs] = rule.logProbability
            }
            else
            {
                throw RuntimeException("Illegal rule: $rule")
            }
        }

        return ChomskyNormalFormGrammar(grammar.startSymbol, grammar.terminals, grammar.nonTerminals, terminalRules, nonTerminalRules)
    }


    private fun isTerminalRule(rule: Rule<N, T>): Boolean =
            (rule.rhs.size == 1) && (rule.rhs.first().terminal != null)

    private fun isNonTerminalRule(rule: Rule<N, T>): Boolean =
            (rule.rhs.size == 2) && ( rule.rhs.all { it.symbol != null } )


    private val terminalRules = Table2D<T, N, Double>() // from terminal to non-terminals (to log probability)
    private val nonTerminalRules = Table3D<N, N, N, Double>() // from first & second in the RHS to the LHS (to log probability)
}