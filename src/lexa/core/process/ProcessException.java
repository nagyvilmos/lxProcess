/*
 * ================================================================================
 * Lexa - Property of William Norman-Walker
 * --------------------------------------------------------------------------------
 * ProcessException.java
 *--------------------------------------------------------------------------------
 * Author:  William Norman-Walker
 * Created: April 2013
 *--------------------------------------------------------------------------------
 * Change Log
 * Date:        By: Ref:        Description:
 * ----------   --- ----------  --------------------------------------------------
 * 2016-08-30	WNW	2016-08		Replace clone of DataSet with copy constructors
 *================================================================================
 */
package lexa.core.process;

import lexa.core.data.DataSet;
import lexa.core.data.SealedDataSet;
import lexa.core.logging.Logger;

/**
 * An exception that occurred processing a message.
 * @author  William
 * @since   2013-04
 */
public class ProcessException
        extends Exception {

    /**
     * The message context for the exception.
     */
    private final DataSet context;

    /**
     * Constructs a new exception with the specified detail message.
     * <p>This is equivalent to using {@code ProcessException(message, null, null)}.
     *
     * @param   message
     *          the detail message (which is saved for later retrieval by the
     *          {@link Exception#getMessage()} method).
     */
    public ProcessException(String message) {
        this(message, null, null);
    }

    /**
     * Constructs a new exception with the specified detail message and context.
     * <p>This is equivalent to using {@code ProcessException(message, context, null)}.
     *
     * @param   message
     *          the detail message (which is saved for later retrieval by the
     *          {@link Exception#getMessage()} method).
     * @param   context
     *          the context (which is saved for later retrieval by the
     *          {@link ProcessException#getContext()} method).
     *          (A null value is permitted, and indicates that the context is nonexistent
     *          or unknown.)
     */
    public ProcessException(String message, DataSet context) {
        this(message, context, null);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * <p>This is equivalent to using {@code ProcessException(message, null, cause)}.
     * <p>Note that the detail message associated with cause is not automatically
     * incorporated in this exception's detail message.
     *
     * @param   message
     *          the detail message (which is saved for later retrieval by the
     *          {@link Exception#getMessage()} method).
     * @param   cause
     *          the cause (which is saved for later retrieval by the
     *          {@link Exception#getCause()} method).
     *          (A null value is permitted, and indicates that the cause is nonexistent
     *          or unknown.)
     */
    public ProcessException(String message, Throwable cause) {
        this(message, null, cause);
    }

    /**
     * Constructs a new exception with the specified detail message, context and cause.
     * <p>Note that the detail message associated with cause is not automatically
     * incorporated in this exception's detail message.
     *
     * @param   message
     *          the detail message (which is saved for later retrieval by the
     *          {@link Exception#getMessage()} method).
     * @param   context
     *          the context (which is saved for later retrieval by the
     *          {@link ProcessException#getContext()} method).
     *          (A null value is permitted, and indicates that the context is nonexistent
     *          or unknown.)
     * @param   cause
     *          the cause (which is saved for later retrieval by the
     *          {@link Exception#getCause()} method).
     *          (A null value is permitted, and indicates that the cause is nonexistent
     *          or unknown.)
     */
    public ProcessException(String message, DataSet context, Throwable cause) {
        super(message, cause);
            this.context = context == null ? null :
                    new SealedDataSet(context);
        new Logger("ProcessException", null).error(message, context, cause);
    }

    /**
     * Returns the context of this exception or {@code null} if the context is nonexistent or unknown.
     *
     * @return  the context of this exception or {@code null} if the context is nonexistent or unknown.
     */
    public DataSet getContext() {
        return this.context;
    }
}
