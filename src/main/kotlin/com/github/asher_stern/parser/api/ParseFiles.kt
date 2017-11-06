package com.github.asher_stern.parser.api

import com.github.asher_stern.parser.grammar.ChomskyNormalFormGrammar
import com.github.asher_stern.parser.penn_treebank.normalizePtbPosTag
import com.github.asher_stern.parser.tree.PosAndWord
import com.github.asher_stern.parser.tree.TreePresent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

/**
 * Created by Asher Stern on November-06 2017.
 */

fun main(args: Array<String>)
{
    val sentenceFile = File(args[0])
    val resultFile = File(args[1])
    val modelDirectory = File(args[2])

    val gson = Gson()

    val setStringType = object : TypeToken<Set<String>>(){}.type
    val auxiliarySymbols = gson.fromJson<Set<String>>(File(modelDirectory, "auxiliary.json").readText(), setStringType)
    val grammarType = object : TypeToken<ChomskyNormalFormGrammar<String, String>>(){}.type
    val chomskyNormalFormGrammar = gson.fromJson<ChomskyNormalFormGrammar<String, String>>(File(modelDirectory, "grammar.json").readText(), grammarType)

    val parser = Parser(chomskyNormalFormGrammar, auxiliarySymbols)

    resultFile.printWriter().use { writer ->
        SentenceLoader(sentenceFile).use { loader ->
            for (sentence in loader)
            {
                val (tree, grammatical, probability) = parser.parse(sentence)
                writer.println(grammatical)
                writer.println(probability)
                writer.println(TreePresent(tree).present())
            }
        }
    }
}


private class SentenceLoader(private val sentenceFile: File) : Sequence<Array<PosAndWord>>, AutoCloseable
{
    override fun close()
    {
        reader.close()
    }

    override fun iterator(): Iterator<Array<PosAndWord>>
    {
        theNext = readNext()

        return object : Iterator<Array<PosAndWord>>
        {
            override fun hasNext(): Boolean = (theNext != null)

            override fun next(): Array<PosAndWord>
            {
                if (theNext == null) throw NoSuchElementException()
                val ret = theNext!!
                theNext = readNext()
                return ret
            }
        }
    }


    private fun readNext(): Array<PosAndWord>?
    {
        val lines = mutableListOf<String>()
        var line: String? = reader.readLine()

        while (line != null)
        {
            if (line.trim().isNotEmpty()) { break }
            line = reader.readLine()
        }

        while (line != null)
        {
            if (line.trim().isEmpty()) break
            lines.add(line)
            line = reader.readLine()
        }

        if (lines.isEmpty()) return null
        return lines.map { lineToPosAndWord(it) }.toTypedArray()
    }

    private fun lineToPosAndWord(line: String): PosAndWord
    {
        val (word, pos) = line.split("\t")
        return PosAndWord(normalizePtbPosTag(pos), word)
    }

    private val reader = sentenceFile.bufferedReader()
    private var theNext: Array<PosAndWord>? = null
}