package br.com.afadc.folderstoalbumsconvertergooglephotos.utils

import java.io.File
import javax.swing.tree.DefaultMutableTreeNode

class FileNodesCreator {

    class FileNode(val file: File, val isRoot: Boolean) {

        var photoCount = 0

        override fun toString(): String {
            return if (!isRoot) {
                if (file.isFile) {
                    file.name
                } else {
                    "[$photoCount] ${file.name}"
                }
            } else {
                "[$photoCount] ${file.absolutePath}"
            }
        }
    }

    fun createFrom(file: File, validExtensions: Array<String>): DefaultMutableTreeNode {
        val rootNode = DefaultMutableTreeNode(
            FileNode(
                file,
                true
            )
        )

        createChildrenNodesFrom(
            file,
            rootNode,
            validExtensions.map { it.toLowerCase() }.toTypedArray()
        )

        return rootNode
    }

    private fun createChildrenNodesFrom(
        file: File,
        node: DefaultMutableTreeNode,
        validExtensions: Array<String>
    ): Int {
        var photoCount = 0

        val childrenFiles = file.listFiles()
        if (childrenFiles == null) {
            return photoCount
        }

        childrenFiles.sortWith(
            compareBy(
                { if (it.isDirectory) 0 else 1 },
                { it.name }
            )
        )

        for (childFile in childrenFiles) {
            if (childFile.isFile) {
                val childFileExtension = childFile.extension
                if (childFileExtension.isEmpty() || !validExtensions.contains(childFile.extension.toLowerCase())) {
                    continue
                }

                photoCount++
            }

            val childNode = DefaultMutableTreeNode(
                FileNode(
                    childFile,
                    false
                )
            )
            node.add(childNode)

            if (file.isDirectory) {
                photoCount += createChildrenNodesFrom(childFile, childNode, validExtensions)
            }
        }

        if (photoCount == 0) {
            node.removeFromParent()
        }

        (node.userObject as FileNode).photoCount = photoCount

        return photoCount
    }
}