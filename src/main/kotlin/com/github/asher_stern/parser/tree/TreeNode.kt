package com.github.asher_stern.parser.tree

import com.github.asher_stern.parser.grammar.SyntacticItem

/**
 * Created by Asher Stern on November-02 2017.
 */
class TreeNode<N, T>(val content: SyntacticItem<N, T>)
{
    constructor(content: SyntacticItem<N, T>, givenChildren: List<TreeNode<N, T>>) : this(content)
    {
        _children.addAll(givenChildren)
    }

    val children: List<TreeNode<N, T>>
        get() = _children

    fun addChild(child: TreeNode<N, T>)
    {
        _children.add(child)
    }


    private val _children = mutableListOf<TreeNode<N, T>>()
}