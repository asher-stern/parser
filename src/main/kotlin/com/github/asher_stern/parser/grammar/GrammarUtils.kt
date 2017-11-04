package com.github.asher_stern.parser.grammar

/**
 * Created by Asher Stern on November-03 2017.
 */

fun <N> extractAllNonTerminals(rules: Collection<Rule<N, *>>): Set<N>
{
    val ret = mutableSetOf<N>()
    for (rule in rules)
    {
        ret.add(rule.lhs)
        ret.addAll(rule.rhs.filter { it.symbol != null }.map { it.symbol!! })
    }
    return ret
}
