/*
 * =============================================================================
 * Lexa - Property of William Norman-Walker
 * -----------------------------------------------------------------------------
 * ProcessLoaderInterface.java
 *------------------------------------------------------------------------------
 * Author:  William Norman-Walker
 * Created: April 2015
 *------------------------------------------------------------------------------
 * Change Log
 * Date:        By: Ref:        Description:
 * ----------   --- ----------  ------------------------------------------------
 * 2016-08-30   WNW 2016-08     Update the JavaDoc
 *==============================================================================
 */
package lexa.core.process.factory;

import lexa.core.data.exception.DataException;
import lexa.core.process.LexaProcess;
/**
 * Interface for a process loader.
 * <br>
 * A process loader is used by the {@link ProcessFactory} to provide instances
 * of the relevant {@link LexaProcess}.  Once created, the loader is passed a 
 * class path parameter in the initialiser to determine the specific path.
 * @author william
 * @since 2015-04
 */
public interface ProcessLoaderInterface
{
    /**
     * Initialise the process loader.
     * <br>
     * The loader uses the path parameter to determine which class to load.
     * @param classPath the path for the class to be loaded
     * @throws DataException when there is an error with the initialisation
     */
	void initialise(String classPath)
			throws DataException;
    
    /**
     * Get a new instance of the class from the loader
     * <br>
     * Create a new instance of the loader.  Critically this returns an instance
     * that has not been initialised.
     * Any initialisation must be done by the {@link ProcessFactory} using the 
     * loader.
     * 
     * @return an instance of the required class
     * @throws DataException when there is an error with the instantiation
     */
	LexaProcess getInstance()
			throws DataException;
}
