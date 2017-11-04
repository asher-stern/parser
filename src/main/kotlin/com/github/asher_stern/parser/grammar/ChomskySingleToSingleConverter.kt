package com.github.asher_stern.parser.grammar

/**
 * Created by Asher Stern on November-04 2017.
 */
class ChomskySingleToSingleConverter<N, T>(private val grammar: Grammar<N ,T>)
{
    fun convert()
    {
        val listNonTerminals = (grammar.nonTerminals - grammar.startSymbol).toList() + (grammar.startSymbol)
        for (lhs in lhsToRules.keys)
        {
            for (rule in lhsToRules.getValue(lhs))
            {
                processRule(rule)
            }
        }
    }

    val newRules = mutableMapOf<N, MutableSet<Rule<N, T>>>()
    val collapsedMap = mutableMapOf<NakedRule<N, T>, List<N>>()
    val newGrammar: Grammar<N, T> by lazy {
        val setOfRules = mutableSetOf<Rule<N, T>>()
        for ((_, lhsRules) in newRules)
        {
            setOfRules.addAll(lhsRules)
        }
        Grammar(grammar.startSymbol, grammar.nonTerminals, grammar.terminals, setOfRules)
    }


    private fun processRule(rule: Rule<N, T>)
    {
        if (isSingleToSingle(rule))
        {
            val lhs = rule.lhs
            val ruleProbability = Math.exp(rule.logProbability)

            val childSymbol = rule.rhs.first().symbol!!
            val childSymbolRules: Set<Rule<N, T>> = when
            {
                (childSymbol != lhs) && (childSymbol in newRules.keys) -> newRules.getValue(childSymbol)
                else -> lhsToRules.getValue(childSymbol)
            }
            val permittedRules = childSymbolRules.filter { !isSingleToSingle(it) }

            val sumProbabilities = permittedRules.sumByDouble { Math.exp(it.logProbability) }
            for (permittedRule in permittedRules)
            {
                val newNakedRule = NakedRule(lhs, permittedRule.rhs)
                val newRuleProbability = (Math.exp(permittedRule.logProbability) / sumProbabilities) * ruleProbability
                val existingProbability = getNakedRuleProbability(newNakedRule)
                if (existingProbability < newRuleProbability)
                {
                    val newRule = Rule<N, T>("${rule.name}_collapse_$lhs", lhs, permittedRule.rhs, false, Math.log(newRuleProbability))
                    newRules._removeByNaked(lhs, newNakedRule)
                    newRules._addToSet(lhs, newRule)

                    collapsedMap.put(newNakedRule, listOf(childSymbol))
                }
            }
        }
        else
        {
            newRules.computeIfAbsent(rule.lhs) { mutableSetOf() }.add(rule)
        }
    }

    private fun isSingleToSingle(rule: Rule<N, T>): Boolean =
            (rule.rhs.size == 1) && ( rule.rhs.first().symbol != null )

    private fun getNakedRuleProbability(nakedRule: NakedRule<N, T>): Double
    {
        val originalMaxRule = lhsToRules.getOrDefault(nakedRule.lhs, emptySet()).filter { it._toNakedRule() == nakedRule }.maxBy { it.logProbability }
        val newMaxRule = newRules.getOrDefault(nakedRule.lhs, emptySet<Rule<N, T>>()).filter { it._toNakedRule() == nakedRule }.maxBy { it.logProbability }
        val originalProbability = if (originalMaxRule != null) Math.exp(originalMaxRule.logProbability) else 0.0
        val newProbability = if (newMaxRule != null) Math.exp(newMaxRule.logProbability) else 0.0
        return maxOf(originalProbability, newProbability)
    }

    private val lhsToRules: Map<N, Set<Rule<N, T>>> = grammar.rules._associateToMapSetBy { it.lhs }
}


private fun <K, V> MutableMap<K , MutableSet<V>>._addToSet(key: K, value: V)
{
    computeIfAbsent(key) { mutableSetOf() }.add(value)
}

private fun <T, K> Set<T>._associateToMapSetBy(keySelector: (T)->K): MutableMap<K, MutableSet<T>>
{
    val ret = mutableMapOf<K, MutableSet<T>>()
    for (item in this)
    {
        ret._addToSet( keySelector(item), item )
    }
    return ret
}

private fun <N, T> MutableMap<N, MutableSet<Rule<N, T>>>._removeByNaked(lhs: N, nakedRule: NakedRule<N, T>)
{
    if (containsKey(lhs))
    {
        val set = getValue(lhs)
        set.removeIf { it._toNakedRule() == nakedRule }
    }
}

private fun <N, T> Rule<N, T>._toNakedRule(): NakedRule<N, T>
{
    return NakedRule(lhs, rhs)
}