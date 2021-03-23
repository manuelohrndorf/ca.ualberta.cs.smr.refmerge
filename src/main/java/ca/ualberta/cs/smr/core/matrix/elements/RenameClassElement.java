package ca.ualberta.cs.smr.core.matrix.elements;


import ca.ualberta.cs.smr.core.dependenceGraph.Node;
import ca.ualberta.cs.smr.core.matrix.logicCells.RenameClassRenameClassCell;
import ca.ualberta.cs.smr.core.matrix.logicCells.RenameClassRenameMethodCell;
import ca.ualberta.cs.smr.core.matrix.visitors.Visitor;
import com.intellij.openapi.project.Project;




public class RenameClassElement extends RefactoringElement {
    Project project;
    Node elementNode;

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

    public void set(Node elementNode, Project project) {
        this.elementNode = elementNode;
        this.project = project;
    }

    /*
     * Check if rename class conflicts with rename class
     */
    public boolean checkRenameClassConflict(Node visitorNode) {
        // Check class naming conflict
        if(RenameClassRenameClassCell.checkClassNamingConflict(elementNode, visitorNode)) {
            System.out.println("Naming conflict");
            return true;
        }
        return false;
    }

    /*
     * Check if a rename class refactoring depends on a rename method refactoring to be performed first.
     */
    public Node checkRenameMethodDependence(Node visitorNode) {
        if(RenameClassRenameMethodCell.checkRenameMethodRenameClassDependence(elementNode, visitorNode)) {
            return elementNode;
        }
        return null;
    }

}
