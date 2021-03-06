package com.github.asher_stern.parser.cyk

import com.github.asher_stern.parser.grammar.ChomskyNormalFormGrammar
import com.github.asher_stern.parser.grammar.SyntacticItem
import com.github.asher_stern.parser.tree.TreeNode
import com.github.asher_stern.parser.utils.Array1
import com.github.asher_stern.parser.utils.Table3D

/**
 * Created by Asher Stern on November-02 2017.
 */

/**
 * Implementation of CYK algorithm to parse a given sentence. It generates the most-likely parse tree for the given sentence.
 *
 * See http://www.cs.columbia.edu/~mcollins/courses/nlp2011/notes/pcfgs.pdf
 *
 * A subclass of this class, [CykAlgorithmWithHack], generates parse-trees also for non-grammatical sentence (sentences that
 * cannot be parsed by the given grammar). (This class returns null for such sentences).
 *
 * The algorithm here is a modified version of CYK algorithm. The original CYK algorithm accepts syntactic-rules of
 * Chomsky normal form: rules that are either symbol to terminal, or symbol to two symbols.
 * This modified version also accepts rules that are single-symbol to single-symbol.
 *
 *
 * @param grammar the grammar for parsing the given sentence.
 * @param sentence the sentence to parse (usually it is an array of part-of-speech tags).
 */
open class CykAlgorithm<N, T>(
        protected val grammar: ChomskyNormalFormGrammar<N, T>,
        protected val sentence: Array1<T>
)
{
    /**
     * Parse the sentence given in the constructor.
     * @return the parse-tree representing the syntactic analysis of the given sentence. Null is returned if no such
     * a tree can be constructed, according to the grammar provided in the constructor.
     * Note that [CykAlgorithmWithHack] subclass **always** returns a tree, and never returns null.
     */
    fun parse(): TreeNode<N,T>?
    {
        fillTerminalRules()
        fillNonTerminalRules()
        if (table[1, sentence.size, grammar.startSymbol] != null)
        {
            _parseProbability = Math.exp(table[1, sentence.size, grammar.startSymbol]!!.logProbability)
            _wellParsed = true
            return buildTree(1, sentence.size, grammar.startSymbol)
        }
        else
        {
            return hackTree()
        }
    }

    val wellParsed: Boolean get() = _wellParsed
    val parseProbability: Double get() = _parseProbability


    open protected fun hackTree(): TreeNode<N,T>?
    {
        // Can be implemented by sub-class
        return null
    }


    private fun fillTerminalRules()
    {
        val sentenceSize = sentence.size
        for (index in 1..sentenceSize)
        {
            val terminal = sentence[index]
            val rules = grammar.terminalRules[terminal]
            if ( (rules != null) && (rules.isNotEmpty()) )
            {
                for ((nonTerminal, logProbability) in rules.orEmpty())
                {
                    table[index, index, nonTerminal] = CykTableItem<N>(nonTerminal, null, null, null, null, logProbability!!)
                }

                addSingleToSingleRulesToTable(index, index)
            }
        }
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
                        val rhsList = listOf(s2sRule.rhs) + correspondingRhsInTable.rhsSingleSymbol.orEmpty()
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

    protected fun buildTree(start: Int, end: Int, symbol: N): TreeNode<N, T>
    {
        val item = table[start, end, symbol] ?: throw RuntimeException("Tree cannot be built. Null for $start, $end, $symbol.")
        val (root, lowestNode) = convertItemToTreeNode(item)
        if (item.rhsFirst == null)
        {
            if (start == end)
            {
                val terminal = sentence[start]
                val terminalNode = TreeNode<N, T>(SyntacticItem.createTerminal(terminal))

                lowestNode.addChild(terminalNode)
                return root
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

            lowestNode.addChild(rhsFirstChild)
            lowestNode.addChild(rhsSecondChild)

            return root
        }
    }

    private fun convertItemToTreeNode(item: CykTableItem<N>): Pair<TreeNode<N, T>, TreeNode<N, T>>
    {
        val root = TreeNode<N, T>(SyntacticItem.createSymbol(item.lhs))
        var node = root
        if (item.rhsSingleSymbol != null)
        {
            for (collapsedSymbol in item.rhsSingleSymbol)
            {
                val child = TreeNode<N, T>(SyntacticItem.createSymbol(collapsedSymbol))
                node.addChild(child)
                node = child
            }
        }
        return Pair(root, node)
    }



    protected val table = Table3D<Int, Int, N, CykTableItem<N>>()

    private var _wellParsed: Boolean = false
    private var _parseProbability: Double = 0.0
}
