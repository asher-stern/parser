package com.github.asher_stern.parser.tree

/**
 * Created by Asher Stern on November-03 2017.
 */
class TreePresent(tree: TreeNode<String, PosAndWord>) : AbstractTreePresent<PosAndWord>(tree)
{
    override fun terminalString(terminal: PosAndWord): String =
        terminal.word+"/"+terminal.pos
}
