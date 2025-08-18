package logic.util.eval;

import logic.bdo.SQLQueryWrapper;

/**
 * Strategy interface for comparing two SQL statements and producing a similarity score.
 * <p>
 * Implementations define how similarity is measured (e.g., syntactic or LLM-based).
 * The score is typically normalized to {@code 0.0}–{@code 1.0}.
 * </p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public interface StatementComparator {
    
    /**
     * Compares two SQL statements and returns a similarity score.
     *
     * @param query1 first SQL statement wrapper
     * @param query2 second SQL statement wrapper
     * @return similarity score (typically normalized to {@code 0.0}–{@code 1.0});
     *         may be {@code Double.NaN} if no score can be computed
     */
    double compare(SQLQueryWrapper query1, SQLQueryWrapper query2);
}
