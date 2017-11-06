package com.github.asher_stern.parser.tree

import com.github.asher_stern.parser.cyk.CykTreeDerivationNode
import com.github.asher_stern.parser.grammar.NakedRule
import com.github.asher_stern.parser.grammar.SyntacticItem
import com.github.asher_stern.parser.utils.Array1
import java.util.*

/**
 * Created by Asher Stern on November-03 2017.
 */

fun <N, T> extractNakedRuleFromNode(content: N, children: List<TreeNode<N, T>>): NakedRule<N, T> =
        NakedRule(content, children.map { it.content })

fun <N, T> extractNakedRuleFromNode(node: TreeNode<N, T>): NakedRule<N, T> =
        extractNakedRuleFromNode(node.content.symbol!!, node.children)


fun <N, T> convertCykToSimpleTree(cykTree: CykTreeDerivationNode<N, T>): TreeNode<N, T>
{
    if (cykTree.terminal != null)
    {
        return TreeNode(SyntacticItem.createTerminal(cykTree.terminal))
    }
    else
    {
        cykTree.item!! // Merely check not null

        val rootNode = TreeNode<N, T>(SyntacticItem.createSymbol(cykTree.item.lhs))
        var node: TreeNode<N, T> = rootNode
        for (singleSymbol in cykTree.item.rhsSingleSymbol.orEmpty())
        {
            val child = TreeNode(SyntacticItem.createSymbol<N,T>(singleSymbol))
            node.addChild(child)
            node = child
        }

        if (cykTree.singleChild != null)
        {
            node.addChild(convertCykToSimpleTree(cykTree.singleChild!!))
            return rootNode
        }
        else if ( (cykTree.firstChild != null) && (cykTree.secondChild != null) )
        {
            node.addChild(convertCykToSimpleTree(cykTree.firstChild!!))
            node.addChild(convertCykToSimpleTree(cykTree.secondChild!!))
            return rootNode
        }
        else
        {
            throw RuntimeException("Non terminal node has no children.")
        }
    }
}

fun <N> mergeWordsToTree(sentence: Array1<String>, tree: TreeNode<N, String>): TreeNode<N, PosAndWord>
{
    return WordsToTreeMerger(sentence, tree).merge()
}

fun <N> removeWordsFromTree(tree: TreeNode<N, PosAndWord>): TreeNode<N, String> =
        removeWordsOrPosFromTree(tree, true)

fun <N> removePosFromTree(tree: TreeNode<N, PosAndWord>): TreeNode<N, String> =
        removeWordsOrPosFromTree(tree, false)

fun <T> treeYield(tree: TreeNode<*, T>): List<T>
{
    val ret = mutableListOf<T>()
    val stack = Stack<TreeNode<*, T>>()
    stack.push(tree)
    while (!stack.empty())
    {
        val node = stack.pop()
        if (node.content.terminal != null)
        {
            ret.add(node.content.terminal)
        }
        else
        {
            for (child in node.children.asReversed())
            {
                stack.push(child)
            }
        }
    }
    return ret
}


private fun <N> removeWordsOrPosFromTree(tree: TreeNode<N, PosAndWord>, wordOrPos: Boolean): TreeNode<N, String>
{
    val content: SyntacticItem<N, String> = when
    {
        tree.content.symbol != null -> SyntacticItem.createSymbol(tree.content.symbol)
        else -> SyntacticItem.createTerminal( if (wordOrPos) tree.content.terminal!!.pos else tree.content.terminal!!.word)
    }

    return TreeNode(content, tree.children.map { removeWordsOrPosFromTree(it, wordOrPos) }.toMutableList())
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

