package com.github.asher_stern.parser.api

import com.github.asher_stern.parser.cyk.CykAlgorithmWithHack
import com.github.asher_stern.parser.grammar.ChomskyNormalFormGrammar
import com.github.asher_stern.parser.grammar.TreeBackwardConverter
import com.github.asher_stern.parser.tree.*
import com.github.asher_stern.parser.utils.Array1

/**
 * Created by Asher Stern on November-06 2017.
 */

/**
 * Programmatical API class to parse sentences. See ParseFiles.kt
 */
class Parser(private val chomskyNormalFormGrammar: ChomskyNormalFormGrammar<String, String>, private val auxiliarySymbols: Set<String>)
{
    fun parse(sentence: Array<PosAndWord>): ParseResult
    {
        val sentence_asArray1 = Array1(sentence.map { it.pos }.toTypedArray())
        val cykAlgorithm = CykAlgorithmWithHack(chomskyNormalFormGrammar, sentence_asArray1)
        val treePosOnly = cykAlgorithm.parse()!!

        val treeBackwardConverted = TreeBackwardConverter<String, String>(auxiliarySymbols).convertTree(treePosOnly)

        val sentence_wordsOnly = Array1(sentence.map { it.word }.toTypedArray())
        val tree = mergeWordsToTree(sentence_wordsOnly, treeBackwardConverted)

        return ParseResult(tree, cykAlgorithm.wellParsed, cykAlgorithm.parseProbability)
    }
}

data class ParseResult(val tree: TreeNode<String, PosAndWord>, val grammatical: Boolean, val probability: Double)