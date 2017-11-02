package com.github.asher_stern.parser.grammar

/**
 * Created by Asher Stern on November-02 2017.
 */
data class SyntacticItem<N, T> protected constructor(val symbol: N?, val terminal: T?)
{
    companion object
    {
        fun <N, T> createSymbol(symbol: N): SyntacticItem<N, T> = SyntacticItem<N, T>(symbol, null)
        fun <N, T> createTerminal(terminal: T): SyntacticItem<N, T> = SyntacticItem<N, T>(null, terminal)
    }
}
