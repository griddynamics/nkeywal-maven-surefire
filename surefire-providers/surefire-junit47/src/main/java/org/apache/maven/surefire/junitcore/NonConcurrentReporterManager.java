package org.apache.maven.surefire.junitcore;


import org.apache.maven.surefire.report.ConsoleLogger;
import org.apache.maven.surefire.report.ConsoleOutputReceiver;
import org.apache.maven.surefire.report.ReportEntry;
import org.apache.maven.surefire.report.ReporterFactory;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.testset.TestSetFailedException;

import java.util.Map;

/**
 * A class to be used when there is no JUnit parallelism (methods or/and class). This
 *  allow to workaround JUnit limitation a la Junit4 provider. Specifically, we can redirect
 *  properly the output even if we don't have class demarcation in JUnit. It works only
 *  if there is a JVM instance per test run, i.e. with forkMode=always or perthread.
 */
public class NonConcurrentReporterManager extends ClassesParallelRunListener {
  private RunListener runListener;

  @Override
  public synchronized void writeTestOutput( byte[] buf, int off, int len, boolean stdout )
  {
      ((ConsoleOutputReceiver)getRunListener()).writeTestOutput( buf, off, len, stdout );
  }


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
