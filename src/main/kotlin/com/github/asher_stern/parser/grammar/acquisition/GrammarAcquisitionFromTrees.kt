package com.github.asher_stern.parser.grammar.acquisition


import com.github.asher_stern.parser.grammar.NakedRule
import com.github.asher_stern.parser.grammar.Rule
import com.github.asher_stern.parser.tree.TreeNode


/**
 * Created by Asher Stern on November-03 2017.
 */
class GrammarAcquisitionFromTrees<N, T>(private val trees: List<TreeNode<N, T>>)
{
    var absoluteEncounteredLimitation: Int = 0

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

        val nameCounter = mutableMapOf<N, Long>()
        for (nakedRule in nakedRules.keys.sortedByDescending { nakedRules.getValue(it) })
        {
            val encountered = nakedRules.getValue(nakedRule)
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
        if (tree.content.symbol != null)
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
            val rhs = node.children.map { it.content }
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