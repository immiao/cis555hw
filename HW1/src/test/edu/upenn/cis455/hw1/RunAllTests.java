package test.edu.upenn.cis455.hw1;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RunAllTests extends TestCase {
        @SuppressWarnings("rawtypes")
        public static Test suite() {
                Class[] testClasses = {
                                HttpRequestTest.class,
                                HttpResponseTest.class,
                                HttpSessionTest.class
                };
                return new TestSuite(testClasses);
        }
}
