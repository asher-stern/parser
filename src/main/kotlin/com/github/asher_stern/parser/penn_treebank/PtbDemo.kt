package com.github.asher_stern.parser.penn_treebank

import com.github.asher_stern.parser.tree.TreePresent
import java.io.File
import java.io.FileFilter

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

    val rootSymbols = mutableMapOf<String, Int>()
    for (file in File("/home/asher/main/data/resources/datasets/penn_treebank/10_percent/treebank/combined/").listFiles(filter).sorted().take(109).takeLast(10) )
    {
        println(file.name)
        val str = file.readText().trim()
        val trees = extractTrees(str)

        val s = trees.map { it.content.symbol }
        for (symbol in s)
        {
            if (symbol != null)
            {
                rootSymbols[symbol] = rootSymbols.getOrDefault(symbol, 0) + 1
            }
        }

        for (tree in trees)
        {
            println(TreePresent(tree).present())
            println()
        }

    }

    println(rootSymbols)
}