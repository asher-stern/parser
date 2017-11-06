package com.github.asher_stern.parser.api

import com.github.asher_stern.parser.RULE_ACQUISITION_ABSOLUTE_ENCOUNTER_LIMITATION
import com.github.asher_stern.parser.grammar.*
import com.github.asher_stern.parser.grammar.acquisition.GrammarAcquisitionFromTrees
import com.github.asher_stern.parser.penn_treebank.PtbPosTags
import com.github.asher_stern.parser.penn_treebank.extractTrees
import com.github.asher_stern.parser.tree.PosAndWord
import com.github.asher_stern.parser.tree.TreeNode
import com.github.asher_stern.parser.tree.removeWordsFromTree
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileFilter


/**
 * Created by Asher Stern on November-06 2017.
 */

fun main(args: Array<String>)
{
    val pennTreebankDirectory = args[0]
    val destinationDirectory = args[1]

    println("Loading syntactic-trees from pennTreebankDirectory")
    val trees = mutableListOf<TreeNode<String, PosAndWord>>()
    for (file in File(pennTreebankDirectory).listFiles(MrgFileFilter))
    {
        trees.addAll(extractTrees(file.readText()))
    }

    println("Acquiring rules from trees")
    val acquisition = GrammarAcquisitionFromTrees(trees.map { removeWordsFromTree(it) })
    acquisition.absoluteEncounteredLimitation = RULE_ACQUISITION_ABSOLUTE_ENCOUNTER_LIMITATION
    val rules = acquisition.acquire()

    val nonTerminals = extractAllNonTerminals(rules)
    val originalGrammar = Grammar("S", nonTerminals, PtbPosTags.tags, rules.toSet())

    println("Convert grammar to Chomsky normal form")
    val converter = StringChomskyNormalFormConverter(originalGrammar)
    converter.convert()
    val newGrammar = converter.newGrammar


    val chomskyNormalFormGrammar: ChomskyNormalFormGrammar<String, String> = ChomskyNormalFormGrammarConstructor(newGrammar).construct()

    val gson = GsonBuilder().setPrettyPrinting().create()
    val grammarType = object : TypeToken<ChomskyNormalFormGrammar<String, String>>(){}.type
    File(destinationDirectory, "grammar.json").printWriter().use { it.println(gson.toJson(chomskyNormalFormGrammar, grammarType)) }
}


private object MrgFileFilter : FileFilter
{
    override fun accept(pathname: File?): Boolean
    {
        return (pathname?.extension == "mrg")
    }
}
