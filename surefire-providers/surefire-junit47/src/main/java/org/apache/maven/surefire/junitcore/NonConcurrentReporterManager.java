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

import org.apache.maven.surefire.report.ConsoleLogger;
import org.apache.maven.surefire.report.ConsoleOutputReceiver;
import org.apache.maven.surefire.report.ReportEntry;
import org.apache.maven.surefire.report.ReporterFactory;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.testset.TestSetFailedException;

import java.util.Map;

/**
 * A class to be used when there is no JUnit parallelism (methods or/and class). This
 * allow to workaround JUnit limitation a la Junit4 provider. Specifically, we can redirect
 * properly the output even if we don't have class demarcation in JUnit. It works when
 * if there is a JVM instance per test run, i.e. with forkMode=always or perthread.
 */
public class NonConcurrentReporterManager extends ClassesParallelRunListener {
    private RunListener runListener;

    /**
     * Return the runListener. There is only one for the test, as it's used when there is only one class test
     *  to execute.
     */
    @Override
    RunListener getRunListener() {
        return runListener;
    }


    @Override
    public synchronized void writeTestOutput(byte[] buf, int off, int len, boolean stdout) {
        // We can write immediately: no parallelism and a single class.
        ((ConsoleOutputReceiver) getRunListener()).writeTestOutput(buf, off, len, stdout);
    }


    public NonConcurrentReporterManager(Map<String, TestSet> classMethodCounts, ReporterFactory reporterFactory, ConsoleLogger consoleLogger)
            throws TestSetFailedException {
        super(classMethodCounts, reporterFactory, consoleLogger);
        runListener = reporterFactory.createReporter();
    }
}
