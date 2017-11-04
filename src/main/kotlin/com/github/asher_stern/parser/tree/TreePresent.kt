package com.github.asher_stern.parser.tree

/**
 * Created by Asher Stern on November-03 2017.
 */
class TreePresent(private val tree: TreeNode<String, PosAndWord>)
{
    fun present(): String
    {
        presentNode(tree, 0)
        return sb.toString()
    }

    private fun presentNode(node: TreeNode<String, PosAndWord>, indentation: Int)
    {
        sb.append( CharArray(indentation) { '\t' } )

        node.content.symbol ?.let {
            sb.append(it)
        }
        node.content.terminal ?.let {
            sb.append(it.word+"/"+it.pos)
        }

        sb.append("\n")

        for (child in node.children)
        {
            presentNode(child, 1+indentation)
        }
    }

    private val sb = StringBuilder()
}
