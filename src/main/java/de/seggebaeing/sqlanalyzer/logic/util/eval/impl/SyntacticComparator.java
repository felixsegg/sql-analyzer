package de.seggebaeing.sqlanalyzer.logic.util.eval.impl;

import de.seggebaeing.sqlanalyzer.logic.bdo.SQLQueryWrapper;
import de.seggebaeing.sqlanalyzer.logic.util.eval.StatementComparator;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The SyntacticComparator class compares two SQL statements and returns a similarity score
 * between 0.0 and 1.0. A score of 1.0 indicates that the two statements are structurally identical
 * based on the evaluated components.
 * <br>
 * Components considered include:
 * <ul>
 *  <li>FROM clause (tables used)</li>
 *  <li>SELECT items (columns/expressions with alias normalization)</li>
 *  <li>JOIN targets (tables in JOINs)</li>
 *  <li>WHERE conditions (flattened and normalized; includes AND/OR and subselects)</li>
 *  <li>GROUP BY columns (if present)</li>
 *  <li>ORDER BY elements (if present)</li>
 * </ul>
 * This approach attempts to capture semantic equivalence rather than relying on a simple string comparison.
 */
public class SyntacticComparator implements StatementComparator {
    private static final Logger log = LoggerFactory.getLogger(SyntacticComparator.class);
    
    private static final SyntacticComparator instance = new SyntacticComparator();
    
    // Constants for component weights.
    public final double WEIGHT_TABLES = 0.15; // FROM clause tables.
    public final double WEIGHT_SELECT = 0.25; // SELECT items.
    public final double WEIGHT_JOINS = 0.15; // JOIN targets.
    public final double WEIGHT_WHERE = 0.25; // WHERE conditions.
    public final double WEIGHT_GROUP_BY = 0.1;  // GROUP BY columns.
    public final double WEIGHT_ORDER_BY = 0.1;  // ORDER BY elements.
    
    private SyntacticComparator() {
    }
    
    public static SyntacticComparator getInstance() {
        return instance;
    }
    
    /**
     * Compares two SQL statements and returns a similarity score.
     *
     * @param query1 The first SQL statement.
     * @param query2 The second SQL statement.
     * @return A similarity score between 0.0 and 1.0.
     */
    @Override
    public double compare(SQLQueryWrapper query1, SQLQueryWrapper query2) {
        String sql1 = query1.getSql();
        String sql2 = query2.getSql();
        
        try {
            log.info("Comparing two SQL statements.");
            // Parse both SQL statements into their AST representations.
            PlainSelect ps1 = (PlainSelect) ((Select) CCJSqlParserUtil.parse(sql1)).getSelectBody();
            PlainSelect ps2 = (PlainSelect) ((Select) CCJSqlParserUtil.parse(sql2)).getSelectBody();
            
            double score = 0.0;
            
            // 1. FROM Clause: Compare tables (including those from JOINs).
            Set<String> tables1 = extractTables(ps1);
            Set<String> tables2 = extractTables(ps2);
            double tableSimilarity = jaccardSimilarity(tables1, tables2);
            log.debug("FROM clause similarity: {} (tables1: {}, tables2: {})", tableSimilarity, tables1, tables2);
            score += WEIGHT_TABLES * tableSimilarity;
            
            // 2. SELECT Items: Extract and normalize SELECT items.
            Set<String> selectItems1 = extractSelectItems(ps1);
            Set<String> selectItems2 = extractSelectItems(ps2);
            double selectSimilarity = jaccardSimilarity(selectItems1, selectItems2);
            log.debug("SELECT items similarity: {} (selectItems1: {}, selectItems2: {})", selectSimilarity, selectItems1, selectItems2);
            score += WEIGHT_SELECT * selectSimilarity;
            
            // 3. JOIN Targets: Compare target tables of JOINs.
            Set<String> joinItems1 = extractJoinTargets(ps1);
            Set<String> joinItems2 = extractJoinTargets(ps2);
            double joinSimilarity = jaccardSimilarity(joinItems1, joinItems2);
            log.debug("JOIN targets similarity: {} (joinItems1: {}, joinItems2: {})", joinSimilarity, joinItems1, joinItems2);
            score += WEIGHT_JOINS * joinSimilarity;
            
            // 4. WHERE Clause: Extract and normalize individual conditions.
            Set<String> whereConditions1 = extractWhereConditions(ps1.getWhere());
            Set<String> whereConditions2 = extractWhereConditions(ps2.getWhere());
            double whereSimilarity = jaccardSimilarity(whereConditions1, whereConditions2);
            log.debug("WHERE clause similarity: {} (whereConditions1: {}, whereConditions2: {})", whereSimilarity, whereConditions1, whereConditions2);
            score += WEIGHT_WHERE * whereSimilarity;
            
            // 5. GROUP BY: Compare GROUP BY columns.
            Set<String> groupBy1 = extractGroupByColumns(ps1);
            Set<String> groupBy2 = extractGroupByColumns(ps2);
            double groupBySimilarity = jaccardSimilarity(groupBy1, groupBy2);
            log.debug("GROUP BY similarity: {} (groupBy1: {}, groupBy2: {})", groupBySimilarity, groupBy1, groupBy2);
            score += WEIGHT_GROUP_BY * groupBySimilarity;
            
            // 6. ORDER BY: Compare ORDER BY elements.
            Set<String> orderBy1 = extractOrderByColumns(ps1);
            Set<String> orderBy2 = extractOrderByColumns(ps2);
            double orderBySimilarity = jaccardSimilarity(orderBy1, orderBy2);
            log.debug("ORDER BY similarity: {} (orderBy1: {}, orderBy2: {})", orderBySimilarity, orderBy1, orderBy2);
            score += WEIGHT_ORDER_BY * orderBySimilarity;
            
            double finalScore = Math.round(score * 1000.0) / 1000.0;
            log.info("Final similarity score: {}", finalScore);
            return finalScore;
        } catch (Exception e) {
            log.error("Error comparing SQL statements: ", e);
            return Double.NaN;
        }
    }
    
    /**
     * Extracts table names from the FROM clause (including those in JOINs) and normalizes them.
     *
     * @param ps The PlainSelect object representing the parsed SQL.
     * @return A set of lower-case table names.
     */
    private Set<String> extractTables(PlainSelect ps) {
        // Wrap the PlainSelect in a Select to avoid ambiguity in TablesNamesFinder.
        Select select = new Select();
        select.setSelectBody(ps);
        TablesNamesFinder finder = new TablesNamesFinder();
        // Explicitly cast to Statement to resolve ambiguity.
        return finder.getTableList(select).stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }
    
    /**
     * Extracts and normalizes SELECT items from the SELECT clause.
     * This method removes common alias syntax (e.g., " AS alias") and extra whitespace.
     *
     * @param ps The PlainSelect object representing the parsed SQL.
     * @return A set of normalized SELECT item strings.
     */
    private Set<String> extractSelectItems(PlainSelect ps) {
        return ps.getSelectItems().stream()
                .map(item -> {
                    String str = item.toString().toLowerCase().replaceAll("\\s+", " ");
                    // Remove alias part if exists (e.g., "column as alias" becomes "column").
                    if (str.contains(" as ")) {
                        str = str.substring(0, str.indexOf(" as ")).trim();
                    }
                    return str;
                })
                .collect(Collectors.toSet());
    }
    
    /**
     * Extracts the target table names from JOIN clauses and normalizes them.
     *
     * @param ps The PlainSelect object representing the parsed SQL.
     * @return A set of lower-case join target strings.
     */
    private Set<String> extractJoinTargets(PlainSelect ps) {
        if (ps.getJoins() == null) {
            return Collections.emptySet();
        }
        return ps.getJoins().stream()
                .map(j -> j.getRightItem().toString().toLowerCase())
                .collect(Collectors.toSet());
    }
    
    /**
     * Recursively extracts individual conditions from the WHERE clause.
     * It handles both AND and OR expressions and also processes subselects if present.
     *
     * @param expr The WHERE clause expression.
     * @return A set of normalized condition strings.
     */
    private Set<String> extractWhereConditions(Expression expr) {
        Set<String> conditions = new HashSet<>();
        if (expr == null) {
            return conditions;
        }
        if (expr instanceof AndExpression andExpr) {
            // For AND expressions, split into individual conditions.
            conditions.addAll(extractWhereConditions(andExpr.getLeftExpression()));
            conditions.addAll(extractWhereConditions(andExpr.getRightExpression()));
        } else if (expr instanceof OrExpression orExpr) {
            // For OR expressions, process each side and combine them in sorted order.
            Set<String> leftConditions = extractWhereConditions(orExpr.getLeftExpression());
            Set<String> rightConditions = extractWhereConditions(orExpr.getRightExpression());
            List<String> combined = new ArrayList<>();
            combined.addAll(leftConditions);
            combined.addAll(rightConditions);
            Collections.sort(combined);
            conditions.add("OR(" + String.join(" , ", combined) + ")");
        } else if (expr instanceof SubSelect) {
            // For subqueries, parse the subselect and process its WHERE clause recursively.
            try {
                SubSelect subSelect = (SubSelect) expr;
                PlainSelect subPs = (PlainSelect) ((Select) subSelect.getSelectBody()).getSelectBody();
                Set<String> subConditions = extractWhereConditions(subPs.getWhere());
                List<String> sorted = new ArrayList<>(subConditions);
                Collections.sort(sorted);
                conditions.add("SUBSELECT(" + String.join(" , ", sorted) + ")");
            } catch (Exception e) {
                // If parsing fails, fall back to simple normalization.
                conditions.add(normalizeExpression(expr));
            }
        } else {
            // For other expressions, use normalization.
            conditions.add(normalizeExpression(expr));
        }
        return conditions;
    }
    
    /**
     * Normalizes an expression into a canonical string.
     * For EqualsTo expressions, the operands are sorted to ensure that "a = b" equals "b = a".
     * For other expressions, lower-case conversion and whitespace normalization are applied.
     *
     * @param expr The expression to normalize.
     * @return A normalized string representation of the expression.
     */
    private String normalizeExpression(Expression expr) {
        if (expr instanceof EqualsTo equalsExpr) {
            String left = equalsExpr.getLeftExpression()
                    .toString().toLowerCase().replaceAll("\\s+", "");
            String right = equalsExpr.getRightExpression()
                    .toString().toLowerCase().replaceAll("\\s+", "");
            List<String> operands = Arrays.asList(left, right);
            Collections.sort(operands); // Ensure commutativity.
            return String.join("=", operands);
        }
        // For other expressions, normalize by converting to lower-case and removing extra spaces.
        return expr.toString().toLowerCase().replaceAll("\\s+", "");
    }
    
    /**
     * Extracts and normalizes GROUP BY columns from the SQL.
     *
     * @param ps The PlainSelect object representing the parsed SQL.
     * @return A set of normalized GROUP BY column strings.
     */
    @SuppressWarnings("deprecation") // getGroupByExpressions() is deprecated, but acceptable for now
    private Set<String> extractGroupByColumns(PlainSelect ps) {
        if (ps.getGroupBy() == null || ps.getGroupBy().getGroupByExpressions() == null) {
            return Collections.emptySet();
        }
        return ps.getGroupBy().getGroupByExpressions().stream()
                .map(expr -> expr.toString().toLowerCase().replaceAll("\\s+", ""))
                .collect(Collectors.toSet());
    }
    
    /**
     * Extracts and normalizes ORDER BY elements from the SQL.
     *
     * @param ps The PlainSelect object representing the parsed SQL.
     * @return A set of normalized ORDER BY element strings.
     */
    private Set<String> extractOrderByColumns(PlainSelect ps) {
        if (ps.getOrderByElements() == null) {
            return Collections.emptySet();
        }
        return ps.getOrderByElements().stream()
                .map(o -> o.toString().toLowerCase().replaceAll("\\s+", ""))
                .collect(Collectors.toSet());
    }
    
    /**
     * Computes the Jaccard similarity between two sets.
     * The similarity is the size of the intersection divided by the size of the union.
     *
     * @param set1 The first set.
     * @param set2 The second set.
     * @return A similarity score between 0.0 and 1.0.
     */
    private double jaccardSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() && set2.isEmpty()) {
            return 1.0;
        }
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        return (double) intersection.size() / union.size();
    }
}
