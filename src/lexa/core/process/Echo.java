/*==============================================================================
 * Lexa - Property of William Norman-Walker
 *------------------------------------------------------------------------------
 * Echo.java (lxProcess)
 *------------------------------------------------------------------------------
 * Author:  William Norman-Walker
 * Created: April 2013
 *==============================================================================
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
 * Test process using a simple echo.
 *
 * <p>Configured as:
 * <pre>
 * {
 *   name &lt;process description&gt;
 *   classPath Echo
 * }
 * </pre>
 *
 * <p>The returned reply will be a copy of the request; no further processing
 * takes place.
 *
 * @author William
 * @since 2013-04
 */
public class Echo
        extends RequestProcess {

    /** request context to be returned */
    private DataSet context;
    /** the last reply created */
    private DataSet reply;

    @Override
    public DataSet buildReply() throws ProcessException {
        DataSet replyContext = new ArrayDataSet(this.context)
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
        return (this.reply == null);
    }

    @Override
    public void onClose() throws ProcessException {
        // no special processing
    }

    @Override
    public void onInitialise(FunctionLibrary funtionLibrary, ConfigDataSet config)
            throws ProcessException,
                    DataException,
                    ExpressionException {
        // no special processing
    }

    @Override
    public void onNewRequest(DataSet request) throws ProcessException {
        this.context = request;
        this.reply = null;
    }

    @Override
    public boolean onReply(DataSet reply) throws ProcessException {
        return true;
    }

    @Override
    public void onProcess() throws ProcessException {
        this.reply = new ArrayDataSet(
                this.context.getDataSet(Context.REQUEST)
        );
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
		return new ArrayDataSet(this.context)
				.put(Context.REPLY, this.reply);
	}
}
