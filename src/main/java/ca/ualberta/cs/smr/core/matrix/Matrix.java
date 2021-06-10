package ca.ualberta.cs.smr.core.matrix;

import ca.ualberta.cs.smr.core.matrix.dispatcher.ExtractMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RefactoringDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.RenameClassDispatcher;
import ca.ualberta.cs.smr.core.matrix.dispatcher.MoveRenameMethodDispatcher;
import ca.ualberta.cs.smr.core.matrix.receivers.ExtractMethodReceiver;
import ca.ualberta.cs.smr.core.matrix.receivers.Receiver;
import ca.ualberta.cs.smr.core.matrix.receivers.RenameClassReceiver;
import ca.ualberta.cs.smr.core.matrix.receivers.MoveRenameMethodReceiver;
import ca.ualberta.cs.smr.core.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.utils.RefactoringObjectUtils;
import com.intellij.openapi.project.Project;
import org.refactoringminer.api.RefactoringType;

import java.util.*;

/*
 * Creates the dependence graph for the refactoring lists and dispatches to the corresponding logic cell for each pair
 * of refactorings.
 */

public class Matrix {
    final Project project;

    /*
     * The dispatcherMap uses the refactoring type to create the corresponding dispatcher class. This needs to be updated
     * each time a new refactoring is added.
     */
    static final HashMap<RefactoringType, RefactoringDispatcher> dispatcherMap =
                                                    new HashMap<RefactoringType, RefactoringDispatcher>() {{
       put(RefactoringType.RENAME_METHOD, new MoveRenameMethodDispatcher());
       put(RefactoringType.RENAME_CLASS, new RenameClassDispatcher());
       put(RefactoringType.EXTRACT_OPERATION, new ExtractMethodDispatcher());
    }};

    /*
     * The receiverMap uses the refactoring type to create the corresponding receiver class. This needs to be updated
     * each time a new refactoring is added.
     */
    static final HashMap<RefactoringType, Receiver> receiverMap =
                                                    new HashMap<RefactoringType, Receiver>() {{
        put(RefactoringType.RENAME_METHOD, new MoveRenameMethodReceiver());
        put(RefactoringType.RENAME_CLASS, new RenameClassReceiver());
        put(RefactoringType.EXTRACT_OPERATION, new ExtractMethodReceiver());
    }};

    public Matrix(Project project) {
        this.project = project;
    }

    /*
     * Iterate through each of the left refactorings to compare against the right refactorings.
     */
    public ArrayList<RefactoringObject> runMatrix(ArrayList<RefactoringObject> leftRefactoringList,
                                             ArrayList<RefactoringObject> rightRefactoringList) {
        if(leftRefactoringList.isEmpty() && rightRefactoringList.isEmpty()) {
            return new ArrayList<>();
        }
        // If the right refactoring list is empty, return the left refactoring list
        else if(!leftRefactoringList.isEmpty() && rightRefactoringList.isEmpty()) {
            return leftRefactoringList;
        }
        // If the left refactoring list is empty, return the right refactoring list
        else if(leftRefactoringList.isEmpty()) {
            return rightRefactoringList;
        }
        for(RefactoringObject leftRefactoring : leftRefactoringList) {
            compareRefactorings(leftRefactoring, rightRefactoringList);
        }
        // Insert each refactoring in the left refactoring list into the right to combine them
        for(RefactoringObject leftRefactoring : leftRefactoringList) {
            RefactoringObjectUtils.insertRefactoringObject(leftRefactoring, rightRefactoringList);
        }
        return rightRefactoringList;
    }

    /*
     * This calls dispatch for each pair of refactorings to check for conflicts.
     */
    void compareRefactorings(RefactoringObject leftRefactoring, List<RefactoringObject> rightRefactoringList) {
        for(RefactoringObject rightRefactoring : rightRefactoringList) {
            dispatch(leftRefactoring, rightRefactoring);
        }
    }

    /*
     * Perform double dispatch to check if the two refactoring elements conflict.
     */
    void dispatch(RefactoringObject leftRefactoring, RefactoringObject rightRefactoring) {
        // Get the refactoring types so we can create the corresponding dispatcher and receiver
        int leftValue = getRefactoringValue(leftRefactoring.getRefactoringType());
        int rightValue = getRefactoringValue(rightRefactoring.getRefactoringType());

        RefactoringDispatcher dispatcher;
        Receiver receiver;
        if(leftValue > rightValue) {
            dispatcher = makeDispatcher(rightRefactoring, false);
            receiver = makeReceiver(leftRefactoring);
        }
        else {
            dispatcher = makeDispatcher(leftRefactoring, false);
            receiver = makeReceiver(rightRefactoring);
        }
        dispatcher.dispatch(receiver);
    }

    /*
     * Simplify refactorings based on the new refactoring and if the new refactoring is not a transitive refactoring,
     * insert it to the list.
     */
    public void simplifyAndInsertRefactorings(RefactoringObject newRefactoring, ArrayList<RefactoringObject> simplifiedRefactorings) {
        int transitiveCount = 0;
        for(RefactoringObject simplifiedRefactoring : simplifiedRefactorings) {
            boolean isTransitive = simplifyRefactorings(newRefactoring, simplifiedRefactoring);
            // Keep track of how many refactorings are transitive
            if(isTransitive) {
                transitiveCount++;
            }
        }

        // If the refactoring is not transitive, add it to the simplified refactoring list
        if(transitiveCount == 0) {
            RefactoringObjectUtils.insertRefactoringObject(newRefactoring, simplifiedRefactorings);
        }
    }

    /*
     * Simplify the refactorings that have been detected and combine transitive refactorings. Return true if the
     * refactoring operations are transitive.
     */
    private boolean simplifyRefactorings(RefactoringObject newRefactoring, RefactoringObject previousRefactoring) {
        int leftValue = getRefactoringValue(newRefactoring.getRefactoringType());
        int rightValue = getRefactoringValue(previousRefactoring.getRefactoringType());
        RefactoringDispatcher dispatcher;
        Receiver receiver;
        if(leftValue > rightValue) {
            dispatcher = makeDispatcher(previousRefactoring, true);
            receiver = makeReceiver(newRefactoring);
        }
        else {
            dispatcher = makeDispatcher(newRefactoring, true);
            receiver = makeReceiver(previousRefactoring);
        }
        dispatcher.dispatch(receiver);
        return receiver.hasTransitivity();
    }


    /*
     * Use the refactoring type to get the refactoring dispatcher class from the dispatcherMap.
     * Set the refactoring field in the dispatcher.
     */
    RefactoringDispatcher makeDispatcher(RefactoringObject refactoringObject, boolean simplify) {
        RefactoringType type = refactoringObject.getRefactoringType();
        RefactoringDispatcher dispatcher = dispatcherMap.get(type);
        dispatcher.set(refactoringObject, project, simplify);
        return dispatcher;
    }

    /*
     * Use the refactoring type to get the refactoring receiver class from the receiverMap.
     * Set the refactoring field in the receiver.
     * Set the node field in the receiver and get an instance of the graph so we can update it.
     */
    Receiver makeReceiver(RefactoringObject refactoringObject) {
        RefactoringType type = refactoringObject.getRefactoringType();
        Receiver receiver = receiverMap.get(type);
        receiver.set(refactoringObject);
        return receiver;
    }


    /*
     * Get the ordered refactoring value of the refactoring type. We need to update this method each time we add a new
     * refactoring type.
     */
    protected int getRefactoringValue(RefactoringType refactoringType) {
        Vector<RefactoringType> vector = new Vector<>();
        vector.add(RefactoringType.RENAME_METHOD);
        vector.add(RefactoringType.RENAME_CLASS);
        vector.add(RefactoringType.EXTRACT_OPERATION);

        Enumeration<RefactoringType> enumeration = vector.elements();
        int value = 0;
        while(enumeration.hasMoreElements()) {
            value++;
            if(refactoringType.equals(enumeration.nextElement())) {
                return value;
            }
        }
        return -1;
    }

}
