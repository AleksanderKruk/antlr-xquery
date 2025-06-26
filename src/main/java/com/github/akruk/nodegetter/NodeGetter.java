package com.github.akruk.nodegetter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.antlr.v4.runtime.tree.ParseTree;


public class NodeGetter implements INodeGetter {

    @Override
    public List<ParseTree> getPrecedingSiblingsOrSelf(final ParseTree node) {
        final var newMatched = new ArrayList<ParseTree>();
        final var preceding = getPrecedingSiblings(node);
        newMatched.add(node);
        newMatched.addAll(preceding);
        return newMatched;
    }


    @Override
    public List<ParseTree> getAllPrecedingSiblingsOrSelf(final List<ParseTree> matchedTreeNodes) {
        final var result = new ArrayList<ParseTree>();
        for (final var node : matchedTreeNodes) {
            final var followingSiblings = getPrecedingSiblingsOrSelf(node);
            result.addAll(followingSiblings);
        }
        return result;
    }


    @Override
    public List<ParseTree> getFollowingSiblingsOrSelf(final ParseTree node) {
        final var newMatched = new ArrayList<ParseTree>();
        final var following = getFollowingSiblings(node);
        newMatched.add(node);
        newMatched.addAll(following);
        return newMatched;
    }


    @Override
    public List<ParseTree> getAllFollowingSiblingsOrSelf(final List<ParseTree> matchedTreeNodes) {
        final var result = new ArrayList<ParseTree>();
        for (final var node : matchedTreeNodes) {
            final var followingSiblings = getFollowingSiblingsOrSelf(node);
            result.addAll(followingSiblings);
        }
        return result;
    }

    @Override
    public List<ParseTree> getPrecedingOrSelf(final ParseTree node) {
        final var newMatched = new ArrayList<ParseTree>();
        final var following = getPreceding(node);
        newMatched.add(node);
        newMatched.addAll(following);
        return newMatched;
    }


    @Override
    public List<ParseTree> getAllPrecedingOrSelf(final List<ParseTree> matchedTreeNodes) {
        final var result = new ArrayList<ParseTree>();
        for (final var node : matchedTreeNodes) {
            final var followingSiblings = getPrecedingOrSelf(node);
            result.addAll(followingSiblings);
        }
        return result;
    }


    @Override
    public List<ParseTree> getFollowingOrSelf(final ParseTree node) {
        final var newMatched = new ArrayList<ParseTree>();
        final var following = getFollowing(node);
        newMatched.add(node);
        newMatched.addAll(following);
        return newMatched;
    }


    @Override
    public List<ParseTree> getAllFollowingOrSelf(final List<ParseTree> matchedTreeNodes) {
        final var result = new ArrayList<ParseTree>();
        for (final var node : matchedTreeNodes) {
            final var followingSiblings = getFollowingOrSelf(node);
            result.addAll(followingSiblings);
        }
        return result;
    }

    @Override
    public List<ParseTree> getAllFollowing(final List<ParseTree> nodes) {
        final var result = new ArrayList<ParseTree>();
        for (final var node : nodes) {
            final var followingSiblings = getFollowing(node);
            result.addAll(followingSiblings);
        }
        return result;
    }

    @Override
    public List<ParseTree> getFollowing(final ParseTree node) {
        final List<ParseTree> ancestors = getAncestors(node);
        final List<ParseTree> ancestorFollowingSiblings = getAllFollowingSiblings(ancestors);
        final List<ParseTree> followingSiblingDescendants =  getAllDescendants(ancestorFollowingSiblings);
        final List<ParseTree> thisNodeDescendants = getDescendants(node);
        final List<ParseTree> thisNodefollowingSiblings = getFollowingSiblings(node);
        final List<ParseTree> thisNodeFollowingSiblingDescendants = getAllDescendantsOrSelf(thisNodefollowingSiblings);
        final List<ParseTree> following = new ArrayList<>(ancestorFollowingSiblings.size()
                                                    + followingSiblingDescendants.size()
                                                    + followingSiblingDescendants.size()
                                                    + thisNodeDescendants.size()
                                                    + thisNodeFollowingSiblingDescendants.size());
        following.addAll(ancestorFollowingSiblings);
        following.addAll(followingSiblingDescendants);
        following.addAll(thisNodeDescendants);
        following.addAll(thisNodeFollowingSiblingDescendants);
        return following;
    }


    @Override
    public List<ParseTree> getAllPreceding(final List<ParseTree> nodes) {
        final var result = new ArrayList<ParseTree>();
        for (final var node : nodes) {
            final var precedingSiblings = getPreceding(node);
            result.addAll(precedingSiblings);
        }
        return result;
    }


    @Override
    public List<ParseTree> getPreceding(final ParseTree node) {
        final List<ParseTree> ancestors = getAncestors(node);
        final List<ParseTree> ancestorPrecedingSiblings = getAllPrecedingSiblings(ancestors);
        final List<ParseTree> precedingSiblingDescendants =  getAllDescendantsOrSelf(ancestorPrecedingSiblings);
        final List<ParseTree> thisNodePrecedingSiblings = getPrecedingSiblings(node);
        final List<ParseTree> thisNodePrecedingSiblingDescendants = getAllDescendantsOrSelf(thisNodePrecedingSiblings);
        final List<ParseTree> following = new ArrayList<>(ancestors.size()
                                                    + precedingSiblingDescendants.size()
                                                    + thisNodePrecedingSiblingDescendants.size());
        following.addAll(ancestors);
        following.addAll(precedingSiblingDescendants);
        following.addAll(thisNodePrecedingSiblingDescendants);
        return following;
    }

    @Override
    public List<ParseTree> getAllFollowingSiblings(final List<ParseTree> nodes) {
        final var result = new ArrayList<ParseTree>();
        for (final var node : nodes) {
            final var followingSiblings = getFollowingSiblings(node);
            result.addAll(followingSiblings);
        }
        return result;
    }

    @Override
    public List<ParseTree> getFollowingSiblings(final ParseTree node) {
        final var parent = node.getParent();
        if (parent == null)
            return List.of();
        final var parentsChildren = getChildren(parent);
        final var nodeIndex = parentsChildren.indexOf(node);
        final var followingSibling = parentsChildren.subList(nodeIndex+1, parentsChildren.size());
        return followingSibling;
    }



    @Override
    public List<ParseTree> getAllPrecedingSiblings(final List<ParseTree> nodes) {
        final var result = new ArrayList<ParseTree>();
        for (final var node : nodes) {
            final var precedingSiblings = getPrecedingSiblings(node);
            result.addAll(precedingSiblings);
        }
        return result;
    }


    @Override
    public List<ParseTree> getPrecedingSiblings(final ParseTree node) {
        final var parent = node.getParent();
        if (parent == null)
            return List.of();
        final var parentsChildren = getChildren(parent);
        final var nodeIndex = parentsChildren.indexOf(node);
        final var precedingSibling = parentsChildren.subList(0, nodeIndex);
        return precedingSibling;
    }


    @Override
    public List<ParseTree> getAllDescendantsOrSelf(final List<ParseTree> nodes) {
        final var newMatched = new ArrayList<ParseTree>();
        for (final var node :nodes) {
            final var descendants = getDescendantsOrSelf(node);
            newMatched.addAll(descendants);
        }
        return newMatched;
    }


    @Override
    public List<ParseTree> getDescendantsOrSelf(final ParseTree node) {
        final var newMatched = new ArrayList<ParseTree>();
        final var descendants = getDescendants(node);
        newMatched.add(node);
        newMatched.addAll(descendants);

        return newMatched;
    }

    @Override
    public List<ParseTree> getAllDescendants(final List<ParseTree> nodes) {
        final var allDescendants = new ArrayList<ParseTree>();
        for (final var node : nodes) {
            final var descendants = getDescendants(node);
            allDescendants.addAll(descendants);
        }
        return allDescendants;
    }


    @Override
    public List<ParseTree> getDescendants(final ParseTree treenode) {
        final List<ParseTree> allDescendants = new ArrayList<>();
        final List<ParseTree> children = getChildren(treenode);
        while (children.size() != 0) {
            final var child = children.removeFirst();
            allDescendants.add(child);
            final var descendants = getChildren(child);
            for (final ParseTree descendantTree : descendants.reversed()) {
                children.addFirst(descendantTree);
            }
        }
        return allDescendants;
    }


    @Override
    public List<ParseTree> getChildren(final ParseTree treenode) {
        final List<ParseTree> children = IntStream.range(0, treenode.getChildCount())
            .mapToObj(i->treenode.getChild(i))
            .collect(Collectors.toList());
        return children;
    }


    @Override
    public List<ParseTree> getAllChildren(final List<ParseTree> nodes) {
        final var newMatched = new ArrayList<ParseTree>();
        for (final var node : nodes) {
            final var children = getChildren(node);
            newMatched.addAll(children);
        }
        return newMatched;
    }


    @Override
    public List<ParseTree> getAllAncestors(final List<ParseTree> nodes) {
        final var newMatched = new ArrayList<ParseTree>();
        for (final var valueNode : nodes) {
            final var ancestors = getAncestors(valueNode);
            newMatched.addAll(ancestors);
        }
        return newMatched.reversed();
    }

    @Override
    public List<ParseTree> getAncestors(final ParseTree node) {
        final List<ParseTree> newMatched = new ArrayList<ParseTree>();
        ParseTree parent = node.getParent();
        while (parent != null) {
            newMatched.add(parent);
            parent = parent.getParent();
        }
        return newMatched.reversed();
    }

    @Override
    public List<ParseTree> getAllParents(final List<ParseTree> nodes) {
        final List<ParseTree> newMatched = nodes.stream()
            .map(ParseTree::getParent)
            .toList();
        return newMatched;
    }

    @Override
    public List<ParseTree> getAllAncestorsOrSelf(final List<ParseTree> nodes) {
        // TODO: Correct sequence
        final var newMatched = new ArrayList<ParseTree>();
        final var ancestorPart = getAllAncestors(nodes);
        newMatched.addAll(ancestorPart);
        newMatched.addAll(nodes);
        return newMatched;
    }




}
