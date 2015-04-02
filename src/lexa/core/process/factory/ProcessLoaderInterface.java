/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lexa.core.process.factory;

import lexa.core.data.exception.DataException;
import lexa.core.process.LexaProcess;
/**
 *
 * @author william
 */
public interface ProcessLoaderInterface
{
	void initialise(String classPath)
			throws DataException;
	LexaProcess getInstance()
			throws DataException;
}
