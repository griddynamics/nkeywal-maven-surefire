package org.apache.maven.surefire.junitcore;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.surefire.common.junit4.JUnit4RunListener;
import org.apache.maven.surefire.report.*;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.junit.runner.Description;
import org.junit.runner.Result;

/**
 * A class to be used when there is no JUnit parallelism (methods or/and class). This
 * allow to workaround JUnit limitation a la Junit4 provider. Specifically, we can redirect
 * properly the output even if we don't have class demarcation in JUnit. It works when
 * if there is a JVM instance per test run, i.e. with forkMode=always or perthread.
 */
public class NonConcurrentReporterManager extends JUnit4RunListener implements ConsoleOutputReceiver {
    private ConsoleLogger consoleLogger;
    private long startTime;
    private SimpleReportEntry report;
    private String testedClassName;

    public synchronized void writeTestOutput(byte[] buf, int off, int len, boolean stdout) {
        // We can write immediately: no parallelism and a single class.
        ((ConsoleOutputReceiver) reporter).writeTestOutput(buf, off, len, stdout);
    }

    @Override
    protected SimpleReportEntry createReportEntry( Description description )
    {
        return new SimpleReportEntry( testedClassName, testedClassName, 0 );
    }

    @Override
    public String extractClassName( Description description )
    {
        return testedClassName;
    }


        @Override
    public void testRunStarted( Description description )
            throws Exception
    {
        startTime = System.currentTimeMillis();
        report = createReportEntry( description );
        reporter.testSetStarting( report );
    }

    @Override
    public void testRunFinished( Result result )
            throws Exception
    {
        final long elapsed = System.currentTimeMillis() - startTime;
        SimpleReportEntry sre = new SimpleReportEntry( report.getSourceName(), report.getName(), (int)elapsed);
        reporter.testSetCompleted( sre );
    }

    public NonConcurrentReporterManager(Class<?> testClass, ReporterFactory reporterFactory, ConsoleLogger consoleLogger)
            throws TestSetFailedException {
        super(reporterFactory.createReporter());
        this.consoleLogger = consoleLogger;
        // We need this because we could be called with a filtering by category that would filter everything, and
        //  then the Description in testRunStarted would be empty.
        this.testedClassName = testClass.getName();
    }
}
