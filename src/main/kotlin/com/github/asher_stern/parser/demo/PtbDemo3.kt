package com.github.asher_stern.parser.demo

import com.github.asher_stern.parser.cyk.CykAlgorithm
import com.github.asher_stern.parser.cyk.CykAlgorithmWithHack
import com.github.asher_stern.parser.grammar.*
import com.github.asher_stern.parser.grammar.acquisition.GrammarAcquisitionFromTrees
import com.github.asher_stern.parser.penn_treebank.PtbPosTags
import com.github.asher_stern.parser.penn_treebank.extractTrees
import com.github.asher_stern.parser.tree.*
import com.github.asher_stern.parser.utils.Array1
import java.io.File
import java.io.FileFilter
import kotlin.system.exitProcess

/**
 * Created by Asher Stern on November-03 2017.
 */

fun main(args: Array<String>)
{
    val filter = object : FileFilter
    {
        override fun accept(pathname: File?): Boolean
        {
            return pathname?.extension=="mrg"
        }
    }

    val trees = mutableListOf<TreeNode<String, PosAndWord>>()

    for (file in File("/home/asher/main/data/resources/datasets/penn_treebank/10_percent/treebank/combined/").listFiles(filter))
    {
        trees.addAll(extractTrees(file.readText()))
    }

    println(trees.size)
    println (treeYield(trees.first()).joinToString(" ") { it.word+"/"+it.pos })

    val acquisition = GrammarAcquisitionFromTrees(trees.map { removeWordsFromTree(it) })
    acquisition.absoluteEncounteredLimitation = 3
    val rules = acquisition.acquire()
    println(rules.size)


    for (rule in rules)
    {
        println(rule.friendlyString)
    }




    val nonTerminals = extractAllNonTerminals(rules)
    println(nonTerminals)
    val originalGrammar = Grammar("S", nonTerminals, PtbPosTags.tags, rules.toSet())

    println()
//    println("Remove single-to-single")
//    val s2sConverter = ChomskySingleToSingleConverter(originalGrammar)
//    s2sConverter.convert()
//    val noS2sGrammar = s2sConverter.newGrammar
//
//    println(noS2sGrammar.rules.size)
//    for (rule in noS2sGrammar.rules)
//    {
//        println(rule.friendlyString)
//    }
//    println()
//    for ( (nr, c) in s2sConverter.collapsedMap )
//    {
//        println (nr.friendlyString + " : " + c)
//    }


    println("Convert to Chomsky normal form")
    val converter = StringChomskyNormalFormConverter(originalGrammar)
    converter.convert()
    val newGrammar = converter.newGrammar

    println(newGrammar.rules.size)
//    for (rule in newGrammar.rules)
//    {
//        println(rule.friendlyString)
//    }

    println("Convert to Chomsky Normal Form Grammar Object...")
    val chomskyNormalFormGrammar = ChomskyNormalFormGrammarConstructor(newGrammar).construct()
    println("Convert to Chomsky Normal Form Grammar Object - done")
//    println("Terminal rules")
//    println (chomskyNormalFormGrammar.terminalRules.firstIndexes)
//    for (terminal in chomskyNormalFormGrammar.terminalRules.firstIndexes)
//    {
//        for ( (symbol, logProbability ) in chomskyNormalFormGrammar.terminalRules[terminal]!!)
//        {
//            println ("$symbol -> #$terminal ($logProbability)")
//        }
//    }
//    println("Non terminal rules")
//    for (first in chomskyNormalFormGrammar.nonTerminalRules.firstIndexes)
//    {
//        for ( (second, lhsMap) in chomskyNormalFormGrammar.nonTerminalRules[first]!!)
//        {
//            for ( (lhs, logProbability) in lhsMap )
//            {
//                println("$lhs -> $first $second ($logProbability)")
//            }
//        }
//    }


    for (treeToTest in trees.take(10))
    {
        println("About to test the following tree:")
        println(TreePresent(treeToTest).present())

        val sentenceToParse = Array1(treeYield(removeWordsFromTree(treeToTest)).toTypedArray())
        val sentenceWords = Array1(treeYield(removePosFromTree(treeToTest)).toTypedArray())

        println("About to parse the following sentence")
        println(sentenceToParse)

        val cykAlgorithm = CykAlgorithmWithHack<String, String>(chomskyNormalFormGrammar, sentenceToParse)
        val resultTree = cykAlgorithm.parse()
        if (resultTree == null)
        {
            println("No result")
        }
        else
        {
            println("Parsed. Grammatical = ${cykAlgorithm.wellParsed}. Probability = ${cykAlgorithm.parseProbability}")
            val resultTreeSimple = convertCykToSimpleTree(resultTree)
//            println("Result tree as is:")
//            println(TreePresent(mergeWordsToTree(sentenceWords, resultTreeSimple)).present())
            val resultTreeSimpleConverted = TreeBackwardConverter(originalGrammar, newGrammar).convertTree(resultTreeSimple)
            println("Result tree:")
            println(TreePresent(mergeWordsToTree(sentenceWords, resultTreeSimpleConverted)).present())
        }
    }

}