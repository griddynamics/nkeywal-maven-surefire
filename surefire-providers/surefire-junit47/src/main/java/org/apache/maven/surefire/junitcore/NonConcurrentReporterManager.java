package org.apache.maven.surefire.junitcore;


import org.apache.maven.surefire.report.ConsoleLogger;
import org.apache.maven.surefire.report.ConsoleOutputReceiver;
import org.apache.maven.surefire.report.ReportEntry;
import org.apache.maven.surefire.report.ReporterFactory;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.testset.TestSetFailedException;

import java.util.Map;

public class NonConcurrentReporterManager extends ClassesParallelRunListener {

  @Override
  public synchronized void writeTestOutput( byte[] buf, int off, int len, boolean stdout )
  {
      ((ConsoleOutputReceiver)getRunListener()).writeTestOutput( buf, off, len, stdout );
  }


  private RunListener runListener;
  @Override
  RunListener getRunListener()
  {
    return runListener;
  }

  public NonConcurrentReporterManager(Map<String, TestSet> classMethodCounts, ReporterFactory reporterFactory, ConsoleLogger consoleLogger) throws TestSetFailedException {
    super( classMethodCounts, reporterFactory, consoleLogger );
    runListener = reporterFactory.createReporter();
  }
}
