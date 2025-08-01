
package com.github.akruk.nodeindexer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Trees;


public class NodeIndexer {
    public Map<ParseTree, Integer> indexNodes(final ParseTree tree) {
        // Trees.getDescendants actually returns descendants-or-self
        final List<ParseTree> descendants = Trees.getDescendants(tree);
        final Map<ParseTree, Integer> indexedNodes = new HashMap<>(descendants.size(), 1);
        int i = 0;
        for (final ParseTree descendant : descendants) {
            indexedNodes.put(descendant, i++);
        }
        return indexedNodes;
    }

}
