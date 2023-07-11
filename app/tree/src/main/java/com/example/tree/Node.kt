package com.example.tree

data class Node(val name: String, val children : List<Node>) {

    constructor(name: String, vararg children : Node): this(name, children.toList())
}

val mock =
Node("Main",
    Node("Bottom",
        Node("Feed"),
        Node("Video"),
    ),
    Node("Search"),
    Node("Dialog")
)


