/*
 * ================================================================================
 * Lexa - Property of William Norman-Walker
 * --------------------------------------------------------------------------------
 * RequestProcess.java
 *--------------------------------------------------------------------------------
 * Author:  William Norman-Walker
 * Created: August 2013
 *--------------------------------------------------------------------------------
 * Change Log
 * Date:        By: Ref:        Description:
 * ----------   --- ----------  --------------------------------------------------
 * 2015-03-11	WNW	2015-03		Updated in line with new lxData
 *================================================================================
 */
package lexa.core.process;

import lexa.core.data.config.ConfigDataSet;
import lexa.core.data.DataSet;
import lexa.core.data.ArrayDataSet;
import lexa.core.data.exception.DataException;
import lexa.core.expression.ExpressionException;
import lexa.core.expression.function.FunctionLibrary;
import lexa.core.process.context.Context;

/**
 * A request process implements a basic process that receives requests and processes them.
 * <p>This handles the status of the process and allows the implementation to just
 * work on handling the data flow.

* @author William
 * @since 2013-08
 */
public abstract class RequestProcess
        implements LexaProcess {

    /** the current status */
    private final Status status;
    /** Unique id assigned to the process */
    private int id;

	/** received request */
	private DataSet request;
	/** requests made to other services */
	private DataSet forwardRequests;
	/** replies from other services */
	private DataSet forwardReplies;
	/** reply data */
	private DataSet reply;
    /**
     * creates a new process
     */
    public RequestProcess() {
        this.status = new Status();
    }

    /**
     * Method to build a new reply.
     * <p>This is called by {@link RequestProcess#getReply() getReply} and the results
     * from this method are returned by it.
     *
     * @return  The data for the reply.
     * @throws  ProcessException
     *          when an exception occurs building the reply.
     */
    public abstract DataSet buildReply() throws ProcessException;

    /**
     * Method to build outstanding requests.
     * <p>This is called by {@link RequestProcess#getRequests() getRequests} and the results
     * from this method are returned by it.
     * <p>This is only called if the status has pending requests; the flag is reset after this is called.
     *
	 * @param context the context for making requests; normally the message received.
     * @return  The data for the requests; see {@link Process#getRequests()}.
     * @throws ProcessException
     *          when an exception occurs building the requests.
     */
    public abstract DataSet buildRequests(DataSet context) throws ProcessException;

    @Override
    public void close() throws ProcessException {
        this.onClose();
        this.status.closed();
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public DataSet getReply() throws ProcessException {
        if (!this.status.replyReady()) {
            throw new ProcessException("Process has no replies ready.");
        }
        DataSet reply = this.buildReply();

        this.status.setReplyReady(false);
        if (this.hasFurtherWork()) {
            this.status.setWaitingProcess(true);
        } else {
            reply.put(Context.CLOSE,true);
            this.status.setAcceptRequests(true);
        }
        return reply;
    }

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
    @Override
    public DataSet getRequests() throws ProcessException {
        if (!this.status.requestPending()) {
            throw new ProcessException("Process has no pending requests.");
        }
        this.forwardRequests = this.buildRequests(this.request);
        this.status.setRequestPending(false);
        if (this.forwardRequests == null) {
			// nothing we can carry on
			this.status.setWaitingProcess(true);
			return null;
		}
		this.status.setReplyReady(false);
		return this.forwardRequests;
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    @Override
    public void handleReply(DataSet reply) throws ProcessException {
        if (!this.status.waitingReply()) {
            throw new ProcessException("Process cannot accept requests.");
        }
        if (this.onReply(reply)) {
            this.status.setWaitingReply(false);
        }
        this.status.setWaitingProcess(true);

    }

	/**
	 * Handle a request made to the process.
	 * 
	 * <p>
     * @param   request
     *          a request made to the process
     *
     * @throws  ProcessException
     *          when an exception occurs during processing.
     */
    @Override
    public void handleRequest(DataSet request) throws ProcessException {
        if (!this.status.acceptRequests()) {
            throw new ProcessException("Process cannot accept requests.");
        }
        this.onNewRequest(request);
        this.status.setAcceptRequests(false);
        if (this.hasForwardRequests()) {
            this.status.setRequestPending(true);
        } else {
            this.status.setWaitingProcess(true);
        }
    }

    /**
     * Indicates if there are any requests needed to be built.
     * <p>This is called after {@link RequestProcess#onNewRequest(lexa.core.data.DataSet) onNewRequest}
     * and {@link RequestProcess#onProcess() onProcess} to determine if any more requests are now needed.
     *
     * @return  {@code true} if requests are needed,
     *          otherwise {@code false}.
     * @throws  ProcessException
     *          when an exception occurs determining if there are more requests.
     */
    public abstract boolean hasForwardRequests() throws ProcessException;

    /**
     * Called to determine if the process has further work after replying.
     *
     * @return  {@code true} if the process has further,
     *          otherwise {@code false}.
     * @throws  ProcessException
     *          when an exception occurs checking for further work.
     */
    public abstract boolean hasFurtherWork() throws ProcessException;

    @Override
    public void initialise(FunctionLibrary functionLibrary, ConfigDataSet config)
            throws ProcessException,
                    DataException,
                    ExpressionException {
        if (this.status.active() || this.status.closed()) {
            throw new ProcessException("Process cannot be initialised in current state.");
        }
        this.onInitialise(functionLibrary, config);
        this.status.setActive();
        this.status.setAcceptRequests(true);
    }

    /**
     * Called when the process is being closed.
     * <p>Use this method in preference to overriding {@link Process#close() close}.
     *
     * @throws  ProcessException
     *          when an exception occurs closing the process.
     */
    public abstract void onClose()throws ProcessException;

    /**
     * Called when the process is being initialised.
     * <p>Use this method in preference to overriding
     * {@link Process#initialise(lexa.core.data.ConfigData) initialise}.
     *
     * @param   config
     *          the configuration data for the process.
     * @throws  ProcessException
     *          when an exception occurs initialising the process.
     * @throws  DataException
     *          when an exception occurs in the configuration.
     */
    public abstract void onInitialise(FunctionLibrary functionLibrary, ConfigDataSet config)
            throws ProcessException,
                    DataException,
                    ExpressionException;

    /**
     * Called when the process receives a new request.
     * <p>Use this method in preference to overriding
     * {@link Process#handleRequest(lexa.core.data.DataSet) handleRequest}.
     *
     * @param   request
     *          the data for the request.
     * @throws  ProcessException
     *          when an exception occurs handling the request.
     */
    public abstract void onNewRequest(DataSet request) throws ProcessException;

    /**
     * Called when the process receives a new reply.
     * <p>Use this method in preference to overriding
     * {@link Process#handleReply(lexa.core.data.DataSet) handleReply}.
     *
     * @param   reply
     *          the data for the reply.
     * @throws  ProcessException
     *          when an exception occurs handling the reply.
     */
    public abstract boolean onReply(DataSet reply) throws ProcessException;

    /**
     * Called when the process needs to perform some work.
     *
     * @throws  ProcessException
     *          when an exception occurs performing the processing.
     */
    public abstract void onProcess()throws ProcessException;

    @Override
    public void process()
			throws ProcessException
	{
        if (!this.status.waitingProcess()) {
            throw new ProcessException("Process is not waiting to process.");
        }
        this.onProcess();
        this.status.setWaitingProcess(false);
        if (this.hasForwardRequests()) {
            this.status.setRequestPending(true);
        } else {
            this.status.setReplyReady(true);
        }
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

	@Override
	public DataSet getMessageData()
	{
		DataSet msg = new ArrayDataSet(this.request)
				.put(Context.REPLY, this.reply);
		
		return msg;
	}
}
