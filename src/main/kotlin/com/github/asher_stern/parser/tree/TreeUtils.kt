package com.github.asher_stern.parser.tree

import com.github.asher_stern.parser.cyk.CykTreeDerivationNode
import com.github.asher_stern.parser.grammar.SyntacticItem
import com.github.asher_stern.parser.utils.Array1

/**
 * Created by Asher Stern on November-03 2017.
 */




fun <N,T> convertCykToSimpleTree(node: CykTreeDerivationNode<N, T>): TreeNode<N, T>
{
    val children = mutableListOf<TreeNode<N, T>>()

    node.firstChild ?.let {
        children.add(convertCykToSimpleTree(it))
    }
    node.secondChild ?.let {
        children.add(convertCykToSimpleTree(it))
    }

    val item: SyntacticItem<N, T> = when
    {
        node.terminal != null -> SyntacticItem.createTerminal<N,T>(node.terminal!!)
        else -> SyntacticItem.createSymbol<N, T>(node.item!!.lhs)
    }

    return TreeNode(item, children)
}

fun <N> mergeWordsToTree(sentence: Array1<String>, tree: TreeNode<N, String>): TreeNode<N, PosAndWord>
{
    return WordsToTreeMerger(sentence, tree).merge()
}



private class WordsToTreeMerger<N>(private val sentence: Array1<String>, private val tree: TreeNode<N, String>)
{
    fun merge(): TreeNode<N, PosAndWord> = merge(tree)


    private fun merge(node: TreeNode<N, String>): TreeNode<N, PosAndWord>
    {
        if (node.content.terminal != null)
        {
            return TreeNode(SyntacticItem.createTerminal(PosAndWord(node.content.terminal, sentence[sentenceIndex++])))
        }
        else
        {
            return TreeNode(
                    SyntacticItem.createSymbol<N, PosAndWord>(node.content.symbol!!),
                    node.children.map { merge(it) }.toMutableList()
            )
        }
    }

    private var sentenceIndex: Int = 1
}

