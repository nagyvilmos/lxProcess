/*==============================================================================
 * Lexa - Property of William Norman-Walker
 *------------------------------------------------------------------------------
 * ServerTest.java
 *------------------------------------------------------------------------------
 * Author:  William Norman-Walker
 * Created: March 2015
 *==============================================================================
 */

package lxprocess;

import lexa.test.TestClass;
import lexa.test.TestRun;

/**
 * Test bed for lxProcess.
 * <p>Uses a {@link lexa.core.data.DataSet} file to store test processes.
 * This should be used like this to run simple tests on single components and
 * not to test fully functional servers.
 * See the file {@code test.process.lexa} to see the full test structure.
 *
 * @author William
 * @since 2013-08
 * @see lexa.core.process
 */
public class ProcessTest {

    public static void main(String ... args) {
        String fileName = "test.process.lexa";
        if (args != null && args.length > 0) {
            fileName = args[0];
        }

        TestClass[] tests = new TestClass[]{
            new TestProcess(fileName)
        };
        System.out.println(
                new TestRun(tests)
                        .execute()
                        .getReport(false, true)
        );
	}
}
