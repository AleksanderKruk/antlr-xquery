package com.github.akruk.nodegetter;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

public interface INodeGetter {

    List<ParseTree> getPrecedingSiblingsOrSelf(ParseTree node);

    List<ParseTree> getAllPrecedingSiblingsOrSelf(List<ParseTree> matchedTreeNodes);

    List<ParseTree> getFollowingSiblingsOrSelf(ParseTree node);

    List<ParseTree> getAllFollowingSiblingsOrSelf(List<ParseTree> matchedTreeNodes);

    List<ParseTree> getPrecedingOrSelf(ParseTree node);

    List<ParseTree> getAllPrecedingOrSelf(List<ParseTree> matchedTreeNodes);

    List<ParseTree> getFollowingOrSelf(ParseTree node);

    List<ParseTree> getAllFollowingOrSelf(List<ParseTree> matchedTreeNodes);

    List<ParseTree> getAllFollowing(List<ParseTree> nodes);

    List<ParseTree> getFollowing(ParseTree node);

    List<ParseTree> getAllPreceding(List<ParseTree> nodes);

    List<ParseTree> getPreceding(ParseTree node);

    List<ParseTree> getAllFollowingSiblings(List<ParseTree> nodes);

    List<ParseTree> getFollowingSiblings(ParseTree node);

    List<ParseTree> getAllPrecedingSiblings(List<ParseTree> nodes);

    List<ParseTree> getPrecedingSiblings(ParseTree node);

    List<ParseTree> getAllDescendantsOrSelf(List<ParseTree> nodes);

    List<ParseTree> getDescendantsOrSelf(ParseTree node);

    List<ParseTree> getAllDescendants(List<ParseTree> nodes);

    List<ParseTree> getDescendants(ParseTree treenode);

    List<ParseTree> getChildren(ParseTree treenode);

    List<ParseTree> getAllChildren(List<ParseTree> nodes);

    List<ParseTree> getAllAncestors(List<ParseTree> nodes);

    List<ParseTree> getAncestors(ParseTree node);

    List<ParseTree> getAllParents(List<ParseTree> nodes);

    List<ParseTree> getAllAncestorsOrSelf(List<ParseTree> nodes);

}
