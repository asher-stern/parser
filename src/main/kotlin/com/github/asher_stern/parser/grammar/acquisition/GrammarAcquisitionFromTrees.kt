package com.github.asher_stern.parser.grammar.acquisition


import com.github.asher_stern.parser.grammar.NakedRule
import com.github.asher_stern.parser.grammar.Rule
import com.github.asher_stern.parser.grammar.SyntacticItem
import com.github.asher_stern.parser.tree.TreeNode


/**
 * Created by Asher Stern on November-03 2017.
 */
class GrammarAcquisitionFromTrees<N, T>(private val trees: List<TreeNode<N, T>>)
{
    /**
     * This property can be set **before** calling [acquire], in order to remove all the rules that do not appear more
     * then the specified limitation.
     */
    var absoluteEncounteredLimitation: Int = 0

    /**
     * Acquire, i.e., extract, all the rules from the corpus (given as the [trees] constructor parameter).
     */
    fun acquire(): Set<Rule<N, T>>
    {
        extractNakedRules()
        return buildRulesFromNakedRules(buildLhsCount())
    }


    private fun extractNakedRules()
    {
        for (tree in trees)
        {
            extractRulesFromTree(tree)
        }
    }

    private fun buildLhsCount(): Map<N, Long>
    {
        val lhsCount = mutableMapOf<N, Long>()

        for ( (rule, count) in nakedRules )
        {
            lhsCount._add(rule.lhs, count)
        }

        return lhsCount
    }

    private fun buildRulesFromNakedRules(lhsCount: Map<N, Long>): Set<Rule<N, T>>
    {
        val ret = mutableSetOf<Rule<N, T>>()

        val nameCounter = mutableMapOf<N, Long>() // This is just a map from each symbol to 1,2,3,... It is used for naming rules, for example S_1, VP_3, etc. The name is not used for computations, but for presentation and debugging only.
        for (nakedRule in nakedRules.keys.sortedByDescending { nakedRules.getValue(it) }) // Sorting is not really necessary here. The effect is just adding the more common rules first. But anyhow, all the (relevant) rules will be added.
        {
            val encountered: Long = nakedRules.getValue(nakedRule)
            if (encountered >= absoluteEncounteredLimitation)
            {
                nameCounter._inc(nakedRule.lhs)
                val name = "${nakedRule.lhs}_${nameCounter.getValue(nakedRule.lhs)}"
                val probability = nakedRules.getValue(nakedRule).toDouble() / lhsCount.getValue(nakedRule.lhs).toDouble()
                ret.add(Rule<N, T>(name, nakedRule.lhs, nakedRule.rhs, false, Math.log(probability)))
            }
        }

        return ret
    }

    private fun extractRulesFromTree(tree: TreeNode<N, T>)
    {
        if (tree.content.symbol != null) // = if the given (sub-)tree is not a terminal node
        {
            nakedRules._inc(extractRuleFromNode(tree))
            for (child in tree.children)
            {
                extractRulesFromTree(child)
            }
        }
    }

    private fun extractRuleFromNode(node: TreeNode<N, T>): NakedRule<N, T>
    {
        if (node.content.symbol == null) throw RuntimeException("Cannot extract rule from a terminal node")
        else
        {
            val lhs: N = node.content.symbol
            val rhs: List<SyntacticItem<N, T>> = node.children.map { it.content }
            return NakedRule(lhs, rhs)
        }
    }


    private val nakedRules = mutableMapOf<NakedRule<N, T>, Long>()
}


private fun <K> MutableMap<K, Long>._inc(key: K)
{
    put(key, getOrDefault(key, 0L) + 1L)
}

private fun <K> MutableMap<K, Long>._add(key: K, l: Long)
{
    put(key, getOrDefault(key, 0L) + l)
}