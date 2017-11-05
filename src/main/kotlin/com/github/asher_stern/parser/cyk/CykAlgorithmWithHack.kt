package com.github.asher_stern.parser.cyk

import com.github.asher_stern.parser.grammar.ChomskyNormalFormGrammar
import com.github.asher_stern.parser.utils.Array1

/**
 * Created by Asher Stern on November-05 2017.
 */
class CykAlgorithmWithHack<N, T>(grammar: ChomskyNormalFormGrammar<N, T>, sentence: Array1<T>) : CykAlgorithm<N, T>(grammar, sentence)
{
    override fun hackTree(): CykTreeDerivationNode<N, T>
    {
        val tree = generateTreeForRange(1, sentence.size)
        if (tree.item!!.lhs == grammar.startSymbol)
        {
            return tree
        }
        else
        {
            val newTree = CykTreeDerivationNode<N, T>(CykTableItem<N>(grammar.startSymbol, null, null, null, null, 0.0))
            newTree.singleChild = tree
            return newTree
        }
    }


    private fun generateTreeForRange(start: Int, end: Int): CykTreeDerivationNode<N, T>
    {
        val longest = findLongest(start, end)
        if (longest == null)
        {
            var lastRoot: CykTreeDerivationNode<N, T>? = null
            for (_end in end downTo start)
            {
                val terminalNode = CykTreeDerivationNode<N, T>(sentence[_end])
                val terminalParent = CykTreeDerivationNode<N, T>(CykTableItem<N>(grammar.startSymbol, null, null, null, null, 0.0))
                terminalParent.singleChild = terminalNode

                if (lastRoot != null)
                {
                    val newRoot = CykTreeDerivationNode<N, T>(CykTableItem<N>(grammar.startSymbol, null, null, null, null, 0.0))
                    newRoot.firstChild = terminalParent
                    newRoot.secondChild = lastRoot
                    lastRoot = newRoot
                }
                else
                {
                    lastRoot = terminalParent
                }
            }
            return lastRoot!!
        }
        else
        {
            val longestTree = buildTree(longest.start, longest.end, longest.symbol)
            var ret: CykTreeDerivationNode<N, T> = longestTree
            if (longest.start > start)
            {
                val toLeft = generateTreeForRange(start, longest.start-1)
                val newRet = CykTreeDerivationNode<N, T>(CykTableItem<N>(grammar.startSymbol, null, null, null, null, 0.0))
                newRet.firstChild = toLeft
                newRet.secondChild = ret
                ret = newRet
            }
            if (longest.end < end)
            {
                val toRight = generateTreeForRange(longest.end+1, end)
                val newRet = CykTreeDerivationNode<N, T>(CykTableItem<N>(grammar.startSymbol, null, null, null, null, 0.0))
                newRet.firstChild = ret
                newRet.secondChild = toRight
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