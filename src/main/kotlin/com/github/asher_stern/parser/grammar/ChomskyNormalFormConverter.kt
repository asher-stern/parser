package com.github.asher_stern.parser.grammar

/**
 * Created by Asher Stern on November-02 2017.
 */
abstract class ChomskyNormalFormConverter<N, T>(private val grammar: Grammar<N ,T>)
{
    fun convert()
    {
        var rules = mutableListOf<Rule<N, T>>()
        rules.addAll(grammar.rules)
        val wellFormedRules = mutableSetOf<Rule<N, T>>()
        val singleToSingleRules = mutableSetOf<Rule<N, T>>()

        while (!rules.isEmpty())
        {
            val nextIterationRules = mutableListOf<Rule<N, T>>()
            for (rule in rules)
            {
                if (isWellFormed(rule))
                {
                    wellFormedRules.add(rule)
                }
                else if (isSingleToSingle(rule))
                {
                    singleToSingleRules.add(rule)
                }
                else
                {
                    val (newRule, auxiliaryRule) = convertRule(rule)
                    wellFormedRules.add(auxiliaryRule)
                    nextIterationRules.add(newRule)
                }
            }
            rules = nextIterationRules
        }

        _newGrammar = Grammar(
                grammar.startSymbol,
                grammar.nonTerminals + _newSymbols,
                grammar.terminals,
                wellFormedRules + singleToSingleRules
                )
    }

    val newGrammar: Grammar<N, T>
        get() = _newGrammar ?: throw RuntimeException("Not yet converted.")
    val newSymbols: List<N>
        get() = _newSymbols


    protected abstract fun createNewSymbol(index: Int): N

    private fun isWellFormed(rule: Rule<N, T>): Boolean
    {
        if (rule.rhs.size == 1)
        {
            return (rule.rhs[0].terminal != null)
        }
        else if (rule.rhs.size == 2)
        {
            return ( (rule.rhs[0].symbol != null) && (rule.rhs[1].symbol != null) )
        }
        return false
    }

    private fun isSingleToSingle(rule: Rule<N, T>): Boolean
    {
        return (rule.rhs.size == 1) && (rule.rhs.first().symbol != null)
    }

    private fun convertRule(rule: Rule<N, T>): Pair<Rule<N, T>, Rule<N, T>>
    {
        if (rule.rhs.size == 1) { throw RuntimeException("Bug: unexpected single-symbol to single-symbol rule.") }
        val lastTerminal = rule.rhs.withIndex().lastOrNull { it.value.terminal!=null }
        if (lastTerminal != null)
        {
            val newSymbol = createNewSymbol(newSymbolIndex++)
            _newSymbols.add(newSymbol)

            val auxiliaryRule = Rule<N, T>(
                    "${rule.name}_auxiliary_terminal_${lastTerminal.index}",
                    newSymbol,
                    listOf(SyntacticItem.createTerminal(lastTerminal.value.terminal!!)),
                    true,
                    0.0
            )

            val newRhs = ArrayList<SyntacticItem<N, T>>(rule.rhs.size)
            newRhs.addAll(rule.rhs)
            newRhs[lastTerminal.index] = SyntacticItem.createSymbol(newSymbol)
            val newRule = Rule<N, T>(
                    rule.name,
                    rule.lhs,
                    newRhs,
                    false,
                    rule.logProbability
            )

            return Pair(newRule, auxiliaryRule)
        }
        else
        {
            if (rule.rhs.size == 2) { throw RuntimeException("Anomaly. No terminals and rhs.size = 2. Should be a well-formed rule.") }

            val newSymbol = createNewSymbol(newSymbolIndex++)
            _newSymbols.add(newSymbol)

            val last2 = rule.rhs.takeLast(2)
            val auxiliaryRule = Rule<N ,T>(
                    "${rule.name}_auxiliary_shrink_at_${rule.rhs.size}",
                    newSymbol,
                    last2,
                    true,
                    0.0
            )

            val newRhs = ArrayList<SyntacticItem<N, T>>(rule.rhs.size-1)
            newRhs.addAll(rule.rhs.take(rule.rhs.size-2))
            newRhs.add(SyntacticItem.createSymbol(newSymbol))
            val newRule = Rule<N, T>(
                    rule.name,
                    rule.lhs,
                    newRhs,
                    false,
                    rule.logProbability
            )

            return Pair(newRule, auxiliaryRule)
        }
    }



    private var _newGrammar: Grammar<N, T>? = null
    private val _newSymbols = mutableListOf<N>()
    private var newSymbolIndex = 1
}

class StringChomskyNormalFormConverter<T>(grammar: Grammar<String, T>) : ChomskyNormalFormConverter<String, T>(grammar)
{
    override fun createNewSymbol(index: Int): String = "AUX_$index"
}