package com.github.asher_stern.parser.tree

/**
 * Created by Asher Stern on November-05 2017.
 */

/**
 * Class for presenting a tree: generating a easily-readable string representation of the tree.
 */
abstract class AbstractTreePresent<T>(private val tree: TreeNode<String, T>)
{
    fun present(): String
    {
        presentNode(tree, 0)
        return sb.toString()
    }

    protected abstract fun terminalString(terminal: T): String

    private fun presentNode(node: TreeNode<String, T>, indentation: Int)
    {
        sb.append( CharArray(indentation) { '\t' } )

        node.content.symbol ?.let {
            sb.append(it)
        }
        node.content.terminal ?.let {
            sb.append(terminalString(it))
            // sb.append(it.word+"/"+it.pos)
        }

        sb.append("\n")

        for (child in node.children)
        {
            presentNode(child, 1+indentation)
        }
    }

    private val sb = StringBuilder()
}