package com.github.asher_stern.parser.penn_treebank

import com.github.asher_stern.parser.grammar.SyntacticItem
import com.github.asher_stern.parser.tree.PosAndWord
import com.github.asher_stern.parser.tree.TreeNode

/**
 * Created by Asher Stern on November-03 2017.
 */


fun extractTrees(str: String): List<TreeNode<String, PosAndWord>> =
        str.trim().let { _str -> extractTrees(_str.toCharArray(), 0, _str.length).map { removeRedundantRoot(it) }.map { addSRoot(it) }.map { removeNone(it)!! } }


fun normalizePtbPosTag(tag: String): String
{
    val _tag = tag.trim()
    if (_tag.isEmpty()) throw RuntimeException("Empty tag")
    if (_tag in PtbPosTags.tags) return tag
    if (_tag.any { it.isLetter() }) throw RuntimeException("Bad tag: $tag")
    return "PUNCT"
}

private fun extractTrees(str: CharArray, start: Int, end: Int): List<TreeNode<String, PosAndWord>>
{
    val indexes = mutableListOf<StartEnd>()
    var _start = start
    while (_start >= 0)
    {
        val _end = parenthesesEndDetector(str, _start)
        if (_end > end) throw RuntimeException("Bad start/end indexes: $start/$end")
        indexes.add(StartEnd(_start, _end))
        _start = findIndex(str, _end + 1, end, '(')
    }

    return indexes.map { (s, e) -> extractTree(str, s, e) }
}

private fun extractTree(str: CharArray, start: Int, end: Int): TreeNode<String, PosAndWord>
{
    val cStart = start + 1
    val cEnd = end - 1
    if (cEnd <= cStart)
    {
        throw RuntimeException("Illegal start/end $start/$end")
    }

    val itemContentEndIndex = minNonNegativeOf(findIndex(str, cStart, cEnd, ' '), findIndex(str, cStart, cEnd, '('))
    if (itemContentEndIndex < 0)
    {
        throw RuntimeException("Bad string: ${str.joinToString("")}")
    }
    val contentString = String(str.copyOfRange(cStart, itemContentEndIndex)).trim()

    val nextParenthesisStart = findIndex(str, itemContentEndIndex, cEnd, '(')
    if (nextParenthesisStart < 0)
    {
        val word = String(str.copyOfRange(itemContentEndIndex, cEnd)).trim()
        val pos = normalizePtbPosTag(contentString)
        return TreeNode<String, PosAndWord>(SyntacticItem.createTerminal<String, PosAndWord>(PosAndWord(pos, word)))
    }
    else
    {
        return TreeNode<String, PosAndWord>(
                SyntacticItem.createSymbol<String, PosAndWord>(contentString._symbolNormalized),
                extractTrees(str, nextParenthesisStart, cEnd).toMutableList()
        )
    }
}

private fun removeRedundantRoot(tree: TreeNode<String, PosAndWord>): TreeNode<String, PosAndWord>
{
    if (0 == tree.content.symbol?.trim()?.length)
    {
        if (tree.children.size == 1)
        {
            return tree.children.first()
        }
    }
    return tree
}

private fun addSRoot(tree: TreeNode<String, PosAndWord>): TreeNode<String, PosAndWord>
{
    if (tree.content.symbol == "S") return tree
    else return TreeNode<String, PosAndWord>(SyntacticItem.createSymbol("S"), mutableListOf(tree))
}

private fun removeNone(tree: TreeNode<String, PosAndWord>): TreeNode<String, PosAndWord>?
{
    if (tree.content.terminal != null)
    {
        if (tree.content.terminal.pos == "-NONE-") { return null }
        else { return tree }
    }
    else
    {
        val processedChildren = tree.children.map { removeNone(it) }.filter { it != null }
        if (processedChildren.isNotEmpty())
        {
            return TreeNode(tree.content, processedChildren.map { it!! }.toMutableList())
        }
        else
        {
            return null
        }
    }
}

private val String._symbolNormalized: String
    get()
    {
        val hyphenIndex = this.indexOfFirst { !it.isLetterOrDigit() }
        if (hyphenIndex > 0)
        {
            return substring(0, hyphenIndex)
        }
        else return this
    }

/**
 * Finds the end(-exclusive) index of ')' in the given string, that corresponds to the '(' that appears in
 * the start-index.
 * @param str a string
 * @param start index in [str] of a '(' character.
 */
private fun parenthesesEndDetector(str: CharArray, start: Int): Int
{
    var counter = 0
    if (str[start] != '(')
    {
        throw RuntimeException("Bad start parameter: str[start] = ${str[start]}")
    }

    ++counter
    var index = start + 1

    while (counter > 0)
    {
        if (index >= str.size)
        {
            throw RuntimeException("Bad Penn-TreeBank string / or bad start index")
        }
        if (str[index] == '(') ++counter
        if (str[index] == ')') --counter
        ++index
    }
    return index
}

private data class StartEnd(val start: Int, val end: Int)

private fun findIndex(str: CharArray, start: Int, end: Int, c: Char): Int
{
    for (index in start until end)
    {
        if (str[index] == c) return index
    }
    return -1
}

private fun minNonNegativeOf(x1: Int, x2: Int): Int
{
    if ((x1 < 0) && (x2 < 0))
    {
        return minOf(x1, x2)
    }
    else if (x1 < 0)
    {
        return x2
    }
    else if (x2 < 0)
    {
        return x1
    }
    else return minOf(x1, x2)
}
