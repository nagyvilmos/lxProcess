/*==============================================================================
 * Lexa - Property of William Norman-Walker
 *------------------------------------------------------------------------------
 * TestProcess.java
 *------------------------------------------------------------------------------
 * Author:  William Norman-Walker
 * Created: February 2017
 *==============================================================================
 */
package lxprocess;

import java.io.File;
import java.io.IOException;
import lexa.core.data.DataItem;
import lexa.core.data.DataSet;
import lexa.core.data.ArrayDataSet;
import lexa.core.data.config.ConfigDataSet;
import lexa.core.data.exception.DataException;
import lexa.core.data.io.DataReader;
import lexa.core.expression.ExpressionException;
import lexa.core.expression.function.FunctionLibrary;
import lexa.core.logging.Logger;
import lexa.core.process.LexaProcess;
import lexa.core.process.ProcessException;
import lexa.core.process.Status;
import lexa.core.process.context.Config;
import lexa.core.process.context.Context;
import lexa.core.process.factory.ProcessFactory;
import lexa.test.TestAnnotation;
import lexa.test.TestClass;
import lexa.test.TestResult;

/**
 * Testing class for the process component
 * @author william
 * @since 2017-02
 */
@TestAnnotation(arguments = "processList", setUp = "setUpProcess", tearDown = "tearDownProcess")
public class TestProcess
        extends TestClass
{

    private ProcessFactory factory;
    private final String fileName;
    private DataSet testData;
    private Logger logger;

    // all these need to be cleared by the teardown:
    private LexaProcess process;
    private DataSet reply;
    private Status status;
    private DataSet testCase;
    private String testName;

    public TestProcess(String fileName)
    {
        this.fileName = fileName;

    }

    public Object[] processList()
            throws IOException
    {
        this.testData = new DataReader(new File(fileName)).read();
        if (this.testData.contains(Config.LOG_FILE))
        {
            lexa.core.logging.Logger.setLogWriter(
                    new File(testData.getString(Config.LOG_FILE)));
        }
        if (testData.contains(Config.LOGGING))
        {
            lexa.core.logging.Logger.logLevels().setLogging(
                    testData.getDataSet(Config.LOGGING));
        }
        this.logger = new Logger("PROCESS_TEST", null);
        this.logger.info("Test config", this.testData);

        String testList = this.testData.getString("test");
        return (testList != null)
                ? testList.split(" ")
                : this.testData.getDataSet("processes").keys();
    }

    public TestResult setUpProcess(Object arg)
    {
        this.testName = (String) arg;
        this.logger.info("Test:" + this.testName);
        this.testCase = this.testData.getDataSet("processes").getDataSet(this.testName);
        this.logger.info("Config", this.testCase);
        return TestResult.isClass("lexa.core.data.ArrayDataSet", this.testCase);
    }

    @TestAnnotation(order = 10)
    public TestResult loadFactory(Object arg) throws ExpressionException, DataException
    {
        DataSet functions = this.testData.getDataSet("functions");
        DataSet testFunctions = this.testCase.getDataSet("functions");
        if (functions != null)
        {
            functions.put(testFunctions);
        } else
        {
            functions = testFunctions;
        }

        FunctionLibrary functionLibrary = new FunctionLibrary(functions);
        ConfigDataSet config = new ConfigDataSet(testCase.getDataSet("process"));

        // if needed we can always get a URLClassLoader to allow
        // the explicit listing of jars to load.
        ClassLoader loader = ClassLoader.getSystemClassLoader();

        this.factory = new ProcessFactory(loader,config, functionLibrary);

        config.close();
        String factoryClass = config.get("factory",
                "lexa.core.process.factory.ProcessFactory").getString();
        return TestResult.assignableTo(factoryClass, this.factory);
    }

    @TestAnnotation(order = 20)
    public TestResult loadProcess(Object arg) throws ProcessException, DataException, ExpressionException
    {
        this.process = this.factory.instance();
        this.status = this.process.getStatus();
        return TestResult.result(true, this.status.active(), "Loaded process is not activated");
    }

    @TestAnnotation(order = 30)
    public TestResult submitRequest(Object arg) throws ProcessException
    {
        process.handleRequest(
                new ArrayDataSet(testCase.getDataSet(Context.MESSAGE))
        );
        return TestResult.result(true);
    }

    @TestAnnotation(order = 40)
    public TestResult waitResponse(Object arg) throws ProcessException
    {
        this.reply = null;
        DataSet forward = null;
        boolean busy = true;
        this.logger.debug("process.start");
        while (busy)
        {
            this.logger.debug("process.status",null, this.status );
            if (this.status.waitingProcess())
            {
                this.process.process();
            } else if (this.status.requestPending())
            {
                // take the request and dummy the reply from the config:
                forward = process.getRequests();
                this.logger.debug("Forward requests", forward);
            } else if (this.status.waitingReply())
            {
                DataSet replies = this.testCase.getDataSet("replies");
                for (DataItem request : forward.getDataSet(Context.MESSAGE_LIST))
                {
                    String mid = request.getKey();
                    if (!replies.contains(mid))
                    {
                        throw new java.lang.UnsupportedOperationException("No reply in replies block for message " + mid);
                    }
                    process.handleReply(replies.getDataSet(mid));
                }
            } else if (this.status.replyReady())
            {
                this.reply = this.process.getReply();
            } else
            {
                busy = false; //this.status.active();
            }
        }
        this.logger.debug("process.end");
        return TestResult.notNull(this.reply);
    }

    @TestAnnotation(order = 50)
    public TestResult validateResponse(Object arg) throws ProcessException, Exception
    {
        logger.info("Reply received", reply);
        return TestResult.result(
                this.testCase.getDataSet("result"),
                this.reply.getDataSet(Context.REPLY),
                "Reply does not match expected result");
    }

    public TestResult tearDownProcess(Object arg)
    {
        this.process = null;
        this.reply = null;
        this.status = null;
        this.testCase = null;
        this.testName = null;
        return TestResult.result(true);
    }
}
