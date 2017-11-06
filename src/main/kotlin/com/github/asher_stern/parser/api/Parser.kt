package com.github.asher_stern.parser.api

import com.github.asher_stern.parser.cyk.CykAlgorithmWithHack
import com.github.asher_stern.parser.grammar.ChomskyNormalFormGrammar
import com.github.asher_stern.parser.tree.PosAndWord
import com.github.asher_stern.parser.tree.TreeNode
import com.github.asher_stern.parser.tree.convertCykToSimpleTree
import com.github.asher_stern.parser.tree.mergeWordsToTree
import com.github.asher_stern.parser.utils.Array1

/**
 * Created by Asher Stern on November-06 2017.
 */

class Parser(private val chomskyNormalFormGrammar: ChomskyNormalFormGrammar<String, String>)
{
    fun parse(sentence: Array<PosAndWord>): ParseResult
    {
        val sentence_asArray1 = Array1(sentence.map { it.pos }.toTypedArray())
        val cykAlgorithm = CykAlgorithmWithHack(chomskyNormalFormGrammar, sentence_asArray1)
        val treePosOnly = convertCykToSimpleTree(cykAlgorithm.parse()!!)

        val sentence_wordsOnly = Array1(sentence.map { it.word }.toTypedArray())
        val tree = mergeWordsToTree(sentence_wordsOnly, treePosOnly)

        return ParseResult(tree, cykAlgorithm.wellParsed, cykAlgorithm.parseProbability)
    }
}

data class ParseResult(val tree: TreeNode<String, PosAndWord>, val grammatical: Boolean, val probability: Double)