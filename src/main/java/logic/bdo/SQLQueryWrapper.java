package logic.bdo;

/**
 * Interface for objects that provide access to an SQL string.
 * <p>
 * Used to abstract over domain objects that wrap or represent SQL queries.
 * </p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public interface SQLQueryWrapper {
    /**
     * Returns the SQL string represented by this object.
     *
     * @return the SQL string
     */
    String getSql();
}
