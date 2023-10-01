package com.sokima.executor.model.script;

public enum ExecutionStatus {
    /**
     * Just Created
     */
    NEW,

    /**
     * Waiting till scheduled time
     */
    AWAITING,

    /**
     * In order to execute
     */
    QUEUED,

    /**
     * Began execution and executes
     */
    RUNNING,

    /**
     * Stopped execution
     */
    STOPPED,

    /**
     * Cancelled execution due multiple reasons
     * - broken code
     * - malicious code
     * - malformed code
     * - got timeout during execution
     */
    CANCELED,

    /**
     * Execution completed and write result
     */
    COMPLETED
}
