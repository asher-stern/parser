package com.github.asher_stern.parser.tree

import com.github.asher_stern.parser.grammar.SyntacticItem

/**
 * Created by Asher Stern on November-02 2017.
 */
class TreeNode<N, T>(val content: SyntacticItem<N, T>, val children: MutableList<TreeNode<N, T>>)
{
    constructor(content: SyntacticItem<N, T>) : this(content, mutableListOf())

    fun addChild(child: TreeNode<N, T>)
    {
        children.add(child)
    }
}