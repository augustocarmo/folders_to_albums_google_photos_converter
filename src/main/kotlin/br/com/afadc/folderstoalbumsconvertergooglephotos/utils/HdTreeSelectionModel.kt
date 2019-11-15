package br.com.afadc.folderstoalbumsconvertergooglephotos.utils

import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeSelectionModel
import javax.swing.tree.TreePath

class HdTreeSelectionModel : DefaultTreeSelectionModel() {

    private fun filterSelectedPaths(paths: Array<out TreePath>?): Array<out TreePath>? {
        if (paths == null) {
            return null
        }

        return paths.filter {
            val node = (it.lastPathComponent as DefaultMutableTreeNode)

            !node.isLeaf && !node.isRoot && node.parent!!.parent == null
        }.toTypedArray()
    }

    override fun setSelectionPaths(paths: Array<out TreePath>?) {
        super.setSelectionPaths(filterSelectedPaths(paths))
    }

    override fun addSelectionPaths(paths: Array<out TreePath>?) {
        super.addSelectionPaths(filterSelectedPaths(paths))
    }
}