package com.github.asher_stern.parser.cyk

import com.github.asher_stern.parser.grammar.ChomskyNormalFormGrammar
import com.github.asher_stern.parser.grammar.SyntacticItem
import com.github.asher_stern.parser.tree.TreeNode
import com.github.asher_stern.parser.utils.Array1

/**
 * Created by Asher Stern on November-05 2017.
 */


/**
 * Extends [CykAlgorithm] to generate parse-trees also for ungrammatical sentence. So the method [parse] always returns a tree.
 */
class CykAlgorithmWithHack<N, T>(grammar: ChomskyNormalFormGrammar<N, T>, sentence: Array1<T>) : CykAlgorithm<N, T>(grammar, sentence)
{
    override fun hackTree(): TreeNode<N, T>
    {
        val tree = generateTreeForRange(1, sentence.size)
        if (tree.content.symbol == grammar.startSymbol)
        {
            return tree
        }
        else
        {
            val newTree = TreeNode<N, T>(SyntacticItem.createSymbol(grammar.startSymbol))
            newTree.addChild(tree)
            return newTree
        }
    }


    private fun generateTreeForRange(start: Int, end: Int): TreeNode<N, T>
    {
        val longest = findLongest(start, end)
        if (longest == null)
        {
            val children = sentence.slice(start, end).map { TreeNode<N, T>(SyntacticItem.createTerminal(it)) }
            return TreeNode<N, T>(SyntacticItem.createSymbol(grammar.startSymbol), children.toMutableList())
        }
        else
        {
            val longestTree = buildTree(longest.start, longest.end, longest.symbol)
            var ret: TreeNode<N, T> = longestTree
            if (longest.start > start)
            {
                val toLeft = generateTreeForRange(start, longest.start-1)
                val newRet = TreeNode<N, T>(SyntacticItem.createSymbol(grammar.startSymbol))
                newRet.addChild(toLeft)
                newRet.addChild(ret)
                ret = newRet
            }
            if (longest.end < end)
            {
                val toRight = generateTreeForRange(longest.end+1, end)
                val newRet = TreeNode<N, T>(SyntacticItem.createSymbol(grammar.startSymbol))
                newRet.addChild(ret)
                newRet.addChild(toRight)
                ret = newRet
            }
            return ret
        }
    }

    private fun findLongest(start: Int, end: Int): TableIndex<N>?
    {
        val givenRangeLength = end-start+1
        for (length in givenRangeLength downTo 1)
        {
            for (candidateStart in start..(end-length+1))
            {
                val candidateEnd = candidateStart + length -1
                val symbolsMap = table[candidateStart, candidateEnd]
                if (symbolsMap != null)
                {
                    val candidate = symbolsMap.entries.filter { it.value != null }.sortedByDescending { it.value!!.logProbability }.firstOrNull()
                    if (candidate != null)
                    {
                        return TableIndex(candidateStart, candidateEnd, candidate.key)
                    }
                }
            }
        }

        return null
    }

}

private data class TableIndex<N>(val start: Int, val end: Int, val symbol: N)