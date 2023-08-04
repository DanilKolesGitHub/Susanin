package com.example.tree

data class UiNode(val name: String, val children : List<UiNode>) {

    constructor(name: String, vararg children : UiNode): this(name, children.toList())
}

val mock =
UiNode("Main",
    UiNode("Bottom",
        UiNode("Feed"),
        UiNode("Video"),
    ),
    UiNode("Search"),
    UiNode("Dialog")
)


