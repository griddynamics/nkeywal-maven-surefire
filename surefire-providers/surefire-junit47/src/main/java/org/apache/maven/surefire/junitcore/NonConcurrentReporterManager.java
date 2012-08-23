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
import org.junit.runner.notification.Failure;

import java.util.Map;
import java.util.regex.Matcher;

/**
 * A class to be used when there is no JUnit parallelism (methods or/and class). This
 * allow to workaround JUnit limitation a la Junit4 provider. Specifically, we can redirect
 * properly the output even if we don't have class demarcation in JUnit. It works when
 * if there is a JVM instance per test run, i.e. with forkMode=always or perthread.
 */
public class NonConcurrentReporterManager extends JUnit4RunListener implements ConsoleOutputReceiver {

    public synchronized void writeTestOutput(byte[] buf, int off, int len, boolean stdout) {
        // We can write immediately: no parallelism and a single class.
        ((ConsoleOutputReceiver) reporter).writeTestOutput(buf, off, len, stdout);
        //consoleLogger.info( new String( buf, off, len ) );
    }

    @Override
    protected SimpleReportEntry createReportEntry( Description description )
    {
        boolean isJunit3 = description.getTestClass() == null;
        String classNameToUse =
                isJunit3 ? description.getChildren().get( 0 ).getClassName() : description.getClassName();
        return new SimpleReportEntry( classNameToUse, classNameToUse, 0 );
    }


    ReportEntry report;
    @Override
    public void testRunStarted( Description description )
            throws Exception
    {
        report = createReportEntry( null );
        reporter.testSetStarting(report);
    }

    @Override
    public void testRunFinished( Result result )
            throws Exception
    {
        reporter.testSetCompleted( report );
    }

    ConsoleLogger consoleLogger;
    public NonConcurrentReporterManager(ReporterFactory reporterFactory, ConsoleLogger consoleLogger)
            throws TestSetFailedException {
        super(reporterFactory.createReporter());
        this.consoleLogger = consoleLogger;
    }
}
