package com.github.asher_stern.parser.penn_treebank

import com.github.asher_stern.parser.grammar.SyntacticItem
import com.github.asher_stern.parser.tree.PosAndWord
import com.github.asher_stern.parser.tree.TreeNode

/**
 * Created by Asher Stern on November-03 2017.
 */


fun extractTrees(str: String): List<TreeNode<String, PosAndWord>> =
        extractTrees(str.toCharArray(), 0, str.length)



private fun extractTrees(str: CharArray, start: Int, end: Int): List<TreeNode<String, PosAndWord>>
{
    val indexes = mutableListOf<StartEnd>()
    var _start = start
    while (_start > 0)
    {
        val _end = parenthesesEndDetector(str, _start)
        if (_end > end) throw RuntimeException("Bad start/end indexes: $start/$end")
        indexes.add(StartEnd(_start, _end))
        _start = findIndex(str, _end+1, end, '(')
    }

    return indexes.map { (s,e) -> extractTree(str, s, e) }
}

private fun extractTree(str: CharArray, start: Int, end: Int): TreeNode<String, PosAndWord>
{
    val cStart = start + 1
    val cEnd = end - 1
    if (cEnd <= cStart) { throw RuntimeException("Illegal start/end $start/$end") }

    val itemContentEndIndex = findIndex(str, cStart, cEnd, ' ')
    if (itemContentEndIndex < 0) { throw RuntimeException("Bad string: ${str.joinToString("")}") }
    val contentString = String(str.copyOfRange(cStart, itemContentEndIndex)).trim()

    val nextParenthesisStart = findIndex(str, itemContentEndIndex, cEnd, '(')
    if (nextParenthesisStart < 0)
    {
        val word = String(str.copyOfRange(itemContentEndIndex, cEnd)).trim()
        return TreeNode<String, PosAndWord>(SyntacticItem.createTerminal<String, PosAndWord>(PosAndWord(contentString, word)))
    }
    else
    {
        return TreeNode<String, PosAndWord>(
                SyntacticItem.createSymbol<String, PosAndWord>(contentString),
                extractTrees(str, nextParenthesisStart, cEnd).toMutableList()
        )
    }
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
    if (str[start] != '(') { throw RuntimeException("Bad start parameter: str[start] = ${str[start]}") }

    ++counter
    var index = start + 1

    while (counter > 0)
    {
        if (index >= str.size) { throw RuntimeException("Bad Penn-TreeBank string / or bad start index") }
        if (str[index] == '(') --counter
        if (str[index] == ')') ++counter
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