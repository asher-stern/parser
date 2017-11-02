package com.github.asher_stern.parser.cyk

/**
 * Created by Asher Stern on November-02 2017.
 */
class CykTreeDerivationNode<N, T> private constructor(
        val terminal: T?,
        val item: CykTableItem<N>?
)
{
    constructor(terminal: T) : this(terminal, null)
    constructor(item: CykTableItem<N>) : this(null, item)

    var firstChild: CykTreeDerivationNode<N, T>? = null
    var secondChild: CykTreeDerivationNode<N, T>? = null
}