
package com.github.akruk.nodeindexer;

import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Trees;


public class NodeIndexer {
    Map<ParseTree, Integer> indexNodes(ParseTree tree) {
        Map<ParseTree, Integer> indexedNodes = new HashMap<>();
        var descendants = Trees.getDescendants(tree);
        int i = 0;
        for (var descendant : descendants) {
            indexedNodes.put(descendant, i++);
        }
        return indexedNodes;
    }

}
