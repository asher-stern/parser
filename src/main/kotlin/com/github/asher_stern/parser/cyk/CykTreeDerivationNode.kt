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

    var singleChild: CykTreeDerivationNode<N, T>? = null
        set(value)
        {
            if ( (firstChild != null) || (secondChild != null) ) throw RuntimeException("Inconsistency")
            field = value
        }

    var firstChild: CykTreeDerivationNode<N, T>? = null
        set(value)
        {
            if (singleChild != null) throw RuntimeException("Inconsistency")
            field = value
        }

    var secondChild: CykTreeDerivationNode<N, T>? = null
        set(value)
        {
            if (singleChild != null) throw RuntimeException("Inconsistency")
            field = value
        }

}