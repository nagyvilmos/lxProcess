/*
 * ================================================================================
 * Lexa - Property of William Norman-Walker
 * --------------------------------------------------------------------------------
 * Process.java
 *--------------------------------------------------------------------------------
 * Author:  William Norman-Walker
 * Created: June 2013
 *--------------------------------------------------------------------------------
 * Change Log
 * Date:        By: Ref:        Description:
 * ----------   --- ----------  --------------------------------------------------
 * -            -   -           -
 *================================================================================
 */
package lexa.core.process;

/**
 * Manage the status of a process.
 * <p>The process can flag its ability to accept tasks or that it is waiting for
 * a task to performed for it.
 * <p>All flags can be toggled except for active and closed which are only ever set.
 * <p>No flag can be set to {@code true} unless the state engine is active.  Once the state is closed,
 * all flags are toggled to {@code false}.
 * <p>The available flags are:
 * <dl>
 * <dt>{@link Status#active()} active</dt>
 *   <dd>The process is active.  Unless this is set to {@code true} no other flag, except for
 *      {@code closed} can be set.</dd>
 * <dt>{@link Status#closed()} closed</dt>
 *   <dd>The process has been closed.  This sets all other flags to {@code false} and nothing can
 *      subsequently be set to {@code true}.</dd>
 * <dt>{@link Status#acceptRequests()} acceptRequests</dt>
 *   <dd>The process can receive new requests.</dd>
 * <dt>{@link Status#replyReady()} replyReady</dt>
 *   <dd>A reply is ready to be sent back to the caller.</dd>
 * <dt>{@link Status#requestPending()} requestPending</dt>
 *   <dd>A request is waiting to be sent onto the process's linked broker.</dd>
 * <dt>{@link Status#waitingProcess()} waitingProcess</dt>
 *   <dd>The process is waiting to perform some processing.</dd>
 * <dt>{@link Status#waitingReply()} waitingReply</dt>
 *   <dd>The process is waiting for data to be returned from the process's linked broker.</dd>
 * </dl>
 *
 * @author William
 * @since 2013-06
 */
public class Status {
    /** process is active */
    private boolean active;
    /** process has been closed */
    private boolean closed;
    /** process can receive new requests */
    private boolean acceptRequests;
    /** reply is ready to be sent back to the caller */
    private boolean replyReady;
    /** request is waiting to be sent */
    private boolean requestPending;
    /** process is waiting to perform some processing */
    private boolean waitingProcess;
    /** process is waiting for data */
    private boolean waitingReply;

    /**
     * Create a new status engine.
     * <p>On creation all flags are set to {@code false}.
     */
    Status () {
        this.active = false;
        this.closed = false;
        this._checkStates();
    }

    /**
     * Implementation for {@link Status#checkStates() checkStates}.
     * This is private so it can be called by the constructor.
     */
    private void _checkStates() {
        if (this.closed) {
            this.active = false;
        }
        this.acceptRequests = this.acceptRequests && this.active;
        this.replyReady = this.replyReady && this.active;
        this.requestPending = this.requestPending && this.active;
        this.waitingProcess = this.waitingProcess && this.active;
        this.waitingReply = this.waitingReply && this.active;

    }

    /**
     * Indicates if requests will be accepted.
     *
     * @return  {@code true} if requests will be accepted
     *          otherwise {@code false}.
     */
    public boolean acceptRequests() {
        return this.acceptRequests;
    }

    /**
     * Indicates if the process is active.
     *
     * @return  {@code true} if the process is active
     *          otherwise {@code false}.
     */
    public boolean active() {
        return this.active;
    }

    /**
     * Check the state of all the flags.
     * <p>If {@link closed() closed} is {@code true} then {@link active() active} is {@code false}.
     * <p>If {@link active() active} is {@code false} then all other flags are {@code false}.
     */
    public void checkStates() {
        this._checkStates();
    }

    /**
     * Indicates if the process is closed.
     *
     * @return  {@code true} if the process is closed
     *          otherwise {@code false}.
     */
    public boolean closed() {
        return this.closed;
    }

    /**
     * Indicates if a reply is ready.
     *
     * @return  {@code true} if a reply is ready
     *          otherwise {@code false}.
     */
    public boolean replyReady() {
        return this.replyReady;
    }

    /**
     * Indicates if a request is pending.
     *
     * @return  {@code true} if a request is pending
     *          otherwise {@code false}.
     */
    public boolean requestPending() {
        return this.requestPending;
    }

    /**
     * Set the state if requests will be accepted.
     * @param   acceptRequests
     *          {@code true} if requests will be accepted
     *          otherwise {@code false}.
     */
    void setAcceptRequests(boolean acceptRequests) {
        this.acceptRequests = acceptRequests;
        this.checkStates();
    }

    /**
     * Set the process to active.
     */
    void setActive() {
        this.active = true;
        this.checkStates();
    }

    /**
     * Set the process closed.
     */
    void setClosed() {
        this.closed = false;
        this.checkStates();
    }

    void setReplyReady(boolean replyReady) {
        this.replyReady = replyReady;
        this.checkStates();
    }

    void setRequestPending(boolean requestPending) {
        this.requestPending = requestPending;
        this.checkStates();
    }

    void setWaitingProcess(boolean waitingProcess) {
        this.waitingProcess = waitingProcess;
        this.checkStates();
    }

    void setWaitingReply(boolean waitingReply) {
        this.waitingReply = waitingReply;
        this.checkStates();
    }

    /**
     * Indicates if waiting to process.
     *
     * @return  {@code true} if waiting to process
     *          otherwise {@code false}.
     */
    public boolean waitingProcess() {
        return this.waitingProcess;
    }

    /**
     * Indicates if waiting for a reply.
     *
     * @return  {@code true} if waiting for a reply
     *          otherwise {@code false}.
     */
    public boolean waitingReply() {
        return this.waitingReply;
    }

    @Override
    public String toString()
    {
        return "Status{" + "active=" + active + ", closed=" + closed + ", acceptRequests=" + acceptRequests + ", replyReady=" + replyReady + ", requestPending=" + requestPending + ", waitingProcess=" + waitingProcess + ", waitingReply=" + waitingReply + '}';
    }
    
    
}
