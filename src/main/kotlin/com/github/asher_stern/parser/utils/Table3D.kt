package com.github.asher_stern.parser.utils

/**
 * Created by Asher Stern on November-02 2017.
 */
class Table3D<T1,T2,T3,V>
{
    operator fun set(firstIndex: T1, secondIndex: T2, thirdIndex: T3, value: V?)
    {
        val m2 = map.computeIfAbsent(firstIndex) { mutableMapOf() }
        val m3 = m2.computeIfAbsent(secondIndex) { mutableMapOf() }
        m3.put(thirdIndex, value)
    }

    operator fun get(firstIndex: T1, secondIndex: T2, thirdIndex: T3): V?
    {
        return map.get(firstIndex)?.get(secondIndex)?.get(thirdIndex)
    }

    operator fun get(firstIndex: T1, secondIndex: T2): Map<T3, V?>?
    {
        return map.get(firstIndex)?.get(secondIndex)
    }

    operator fun get(firstIndex: T1): Map<T2, Map<T3, V?>>?
    {
        return map[firstIndex]
    }

    val firstIndexes: Set<T1>
        get() = map.keys

    private val map: MutableMap<T1, MutableMap<T2, MutableMap<T3, V?>>> = mutableMapOf()
}


