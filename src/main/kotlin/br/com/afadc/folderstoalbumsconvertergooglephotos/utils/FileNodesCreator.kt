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

        photoCount += childrenFiles.mapNotNull { childFile ->
            if (task.isCanceled) {
                return@mapNotNull null
            }

            GlobalScope.async {
                var childFilePhotoCount = 0

                if (task.isCanceled) {
                    return@async childFilePhotoCount
                }

                if (childFile.isFile) {
                    val childFileExtension = childFile.extension
                    if (childFileExtension.isEmpty() || !validExtensions.contains(childFile.extension.toLowerCase())) {
                        return@async childFilePhotoCount
                    }

                    childFilePhotoCount += 1
                }

                val childNode = DefaultMutableTreeNode(
                    FileNode(
                        childFile,
                        false
                    )
                )

                synchronized(task) {
                    node.add(childNode)
                }

                if (childFile.isDirectory) {
                    childFilePhotoCount += createChildrenNodesFrom(task, childFile, childNode, validExtensions)
                }

                return@async childFilePhotoCount
            }
        }.sumBy {
            it.await()
        }

        if (task.isCanceled) {
            return photoCount
        }

        if (photoCount == 0) {
            synchronized(task) {
                node.removeFromParent()
            }
        }

        (node.userObject as FileNode).photoCount = photoCount

        return photoCount
    }
}