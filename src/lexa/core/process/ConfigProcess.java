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
import lexa.core.expression.map.*;
import lexa.core.process.context.Config;
import lexa.core.process.context.Context;

/**
 * A process that is managed via configuration.
 * <p>Each step on the process is configured using an
 * {@link lexa.core.expression.Expression} to determine the outcome.
 * <p>The configuration of this is as follows:
 * <pre>
 * requestFieldList - &lt;input fields&gt;
 * requestExpression - &lt;request expression&gt;
 * replyMap {
 *   &lt;reply mapping&gt;
 * }
 * data {
 *   &lt;data config&gt;
 * }
 * </pre>
 * <p>Where:
 * <dl>
 * <dt>{@code input fields}</dt>
 *      <dd>A space separated list of field names.</dd>
 * <dt>{@code request expression}</dt>
 *      <dd>An expression to evaluate if the
 *      request can be handled by the process; return is {@link Boolean}.
 *      If the request cannot be handled, then the field {@code return} should
 *      be assigned a user friendly message.</dd>
 * <dt>{@code reply mapping}</dt>
 *      <dd>An {@link ExpressionMap} to build the reply</dd>
 * <dt>{@code ...}</dt>
 *      <dd> ... </dd>
 * <dt>{@code data config}</dt>
 *      <dd>any data used to drive the configuration;
 *      such as look up codes.</dd>
 * </dl>
 *
 * @author William
 * @since 2013-08
 */
public class ConfigProcess
        extends RequestProcess {
    /** these are the fields the process will accept */
    private String[] requestFields;
    /** pre validation of the request */
    private Expression requestExpression;
    /** map to build the reply */
    private ExpressionMap replyMap;

    /** the current message being processed */
    private DataSet request;

    /** processing of the data */
    private Expression handleProcessExpression;
    /** evaluate if any request is outstanding */
    private Expression checkNextRequestExpression;
    /** build the reply */
	private Expression buildReplyExpression;
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
        if (this.checkNextRequestExpression == null) {
            return false;
        }
        String next;
        try {
            next = (String)this.checkNextRequestExpression.evaluate(request);
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
    public boolean hasFurtherWork()
            throws ProcessException
    {
        if (this.handleProcessExpression == null)
        {
            return false;
        }
        DataSet msg = this.getMessageData();
        try
        {
            return (boolean)this.handleProcessExpression.evaluate(msg);
        }
        catch (ExpressionException ex)
        {
            throw new ProcessException("Error in ConfigProcess.hasFurtherWork",msg, ex);
        }
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
                Config.REQUEST_FIELD_LIST,  DataType.STRING,
                Config.REPLY_MAP,           DataType.DATA_SET
        );
        this.requestFields =
                config.getString(Config.REQUEST_FIELD_LIST).split(" ");

        this.requestExpression = config.contains(Config.REQUEST_EXPRESSION) ?
                Expression.parse(
                        config.getString(
                                Config.REQUEST_EXPRESSION),functionLibrary) :
                null;
        this.replyMap = new ExpressionMap(
                config.getDataSet(Config.REPLY_MAP), functionLibrary);

        if (config.contains(Config.DATA))
        {
            config.validateType(
                    Config.DATA, DataType.DATA_SET
            );
            this.data = new ArrayDataSet(config.getDataSet(Config.DATA));
        }
        else
        {
            this.data = null;
        }

        this.requests = new HashMap();
    }

    @Override
    public void onNewRequest(DataSet request)
			throws ProcessException {
        this.request = new ArrayDataSet(request);
        DataSet context = request.getDataSet(Context.REQUEST);
        DataSet cleanContext = new ArrayDataSet();
        // white list for fields:
        String missing = "";
        for (String field : this.requestFields)
        {
            if (context.contains(field))
            {
                cleanContext.put(context.get(field));
            }
            else
            {
                missing = missing + " " + field;
            }
        }
        if (!missing.isEmpty())
        {
            throw new ProcessException("Missing from request:" + missing,this.request);
        }
        this.request.put(Context.REQUEST, cleanContext);
        this.nextRequest = null;
		this.replyData = new ArrayDataSet();
        try
        {
			DataSet msg = this.getMessageData();
			Boolean result = (Boolean)this.requestExpression.evaluate(msg);
            if (!result)
            {
                if (msg.contains(Context.RETURN))
                {
                    throw new ProcessException(
                            msg.getString(Context.RETURN),this.request);
                }
                throw new ProcessException(
                        "Unhandled call to process.handleRequest",this.request);            }
        }
        catch (ExpressionException ex)
        {
            throw new ProcessException(ex.getLocalizedMessage(),this.request,ex);
        }
    }

    @Override
    public boolean onReply(DataSet reply) throws ProcessException {
        throw new UnsupportedOperationException(
                "ConfigProcess.onReply not supported yet.");
    }

    @Override
    public void onProcess() throws ProcessException {
        if (this.handleProcessExpression == null)
        {
            return;
        }
        try
        {
            this.handleProcessExpression.evaluate(
                    this.getMessageData()
            );
        }
        catch (ExpressionException ex)
        {
            this.logger.error("Unable to evaluate process", this.request, ex);
            throw new ProcessException(
                    "Unable to evaluate process", this.request, ex);
        }
    }

	@Override
	public DataSet buildReply()
			throws ProcessException
	{
        return new MapDataSet(replyMap, this.getMessageData());
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
