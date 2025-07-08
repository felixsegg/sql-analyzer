package logic.util.eval;

import logic.bdo.SQLQueryWrapper;

public interface StatementComparator {
    double compare(SQLQueryWrapper query1, SQLQueryWrapper query2);
}
