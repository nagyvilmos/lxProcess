/*==============================================================================
 * Lexa - Property of William Norman-Walker
 *------------------------------------------------------------------------------
 * LexaProcess.java
 *------------------------------------------------------------------------------
 * Author:  William Norman-Walker
 * Created: April 2013
 *==============================================================================
 */
package lexa.core.process;

import lexa.core.data.config.ConfigDataSet;
import lexa.core.data.DataSet;
import lexa.core.data.exception.DataException;
import lexa.core.expression.ExpressionException;
import lexa.core.expression.function.FunctionLibrary;

/**
 * Interface for a process in a message server.
 * <p>The processes are instantiated and controlled by a server.
 * <p>Each process defines how many concurrent requests it can handle. If there
 * are more requests than processes, then the server can create extra processes
 * to handle the load.
 * <p>Each process supports a status that can be
 * @author William
 * @since 2013-04
 */
public interface LexaProcess {

    /**
     * Close the process; no further calls should be accepted after this call.
     *
     * @throws  ProcessException
     *          when an exception occurs during processing.
     */
    public void close()
            throws ProcessException;

    /**
     * Get the unique ID assigned to the process.
     * @return  the unique ID assigned to the process
     */
    public int getId();

	/**
	 * Provides the data for exposing to the handlers:
	 * <pre>
	 * &lt;message received by server&gt;
	 * nextMessage - &lt;next message to send&gt;
	 * data {
	 *   &lt;config data for process&gt;
	 * }
	 * reply {
	 * }
     * </pre>
	 * @return the message data
	 */
	public DataSet getMessageData();

	/**
     * Get a reply to be returned to the caller.
     *
     * @return  the data for the reply.
     *
     * @throws  ProcessException
     *          when an exception occurs during processing.
     */
    public DataSet getReply()
            throws ProcessException;

    /**
     * Get a set of requests to be sent onto another process.
     * <p>The format is:
     * <pre>
     * sourceRef &lt;processId&gt;
     * messageList {
     *   &lt;messageId&gt; {
     *     service &lt;service&gt;
     *     message &lt;message&gt;
     *     source {
     *       &lt;source data&gt;
     *     }
     *     request {
     *       &lt;request data&gt;
     *     }
     *   }
     *   [...]
     * }
     * </pre>
     * <p>Where:
     * <dl>
     * <dt>&lt;processId&gt;</dt><dd>unique id assigned to the process.</dd>
     * <dt>&lt;messageId&gt;</dt><dd>id assigned by the process for the message.
     *      This block can be repeated with multiple unique message ids.</dd>
     * <dt>&lt;service&gt;</dt><dd>service for processing the message.</dd>
     * <dt>&lt;message&gt;</dt><dd>message type being sent.</dd>
     * <dt>&lt;source data&gt;</dt><dd>the source for the message; this is taken from the
     *      message being processed that generated the request.</dd>
     * <dt>&lt;request&gt;</dt><dd>the request message; this is defined by the
     *      relevant {@link Process} implementation.</dd>
     * </dl>
     *
     * @return  a set of requests to be sent onto another process
     *
     * @throws  ProcessException
     *          when an exception occurs during processing.
     */
    public DataSet getRequests()
            throws ProcessException;

    /**
     * Get the status if the process
     * @return  the status if the process
     */
    public Status getStatus();

    /**
     * Handle a reply from a forwarded messages.
     *
     * @param   reply
     *          a reply from a forwarded messages
     *
     * @throws  ProcessException
     *          when an exception occurs during processing.
     */
    public void handleReply(DataSet reply)
            throws ProcessException;

    /**
     * Handle a request made to the process.
     *
     * @param   request
     *          a request made to the process
     *
     * @throws  ProcessException
     *          when an exception occurs during processing.
     */
    public void handleRequest(DataSet request)
            throws ProcessException;

    /**
     * Initialise the process based on the configuration.
     *
	 * @param   functionLibrary
     *          the library to use with this process.
     * @param   config
     *          the configuration for the process.
     *
     * @throws  ProcessException
     *          when an exception has occurred initialising the process.
     * @throws  DataException
     *          when an exception occurs in the configuration.
	 * @throws  lexa.core.expression.ExpressionException
     *          when an exception has occurred with an expression.
     */
    public void initialise(FunctionLibrary functionLibrary, ConfigDataSet config)
            throws ProcessException,
                    DataException,
                    ExpressionException;

    /**
     * Perform any processing of the data received.
     *
     * @throws  ProcessException
     *          when an exception occurs during processing.
     */
    public void process()
            throws ProcessException;

    /**
     * Set the unique ID for the process.
     * @param   id
     *          the unique ID assigned to the process
     */
    public void setId(int id);

}
