package com.github.akruk.nodegetter;

import org.antlr.v4.runtime.tree.ParseTree;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NodeStreamGetter {

    public Stream<ParseTree> getPrecedingSiblingsOrSelf(final ParseTree node) {
        return Stream.concat(Stream.of(node), getPrecedingSiblings(node));
    }

    public Stream<ParseTree> getAllPrecedingSiblingsOrSelf(final Stream<ParseTree> matchedTreeNodes) {
        return matchedTreeNodes.flatMap(this::getPrecedingSiblingsOrSelf);
    }

    public Stream<ParseTree> getFollowingSiblingsOrSelf(final ParseTree node) {
        return Stream.concat(Stream.of(node), getFollowingSiblings(node));
    }

    public Stream<ParseTree> getAllFollowingSiblingsOrSelf(final Stream<ParseTree> matchedTreeNodes) {
        return matchedTreeNodes.flatMap(this::getFollowingSiblingsOrSelf);
    }

    public Stream<ParseTree> getPrecedingOrSelf(final ParseTree node) {
        return Stream.concat(Stream.of(node), getPreceding(node));
    }

    public Stream<ParseTree> getAllPrecedingOrSelf(final Stream<ParseTree> matchedTreeNodes) {
        return matchedTreeNodes.flatMap(this::getPrecedingOrSelf);
    }

    public Stream<ParseTree> getFollowingOrSelf(final ParseTree node) {
        return Stream.concat(Stream.of(node), getFollowing(node));
    }

    public Stream<ParseTree> getAllFollowingOrSelf(final Stream<ParseTree> matchedTreeNodes) {
        return matchedTreeNodes.flatMap(this::getFollowingOrSelf);
    }

    public Stream<ParseTree> getAllFollowing(final Stream<ParseTree> nodes) {
        return nodes.flatMap(this::getFollowing);
    }

    public Stream<ParseTree> getFollowing(final ParseTree node) {
        Stream<ParseTree> ancestors = getAncestors(node);
        Stream<ParseTree> ancestorFollowingSiblings = getAllFollowingSiblings(ancestors);
        Stream<ParseTree> followingSiblingDescendants = getAllDescendants(ancestorFollowingSiblings);
        Stream<ParseTree> thisNodeDescendants = getDescendants(node);
        Stream<ParseTree> thisNodeFollowingSiblingDescendants = getAllDescendantsOrSelf(getFollowingSiblings(node));

        return Stream.of(
                ancestorFollowingSiblings,
                followingSiblingDescendants,
                thisNodeDescendants,
                thisNodeFollowingSiblingDescendants
        ).flatMap(s -> s);
    }

    public Stream<ParseTree> getAllPreceding(final Stream<ParseTree> nodes) {
        return nodes.flatMap(this::getPreceding);
    }

    public Stream<ParseTree> getPreceding(final ParseTree node) {
        Stream<ParseTree> ancestors = getAncestors(node);
        Stream<ParseTree> ancestorPrecedingSiblings = getAllPrecedingSiblings(ancestors);
        Stream<ParseTree> precedingSiblingDescendants = getAllDescendantsOrSelf(ancestorPrecedingSiblings);
        Stream<ParseTree> thisNodePrecedingSiblings = getPrecedingSiblings(node);
        Stream<ParseTree> thisNodePrecedingSiblingDescendants = getAllDescendantsOrSelf(thisNodePrecedingSiblings);

        return Stream.of(
                ancestors,
                precedingSiblingDescendants,
                thisNodePrecedingSiblingDescendants
        ).flatMap(s -> s);
    }

    public Stream<ParseTree> getAllFollowingSiblings(final Stream<ParseTree> nodes) {
        return nodes.flatMap(this::getFollowingSiblings);
    }

    public Stream<ParseTree> getFollowingSiblings(final ParseTree node) {
        final var parent = node.getParent();
        if (parent == null) return Stream.empty();
        final var parentsChildren = getChildren(parent).toList();
        final var nodeIndex = parentsChildren.indexOf(node);
        return parentsChildren.subList(nodeIndex + 1, parentsChildren.size()).stream();
    }

    public Stream<ParseTree> getAllPrecedingSiblings(final Stream<ParseTree> nodes) {
        return nodes.flatMap(this::getPrecedingSiblings);
    }

    public Stream<ParseTree> getPrecedingSiblings(final ParseTree node) {
        final var parent = node.getParent();
        if (parent == null) return Stream.empty();
        final var parentsChildren = getChildren(parent).toList();
        final var nodeIndex = parentsChildren.indexOf(node);
        return parentsChildren.subList(0, nodeIndex).stream();
    }

    public Stream<ParseTree> getAllDescendantsOrSelf(final Stream<ParseTree> nodes) {
        return nodes.flatMap(this::getDescendantsOrSelf);
    }

    public Stream<ParseTree> getDescendantsOrSelf(final ParseTree node) {
        return Stream.concat(Stream.of(node), getDescendants(node));
    }

    public Stream<ParseTree> getAllDescendants(final Stream<ParseTree> nodes) {
        return nodes.flatMap(this::getDescendants);
    }

    public Stream<ParseTree> getDescendants(final ParseTree treenode) {
        return getChildren(treenode).flatMap(child ->
                Stream.concat(Stream.of(child), getDescendants(child))
        );
    }

    public Stream<ParseTree> getChildren(final ParseTree treenode) {
        return IntStream.range(0, treenode.getChildCount())
                .mapToObj(treenode::getChild);
    }

    public Stream<ParseTree> getAllChildren(final Stream<ParseTree> nodes) {
        return nodes.flatMap(this::getChildren);
    }

    public Stream<ParseTree> getAllAncestors(final Stream<ParseTree> nodes) {
        return nodes.flatMap(this::getAncestors);
    }

    public Stream<ParseTree> getAncestors(final ParseTree node) {
        Stream.Builder<ParseTree> builder = Stream.builder();
        ParseTree parent = node.getParent();
        while (parent != null) {
            builder.add(parent);
            parent = parent.getParent();
        }
        return builder.build();
    }

    public Stream<ParseTree> getAllParents(final Stream<ParseTree> nodes) {
        return nodes.map(ParseTree::getParent).filter(p -> p != null);
    }

    public Stream<ParseTree> getAllAncestorsOrSelf(final Stream<ParseTree> nodes) {
        return Stream.concat(getAllAncestors(nodes), nodes);
    }
}
