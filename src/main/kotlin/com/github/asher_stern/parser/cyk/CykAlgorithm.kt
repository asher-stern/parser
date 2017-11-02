package com.github.asher_stern.parser.cyk

import com.github.asher_stern.parser.grammar.ChomskyNormalFormGrammar
import com.github.asher_stern.parser.utils.Array1
import com.github.asher_stern.parser.utils.Table3D

/**
 * Created by Asher Stern on November-02 2017.
 */

/**
 * See http://www.cs.columbia.edu/~mcollins/courses/nlp2011/notes/pcfgs.pdf
 */
class CykAlgorithm<N, T>(
        private val grammar: ChomskyNormalFormGrammar<N, T>,
        private val sentence: Array1<T>
)
{
    fun parse(): CykTreeDerivationNode<N, T>?
    {
        if (!fillTerminalRules())
        {
            return null
        }
        fillNonTerminalRules()
        if (table[1, sentence.size, grammar.startSymbol] != null)
        {
            return buildTree(1, sentence.size, grammar.startSymbol)
        }
        else
        {
            return null
        }
    }


    private fun fillTerminalRules(): Boolean
    {
        val sentenceSize = sentence.size
        for (index in 1..sentenceSize)
        {
            val terminal = sentence[index]
            val rules = grammar.terminalRules[terminal]
            if ( (rules == null) || (rules.isEmpty()) )
            {
                return false
            }
            for ( (nonTerminal, logProbability) in rules.orEmpty() )
            {
                table[index, index, nonTerminal] = CykTableItem<N>(nonTerminal, null, null, null, logProbability!!)
            }
        }
        return true
    }

    private fun find(firstStart: Int, firstEnd: Int, secondStart: Int, secondEnd: Int): Map<N, CykTableItem<N>>
    {
        val firstCandidates = table[firstStart, firstEnd]
        val secondCandidates = table[secondStart, secondEnd]

        val lhsCandidates = mutableMapOf<N, CykTableItem<N>>()
        for ( (rhsFirst, rhsFirstItem) in firstCandidates.orEmpty() )
        {
            for ( (rhsSecond, rhsSecondItem) in secondCandidates.orEmpty() )
            {
                val lhss = grammar.nonTerminalRules[rhsFirst, rhsSecond]
                for ( (lhs, lhsLogProbability) in lhss.orEmpty())
                {
                    if ( (lhsLogProbability != null) && (rhsFirstItem != null) && (rhsSecondItem != null) )
                    {
                        val candidateLogProbability = rhsFirstItem.logProbability + rhsSecondItem.logProbability + lhsLogProbability
                        val soFarItem = lhsCandidates[lhs]
                        if ( (soFarItem == null) || (soFarItem.logProbability < candidateLogProbability) )
                        {
                            lhsCandidates[lhs] = CykTableItem<N>(lhs, rhsFirst, rhsSecond, secondStart, candidateLogProbability)
                        }
                    }
                }
            }
        }

        return lhsCandidates
    }

    private fun fillNonTerminalRules()
    {
        val sentenceSize = sentence.size
        for (length_minus_1 in 1..(sentenceSize-1))
        {
            for (start in 1..(sentenceSize-length_minus_1))
            {
                val end = (start + length_minus_1)
                val candidates = mutableMapOf<N, CykTableItem<N>>()
                for (rhsFirstEnds in start..(end-1))
                {
                    val candidatesForBreakpoint = find(start, rhsFirstEnds, rhsFirstEnds+1, end)
                    for ( (lhs, item) in  candidatesForBreakpoint)
                    {
                        if (!candidates.containsKey(lhs))
                        {
                            candidates[lhs] = item
                        }
                        else
                        {
                            if (candidates.getValue(lhs).logProbability < item.logProbability)
                            {
                                candidates[lhs] = item
                            }
                        }
                    }
                }

                for ( (lhs, item) in candidates )
                {
                    table[start, end, lhs] = item
                }
            }
        }
    }

    private fun buildTree(start: Int, end: Int, symbol: N): CykTreeDerivationNode<N, T>
    {
        val item = table[start, end, symbol] ?: throw RuntimeException("Tree cannot be built. Null for $start, $end, $symbol.")
        if (item.rhsFirst == null)
        {
            if (start == end)
            {
                val terminal = sentence[start]
                return CykTreeDerivationNode<N, T>(terminal)
            }
            else
            {
                throw RuntimeException("Bug: start != end, but rhs is null. start = $start, end = $end, symbol = $symbol")
            }
        }
        else
        {
            val rhsFirstChild = buildTree(start, item.secondBeginIndex!!-1, item.rhsFirst)
            val rhsSecondChild = buildTree(item.secondBeginIndex!!, end, item.rhsSecond!!)

            val node = CykTreeDerivationNode<N, T>(item)
            node.firstChild = rhsFirstChild
            node.secondChild = rhsSecondChild
            return node
        }
    }

    private val table = Table3D<Int, Int, N, CykTableItem<N>>()
}
