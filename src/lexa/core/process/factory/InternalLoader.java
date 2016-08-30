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
 * 2016-08-30   WNW 2016-08     Throw an exception when initialised with an
 *                              unknown class
 * 2016-08-30   WNW 2016-08     Update the JavaDoc
 *==============================================================================
 */
package lexa.core.process.factory;

import lexa.core.data.exception.DataException;
import lexa.core.process.ConfigProcess;
import lexa.core.process.Echo;
import lexa.core.process.PassThrough;
import lexa.core.process.LexaProcess;
import lexa.core.process.context.Value;

/**
 *
 * @author william
 */
public class InternalLoader
		implements ProcessLoaderInterface
{
	private Loader loader;

	@Override
	public void initialise(String classPath)
			throws DataException
	{
		switch (classPath)
		{
			case Value.CLASS_CONFIG :
			{
				this.loader = () -> new ConfigProcess();
				break;
			}
			case Value.CLASS_ECHO :
			{
				this.loader = () -> new Echo();
				break;
			}
			case Value.CLASS_PASS_THROUGH :
			{
				this.loader = () -> new PassThrough();
				break;
			}
		}
        throw new DataException("Unknown path " + classPath);
	}

	@Override
	public LexaProcess getInstance()
			throws DataException
	{
		return this.loader.get();
	}
	
	private interface Loader
	{
		LexaProcess get();
	}
}
