/*==============================================================================
 * Lexa - Property of William Norman-Walker
 *------------------------------------------------------------------------------
 * ConfigProcess.java
 *------------------------------------------------------------------------------
 * Author:  William Norman-Walker
 * Created: August 2013
 *==============================================================================
 */
package lexa.core.process;

import java.util.HashMap;
import java.util.Map;
import lexa.core.data.*;
import lexa.core.data.config.ConfigDataSet;
import lexa.core.data.exception.DataException;
import lexa.core.expression.Expression;
import lexa.core.expression.ExpressionException;
import lexa.core.expression.function.FunctionLibrary;
import lexa.core.process.context.Config;
import lexa.core.process.context.Context;

/**
 * A process that is managed via configuration.
 * <p>Each step on the process is configured using an {@link lexa.core.expression.Expression}
 * to determine the outcome.
 * <p>The configuration of this is as follows:
 * <pre>
 * handleRequest &lt;handleRequest expression&gt;
 * data {
 *   &lt;data config&gt;
 * }
 * </pre>
 * <p>Where:
 * <dl>
 * <dt>&lt;handleRequest expression&gt;</dt><dd>an expression to determine if the request is valid.
 *          This must return {@code true} or {@code false}.</dd>
 * <dt>&lt;data config&gt;</dt><dd>any data used to drive the configuration; such as look up codes.</dd>
 * </dl>
 * @author William
 * @since 2013-08
 */
public class ConfigProcess
        extends RequestProcess {
    /** these are the fields the process will accept */
    private String[] requestFields;

    /** these are the fields the process will return */
    private String[] replyFields;

    /** the current message being processed */
    private DataSet request;

    /** pre validation of the request */
    private Expression handleRequest;
    private Expression checkNextRequest;
	private Expression buildReply;
    private Map<String,Expression> requests;
    private DataSet data;
	private String nextRequest;
	private DataSet replyData;

    public ConfigProcess() {
		super();
    }

    @Override
    public DataSet buildRequests(DataSet context) throws ProcessException {
        String requestName = this.request.getString(Config.NEXT_REQUEST);
        Expression requestBuilder =
                this.requests.get(requestName);
        if (requestBuilder == null) {
            throw new ProcessException("Cannot find handler for " + requestName);
        }
        try {
            return (DataSet)requestBuilder.evaluate(this.request);
        } catch (ExpressionException ex) {
            throw new ProcessException(ex.getLocalizedMessage(),this.request,ex);
        }
    }

    @Override
    public boolean hasForwardRequests() throws ProcessException {
        if (this.checkNextRequest == null) {
            return false;
        }
        String next;
        try {
            next = (String)this.checkNextRequest.evaluate(request);
        } catch (ExpressionException ex) {
            throw new ProcessException(ex.getLocalizedMessage(),this.request,ex);
        }
        if (next != null) {
            request.put(Config.NEXT_REQUEST,next);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasFurtherWork() throws ProcessException {
        throw new UnsupportedOperationException("ConfigProcess.hasFurtherWork not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onClose() throws ProcessException {
        throw new UnsupportedOperationException("ConfigProcess.onClose not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onInitialise(FunctionLibrary functionLibrary, ConfigDataSet config)
            throws ProcessException,
                    DataException,
                    ExpressionException
    {
        config.validateType(
                Config.REQUEST_FIELDS,  DataType.STRING,
                Config.REPLY_FIELDS,    DataType.STRING
        );
        this.requestFields = config.getString(Config.REQUEST_FIELDS).split(" ");
        this.replyFields = config.getString(Config.REPLY_FIELDS).split(" ");
        this.handleRequest = Expression.parse(
				config.get(Config.HANDLE_REQUEST, "true").getString(),functionLibrary);
        this.requests = new HashMap();

        if (config.contains(Config.BUILD_REPLY))
        {
            config.validateType(Config.BUILD_REPLY, DataType.STRING);
            this.buildReply = Expression.parse(
                        config.getString(Config.BUILD_REPLY),
                        functionLibrary);
        }
        else
        {
            this.buildReply = null;
        }

        if (config.contains(Config.DATA)) {
            config.validateType(
                    Config.DATA, DataType.DATA_SET
            );
            this.data = new ArrayDataSet(config.getDataSet(Config.DATA));
        }
    }

    @Override
    public void onNewRequest(DataSet request)
			throws ProcessException {
        this.request = new ArrayDataSet();
        // white list for fields:
        for (String field : this.requestFields)
        {
            if (request.contains(field))
            {
                this.request.put(request.get(field));
            }
        }
		this.nextRequest = null;
		this.replyData = new ArrayDataSet();
        try
        {
			DataSet msg = this.getMessageData();
			Boolean result = (Boolean)this.handleRequest.evaluate(msg);
            if (!result)
            {
                if (msg.contains(Context.RETURN))
                {
                    throw new ProcessException(msg.getString(Context.RETURN),this.request);
                }
                throw new ProcessException("Unhandled call to process.handleRequest",this.request);            }
        }
        catch (ExpressionException ex)
        {
            throw new ProcessException(ex.getLocalizedMessage(),this.request,ex);
        }
    }

    @Override
    public boolean onReply(DataSet reply) throws ProcessException {
        throw new UnsupportedOperationException("ConfigProcess.onReply not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onProcess() throws ProcessException {
        throw new UnsupportedOperationException("ConfigProcess.onProcess not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

	@Override
	public DataSet buildReply()
			throws ProcessException
	{
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

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
	@Override
	public DataSet getMessageData()
	{
		DataSet msg = new ArrayDataSet(this.request)
				.put (Config.NEXT_REQUEST, this.nextRequest)
				.put (Config.DATA,this.data)
				.put(Context.REPLY, this.replyData);
		return msg;
	}
}
