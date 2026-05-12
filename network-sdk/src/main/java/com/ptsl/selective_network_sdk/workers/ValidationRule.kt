package com.ptsl.selective_network_sdk.workers

/**
 * Interface for a single validation rule.
 */
interface ValidationRule {
    /**
     * Executes the validation check.
     * @return A Pair containing the error message and status code, or null if validation passes.
     */
    fun validate(): Pair<String, Int>?
}
