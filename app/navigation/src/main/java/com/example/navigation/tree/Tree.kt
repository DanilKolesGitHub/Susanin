package com.example.navigation.tree

import java.util.LinkedList

class Tree<T>(dependencyMap: Map<T, Collection<T>>) {

    val root: Node<T>

    init {
        root = createNode(
            findRoot(dependencyMap),
            null,
            dependencyMap
        )
    }

    data class Node<T>(
        val data: T,
        val parent: Node<T>?,
        val children: Map<T, Node<T>>,
    ) {
        fun toString(prefix: String, builder: StringBuilder) {
            builder.append(prefix).append(this.toString()).append("\n")
            val childPrefix = "$prefix    "
            children.values.forEach {
                it.toString(childPrefix, builder)
            }
        }

        override fun toString(): String {
            return data.toString()
        }
    }

    fun findPath(from: List<T>, path: List<T>): List<T> {
        val fromNode = findNodeByFullPath(root, from) ?: throw IllegalArgumentException("Incorrect path $from")
        val target = bubbleSearch(fromNode, path) ?: throw IllegalArgumentException("Incorrect path $path")
        return pathToRoot(target)
    }

    override fun toString(): String {
        val builder = StringBuilder()
        root.toString("", builder)
        return builder.toString()
    }

    companion object {

        private fun <T> findRoot(
            dependencyMap: Map<T, Collection<T>>
        ): T {
            val hosts = dependencyMap.keys.toMutableSet()
            dependencyMap
                .flatMap { it.value }
                .forEach { hosts.remove(it) }
            if (hosts.size != 1) {
                throw IllegalArgumentException("Only one root, found $hosts")
            }
            return hosts.first()
        }

        private fun <T> createNode(
            root: T,
            parent: Node<T>?,
            dependencyMap: Map<T, Collection<T>>,
        ): Node<T> {
            val children: MutableMap<T, Node<T>> = mutableMapOf()
            val node = Node(root, parent, children)
            dependencyMap[root]?.forEach {
                children[it] = createNode(it, node, dependencyMap)
            }
            return node
        }

        private fun <T> findNodeByPartPath(root: Node<T>, path: List<T>, exclude: Node<T>? = null): Node<T>? {
            if (path.isEmpty()) return null
            val found: Node<T>? =
                if (root.data == path.first()) {
                    if (path.size == 1) {
                        root
                    } else {
                        findNodeByPartPathInChildren(root, path.subList(1, path.size), exclude)
                    }
                } else {
                    findNodeByPartPathInChildren(root, path, exclude)
                }
            return found
        }

        private fun <T> findNodeByPartPathInChildren(node: Node<T>, path: List<T>, exclude: Node<T>? = null): Node<T>?{
            node.children.values.forEach {
                if (it != exclude) {
                    val found = findNodeByPartPath(it, path)
                    if (found != null) {
                        return found
                    }
                }
            }
            return null
        }

        private fun <T> findNodeByFullPath(root: Node<T>, path: List<T>): Node<T>? {
            var node = root
            path.forEachIndexed { index, data ->
                if (index == 0 && root.data == data) return@forEachIndexed
                node = node.children[data] ?: return null
            }
            return node
        }

        private fun <T> pathToRoot(node: Node<T>): List<T> {
            val path = LinkedList<T>()
            var current: Node<T>? = node
            while (current != null) {
                path.addFirst(current.data)
                current = current.parent
            }
            return path
        }

        private fun <T> bubbleSearch(node: Node<T>, path: List<T>): Node<T>? {
            var parent: Node<T>? = node.parent
            var current: Node<T> = node
            var found = findNodeByPartPath(current, path)
            if (found != null) return found
            while (parent != null) {
                found = findNodeByPartPath(parent, path, current)
                if (found != null) return found
                current = parent
                parent = current.parent
            }
            return findNodeByPartPath(current, path)
        }
    }
}
