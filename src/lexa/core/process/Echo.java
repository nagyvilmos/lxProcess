/*
 * ================================================================================
 * Lexa - Property of William Norman-Walker
 * --------------------------------------------------------------------------------
 * Echo.java
 *--------------------------------------------------------------------------------
 * Author:  William Norman-Walker
 * Created: April 2013
 *--------------------------------------------------------------------------------
 * Change Log
 * Date:        By: Ref:        Description:
 * ----------   --- ----------  --------------------------------------------------
 * 2013-08-10   WNW -           Changed to use RequestProcess
 * 2015-03-11	WNW	2015-03		Updated in line with new lxData
 *================================================================================
 */
package lexa.core.process;

import lexa.core.data.ConfigData;
import lexa.core.data.DataSet;
import lexa.core.data.exception.DataException;
import lexa.core.expression.ExpressionException;
import lexa.core.expression.function.FunctionLibrary;
import lexa.core.process.context.Context;

/**
 * Test process using a simple echo.
 * <p>Configured as:
 * <pre>
 * &lt;process&gt; {
 *   name &lt;process description&gt;
 *   classPath internal:echo
 * }
 * </pre>
 *
 * <p>The returned reply will be a copy of the request; no further processing takes place.
 *
 * @author William
 * @since 2013-04
 */
public class Echo
        extends RequestProcess {

    private DataSet context;
    /** the last reply created */
    private DataSet reply;

    @Override
    public DataSet buildReply() throws ProcessException {
        DataSet replyContext = this.context.clone()
				.put(Context.REPLY,this.reply);
        return replyContext;
    }

    @Override
    public DataSet buildRequests(DataSet context) throws ProcessException {
        return null;
    }

    @Override
    public boolean hasForwardRequests() throws ProcessException {
        return false; // never has any
    }

    @Override
    public boolean hasFurtherWork() throws ProcessException {
        return false;
    }

    @Override
    public void onClose() throws ProcessException {
        // no special processing
    }

    @Override
    public void onInitialise(FunctionLibrary funtionLibrary, ConfigData config)
            throws ProcessException,
                    DataException,
                    ExpressionException {
        // no special processing
    }

    @Override
    public void onNewRequest(DataSet request) throws ProcessException {
        this.context = request;
    }

    @Override
    public boolean onReply(DataSet reply) throws ProcessException {
        return true;
    }

    @Override
    public void onProcess() throws ProcessException {
        this.reply = this.context.getDataSet(Context.REQUEST).clone();
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
	 * @return the message data
	 */
	@Override
	public DataSet getMessageData()
	{
		return this.context.clone()
				.put(Context.REPLY, this.reply);
	}
}
