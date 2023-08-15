package com.example.navigation.dispatcher

import com.example.navigation.navigation.NavigationManager
import com.example.navigation.tree.Tree
import java.util.LinkedList
import kotlin.reflect.KClass

internal typealias Type<P> = KClass<out P>

/**
 * Класс нужен для навигации от компонента к компоненту.
 * @param navigationMap
 * Зарегистрированная иерархия компонентов. Вида:
 * Host ->  "stack" -> A, B, C
 *          "slot"  -> D, E
 *          "pages" -> F, G
 *          "tag"   -> X, Y, Z
 * Где tag должен соответствовать навигатору, который прописан в Host компоненте.
 * Смотри SlotExt.slot(...), StackExt.stack(...) и тп.
 * Иерархия должна содержать лишь один корневой компонент.
 * Компонент не может вкладываться сам в себя, в том числе транзитивно (см. Tree).
 *
 * @param default Зарегистрированные дефолтные параметры.
 */
class NavigationDispatcher<P : Any>(
    private val navigationMap: Map<Type<P>, Map<String, Set<Type<P>>>>,
    private val default: Map<Type<P>, P>,
) {

    val tree: Tree<Type<P>> =
        Tree(navigationMap.mapValues { host -> host.value.flatMapTo(hashSetOf()) { it.value } })

    /**
     * Ищет список родительских типов параметров компонента, который открывают или закрывают.
     * @param path
     * Список параметров или типов, которые должны содержаться в списке родительских типов параметрах.
     * @param from
     * Список родительских параметров до текущего компонента.
     */
    private fun preparePath(path: NavigationPath<P>, from: List<P>): List<Type<P>> {
        val fromTypes: List<Type<P>> = from.map { it::class }
        val pathTypes = path.types
        return tree.findPath(fromTypes, pathTypes)
    }

    /**
     * В случае если не известны конкретные параметры компонента, который нужно открыть,
     * те в path передан тип параметра.
     * Беруться соответствующие типу параметры компонента из уже открытых компонентов,
     * если таких нет, то из дефолтных, иначе null.
     */
    private fun findParams(
        pathParams: Map<Type<P>, P>,
        manger: NavigationManager<P>,
        type: Type<P>,
    ): P? {
        return pathParams[type]
            ?: manger.findChildParams(type)
            ?: default[type]
    }

    /**
     * Ищет тег навигации, под которым зарегистрирован дочерний компонент.
     */
    private fun findTagForChild(host: Type<P>, child: Type<P>): String {
        val registeredScreensInHost = navigationMap[host]
            ?: error("Host $host did not register navigation")
        registeredScreensInHost.forEach { (tag, screens) ->
            if (screens.contains(child)) {
                return tag
            }
        }
        error("Host $host did not register screen $child")
    }

    /**
     * Открывает компонент. И все родительские до него.
     * @param manger
     * Мэнеджер текущего открытого компонента.
     * @param path Частичный список родительских параметров до открываемого компонента.
     * Открываемый компонент - последнему элемент в path.
     */
    internal fun open(manger: NavigationManager<P>, path: NavigationPath<P>) {
        val fullPath = preparePath(path, manger.branch) // Список компонентов от корня до того, который нужно открыть.
        var current = manger.root
        val pathParams = path.params
        val operations = LinkedList<() -> Unit>()
        val last = fullPath.size - 1 // Индекс последнего компонента, который открываем.
        fullPath.forEachIndexed { index, currentType ->
            if (index >= last) return@forEachIndexed // Ничего не открываем в последнем компоненте.
            if (current.params::class != currentType) error("Illegal type $currentType for current ${current.params}")
            val childType = fullPath[index + 1]
            val childParam = findParams(pathParams, current, childType)
                ?: error("Can not find params fot type $childType")
            val child = current.provideChild(childParam) // Создаем компонент, если его нет.

            val tag = findTagForChild(currentType, childType)
            val navigationHolder = current.findHolder(tag)
            if (navigationHolder != null) { // Если есть навигатора, то компонент открыт, нужно открыть дочерний.
                operations.add { navigationHolder.navigation.open(childParam) }
            } else { // Если нет навигатора, то компонент еще не открыт, нужно при созданнии открыть дочерний.
                current.pendingParams(tag, childParam)
            }
            current = child
        }
        operations.forEach { it.invoke() }
    }

    /**
     * Закрывает компонент, если он открыт.
     * @param manger
     * Мэнеджер текущего открытого компонента.
     * @param path Частичный список родительских параметров до закрываемого компонента.
     * Закрываемый компонент - последнему элемент в path.
     */
    internal fun close(manger: NavigationManager<P>, path: NavigationPath<P>) {
        val fullPath = preparePath(path, manger.branch) // Список компонентов от корня до того, который нужно закрыть.
        var current = manger.root
        val pathParams = path.params
        var operation: () -> Unit = {}
        val last = fullPath.size - 1 // Индекс последнего компонента, который закрываем.
        fullPath.forEachIndexed { index, currentType ->
            if (index >= last) return@forEachIndexed // Ничего не заыкрываем в последнем компоненте.
            if (current.params::class != currentType) error("Illegal type $currentType for current ${current.params}")
            val childType = fullPath[index + 1]
            val childParam = findParams(pathParams, current, childType)
                ?: error("Can not find params fot type $childType")
            val child = current.findChild(childParam) ?: return // Если нет компонента, то нет и дочернего. Ничего не нужно закрывать.

            val tag = findTagForChild(currentType, childType)
            val navigationHolder = current.findHolder(tag) ?: return // Если нет навигатора, то нет и дочернего. Ничего не нужно закрывать.
            if (index == last - 1) { // Родительский компонент закрываемого.
                operation = { navigationHolder.navigation.close(childParam) }
            }
            current = child
        }
        operation.invoke()
    }
}