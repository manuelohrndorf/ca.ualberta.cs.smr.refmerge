package ca.ualberta.cs.smr.core.matrix.elements;

import ca.ualberta.cs.smr.testUtils.GetDataForTests;
import ca.ualberta.cs.smr.core.matrix.visitors.RenameMethodVisitor;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.mockito.Mockito;
import org.refactoringminer.api.Refactoring;


import java.util.List;

import static org.mockito.Mockito.times;

public class RenameClassElementTest extends LightJavaCodeInsightFixtureTestCase {

    public void testAccept() {
        RenameClassElement element = Mockito.mock(RenameClassElement.class);
        RenameMethodVisitor visitor = new RenameMethodVisitor();
        element.accept(visitor);
        Mockito.verify(element, times(1)).accept(visitor);
    }

    public void testSet() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameClassRenameClassFiles/renameClassNamingConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameClassRenameClassFiles/renameClassNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null;
        Refactoring ref = refactorings.get(0);
        RenameClassElement element = new RenameClassElement();
        element.set(ref, project);
        Assert.assertNotNull("The refactoring element should not be null", element.elementRef);
    }

    public void testCheckRenameClassConflict() {
        Project project = myFixture.getProject();
        String basePath = System.getProperty("user.dir");
        String originalPath = basePath + "/src/test/resources/renameClassRenameClassFiles/renameClassNamingConflict/original";
        String refactoredPath = basePath + "/src/test/resources/renameClassRenameClassFiles/renameClassNamingConflict/refactored";
        List<Refactoring> refactorings = GetDataForTests.getRefactorings("RENAME_CLASS", originalPath, refactoredPath);
        assert refactorings != null && refactorings.size() == 3;
        Refactoring foo = refactorings.get(0);
        Refactoring foo2 = refactorings.get(1);
        Refactoring bar = refactorings.get(2);
        RenameClassElement renameClassElement = new RenameClassElement();
        renameClassElement.set(foo, project);
        boolean isConflicting = renameClassElement.checkRenameClassConflict(foo2);
        Assert.assertTrue(isConflicting);
        isConflicting = renameClassElement.checkRenameClassConflict(bar);
        Assert.assertFalse(isConflicting);
    }
}
