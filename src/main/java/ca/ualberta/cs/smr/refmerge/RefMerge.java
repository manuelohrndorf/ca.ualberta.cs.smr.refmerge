package ca.ualberta.cs.smr.refmerge;

import ca.ualberta.cs.smr.refmerge.matrix.Matrix;
import ca.ualberta.cs.smr.refmerge.replayOperations.*;
import ca.ualberta.cs.smr.refmerge.invertOperations.*;
import ca.ualberta.cs.smr.refmerge.utils.RefactoringObjectUtils;
import ca.ualberta.cs.smr.refmerge.refactoringObjects.RefactoringObject;
import ca.ualberta.cs.smr.refmerge.utils.Utils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.DumbService;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.eclipse.jgit.api.Git;
import ca.ualberta.cs.smr.refmerge.utils.GitUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;


public class RefMerge extends AnAction {

    Git git;
    Project project;



    @Override
    public void update(@NotNull AnActionEvent e) {
        // Using the event, evaluate the context, and enable or disable the action.
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
        List<GitRepository> repos = repoManager.getRepositories();
        GitRepository repo = repos.get(0);

        String leftCommit = System.getenv("LEFT_COMMIT");
        String rightCommit = System.getenv("RIGHT_COMMIT");

        List<Refactoring> detectedRefactorings = new ArrayList<>();
        refMerge(rightCommit, leftCommit, project, repo, detectedRefactorings);

    }

    /*
     * Gets the directory of the project that's being merged, then it calls the function that performs the merge.
     */
    public ArrayList<Pair<RefactoringObject, RefactoringObject>> refMerge(String rightCommit, String leftCommit,
                                                                          Project project, GitRepository repo,
                                                                          List<Refactoring> detectedRefactorings) {
        this.project = project;
        File dir = new File(Objects.requireNonNull(project.getBasePath()));
        try {
            git = Git.open(dir);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return doMerge(rightCommit, leftCommit, repo, detectedRefactorings);

    }

    /*
     * This method gets the refactorings that are between the base commit and the left and right commits. It uses the
     * matrix to determine if any of the refactorings are conflicting or have ordering dependencies.
     * Then it checks out the base commit, saving it in a temporary directory. It checks out the right commit, undoes
     * the refactorings, and saves the content into a respective temporary directory. It does the same thing for the
     * left commit, but it uses the current directory instead of saving it to a new one. After it's undone all the
     * refactorings, the merge function is called and it replays the refactorings.
     */
    private ArrayList<Pair<RefactoringObject, RefactoringObject>> doMerge(String rightCommit, String leftCommit,
                                                                          GitRepository repo,
                                                                          List<Refactoring> detectedRefactorings){
        long time = System.currentTimeMillis();
        GitUtils gitUtils = new GitUtils(repo, project);
        String baseCommit = gitUtils.getBaseCommit(leftCommit, rightCommit);
        System.out.println("Detecting refactorings");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AtomicReference<ArrayList<RefactoringObject>> rightRefsAtomic = new AtomicReference<>(new ArrayList<>());
        AtomicReference<ArrayList<RefactoringObject>> leftRefsAtomic = new AtomicReference<>(new ArrayList<>());
        Future futureRefMiner = executor.submit(() -> {
            rightRefsAtomic.set(detectAndSimplifyRefactorings(rightCommit, baseCommit, detectedRefactorings));
            leftRefsAtomic.set(detectAndSimplifyRefactorings(leftCommit, baseCommit, detectedRefactorings));
        });
        try {
            futureRefMiner.get(11, TimeUnit.MINUTES);


        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            System.out.println("RefMerge Timed Out");
            return null;
        }
        ArrayList<RefactoringObject> rightRefs = rightRefsAtomic.get();
        ArrayList<RefactoringObject> leftRefs = leftRefsAtomic.get();

        long time2 = System.currentTimeMillis();
        // If it has been 10 minutes, it will take more than 15 minutes to complete RefMerge
        if((time - time2) > 600000) {
            System.out.println("RefMerge Timed Out");
            return null;
        }


        gitUtils.checkout(rightCommit);
        // Update the PSI classes after the commit
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);
        System.out.println("Inverting right refactorings");
        // InvertRefactorings.invertRefactorings(rightRefs, project);
        try {
            Utils.runWhenSmartWithFuture(project, () -> InvertRefactorings.invertRefactorings(rightRefs, project)).get(); // Blocks until the task is complete
            System.out.println("Task completed successfully: Inverting right refactorings");
        } catch (Exception e) {
            System.err.println("Error while waiting for the task: Inverting right refactorings: " + e.getMessage());
        }

        String rightUndoCommit = gitUtils.addAndCommit();
        gitUtils.checkout(leftCommit);
        // Update the PSI classes after the commit
        Utils.reparsePsiFiles(project);
        Utils.dumbServiceHandler(project);
        System.out.println("Inverting left refactorings");
        // InvertRefactorings.invertRefactorings(leftRefs, project);
        try {
            Utils.runWhenSmartWithFuture(project, () -> InvertRefactorings.invertRefactorings(leftRefs, project)).get(); // Blocks until the task is complete
            System.out.println("Task completed successfully: Inverting left refactorings");
        } catch (Exception e) {
            System.err.println("Error while waiting for the task: Inverting left refactorings: " + e.getMessage());
        }

        gitUtils.addAndCommit();
        boolean isConflicting = gitUtils.merge(rightUndoCommit);

        Utils.runWhenSmartWithFuture(project, () -> {
                    Utils.refreshVFS();
                    Utils.reparsePsiFiles(project);
                    Utils.dumbServiceHandler(project);
                });

        // Check if any of the refactorings are conflicting or have ordering dependencies
        System.out.println("Detecting refactoring conflicts");
        Matrix matrix = new Matrix(project);

        Pair<ArrayList<Pair<RefactoringObject, RefactoringObject>>, ArrayList<RefactoringObject>> pair = matrix.detectConflicts(leftRefs, rightRefs);

        time2 = System.currentTimeMillis();
        // If it has been 14 minutes, it will take more than 15 minutes to complete RefMerge
        if((time - time2) > 780000) {
            System.out.println("RefMerge Timed Out");
            return null;
        }

        ArrayList<RefactoringObject> refactorings = pair.getRight();
        if(isConflicting) {
            List<String> conflictingFilePaths = gitUtils.getConflictingFilePaths();
            for(String conflictingFilePath : conflictingFilePaths) {
                try {
                    // FIXME: Deadlock!?
                    //Utils.runWhenSmartWithFuture(project, () -> {
                        Utils utils = new Utils(project);
                        String absoluteConflictingFilePath = project.getBasePath() + "/" + conflictingFilePath;
                        utils.removeRefactoringsInConflictingFile(conflictingFilePath, absoluteConflictingFilePath, refactorings);
                    //}).get(); // Blocks until the task is complete
                    System.out.println("Task completed successfully.");
                } catch (Exception e) {
                    System.err.println("Error while waiting for the task: " + e.getMessage());
                }
            }
        }

        // Combine the lists so we can perform all the refactorings on the merged project
        // Replay all of the refactorings
        System.out.println("Replaying refactorings");
        // ReplayRefactorings.replayRefactorings(pair.getRight(), project);
        try {
            Utils.runWhenSmartWithFuture(project, () -> ReplayRefactorings.replayRefactorings(pair.getRight(), project)).get(); // Blocks until the task is complete
            System.out.println("Task completed successfully: Replaying refactorings");
        } catch (Exception e) {
            System.err.println("Error while waiting for the task: Replaying refactorings: " + e.getMessage());
        }

        return pair.getLeft();

    }

    /*
     * Use RefMiner to detect refactorings in commits between the base commit and the parent commit. Compare each newly
     * detected refactoring against previously detected refactorings to check for transitivity or if the refactorings can
     * be simplified.
     */
    public ArrayList<RefactoringObject> detectAndSimplifyRefactorings(String commit, String base, List<Refactoring> detectedRefactorings) {
        ArrayList<RefactoringObject> simplifiedRefactorings = new ArrayList<>();
        Matrix matrix = new Matrix(project);
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
        try {
            miner.detectBetweenCommits(git.getRepository(), base, commit,
                new RefactoringHandler() {
                    @Override
                    public void handle(String commitId, List<Refactoring> refactorings) {
                        // Add each refactoring to refResult
                        for(Refactoring refactoring : refactorings) {
                            detectedRefactorings.add(refactoring);
                            // Create the refactoring object so we can compare and update
                            RefactoringObject refactoringObject = RefactoringObjectUtils.createRefactoringObject(refactoring);
                            // If the refactoring type is not presently supported, skip it
                            if(refactoringObject == null) {
                                continue;
                            }
                            // simplify refactorings and check if factoring is transitive
                            matrix.simplifyAndInsertRefactorings(refactoringObject, simplifiedRefactorings);
                        }
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return simplifiedRefactorings;
    }

}