package ca.ualberta.cs.smr.core.matrix.receivers;

import ca.ualberta.cs.smr.core.dependenceGraph.DependenceGraph;
import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.dispatcher.ExtractMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameClassDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameMethodDispatcher;
import com.intellij.openapi.project.Project;

/*
 * The Receiver superclass contains each receive method that the receiver classes will need to use. Each time we
 * add a new refactoring type, we need to add a new receive method with the corresponding refactoring element. No
 * methods other than `receive()` should be added to this class.
 */

public class Receiver {
    Node receiverNode;
    DependenceGraph graph;
    Project project;

    public void set(Node receiverNode, DependenceGraph graph, Project project) {
        this.receiverNode = receiverNode;
        this.graph = graph;
        this.project = project;
    }

    /*
     * Any method that overrides this will dispatch to a logic cell containing a rename method refactoring.
     */
    public void receive(RenameMethodDispatcher dispatcher) {
        // This is empty because subclasses will override this to dispatch to the correct logic cell.
    }

    /*
     * Any method that overrides this will dispatch to a logic cell containing a rename class refactoring.
     */
    public void receive(RenameClassDispatcher dispatcher) {
        // This is empty because subclasses will override this to dispatch to the correct logic cell.
    }

    /*
     * Any method that overrides this will dispatch to a logic cell containing an extract method refactoring.
     */
    public void receive(ExtractMethodDispatcher dispatcher) {
        // This is empty because subclasses will override this to dispatch to the correct logic cell.
    }


}
