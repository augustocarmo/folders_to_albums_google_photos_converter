package br.com.afadc.folderstoalbumsconvertergooglephotos.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
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

            isCanceled = true

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

        var mediaCount = 0

        override fun toString(): String {
            return if (!isRoot) {
                if (file.isFile) {
                    file.name
                } else {
                    "[$mediaCount] ${file.name}"
                }
            } else {
                "[$mediaCount] ${file.absolutePath}"
            }
        }
    }

    fun createFileNodesCreatorTask(rootFile: File, validExtensions: Array<String>): FileNodesCreatorTask {
        return FileNodesCreatorTask(rootFile, validExtensions)
    }

    fun executeFileNodesCreatorTask(task: FileNodesCreatorTask, listener: FileNodesCreatorTaskListener) {
        GlobalScope.launch {
            val taskCancelListener = object : FileNodesCreatorTask.CancelListener {
                override fun onCanceled() {
                    listener.onCanceled()
                }
            }
            task.addCancelListener(taskCancelListener)

            try {
                if (task.isCanceled) {
                    return@launch
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
                    return@launch
                }

                withContext(IO) {
                    createChildrenNodesFrom(
                        task,
                        rootFile,
                        rootNode,
                        validExtensions.map { it.toLowerCase() }.toTypedArray()
                    )
                }

                if (task.isCanceled) {
                    return@launch
                }

                listener.onCompleted(rootNode)
            } finally {
                task.removeCancelListener(taskCancelListener)
            }
        }
    }

    private suspend fun createChildrenNodesFrom(
        task: FileNodesCreatorTask,
        file: File,
        node: DefaultMutableTreeNode,
        validExtensions: Array<String>
    ): Int {
        var mediaCount = 0

        if (task.isCanceled) {
            return 0
        }

        val childrenFiles = file.listFiles()
        if (childrenFiles == null) {
            return 0
        }

        if (task.isCanceled) {
            return 0
        }

        val isNodeRootNode = (node.userObject as FileNode).isRoot

        val childrenNodesToAdd = ArrayList<DefaultMutableTreeNode>()

        mediaCount += childrenFiles.mapNotNull { childFile ->
            if (task.isCanceled) {
                return@mapNotNull null
            }

            GlobalScope.async {
                var childFileMediaCount = 0

                if (task.isCanceled) {
                    return@async 0
                }

                if (childFile.isFile) {
                    if ((isNodeRootNode)) {
                        return@async 0
                    }

                    val childFileExtension = childFile.extension
                    if (childFileExtension.isEmpty() || !validExtensions.contains(childFile.extension.toLowerCase())) {
                        return@async 0
                    }

                    childFileMediaCount += 1
                }

                val childNode = DefaultMutableTreeNode(
                    FileNode(
                        childFile,
                        false
                    )
                )

                if (childFile.isDirectory) {
                    childFileMediaCount += createChildrenNodesFrom(task, childFile, childNode, validExtensions)
                }

                if (childFileMediaCount > 0) {
                    synchronized(node) {
                        childrenNodesToAdd.add(childNode)
                    }
                }

                return@async childFileMediaCount
            }
        }.sumBy {
            it.await()
        }

        if (task.isCanceled) {
            return 0
        }

        childrenNodesToAdd.sortWith(
            compareBy(
                { if ((it.userObject as FileNode).file.isDirectory) 0 else 1 },
                { (it.userObject as FileNode).file.name.toLowerCase() }
            )
        )

        if (task.isCanceled) {
            return 0
        }

        synchronized(task) {
            if (mediaCount > 0) {
                for (childNode in childrenNodesToAdd) {
                    node.add(childNode)
                }
            } else {
                node.removeFromParent()
            }
        }

        if (task.isCanceled) {
            return 0
        }

        (node.userObject as FileNode).mediaCount = mediaCount

        return mediaCount
    }
}