package schedulingAlgorithm.implementation;

import java.util.*;
import WorkLoad.*;
import WorkLoadSet.*;
import RTSimulator.Definition;
import schedulingAlgorithm.SingleCoreSchedulingAlgorithm;

/**
 * Optimal Priority Assignment (OPA) for single-core systems.
 *
 * <p>
 * OPA uses Audsley's algorithm to assign optimal fixed priorities, ensuring that
 * all tasks are schedulable on a single-core system. The algorithm assigns tasks
 * starting with the lowest priority and proceeds until an optimal allocation is
 * achieved. This implementation performs response-time analysis and prints
 * diagnostic messages during the assignment process.
 * </p>
 *
 * @author ShiuJia
 */
public class OPA extends SingleCoreSchedulingAlgorithm {

    /**
     * Constructs a new OPA scheduler instance.
     *
     * <p>
     * Initializes the algorithm name to a human-readable label and sets the
     * priority type to fixed because OPA determines a static, optimal priority
     * ordering for a task set.
     * </p>
     */
    public OPA() {
        this.setName("Optimal Priority Assignment (OPA)");
        this.setPriorityType(Definition.PriorityType.Fixed);
    }

    /**
     * Assigns fixed, optimal priorities to tasks in the provided {@link TaskSet}
     * using Audsley's Optimal Priority Assignment method.
     *
     * <p>
     * The method iterates priority levels from lowest to highest and for each
     * level searches for an unassigned task that is schedulable when given that
     * candidate priority. Schedulability checks are performed by
     * {@link #isSchedulableWithPriority(TaskSet, Task, int, boolean[])}, which
     * runs a response-time analysis. If a suitable task is found it is assigned
     * the current priority; after all levels are processed the tasks in the
     * {@code TaskSet} receive their computed {@link Priority} objects.
     * Diagnostic messages indicating assignment decisions and final priorities
     * are printed to standard output.
     * </p>
     *
     * @param ts the {@link TaskSet} containing tasks to which optimal fixed
     *           priorities will be assigned; must not be {@code null}. Each
     *           task in the set will have its priority updated in-place.
     */
    @Override
    public void calculatePriority(TaskSet ts) {
        int numTasks = ts.size();
        boolean[] assigned = new boolean[numTasks];
        Priority[] priorities = new Priority[numTasks];

        // Audsley 演算法：從最低優先權（數字最大）開始分配
        for (int priorityIndex = 1; priorityIndex <= numTasks; priorityIndex++) {
            Task bestTask = null;
            int bestTaskIndex = -1;

            // 尋找一個未分配且可排程的任務
            for (int i = 0; i < numTasks; i++) {
                Task t = ts.get(i);
                if (!assigned[i] && isSchedulableWithPriority(ts, t, priorityIndex, assigned)) {
                    bestTask = t;
                    bestTaskIndex = i;
                    break; // 找到第一個可行的任務即可
                }
            }

            // Audsley 演算法要求：每個優先權層級都必須找到可排程的任務
            if (bestTask == null) {
                throw new IllegalStateException(
                    "無法為優先權層級 " + priorityIndex + " 找到可排程的任務。任務集合無法使用 OPA 排程！");
            }

            assigned[bestTaskIndex] = true;
            priorities[bestTaskIndex] = new Priority(priorityIndex);
            System.out.println("Task " + bestTask.getID() + " 被分配優先權: " + priorityIndex);
        }

        // 設定所有任務的優先權（此時 priorities 陣列已全部填滿，無 null 值）
        for (int i = 0; i < numTasks; i++) {
            ts.get(i).setPriority(priorities[i]);
        }

        // 顯示最終結果
        System.out.println("\n=== OPA 優先權分配完成 ===");
        for (Task task : ts) {
            System.out.println("Task " + task.getID() + " 最終優先權: " + task.getPriority().getValue());
        }
    }

    /**
     * 檢查候選任務在給定優先權下是否可排程。
     * 
     * <p>使用 Response Time Analysis (RTA) 進行可排程性分析。
     * 公式：R_i = C_i + Σ_{j∈hp(i)} ⌈R_i / T_j⌉ × C_j
     * 其中 hp(i) 表示所有優先權高於 i 的任務集合。</p>
     * 
     * @param ts 任務集合
     * @param candidate 候選任務
     * @param priorityIndex 當前要分配的優先權層級（數字越小優先權越高）
     * @param assigned 記錄哪些任務已被分配優先權
     * @return true 如果候選任務在此優先權下可排程
     */
    private boolean isSchedulableWithPriority(TaskSet ts, Task candidate, int priorityIndex, boolean[] assigned) {
        System.out.println("檢查 Task(" + candidate.getID() + ") 在優先權層級 " + priorityIndex + " 的可排程性...");

        // 建立比候選任務優先權更高的任務列表
        // 在 Audsley 演算法中，所有「尚未分配」且「不是候選任務」的任務
        // 都被視為比候選任務擁有更高的優先權（因為我們從最低優先權開始分配）
        List<Task> higherPriorityTasks = new ArrayList<>();
        for (int i = 0; i < ts.size(); i++) {
            Task t = ts.get(i);
            if (!assigned[i] && t != candidate) {
                higherPriorityTasks.add(t);
            }
        }

        // 快速可行性檢查（保守估計）：候選任務 + 所有高優先權任務的 WCET 總和
        double sumWCET = candidate.getComputationAmount();
        for (Task t : higherPriorityTasks) {
            sumWCET += t.getComputationAmount();
        }
        if (sumWCET > candidate.getRelativeDeadline()) {
            System.out.println("  → Task " + candidate.getID() + " 不可排程（WCET 總和 " + sumWCET + " > 截止時間 " + candidate.getRelativeDeadline() + "）");
            return false;
        }

        // Response Time Analysis 迭代計算
        // 公式：R_i = C_i + Σ_{j∈hp(i)} ⌈R_i / T_j⌉ × C_j
        double C_i = candidate.getComputationAmount();
        double R = C_i;  // 初始值設為任務自身的執行時間
        double R_prev = 0;
        int iteration = 0;
        final double DEADLINE = candidate.getRelativeDeadline();
        final int MAX_ITERATIONS = 1000; // 防止無限迴圈
        final double EPSILON = 1e-9; // 浮點數比較的容差值

        while (Math.abs(R - R_prev) > EPSILON && R <= DEADLINE && iteration < MAX_ITERATIONS) {
            iteration++;
            R_prev = R;
            
            // 計算來自高優先權任務的干擾
            double interference = 0;
            for (Task hp : higherPriorityTasks) {
                interference += Math.ceil(R_prev / hp.getPeriod()) * hp.getComputationAmount();
            }
            
            R = C_i + interference;

            System.out.println("  迭代 " + iteration + "：R = " + String.format("%.2f", R) + 
                             ", R_prev = " + String.format("%.2f", R_prev) + 
                             ", Deadline = " + DEADLINE);
        }

        boolean schedulable = R <= DEADLINE;
        if (schedulable) {
            System.out.println("  ✓ Task " + candidate.getID() + " 可排程（Response Time = " + String.format("%.2f", R) + "）");
        } else {
            System.out.println("  ✗ Task " + candidate.getID() + " 不可排程（Response Time = " + String.format("%.2f", R) + " > Deadline " + DEADLINE + "）");
        }
        
        return schedulable;
    }

    /**
     * Job-queue level priority calculation is not supported by OPA.
     *
     * <p>
     * OPA targets single-core task sets and assigns fixed priorities at the
     * task level using Audsley's algorithm. This implementation does not
     * provide a job-level priority calculation. Invoking this method will throw
     * {@link UnsupportedOperationException}.
     * </p>
     *
     * @param jq the {@link JobQueue} parameter (not used)
     * @return nothing; this method always throws {@link UnsupportedOperationException}
     * @throws UnsupportedOperationException always thrown because job-queue based priority
     *         calculation is not implemented for this algorithm
     */
    @Override
    public JobQueue calculatePriority(JobQueue jq) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
