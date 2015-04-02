/*
 * ================================================================================
 * Lexa - Property of William Norman-Walker
 * --------------------------------------------------------------------------------
 * ProcessFactory.java
 *--------------------------------------------------------------------------------
 * Author:  William Norman-Walker
 * Created: April 2013
 *--------------------------------------------------------------------------------
 * Change Log
 * Date:        By: Ref:        Description:
 * ----------   --- ----------  --------------------------------------------------
 * 2013-09-04   WNW -           Moved the initialisation of the process in here
 *                              where it belongs.
 * 2014-07-10	WNW				Re-write the factory.  Use feflection to get a 
 *								class loader.
 *================================================================================
 */
package lexa.core.process.factory;

//import java.net.URL;

import lexa.core.data.ConfigData;
import lexa.core.data.DataSet;
import lexa.core.data.exception.DataException;
import lexa.core.expression.ExpressionException;
import lexa.core.expression.function.FunctionLibrary;
import lexa.core.logging.Logger;
import lexa.core.process.ProcessException;
import lexa.core.process.LexaProcess;

//import java.net.URLClassLoader;
//import lexa.core.data.ConfigData;
//import lexa.core.data.DataSet;
//import lexa.core.data.exception.DataException;
//import lexa.core.expression.ExpressionException;
//import lexa.core.expression.ExpressionParser;
//import lexa.core.logging.Logger;
//import lexa.core.server.ProcessException;

/**
 * A factory for creating and initialising processes.
 *
 * @author William
 * @since 2013-04
 */
public class ProcessFactory {
	private final Logger logger;

//    private static int lastProcessId = 0;
//    private static int getNextProcessId() {
//        return ++ProcessFactory.lastProcessId;
//    }
//    private final ExpressionParser parser;
//
//    /**
//     * Defining enum for the type of factory.
//     * <p>If this is for an internal class then it can
//     */
//    private enum FactoryType {
//
//        ECHO("internal:Echo") {
//            @Override
//            Process instance() {
//                return new Echo();
//            }
//        },
//        PASS_THROUGH("internal:PassThrough") {
//            @Override
//            Process instance() {
//                return new PassThrough();
//            }
//        },
//        CONFIG("internal:Config") {
//            @Override
//            Process instance() {
//                return new ConfigProcess();
//            }
//        },
//        EXTERNAL(null) {
//            @Override
//            Process instance() {
//                return null;
//            }
//        };
//        private final String classPath;
//
//        private FactoryType(String classPath) {
//            this.classPath = classPath;
//        }
//
//        abstract Process instance();
//
//        static FactoryType typeFromClassPath(String classPath) {
//            for (int t = 0;
//                    t < FactoryType.values().length;
//                    t++) {
//                if (FactoryType.values()[t].classPath.equals(classPath)) {
//                    return FactoryType.values()[t];
//                }
//            }
//            return FactoryType.EXTERNAL;
//        }
//    }
//
//    private final Logger logger;
//    private final String classPath;
//    /** the configuration, as a {@link DataSet}, for all the processes */
//    private final DataSet processConfig;
//    private final FactoryInterface factory;
//
//    /**
//     * Creates a factory for instantiating processes.
//     *
//     * <p>The path can be either one of the internal processes or a path and class name
//     * in the format {@code drive://path//example.jar#com.example.ExampleProcess}.
//     *
//     * @param   classPath
//     *          the path for the process to be loaded
//     * @param   config
//     *          the configuration required by the process
//     * @param   parser
//     *          Expression parser for the process to use
//     * @throws  DataException
//     *          when an exception occurs reading the configuration.
//     */
//    public ProcessFactory(String classPath, ConfigData config, ExpressionParser parser)
//            throws DataException {
//        this.logger = new Logger(ProcessFactory.class.getSimpleName(), classPath);
//        this.classPath = classPath;
//        FactoryInterface fi;
//        FactoryType ft = FactoryType.typeFromClassPath(classPath);
//        if (ft != FactoryType.EXTERNAL) {
//            fi = new InternalFactory(ft);
//        } else {
//            fi = new ExternalFactory();
//        }
//        this.factory = fi;
//
//        this.parser = (ft != FactoryType.CONFIG) ?
//                null :
//                parser;
//
//        this.processConfig = config == null ?
//                null :
//                config.getAll();
//    }
//
//    /**
//     * Get a new initialised instance of the process.
//     * <p>A new instance of the process is instantiated and
//     * then given its configuration via a call to
//     * {@link Process#initialise(lexa.core.data.ConfigData) initialise(ConfigData)}.
//     *
//     * @return  a new initialised instance of the process
//     *
//     * @throws  ProcessException
//     *          when the process failed to initialise
//     * @throws  DataException
//     *          when a problem occured reading the configuration.
//     */
//    public Process instance()
//            throws ProcessException,
//                    DataException,
//                    ExpressionException {
//        Process process = this.factory.instance();
//        if (this.processConfig == null) {
//            process.initialise(null,null);
//        } else {
//            process.initialise(this.parser,
//                    new ConfigData(this.processConfig));
//        }
//        process.setId(ProcessFactory.getNextProcessId());
//        return process;
//    }
//
//    /**
//     * Interface for the factory builder.
//     */
//    private interface FactoryInterface {
//
//        public Process instance()
//                throws ProcessException;
//    }
//
//    private class InternalFactory
//            implements FactoryInterface {
//
//        private final FactoryType factory;
//
//        InternalFactory(FactoryType factory) {
//            logger.debug("InternalFactory");
//            this.factory = factory;
//        }
//
//        @Override
//        public Process instance() {
//            return this.factory.instance();
//        }
//    }
//
//    private class ExternalFactory
//            implements FactoryInterface {
//
//        private final Class<?> classDefinition;
//
//        private ExternalFactory() {
//            logger.debug("ExternalFactory");
//            Class<?> c = null;
//            String[] split = classPath.split("#");
//            if (split.length != 2) {
//                logger.error("Class path not in the format 'jarPath#className'\n" + classPath);
//                throw new IllegalArgumentException("Class path not in the format 'jarPath#className'");
//            }
//            try {
//                URLClassLoader classLoader = URLClassLoader.newInstance(
//                        new URL[]{new URL(split[0])});
//                c = classLoader.loadClass(split[1]);
//            } catch (Exception ex) {
//                logger.error("Unable to locate class loader for " + classPath, ex);
//                throw new IllegalArgumentException("Unable to locate class loader for " + classPath, ex);
//            }
//            this.classDefinition = c;
//        }
//
//        @Override
//        public Process instance()
//                throws ProcessException{
//            try {
//                return (Process)this.classDefinition.newInstance();
//            } catch (Exception ex) {
//                logger.error("Cannot create new instance for " + classPath, ex);
//                throw new ProcessException("Cannot create new instance for " + classPath,ex);
//            }
//        }
//    }
	
    private static int lastProcessId = 0;
    private static int getNextProcessId() {
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
	 * @param loaderPath
     * @param   classPath
     *          the path for the process to be loaded
     * @param   config
     *          the configuration required by the process
	 * @param functionLibrary
     *          Expression functions for the process to use
     * @throws  DataException
     *          when an exception occurs reading the configuration.
     */
    public ProcessFactory(ClassLoader classLoader,
			String loaderPath,
			String classPath, 
			ConfigData config, 
			FunctionLibrary functionLibrary)
				throws DataException
	{
        this.logger = new Logger(ProcessFactory.class.getSimpleName(), classPath);
        this.classPath = classPath;
        this.processConfig = config == null ?
                null :
                config.getAll();
        this.functionLibrary = functionLibrary;
		try
		{
			Class c = classLoader.loadClass(loaderPath);
			this.loader = (ProcessLoaderInterface)c.newInstance();
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex)
		{
			String msg = "Unable to instantiate the class loader @ " +  loaderPath;
			this.logger.error(msg,processConfig, ex);
			throw new DataException(classPath);
		}
		this.loader.initialise(classPath);
    }

	/**
	 * Get a new initialised instance of the process.
     * <p>A new instance of the process is instantiated and
     * then given its configuration via a call to
     * {@link Process#initialise(lexa.core.data.ConfigData) initialise(ConfigData)}.
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
                    ExpressionException {
        LexaProcess process = this.loader.getInstance();
        if (this.processConfig == null) {
            process.initialise(null,null);
        } else {
            process.initialise(this.functionLibrary,
                    new ConfigData(this.processConfig));
        }
        process.setId(ProcessFactory.getNextProcessId());
        return process;
    }
}
