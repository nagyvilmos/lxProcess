/*
 * ================================================================================
 * Lexa - Property of William Norman-Walker
 * --------------------------------------------------------------------------------
 * PassThrough.java
 *--------------------------------------------------------------------------------
 * Author:  William Norman-Walker
 * Created: July 2013
 *--------------------------------------------------------------------------------
 * Change Log
 * Date:        By: Ref:        Description:
 * ----------   --- ----------  --------------------------------------------------
 * 2015-03-11	WNW	2015-03		Updated in line with new lxData
 *================================================================================
 */
package lexa.core.process;

import lexa.core.data.config.ConfigDataSet;
import lexa.core.data.DataItem;
import lexa.core.data.DataSet;
import lexa.core.data.SimpleDataSet;
import lexa.core.data.exception.DataException;
import lexa.core.expression.function.FunctionLibrary;
import lexa.core.process.context.Config;
import lexa.core.process.context.Context;

/**
 * Provide a pass through process.  Each request is forwarded on to another service
 * on the remote connection for the process.
 * @author William
 * @since 2013-07
 */
public class PassThrough
        implements LexaProcess {

    private final static String STATE_REPLY_READY       = "REPLY_READY";
    private final static String STATE_REQUEST_PENDING   = "REQUEST_PENDING";
    private final static String STATE_WAITING_REPLY     = "WAITING_REPLY";
    private final Status status;
    /** Unique id assigned to the process */
    private int id;

    private int lastSid;
    private boolean allowAnonymous;
    private DataSet messageMap;
    private DataSet requests;

    public PassThrough() {
        this.status = new Status();
        this.lastSid = 0;
    }

    @Override
    public void close() throws ProcessException {
        this.status.setClosed();
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void initialise(FunctionLibrary funtionLibrary, ConfigDataSet config) throws ProcessException, DataException {
        if (this.status.active() || this.status.closed()) {
            throw new ProcessException("Process cannot be initialised in current state.");
        }
        this.allowAnonymous = config.getBoolean(Config.ALLOW_ANONYMOUS);
        this.messageMap = new SimpleDataSet();
        this.requests = new SimpleDataSet();
        ConfigDataSet serviceConfig = config.getDataSet(Config.SERVICE_LIST);
        String[] keys = serviceConfig.keys();
        for (int k = 0;
                k < keys.length;
                k++) {
            String from = keys[k];
            String to = serviceConfig.getString(from);
            this.messageMap.put(from,to);
        }
        serviceConfig.close();
        this.status.setActive();
        this.status.setAcceptRequests(true);
    }

    @Override
    public DataSet getReply() throws ProcessException {
        if (!this.status.replyReady()) {
            throw new ProcessException("No reply ready");
        }
        DataSet reply = null;
        for (DataItem item : this.requests) {
            DataSet request = item.getDataSet();
            if (request.getBoolean(STATE_REPLY_READY)) {
                // set the reply:
                reply = new SimpleDataSet(request.getDataSet(Context.REQUEST))
						.put(request.get(Context.REPLY))
						.put(request.get(Context.RETURN))
						.put(request.get(Context.CLOSE));
                request.put(STATE_REPLY_READY,false);
                break;
            }
        }
        this.status.setReplyReady(false);
        this.status.setWaitingProcess(true);
        return reply;
    }

    @Override
    public DataSet getRequests() throws ProcessException {
        if (!this.status.requestPending()) {
            throw new ProcessException("No requests pending");
        }
        DataSet messageList = new SimpleDataSet();
        for (DataItem item : this.requests) {
            DataSet request = item.getDataSet();
            if (request.getBoolean(PassThrough.STATE_REQUEST_PENDING)) {
//                DataSet data = new SimpleDataSet();
//                data.put(request.get(Context.SERVICE));
//                DataSet original = request.getDataSet(Context.REQUEST);
//                data.put(original.get(Context.MESSAGE));
//
//                DataSet clientSource = new SimpleDataSet();
//                clientSource.put(original.get(Context.SERVICE));
//                clientSource.put(original.get(Context.MESSAGE));
//                clientSource.put(original.get(Context.SOURCE_ID));
//                clientSource.put(original.get(Context.SOURCE_REF));
//                clientSource.put(original.get(Context.SOURCE));
//                DataSet source = new SimpleDataSet();
//                source.put(request.get(Context.SOURCE_REF));
//                source.put(Context.SOURCE,clientSource);
//                data.put(Context.SOURCE,source);
//                data.put(original.get(Context.REQUEST));

				DataSet original = request.getDataSet(Context.REQUEST);
				DataSet data = new SimpleDataSet()
					.put(request.get(Context.SERVICE))
					.put(original.get(Context.MESSAGE))
					.put(Context.SOURCE, new SimpleDataSet()
						.put(request.get(Context.SOURCE_REF))
						.put(Context.SOURCE,new SimpleDataSet()
							.put(original.get(Context.SERVICE))
							.put(original.get(Context.MESSAGE))
							.put(original.get(Context.SOURCE_ID))
							.put(original.get(Context.SOURCE_REF))
							.put(original.get(Context.SOURCE))))
					.put(original.get(Context.REQUEST));


                messageList.put(item.getKey(),data);
                request
						.put(PassThrough.STATE_REQUEST_PENDING,false)
						.put(PassThrough.STATE_WAITING_REPLY,true);
            }
        }
        this.status.setRequestPending(false);
        if (messageList.isEmpty()) {
            return null;
        }
        DataSet messages = new SimpleDataSet()
				.put(Context.SOURCE_REF, this.getId())
				.put(Context.MESSAGE_LIST,messageList);
        this.status.setRequestPending(false);
        this.status.setWaitingProcess(true);
        return messages;
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    @Override
    public void process() throws ProcessException {
        if (!this.status.waitingProcess()) {
            throw new ProcessException("No pending process");
        }
        // after each step, the status flags are reset and we come back here.
        // now check all requests for pending work;
        for (DataItem item : this.requests) {
            DataSet request = item.getDataSet();
            if (request.getBoolean(PassThrough.STATE_REPLY_READY)) {
                this.status.setReplyReady(true);
            }
            if (request.getBoolean(PassThrough.STATE_REQUEST_PENDING)) {
                this.status.setRequestPending(true);
            }
            if (request.getBoolean(PassThrough.STATE_WAITING_REPLY)) {
                this.status.setWaitingReply(true);
            }
        }
        this.status.setWaitingProcess(false);
    }

    @Override
    public void handleRequest(DataSet request) throws ProcessException {
        String from = request.getString(Context.MESSAGE);
        String to = this.messageMap.getString(from);
        if (to == null) {
            //if (!this.allowAnonymous) {
                throw new ProcessException("Unknown service", request);
            //}
            //to = from;
            //this.messageMap.put(from,to);
        }
        int sid = ++this.lastSid;
        DataSet data = new SimpleDataSet()
				.put(Context.REQUEST, request)
				.put(Context.SOURCE_REF, sid)
				.put(Context.SERVICE,to)
				.put(PassThrough.STATE_REQUEST_PENDING,true)
				.put(PassThrough.STATE_WAITING_REPLY,false)
				.put(PassThrough.STATE_REPLY_READY,false);
        this.requests.put(String.valueOf(sid),data);
        this.status.setWaitingProcess(true);
    }

    @Override
    public void handleReply(DataSet reply) throws ProcessException {
        if (!this.status.waitingReply()) {
            throw new ProcessException("No replies waiting");
        }

        DataSet request = this.requests.getDataSet(String.valueOf(
				reply.getDataSet(Context.SOURCE).getInteger(Context.SOURCE_REF)))
			.put(reply.get(Context.REPLY))
			.put(reply.get(Context.RETURN))
			.put(reply.get(Context.CLOSE))
			.put(PassThrough.STATE_WAITING_REPLY,false) // maybe not?
			.put(PassThrough.STATE_REPLY_READY,true);

        this.status.setWaitingProcess(true);
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

	@Override
	public DataSet getMessageData()
	{
		throw new UnsupportedOperationException("lexa.core.server.process.PassThrough.getMessageData:lexa.core.data.DataSet not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
