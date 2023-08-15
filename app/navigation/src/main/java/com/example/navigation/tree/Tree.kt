package com.example.navigation.tree
import java.util.LinkedList

/**
 * Дерево навигации.
 * @param navigationMap
 * Зарегистрированная навигация в виде:
 * Host -> Child1, Child2, ...
 * Дерево не построится если нода содержит себя вниз по иерархии:
 * A -> B, A
 * или
 * A -> B, C
 * B -> A, D
 */
class Tree<T>(navigationMap: Map<T, Collection<T>>) {

    val root: Node<T> = createNode(
        findRoot(navigationMap),
        null,
        navigationMap,
    )

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

    /**
     * Поиск ноды от заданной по частичному пути.
     *
     * @param from Полный список родительских параметров до стартовой ноды.
     * @param path Частичный список родительских параметров до искомой ноды.
     * Искомоой нодой - называется нода, data которой равна последнему элементу в path.
     * @return Полный список родительских параметров до искомой ноды.
     */
    internal fun findPath(from: List<T>, path: List<T>): List<T> {
        val fromNode = findNodeByFullPath(root, from) ?: error("Incorrect from path $from")
        val target = bubbleSearch(fromNode, path) ?: error("Can not find path $path from $from")
        return pathToRoot(target)
    }

    override fun toString(): String {
        val builder = StringBuilder()
        root.toString("", builder)
        return builder.toString()
    }

    private companion object {

        /**
         * Поиск корня дерева.
         * Корень - нода, у которой нет родителя.
         */
        private fun <T> findRoot(
            navigationMap: Map<T, Collection<T>>,
        ): T {
            val hosts = navigationMap.keys.toMutableSet()
            navigationMap
                .flatMap { it.value }
                .forEach { hosts.remove(it) }
            if (hosts.size != 1) {
                error("Only one root! Found several $hosts")
            }
            return hosts.first()
        }

        /**
         * Рекурсивно строит дерево.
         */
        private fun <T> createNode(
            data: T,
            parent: Node<T>?,
            navigationMap: Map<T, Collection<T>>,
        ): Node<T> {
            checkSelfContains(parent, navigationMap[data])
            val children: MutableMap<T, Node<T>> = mutableMapOf()
            val node = Node(data, parent, children)
            navigationMap[data]?.forEach {
                children[it] = createNode(it, node, navigationMap)
            }
            return node
        }

        /**
         * Проверяет что не является своим же предком.
         */
        private fun <T> checkSelfContains(
            parent: Node<T>?,
            children: Collection<T>?,
        ) {
            children ?: return
            var current = parent
            while (current != null) {
                if (current.data in children) {
                    error("Can not build tree, ${current.data} contains itself.\n${errorPath(parent!!, current)}")
                }
                current = current.parent
            }
        }

        private fun <T> errorPath(from: Node<T>, to: Node<T>): String {
            val builder = StringBuilder()
            builder.append("${to.data} <- ")
            var current = from
            while (current != to) {
                builder.append("${current.data} <- ")
                current = current.parent ?: break
            }
            builder.append("${to.data}")
            return builder.toString()
        }

        /**
         * Возврощает дочернюю ноду по списку родителей.
         * null если не удалось найти.
         * @param root Корневая нода.
         * @param path data родительских нод от корня до искомой ноды.
         */
        private fun <T> findNodeByFullPath(root: Node<T>, path: List<T>): Node<T>? {
            var node = root
            path.forEachIndexed { index, data ->
                if (index == 0 && root.data == data) return@forEachIndexed
                node = node.children[data] ?: return null
            }
            return node
        }

        /**
         * Возврощает список параметрот от корня до переданной ноды.
         */
        private fun <T> pathToRoot(node: Node<T>): List<T> {
            val path = LinkedList<T>()
            var current: Node<T>? = node
            while (current != null) {
                path.addFirst(current.data)
                current = current.parent
            }
            return path
        }

        /**
         * Поиск ноды от заданной по частичному пути.
         * null если не удалось найти.
         * @param node Стартовая нода.
         * @param path Частичный список родительских параметров до искомой ноды.
         * Искомоой нодой - называется нода, data которой равна последнему элементу в path.
         *
         * Путь может быть вверх по иерархии, потом вниз по иерархии. Вниз потом вверх - не путь.
         * Сначала поиск осуществляется по дочерним нодам стартовой ноды.
         * Потом у братьев стартовой ноды и тд вверх по иерархии.
         * В конце если путь не найден, то поиск происходит по всему дереву.
         */
        private fun <T> bubbleSearch(node: Node<T>, path: List<T>): Node<T>? {
            var parent: Node<T>? = node.parent
            var current: Node<T> = node
            // Поиск вниз от стартовой ноды.
            var found = findNodeByPartPath(current, path)
            if (found != null) return found
            while (parent != null) {
                // Исключаем current, тк уже в искали в этом поддереве.
                found = findNodeByPartPath(parent, path, current)
                if (found != null) return found
                current = parent
                parent = current.parent
            }
            // Поиск вниз от корня.
            return findNodeByPartPath(current, path)
        }

        /**
         * Поиск ноды от заданной по частичному пути.
         * null если не удалось найти.
         * @param node Стартовая нода.
         * @param path Частичный список родительских параметров до искомой ноды.
         * @param exclude Исключает поиск в ноде.
         * Искомоой нодой - называется нода, data которой равна последнему элементу в path.
         *
         * Поиск пути в дочерних нодах от стартовой.
         */
        private fun <T> findNodeByPartPath(node: Node<T>, path: List<T>, exclude: Node<T>? = null): Node<T>? {
            if (path.isEmpty()) return null
            val found: Node<T>? =
                if (node.data == path.first()) {
                    // Эта нода есть в пути.
                    if (path.size == 1) {
                        node // Если последняя в пути значит она искомая.
                    } else {
                        // Если нода не последняя, то удаяляем ее из списка и ищем в дочерних.
                        findNodeByPartPathInChildren(node, path.subList(1, path.size), exclude)
                    }
                } else {
                    // Ноды нет, ищем в дочерних.
                    findNodeByPartPathInChildren(node, path, exclude)
                }
            return found
        }

        /**
         * Поиск ноды от заданной по частичному пути.
         * null если не удалось найти.
         * @param node Стартовая нода.
         * @param path Частичный список родительских параметров до искомой ноды.
         * @param exclude Исключает поиск в ноде.
         * Искомоой нодой - называется нода, data которой равна последнему элементу в path.
         *
         * Поиск пути в дочерних нодах от стартовой.
         */
        private fun <T> findNodeByPartPathInChildren(node: Node<T>, path: List<T>, exclude: Node<T>? = null): Node<T>? {
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
    }
}
