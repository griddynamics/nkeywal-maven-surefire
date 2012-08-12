package org.apache.maven.surefire.junitcore;


import org.apache.maven.surefire.report.ConsoleLogger;
import org.apache.maven.surefire.report.ReportEntry;
import org.apache.maven.surefire.report.ReporterFactory;
import org.apache.maven.surefire.testset.TestSetFailedException;

import java.util.Map;

public class NonConcurrentReporterManager extends ConcurrentReporterManager {
  TestMethod threadTestMethod;

  protected TestMethod getOrCreateTestMethod(ReportEntry description) {
    if (threadTestMethod == null) {
      threadTestMethod = TestMethod.getThreadTestMethod();
      if (threadTestMethod == null) {
        TestSet testSet = getTestSet(description);
        if (testSet == null) {
          consoleLogger.info(description.getName());
          consoleLogger.info(description.getStackTraceWriter().writeTraceToString());
        } else {
          threadTestMethod = testSet.createTestMethod(description);
        }
      }
    }
    return threadTestMethod;
  }

  TestMethod getTestMethod() {
    return threadTestMethod;
  }

  void detachTestMethodFromThread() {
    //TestMethod.detachFromCurrentThread();
  }


  public NonConcurrentReporterManager(Map<String, TestSet> classMethodCounts, ReporterFactory reporterFactory, ConsoleLogger consoleLogger) throws TestSetFailedException {
    super(reporterFactory, consoleLogger, true, classMethodCounts);
    System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
  }

  @Override
  protected void checkIfTestSetCanBeReported(TestSet testSetForTest) {
  }


}
