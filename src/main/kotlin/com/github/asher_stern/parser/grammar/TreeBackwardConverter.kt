package com.github.asher_stern.parser.grammar

import com.github.asher_stern.parser.tree.TreeNode
import com.github.asher_stern.parser.tree.extractNakedRuleFromNode

/**
 * Created by Asher Stern on November-02 2017.
 */
class TreeBackwardConverter<N, T>(
        private val auxiliarySymbols: Set<N>,
        private val collapsedMap: Map<NakedRule<N, T>, List<N>>
)
{
    constructor(originalGrammer: Grammar<N, T>, normalizedGrammar: Grammar<N, T>, collapsedMap: Map<NakedRule<N, T>, List<N>>) : this (normalizedGrammar.nonTerminals - originalGrammer.nonTerminals, collapsedMap)

    fun convertTree(root: TreeNode<N, T>): TreeNode<N, T>
    {
        val convertedChildren = convertChildren(root.children)
        var newChildren = mutableListOf<TreeNode<N, T>>()
        for (child in convertedChildren)
        {
            newChildren.add(convertTree(child))
        }
        if (root.content.symbol != null)
        {
            newChildren = expandCollapsedNodes(root.content.symbol, newChildren)
        }
        return TreeNode(root.content, newChildren)
    }

    private fun convertChildren(children: List<TreeNode<N, T>>): List<TreeNode<N, T>>
    {
        var _children = mutableListOf<TreeNode<N, T>>()
        _children.addAll(children)
        var nextChildren = mutableListOf<TreeNode<N, T>>()

        do
        {
            var converted = false
            for (child in _children)
            {
                if (converted)
                {
                    nextChildren.add(child)
                }
                else
                {
                    if ((child.content.symbol != null) && (child.content.symbol in auxiliarySymbols))
                    {
                        nextChildren.addAll(child.children)
                        converted = true
                    }
                    else
                    {
                        nextChildren.add(child)
                    }
                }
            }
            _children = nextChildren
            nextChildren = mutableListOf()
        } while (converted)

        return _children
    }

    private fun expandCollapsedNodes(content: N, children: MutableList<TreeNode<N, T>>): MutableList<TreeNode<N, T>>
    {
        val nakedRule = extractNakedRuleFromNode(content, children)
        if (nakedRule in collapsedMap)
        {
            val collapsedSymbols = collapsedMap.getValue(nakedRule)
            var _children = children
            for (collapsedSymbol in collapsedSymbols.asReversed())
            {
                val collapsedNode = TreeNode(SyntacticItem.createSymbol(collapsedSymbol), _children)
                _children = mutableListOf(collapsedNode)
            }
            return _children
        }
        else
        {
            return children
        }
    }
}