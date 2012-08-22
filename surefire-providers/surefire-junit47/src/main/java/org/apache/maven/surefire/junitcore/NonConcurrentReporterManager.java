package org.apache.maven.surefire.junitcore;


import org.apache.maven.surefire.report.ConsoleLogger;
import org.apache.maven.surefire.report.ConsoleOutputReceiver;
import org.apache.maven.surefire.report.ReportEntry;
import org.apache.maven.surefire.report.ReporterFactory;
import org.apache.maven.surefire.testset.TestSetFailedException;

import java.util.Map;

public class NonConcurrentReporterManager extends ClassesParallelRunListener {
  ConsoleLogger c;

  // The logical stream to use when we don't have yet a test method
  // => We will attach this data to the first test method when we will have one.
  private LogicalStream preMethodlogicalStream;
  // The logical stream to use when we don't have anymore a test method
  // => we will attach the new data to the previous one
  private LogicalStream postMethodlogicalStream;



  public synchronized void writeTestOutput( byte[] buf, int off, int len, boolean stdout )
  {
    if (true){
      ((ConsoleOutputReceiver)getRunListener()).writeTestOutput(buf, off, len, stdout);
      //new LogicalStream.Entry(stdout, buf, off, len ).writeDetails( (ConsoleOutputReceiver)getRunListener() );
      return;
    }

    TestMethod threadTestMethod = TestMethod.getThreadTestMethod();
    LogicalStream logicalStream;
    if ( threadTestMethod != null )
    {
        logicalStream = threadTestMethod.getLogicalStream(preMethodlogicalStream);
        preMethodlogicalStream = null;
        postMethodlogicalStream = logicalStream;
    }
    else
    {
      if (postMethodlogicalStream != null)
      {
          logicalStream = postMethodlogicalStream;
      }
      else
      {
          if (preMethodlogicalStream == null)
          {
              preMethodlogicalStream = new LogicalStream();
          }
          logicalStream = preMethodlogicalStream;
      }
    }

    logicalStream.write( stdout, buf, off, len );
  }

  public void testSetCompleted( ReportEntry result ){
   // System.out.println("AAAAAAAAAAAAAAAAAAAAAAA testSetCompleted");
    if ( preMethodlogicalStream != null )
    {
        // We started to store stuff expecting a method but nothing came.
        //System.out.println("AAAAAAAAAAAAAAaa preMethodlogicalStream");
    }
    super.testSetCompleted(result);
  }

  public NonConcurrentReporterManager(Map<String, TestSet> classMethodCounts, ReporterFactory reporterFactory, ConsoleLogger consoleLogger) throws TestSetFailedException {
    super(classMethodCounts, reporterFactory, consoleLogger);
  }
}
