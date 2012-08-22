package org.apache.maven.surefire.junitcore;


import org.apache.maven.surefire.common.junit4.JUnit4RunListener;
import org.apache.maven.surefire.report.ConsoleLogger;
import org.apache.maven.surefire.report.ConsoleOutputReceiver;
import org.apache.maven.surefire.report.ReportEntry;
import org.apache.maven.surefire.report.ReporterFactory;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.testset.TestSetFailedException;

import java.util.Map;

public class NonConcurrentReporterManager implements org.apache.maven.surefire.report.RunListener, ConsoleOutputReceiver {
  ConsoleLogger consoleLogger;
  RunListener runListener;
  Map<String, TestSet> classMethodCounts;
  TestMethod testMethod;
  TestSet testSet;
  String currentTestClassName = null;

  static private void p(String s) {
    System.out.println("SAY: " + s);
  }


  public NonConcurrentReporterManager(Map<String, TestSet> classMethodCounts, ReporterFactory reporterFactory, ConsoleLogger consoleLogger) {
    p(" NonConcurrentReporterManager: ");
    this.classMethodCounts = classMethodCounts;
    this.consoleLogger = consoleLogger;
    this.runListener = reporterFactory.createReporter();
  }


  public void testSetStarting(ReportEntry report) {
    currentTestClassName = report.getName();
    resetTestSet(null);

    p(" testSetStarting: testSet=" + testSet+" report.getName();="+report.getName());
  }

  public void testSetCompleted(ReportEntry report) {
    p(" testSetCompleted: ");
    for (TestSet testSet : classMethodCounts.values()) {
      testSet.replay(runListener);
    }
  }

  public void resetTestSet(ReportEntry report) {
    TestSet ts = null;

    if (report != null) {
      ts = getTestSet(report);
    }

    if (ts == null && testSet == null && currentTestClassName != null) {
      ts = classMethodCounts.get(currentTestClassName);
    }

    if (ts != null) {
      testSet = ts;
    }

  }

  public void testStarting(ReportEntry description) {
    p(" testStarting: ");
    resetTestSet(description);
    testMethod = testSet.createTestMethod(description);
  }

  public void testSucceeded(ReportEntry report) {
    p(" testSucceeded: ");
    testMethod.testFinished();
    testSet.incrementFinishedTests(runListener, true);
  }

  public void testAssumptionFailure(ReportEntry failure) {
    p(" testAssumptionFailure: ");
    resetTestSet(failure);
    getOrCreateTestMethod(failure).testIgnored(failure);
  }

  public void testError(ReportEntry failure) {
    p(" testError: ");
    resetTestSet(failure);
    testMethod = getOrCreateTestMethod(failure);
    if (testMethod != null) {
      testMethod.testError(failure);
    }
  }

  public void testFailed(ReportEntry failure) {
    p(" testFailed: ");
    resetTestSet(failure);
    getOrCreateTestMethod(failure).testFailure(failure);
  }

  public void testSkipped(ReportEntry description) {
    p(" testSkipped: ");
    resetTestSet(description);
    testMethod = testSet.createTestMethod(description);
    testMethod.testIgnored(description);
    testSet.incrementFinishedTests(runListener, true);
  }

  public void writeTestOutput(byte[] buf, int off, int len, boolean stdout) {
    if (testMethod != null) {
      final LogicalStream logicalStream = testMethod.getLogicalStream();
      logicalStream.write(stdout, buf, off, len);
    } else if (testSet != null) {
      testSet.getClassLevelLogicalStream().write(stdout, buf, off, len);
    } else {
      resetTestSet(null);
      if (testSet != null) {
        testSet.getClassLevelLogicalStream().write(stdout, buf, off, len);
      } else {
        // Not able to assocaite output with any thread. Just dump to console
        consoleLogger.info("DONT: " + new String(buf, off, len));
      }
    }
  }

  // The logical stream to use when we don't have yet a test method
  // => We will attach this data to the first test method
  private LogicalStream preMethodlogicalStream;
  // The logical stream to use when we don't have anymore a test method
  // => we will attach the new data to the previous one
  private LogicalStream postMethodlogicalStream;
  public void writeTestOutput2( byte[] buf, int off, int len, boolean stdout )
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





  TestSet getTestSet(ReportEntry description) {
    TestSet testSet = null;
    if (description != null) {
      testSet = classMethodCounts.get(description.getSourceName());
    }

    if (testSet == null) {
      p("testSet is null");
    }

    return testSet;
  }

  protected TestMethod getOrCreateTestMethod(ReportEntry description) {
    if (testMethod != null) {
      return testMethod;
    }
    resetTestSet(description);
    if (testSet == null) {
      consoleLogger.info(description.getName());
      consoleLogger.info(description.getStackTraceWriter().writeTraceToString());
      return null;
    } else {
      testMethod = testSet.createTestMethod(description);
      return testMethod;
    }
  }

}
