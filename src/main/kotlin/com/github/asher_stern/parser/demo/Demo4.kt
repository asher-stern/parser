package com.github.asher_stern.parser.demo

import com.github.asher_stern.parser.cyk.CykAlgorithm
import com.github.asher_stern.parser.cyk.CykAlgorithmWithHack
import com.github.asher_stern.parser.grammar.ChomskyNormalFormGrammarConstructor
import com.github.asher_stern.parser.grammar.Grammar
import com.github.asher_stern.parser.grammar.Rule
import com.github.asher_stern.parser.grammar.SyntacticItem
import com.github.asher_stern.parser.tree.AbstractTreePresent
import com.github.asher_stern.parser.tree.convertCykToSimpleTree
import com.github.asher_stern.parser.utils.Array1

/**
 * Created by Asher Stern on November-05 2017.
 */

fun main(args: Array<String>)
{
    val startSymbol = "X1"
    val rule1 = Rule("1", "X1", listOf(SyntacticItem.createSymbol<String, String>("X2"), SyntacticItem.createSymbol<String, String>("X3")), false, Math.log(0.5))
    val rule2 = Rule("2", "X4", listOf(SyntacticItem.createSymbol<String, String>("X2"), SyntacticItem.createSymbol<String, String>("X3")), false, Math.log(0.8))
    val rule3 = Rule("3", "X1", listOf(SyntacticItem.createSymbol<String, String>("X4")), false, Math.log(0.8))
    val rule4 = Rule("4", "X2", listOf(SyntacticItem.createTerminal<String, String>("x2")), false, Math.log(1.0))
    val rule5 = Rule("5", "X3", listOf(SyntacticItem.createTerminal<String, String>("x3")), false, Math.log(1.0))
    val rule6 = Rule("6", "X5", listOf(SyntacticItem.createTerminal<String, String>("x5")), false, Math.log(1.0))

    val grammar = Grammar<String, String>(startSymbol, setOf("X1","X2","X3","X4"), setOf("x2", "x3"), setOf(rule1,rule2,rule3,rule4,rule5, rule6))
    val chomskyNormalFormGrammar = ChomskyNormalFormGrammarConstructor(grammar).construct()

    val sentence = Array1(arrayOf("x2", "x3", "x5", "x5", "x5", "x2", "x3"))

    val cykAlgorithm = CykAlgorithmWithHack(chomskyNormalFormGrammar, sentence)
    val cykTree = cykAlgorithm.parse()
    if (cykTree != null)
    {
        println ("wellParsed = ${cykAlgorithm.wellParsed}")
        println ("probability = ${cykAlgorithm.parseProbability}")
        val tree = convertCykToSimpleTree<String, String>(cykTree)
        val treeString = object : AbstractTreePresent<String>(tree)
        {
            override fun terminalString(terminal: String): String = terminal
        }.present()
        println(treeString)
    }
    else
    {
        println("Not parsed.")
    }


}