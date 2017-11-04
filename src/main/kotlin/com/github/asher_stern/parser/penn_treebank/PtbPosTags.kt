package com.github.asher_stern.parser.penn_treebank

/**
 * Created by Asher Stern on November-03 2017.
 */
object PtbPosTags
{
    val tags: Set<String> =
        PtbPosTags::class.java.getResourceAsStream("/com/github/asher_stern/parser/ptb_tags.txt").bufferedReader().readLines().map { it.trim() }.filter { it.isNotEmpty() }.toSet()
}