package ca.ualberta.cs.smr.refmerge.matrix.logicCells;

import ca.ualberta.cs.smr.refmerge.refactoringObjects.MoveRenameMethodObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RenameFieldObject;
import ca.ualberta.cs.smr.refmerge.utils.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

import static ca.ualberta.cs.smr.refmerge.utils.MatrixUtils.ifClassExtends;
import static ca.ualberta.cs.smr.refmerge.utils.MatrixUtils.isSameName;

public class RenameFieldRenameFieldCell {

    Project project;

    public RenameFieldRenameFieldCell(Project project) {
        this.project = project;
    }

    /*
     * Check if a ename field refactoring conflicts with a rename field refactoring on the other branch.
     * While a field cannot override another field, it can shadow it. Rename Field/Rename Field
     * can result in a naming or shadowing conflict.
     */
    public boolean renameFieldRenameFieldConflictCell(RefactoringObject dispatcherObject, RefactoringObject receiverObject) {
        RenameFieldRenameFieldCell cell = new RenameFieldRenameFieldCell(project);
        // Check for shadowing conflict
        if(cell.checkShadowConflict(dispatcherObject, receiverObject)) {
            System.out.println("Shadow Conflict");
            return true;
        }
        // Check for naming conflict
        else if(cell.checkFieldNamingConflict(dispatcherObject, receiverObject)) {
            System.out.println("Naming Conflict");
            return true;
        }
        return false;
    }

    public boolean checkShadowConflict(RefactoringObject dispatcherObject, RefactoringObject receiverObject) {
        RenameFieldObject dispatcherField = (RenameFieldObject)  dispatcherObject;
        RenameFieldObject receiverField = (RenameFieldObject) receiverObject;

        String newDispatcherClass = dispatcherField.getDestinationClass();
        String newReceiverClass = receiverField.getDestinationClass();

        // Cannot have shadow conflict in same class
        if(newDispatcherClass.equals(newReceiverClass)) {
            return false;
        }

        String dispatcherFile = dispatcherField.getDestinationFilePath();
        String receiverFile = receiverField.getDestinationFilePath();
        Utils utils = new Utils(project);
        PsiClass psiDispatcher = utils.getPsiClassByFilePath(dispatcherFile, newDispatcherClass);
        PsiClass psiReceiver = utils.getPsiClassByFilePath(receiverFile, newReceiverClass);
        if(psiReceiver != null && psiDispatcher != null) {
            // If there is no inheritance relationship, there is no shadow conflict
            if (!ifClassExtends(psiDispatcher, psiReceiver)) {
                return false;
            }
        }

        String originalDispatcherField = dispatcherField.getOriginalName();
        String originalReceiverField = receiverField.getOriginalName();
        String newDispatcherField = dispatcherField.getDestinationName();
        String newReceiverField = receiverField.getDestinationName();

        return !isSameName(originalDispatcherField, originalReceiverField) &&
                isSameName(newDispatcherField, newReceiverField);

    }

    public boolean checkFieldNamingConflict(RefactoringObject dispatcherObject, RefactoringObject receiverObject) {
        RenameFieldObject dispatcherField = (RenameFieldObject) dispatcherObject;
        RenameFieldObject receiverField = (RenameFieldObject) receiverObject;

        // Get the field names
        String originalDispatcherField = dispatcherField.getOriginalName();
        String originalReceiverField = receiverField.getOriginalName();
        String newDispatcherField = dispatcherField.getDestinationName();
        String newReceiverField = receiverField.getDestinationName();

        // Get the relevant class names
        String originalDispatcherClass = dispatcherField.getOriginalClass();
        String newDispatcherClass = dispatcherField.getDestinationClass();
        String originalReceiverClass = receiverField.getOriginalClass();
        String newReceiverClass = receiverField.getDestinationClass();

        // If both refactorings are rename field refactorings
        if(dispatcherField.isRename() && receiverField.isRename()) {
            // Check that the rename refactorings occur in the same class
            if(originalDispatcherClass.equals(originalReceiverClass)) {
                // If the fields have the same starting name but different refactored names, it is conflicting
                if(originalDispatcherField.equals(originalReceiverField) && !newDispatcherField.equals(newReceiverField)) {
                    return true;
                }
                // Otherwise, if two fields are renamed to the same name in the same class, it is conflicting
                else if(!originalDispatcherField.equals(originalReceiverField) && newDispatcherField.equals(newReceiverField)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkTransitivity(RefactoringObject firstRefactoring, RefactoringObject secondRefactoring) {
        RenameFieldObject firstObject = (RenameFieldObject) firstRefactoring;
        RenameFieldObject secondObject = (RenameFieldObject) secondRefactoring;

        // Get field information
        String newFirstName = firstObject.getDestinationName();
        String oldSecondName = secondObject.getOriginalName();
        String newSecondName = secondObject.getDestinationName();

        // Get class information
        String newFirstClass = firstObject.getDestinationClass();
        String oldSecondClass = secondObject.getOriginalClass();
        String newSecondClass = secondObject.getDestinationClass();

        // If c2 == c3 and f2 == f3 where c1.f1 -> c2.f2 and c3.f3 -> c4.f4, then they can be combined to c1.f1 -> c4.f4
        if(newFirstClass.equals(oldSecondClass) && newFirstName.equals(oldSecondName)) {
            firstRefactoring.setDestinationFilePath(secondObject.getDestinationFilePath());
            ((RenameFieldObject) firstRefactoring).setDestinationClassName(newSecondClass);
            ((RenameFieldObject) firstRefactoring).setDestinationFieldName(newSecondName);
            return true;
        }



        return false;
    }
}
