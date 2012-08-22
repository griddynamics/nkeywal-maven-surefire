package org.apache.maven.surefire.junitcore;


import org.apache.maven.surefire.common.junit4.JUnit4RunListener;
import org.apache.maven.surefire.report.ConsoleLogger;
import org.apache.maven.surefire.report.ConsoleOutputCapture;
import org.apache.maven.surefire.report.ConsoleOutputReceiver;
import org.apache.maven.surefire.report.ReporterFactory;
import org.apache.maven.surefire.report.RunListener;

public class NonConcurrentReporterManager2 extends JUnit4RunListener {

  public NonConcurrentReporterManager2(RunListener reporter) {
    super(reporter);
  }
}
