/*==============================================================================
 * Lexa - Property of William Norman-Walker
 *------------------------------------------------------------------------------
 * ProcessFactory.java
 *------------------------------------------------------------------------------
 * Author:  William Norman-Walker
 * Created: April 2013
 *------------------------------------------------------------------------------
 * Change Log
 * Date:        By: Ref:        Description:
 *==============================================================================
 */
package lexa.core.process.factory;

import lexa.core.data.config.ConfigDataSet;
import lexa.core.data.DataSet;
import lexa.core.data.ArrayDataSet;
import lexa.core.data.exception.DataException;
import lexa.core.expression.ExpressionException;
import lexa.core.expression.function.FunctionLibrary;
import lexa.core.logging.Logger;
import lexa.core.process.ProcessException;
import lexa.core.process.LexaProcess;
import lexa.core.process.context.Config;

/**
 * A factory for creating and initialising processes.
 *
 * @author William
 * @since 2013-04
 */
public class ProcessFactory
{
	private final Logger logger;

    private static int lastProcessId = 0;
    private static int getNextProcessId()
    {
        return ++ProcessFactory.lastProcessId;
    }

	private final String classPath;
	private final ProcessLoaderInterface loader;
	private final FunctionLibrary functionLibrary;
	private final DataSet processConfig;

    /**
     * Creates a factory for instantiating processes.
     *
     * <p>The path can be either one of the internal processes or a path and class name
     * in the format {@code drive://path//example.jar#com.example.ExampleProcess}.
     *
	 * @param classLoader
     *          the loader for the class
     * @param   config
     *          the configuration required by the process factory
	 * @param functionLibrary
     *          Expression functions for the process to use
     * @throws  DataException
     *          when an exception occurs reading the configuration.
     */
    public ProcessFactory(ClassLoader classLoader,
            ConfigDataSet config,
			FunctionLibrary functionLibrary)
				throws DataException
	{
        this.logger = new Logger(ProcessFactory.class.getSimpleName(),
                config.getString(Config.CLASS_PATH));
        this.classPath = config.getString(Config.CLASS_PATH);
        this.processConfig = new ArrayDataSet(
                config.get(Config.CONFIG, null).getDataSet());
        this.functionLibrary = functionLibrary;
        String loaderPath=config.get(Config.CLASS_LOADER,
                    InternalLoader.class.getCanonicalName()).getString();
		try
		{
			Class c = classLoader.loadClass(loaderPath);
			this.loader = (ProcessLoaderInterface)c.newInstance();
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex)
		{
			String msg = "Unable to instantiate the class loader @ " +  loaderPath;
			this.logger.error(msg,processConfig, ex, config);
			throw new DataException(classPath);
		}
		this.loader.initialise(classPath);
    }

	/**
	 * Get a new initialised instance of the process.
     * <p>A new instance of the process is instantiated and
     * then given its configuration via a call to
     * {@link LexaProcess#initialise(lexa.core.expression.function.FunctionLibrary, lexa.core.data.config.ConfigDataSet)
     * initialise(FunctionLibrary, ConfigData)}.
     *
     * @return  a new initialised instance of the process
     *
     * @throws  ProcessException
     *          when the process failed to initialise
     * @throws  DataException
     *          when a problem occurred reading the configuration.
     * @throws  ExpressionException
     *          when a problem occurred evaluating an expression.
     */
    public LexaProcess instance()
            throws ProcessException,
                    DataException,
                    ExpressionException
    {
        LexaProcess process = this.loader.getInstance();
        ConfigDataSet config = new ConfigDataSet(this.processConfig);
        process.initialise(this.functionLibrary,config);
        config.close();
        process.setId(ProcessFactory.getNextProcessId());
        return process;
    }
}
