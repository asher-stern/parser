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
    val modelDirectory: File? = if (args.size>=3) File(args[2]) else null

    val auxiliarySymbols = loadAuxiliarySymbols(modelDirectory)
    val chomskyNormalFormGrammar = loadGrammar(modelDirectory)

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


private fun loadGrammar(modelDirectory: File?): ChomskyNormalFormGrammar<String, String>
{
    val json: String = when
    {
        modelDirectory != null ->
        {
            File(modelDirectory, "grammar.json").readText()
        }
        else ->
        {
            ObjectForClassLoader::class.java.getResourceAsStream("/com/github/asher_stern/parser/model/grammar.json").use { it.reader().use { it.readText() } }
        }
    }

    val gson = Gson()
    val grammarType = object : TypeToken<ChomskyNormalFormGrammar<String, String>>() {}.type
    val chomskyNormalFormGrammar = gson.fromJson<ChomskyNormalFormGrammar<String, String>>(json, grammarType)
    return chomskyNormalFormGrammar
}

private fun loadAuxiliarySymbols(modelDirectory: File?): Set<String>
{
    val json: String = when
    {
        modelDirectory != null ->
        {
            File(modelDirectory, "auxiliary.json").readText()
        }
        else ->
        {
            ObjectForClassLoader::class.java.getResourceAsStream("/com/github/asher_stern/parser/model/auxiliary.json").use { it.reader().use { it.readText() } }
        }
    }

    val gson = Gson()
    val setStringType = object : TypeToken<Set<String>>(){}.type
    val auxiliarySymbols = gson.fromJson<Set<String>>(json, setStringType)
    return auxiliarySymbols
}


private object ObjectForClassLoader

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