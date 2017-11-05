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
                table[index, index, nonTerminal] = CykTableItem<N>(nonTerminal, null, null, null, null, logProbability!!)
            }

            addSingleToSingleRulesToTable(index, index)
        }
        return true
    }

    private fun find(firstStart: Int, firstEnd: Int, secondStart: Int, secondEnd: Int): Map<N, CykTableItem<N>>
    {
        val ret = mutableMapOf<N, CykTableItem<N>>()

        val firstCandidates: Map<N, CykTableItem<N>?> = table[firstStart, firstEnd].orEmpty()
        val firstCandidatesKeys = firstCandidates.keys
        val secondCandidates: Map<N, CykTableItem<N>?> = table[secondStart, secondEnd].orEmpty()
        val secondCandidatesKeys = secondCandidates.keys

        for (rawRule in grammar.listNonTerminalRules)
        {
            if ( (rawRule.rhsFirst in firstCandidatesKeys) && (rawRule.rhsSecond in secondCandidatesKeys) )
            {
                val logProbability = firstCandidates.getValue(rawRule.rhsFirst)!!.logProbability + secondCandidates.getValue(rawRule.rhsSecond)!!.logProbability + rawRule.logProbability
                val add = when
                {
                    rawRule.lhs in ret.keys -> logProbability > ret.getValue(rawRule.lhs).logProbability
                    else -> true
                }

                if (add)
                {
                    ret[rawRule.lhs] = CykTableItem<N>(rawRule.lhs, null, rawRule.rhsFirst, rawRule.rhsSecond, secondStart, logProbability)
                }
            }
        }

        return ret
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

                addSingleToSingleRulesToTable(start, end)
            }
        }
    }

    private fun addSingleToSingleRulesToTable(start: Int, end: Int)
    {
        val candidatesRhs1 = mutableMapOf<N, CykTableItem<N>>()
        for (s2sRule in grammar.listSingleToSingleRules)
        {
            val correspondingRhsInTable = table[start, end, s2sRule.rhs]
            if (correspondingRhsInTable != null)
            {
                val newItemLogProbability = s2sRule.logProbability + correspondingRhsInTable.logProbability
                val existingItem = table[start, end, s2sRule.lhs]
                val betterThanTable = (existingItem == null) || (newItemLogProbability > existingItem.logProbability)
                if (betterThanTable)
                {
                    val existingCandidate = candidatesRhs1[s2sRule.lhs]
                    val bestSoFar = (existingCandidate == null) || (newItemLogProbability > existingCandidate.logProbability)
                    if (bestSoFar)
                    {
                        val rhsList = correspondingRhsInTable.rhsSingleSymbol.orEmpty() + s2sRule.rhs
                        candidatesRhs1[s2sRule.lhs] = CykTableItem(s2sRule.lhs, rhsList, correspondingRhsInTable.rhsFirst, correspondingRhsInTable.rhsSecond, correspondingRhsInTable.secondBeginIndex, newItemLogProbability)
                    }
                }
            }
        }

        for ( (symbol, item) in candidatesRhs1)
        {
            table[start, end, symbol] = item
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
                val terminalNode = CykTreeDerivationNode<N, T>(terminal)

                val node = CykTreeDerivationNode<N, T>(item)
                node.singleChild = terminalNode
                return node
            }
            else
            {
                throw RuntimeException("Bug: start != end, but rhs is null. start = $start, end = $end, symbol = $symbol")
            }
        }
        else
        {
            val rhsFirstChild = buildTree(start, item.secondBeginIndex!!-1, item.rhsFirst)
            val rhsSecondChild = buildTree(item.secondBeginIndex, end, item.rhsSecond!!)

            val node = CykTreeDerivationNode<N, T>(item)
            node.firstChild = rhsFirstChild
            node.secondChild = rhsSecondChild
            return node
        }
    }

    private val table = Table3D<Int, Int, N, CykTableItem<N>>()
}
