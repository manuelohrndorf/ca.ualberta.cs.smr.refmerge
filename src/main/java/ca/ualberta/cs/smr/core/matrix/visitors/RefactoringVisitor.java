package ca.ualberta.cs.smr.core.matrix.visitors;

import ca.ualberta.cs.smr.core.dependenceGraph.Graph;
import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import org.refactoringminer.api.Refactoring;

public abstract class RefactoringVisitor implements Visitor {
    Node visitorNode;
    Refactoring visitorRef;
    Graph graph;

    public void set(Node visitorNode, Graph graph) {
        this.visitorNode = visitorNode;
        this.visitorRef = visitorNode.getRefactoring();
        this.graph = graph;
    }

}
