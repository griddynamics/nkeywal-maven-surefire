package org.apache.maven.surefire.junitcore;


import org.apache.maven.surefire.common.junit4.JUnit4RunListener;
import org.apache.maven.surefire.report.ConsoleLogger;
import org.apache.maven.surefire.report.ConsoleOutputCapture;
import org.apache.maven.surefire.report.ConsoleOutputReceiver;
import org.apache.maven.surefire.report.ReporterFactory;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.testset.TestSetFailedException;

import java.util.Map;

public class NonConcurrentReporterManager2 extends ClassesParallelRunListener {


  // The logical stream to use when we don't have yet a test method
  // => We will attach this data to the first test method
  private LogicalStream preMethodlogicalStream;
  // The logical stream to use when we don't have anymore a test method
  // => we will attach the new data to the previous one
  private LogicalStream postMethodlogicalStream;



  public void writeTestOutput( byte[] buf, int off, int len, boolean stdout )
  {
    TestMethod threadTestMethod = TestMethod.getThreadTestMethod();
    LogicalStream logicalStream;
    if ( threadTestMethod != null )
    {
      logicalStream = threadTestMethod.getLogicalStream(); // logicalStream = threadTestMethod.getLogicalStream(preMethodlogicalStream);
      preMethodlogicalStream = null;
      postMethodlogicalStream = logicalStream;
    } else {
      if (postMethodlogicalStream != null){
        logicalStream = postMethodlogicalStream;
      } else {
        if (preMethodlogicalStream == null){
          preMethodlogicalStream = new LogicalStream();
        }
        logicalStream = preMethodlogicalStream;
      }
    }

    logicalStream.write( stdout, buf, off, len );
  }

  public NonConcurrentReporterManager2(Map<String, TestSet> classMethodCounts, ReporterFactory reporterFactory, ConsoleLogger consoleLogger) throws TestSetFailedException {
    super(classMethodCounts, reporterFactory, consoleLogger);
  }
}
