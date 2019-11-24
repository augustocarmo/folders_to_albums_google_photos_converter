package br.com.afadc.folderstoalbumsconvertergooglephotos.utils

import java.io.File
import javax.swing.tree.DefaultMutableTreeNode

class FileNodesCreator {

    interface FileNodesCreatorTaskListener {

        fun onCompleted(rootNode: DefaultMutableTreeNode)
        fun onCanceled()
    }

    class FileNodesCreatorTask(
        val rootFile: File,
        val validExtensions: Array<String>
    ) {

        interface CancelListener {
            fun onCanceled()
        }

        private var cancelListeners = ArrayList<CancelListener>()

        var isCanceled = false
            private set

        @Synchronized
        fun cancel() {
            if (isCanceled) {
                return
            }

            synchronized(cancelListeners) {
                for (listener in cancelListeners.toList()) {
                    listener.onCanceled()
                }
            }
        }

        fun addCancelListener(listener: CancelListener) {
            synchronized(cancelListeners) {
                if (cancelListeners.contains(listener)) {
                    return
                }

                cancelListeners.add(listener)
            }
        }

        fun removeCancelListener(listener: CancelListener) {
            synchronized(cancelListeners) {
                cancelListeners.remove(listener)
            }
        }
    }

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

    fun createFileNodesCreatorTask(rootFile: File, validExtensions: Array<String>): FileNodesCreatorTask {
        return FileNodesCreatorTask(rootFile, validExtensions)
    }

    fun executeFileNodesCreatorTask(task: FileNodesCreatorTask, listener: FileNodesCreatorTaskListener) {
        val taskCancelListener = object : FileNodesCreatorTask.CancelListener {
            override fun onCanceled() {
                listener.onCanceled()
            }
        }
        task.addCancelListener(taskCancelListener)

        try {
            if (task.isCanceled) {
                return
            }

            val rootFile = task.rootFile
            val validExtensions = task.validExtensions.copyOf()

            val rootNode = DefaultMutableTreeNode(
                FileNode(
                    rootFile,
                    true
                )
            )

            if (task.isCanceled) {
                return
            }

            createChildrenNodesFrom(
                task,
                rootFile,
                rootNode,
                validExtensions.map { it.toLowerCase() }.toTypedArray()
            )

            if (task.isCanceled) {
                return
            }

            listener.onCompleted(rootNode)
        } finally {
            task.removeCancelListener(taskCancelListener)
        }
    }

    private fun createChildrenNodesFrom(
        task: FileNodesCreatorTask,
        file: File,
        node: DefaultMutableTreeNode,
        validExtensions: Array<String>
    ): Int {
        var photoCount = 0

        if (task.isCanceled) {
            return photoCount
        }

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

        if (task.isCanceled) {
            return photoCount
        }

        for (childFile in childrenFiles) {
            if (task.isCanceled) {
                return photoCount
            }

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
                photoCount += createChildrenNodesFrom(task, childFile, childNode, validExtensions)
            }
        }

        if (photoCount == 0) {
            node.removeFromParent()
        }

        (node.userObject as FileNode).photoCount = photoCount

        return photoCount
    }
}