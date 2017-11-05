package com.github.asher_stern.parser.grammar

import com.github.asher_stern.parser.tree.TreeNode
import com.github.asher_stern.parser.tree.extractNakedRuleFromNode

/**
 * Created by Asher Stern on November-02 2017.
 */
class TreeBackwardConverter<N, T>(
        private val auxiliarySymbols: Set<N>
)
{
    constructor(originalGrammer: Grammar<N, T>, normalizedGrammar: Grammar<N, T>) : this (normalizedGrammar.nonTerminals - originalGrammer.nonTerminals)

    fun convertTree(root: TreeNode<N, T>): TreeNode<N, T>
    {
        val convertedChildren = convertChildren(root.children)
        val newChildren = mutableListOf<TreeNode<N, T>>()
        for (child in convertedChildren)
        {
            newChildren.add(convertTree(child))
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
}