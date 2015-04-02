/*
 * ================================================================================
 * Lexa - Property of William Norman-Walker
 * --------------------------------------------------------------------------------
 * ServerTest.java
 *--------------------------------------------------------------------------------
 * Author:  William Norman-Walker
 * Created: March 2015
 *--------------------------------------------------------------------------------
 * Change Log
 * Date:        By: Ref:        Description:
 * ----------   --- ----------  --------------------------------------------------
 * -
 *================================================================================
 */

package lxprocess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import lexa.core.data.ConfigData;
import lexa.core.data.DataItem;
import lexa.core.data.DataSet;
import lexa.core.data.SimpleDataSet;
import lexa.core.data.io.DataReader;
import lexa.core.expression.function.FunctionLibrary;
import lexa.core.logging.Logger;
import lexa.core.process.LexaProcess;
import lexa.core.process.Status;
import lexa.core.process.context.Config;
import lexa.core.process.context.Context;
import lexa.core.process.factory.InternalLoader;
import lexa.core.process.factory.ProcessFactory;

/**
 * Test bed for lxServer.
 * <p>Uses a {@see DataSet} file to store test servers.  This should be used like this to run simple
 * tests on single components and not to test fully functional servers.
 * See the file {@code config.test} to see the full test structure.
 *
 * @author William
 * @since 2013-08
 * @see lexa.core.server
 */
public class ProcessTest {

    public static void main(String ... args) {
        String fileName = "test.process.lexa";
        if (args != null && args.length > 0) {
            fileName = args[0];
        }
		testCase(fileName);
	}
	
	/**
	Run a test case as defined by a config file.
	<pre>
		logFile - &lt;name of log file; optional&gt;
		test - &lt;list of tests to run, otherwise all tests are run.
		(functions {
		  &lt;global function defs for lxExression; optional&gt;
		})
		processes {
		  &lt;test name&gt; {
			(functions {
			  &lt;function defs for this test; block is optional&gt;
			})
			process {
			  &lt;process config for this test&gt;
			}
	        (replies {
				&lt;message ID&gt; {
				  &lt;reply for the message&gt;
				}
	        })
			message {
			  &lt;message to send for this test&gt;
			}
			result {
			  &lt;expected results&gt; 
			}
		  }
	   }
	</pre>

	@param fileName the name of the config file.
	*/
	public static void testCase(String fileName)
	{
		System.out.println("Test process: " + fileName);
        DataSet file = null;
        try {
            file = new DataReader(new File(fileName)).read();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (file == null) {
            System.err.println("File not found, exiting.");
            return;
        }
        try {
            new ProcessTest(file).testAll();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

    }

    private final Logger logger;
    private final DataSet testData;

    private ProcessTest(DataSet testData)
            throws FileNotFoundException {
        if (testData.contains(Config.LOG_FILE)) {
            lexa.core.logging.Logger.setLogWriter(
                    new File(testData.getString(Config.LOG_FILE)));
        }
        if (testData.contains(Config.LOGGING)) {
            lexa.core.logging.Logger.logLevels().setLogging(
					testData.getDataSet(Config.LOGGING));
        }
        this.logger = new Logger("PROCESS_TEST", null);
        this.testData = testData;
        this.logger.info("Test config", this.testData);
    }

    private void testAll() {
        this.logger.info("Run all tests");
        String testAll = this.testData.getString("test");
        String[] tests =
                (testAll != null) ?
                        testAll.split(" ") :
                        testData.getDataSet("processes").keys();
		for (String test : tests)
		{
			this.test(test);
		}
        this.logger.info("Full test run complete");
    }

    private void test(String testName)  {
        this.logger.info("Test:" + testName);
        DataSet testCase = this.testData.getDataSet("processes").getDataSet(testName);
        this.logger.info("Config", testCase);
        DataSet functions = this.testData.getDataSet("functions");
        DataSet testFunctions = testCase.getDataSet("functions");
        if (functions != null) {
            functions.put(testFunctions);
        } else {
            functions = testFunctions;
        }

        try {
			FunctionLibrary functionLibrary = new FunctionLibrary(functions);
            ConfigData config = new ConfigData(testCase.getDataSet("process"));
			// if needed we can always get a URLClassLoader to allow 
			// the explicit listing of jars to load.
			ClassLoader loader = ClassLoader.getSystemClassLoader();

			ConfigData processConfig = config.contains(Config.CONFIG) ?
						config.getConfigData(Config.CONFIG) : null;
			ProcessFactory factory = new ProcessFactory(
					loader,
					config.getOptionalSetting(Config.CLASS_LOADER,
							InternalLoader.class.getCanonicalName()),
					config.getSetting(Config.CLASS_PATH),
					processConfig, functionLibrary);
			if (processConfig != null) {
				processConfig.close();
			}
			config.close();
			LexaProcess process = factory.instance();
			Status status = process.getStatus();
			process.handleRequest(testCase.getDataSet(Context.MESSAGE).clone());
			DataSet forward = null;
			DataSet reply = null;
			boolean busy = true;
			this.logger.debug("process.start");
			while (busy)
			{
				if (status.requestPending())
				{
					// take the request and dummy the reply from the config:
					forward = process.getRequests();
					this.logger.debug("Forward requests", forward);
				}
				else if (status.waitingReply())
				{
					DataSet replies = testCase.getDataSet("replies");
					for (DataItem request : forward.getDataSet(Context.MESSAGE_LIST))
					{
						String mid = request.getKey();
						if (!replies.contains(mid))
						{
							throw new Exception("No reply in replies block for message " + mid);
						}
						process.handleReply(replies.getDataSet(mid));
					}
				}
				else if (status.waitingProcess())
				{
					process.process();
				}
				else if (status.replyReady())
				{
					reply = process.getReply();
				} 
				else
				{
					busy = false;
				}
			}
			this.logger.debug("process.end");
			if (reply == null)
			{
				throw new Exception("Reply is null");
			}
			logger.info("Reply received", reply);
			if(!reply.getDataSet(Context.REPLY)
					.equals(testCase.getDataSet("result")))
			{
				throw new Exception("Reply does not match expected result");
			}
			logger.info(testName +  " completed OKAY");
		} catch (Exception ex) {
            logger.error("Exception during test", ex);
        } 
	}
}
