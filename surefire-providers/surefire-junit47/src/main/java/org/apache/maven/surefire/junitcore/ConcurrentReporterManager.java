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

import java.util.Map;
import org.apache.maven.surefire.report.ConsoleLogger;
import org.apache.maven.surefire.report.ConsoleOutputReceiver;
import org.apache.maven.surefire.report.ReportEntry;
import org.apache.maven.surefire.report.ReporterFactory;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.testset.TestSetFailedException;

/**
 * Handles responses from concurrent junit
 *
 * Stuff to remember about JUnit threading:
 * parallel=classes; beforeClass/afterClass, constructor and all tests method run on same thread
 * parallel=methods; beforeClass/afterClass run on main thread, constructor + each test method run on same thread
 * parallel=both; same as parallel=methods
 *
 * @author Kristian Rosenvold
 */
public abstract class ConcurrentReporterManager
    implements RunListener, ConsoleOutputReceiver
{
    private final Map<String, TestSet> classMethodCounts;
    // private final ReporterConfiguration reporterConfiguration;

    private final ThreadLocal<RunListener> reporterManagerThreadLocal = new ThreadLocal<RunListener>();

    private final boolean reportImmediately;

    private final ReporterFactory reporterFactory;

    private final ConsoleLogger consoleLogger;

    private long startTime;
    private long endTime;

    ConcurrentReporterManager( ReporterFactory reporterFactory, ConsoleLogger consoleLogger, boolean reportImmediately,
                               Map<String, TestSet> classMethodCounts )
        throws TestSetFailedException
    {
        this.reportImmediately = reportImmediately;
        this.reporterFactory = reporterFactory;
        this.classMethodCounts = classMethodCounts;
        this.consoleLogger = consoleLogger;
    }

    public void testSetStarting( ReportEntry description )
    {
      startTime = System.currentTimeMillis();
    }

    public void testSetCompleted( ReportEntry result )
    {
        endTime = System.currentTimeMillis();
        final RunListener reporterManager = getRunListener();
        for ( TestSet testSet : classMethodCounts.values() )
        {
            testSet.replay( reporterManager, (int)(endTime - startTime) );
        }
    }

    public void testFailed( ReportEntry failure )
    {
        getOrCreateTestMethod( failure ).testFailure( failure );
    }

    public void testError( ReportEntry failure )
    {
        final TestMethod testMethod = getOrCreateTestMethod( failure );
        if ( testMethod != null )
        {
            testMethod.testError( failure );
        }
    }

    public void testSkipped( ReportEntry description )
    {
        TestSet testSet = getTestSet( description );
        TestMethod testMethod = getTestSet( description ).createTestMethod( description );
        testMethod.testIgnored( description );
        testSet.incrementFinishedTests( getRunListener(), reportImmediately );
    }

    public void testAssumptionFailure( ReportEntry failure )
    {
        getOrCreateTestMethod( failure ).testIgnored( failure );
    }

    public void testStarting( ReportEntry description )
    {
        TestSet testSet = getTestSet( description );
        final TestMethod testMethod = testSet.createTestMethod( description );
        testMethod.attachToThread();

        checkIfTestSetCanBeReported( testSet );
        testSet.attachToThread();
    }

    public void testSucceeded( ReportEntry report )
    {
        getTestMethod().testFinished();
        TestSet.getThreadTestSet().incrementFinishedTests( getRunListener(), reportImmediately );
        detachTestMethodFromThread();
    }

    private TestMethod getOrCreateTestMethod( ReportEntry description )
    {
        TestMethod threadTestMethod = TestMethod.getThreadTestMethod();
        if ( threadTestMethod != null )
        {
            return threadTestMethod;
        }
        TestSet testSet = getTestSet( description );
        if ( testSet == null )
        {
            consoleLogger.info( description.getName() );
            consoleLogger.info( description.getStackTraceWriter().writeTraceToString() );
            return null;
        }
        else
        {
            return testSet.createTestMethod( description );
        }
    }

    protected abstract void checkIfTestSetCanBeReported( TestSet testSetForTest );

    TestMethod getTestMethod()
    {
        return TestMethod.getThreadTestMethod();
    }

    void detachTestMethodFromThread()
    {
        TestMethod.detachFromCurrentThread();
    }

    TestSet getTestSet( ReportEntry description )
    {
        return classMethodCounts.get( description.getSourceName() );
    }

    RunListener getRunListener()
    {
        RunListener runListener = reporterManagerThreadLocal.get();
        if ( runListener == null )
        {
            runListener = reporterFactory.createReporter();
            reporterManagerThreadLocal.set( runListener );
        }
        return runListener;
    }

    public static ConcurrentReporterManager createInstance( Map<String, TestSet> classMethodCounts,
                                                            ReporterFactory reporterManagerFactory,
                                                            boolean parallelClasses, boolean parallelBoth,
                                                            ConsoleLogger consoleLogger )
        throws TestSetFailedException
    {
        if ( parallelClasses )
        {
            return new ClassesParallelRunListener( classMethodCounts, reporterManagerFactory, consoleLogger );
        }
        return new MethodsParallelRunListener( classMethodCounts, reporterManagerFactory, !parallelBoth,
                                               consoleLogger );
    }

    // The logical stream to use when we don't have yet a test method
    // => We will attach this data to the first test method
    private LogicalStream preMethodlogicalStream;
    // The logical stream to use when we don't have anymore a test method
    //  => we will attach the new data to the previous one
    private LogicalStream postMethodlogicalStream;
    public void writeTestOutput( byte[] buf, int off, int len, boolean stdout )
    {
        TestMethod threadTestMethod = TestMethod.getThreadTestMethod();
        LogicalStream logicalStream;
        if ( threadTestMethod != null )
        {
            logicalStream = threadTestMethod.getLogicalStream(preMethodlogicalStream);
            preMethodlogicalStream = null;
            postMethodlogicalStream = logicalStream;
        } else {
            if (postMethodlogicalStream != null){
                logicalStream = postMethodlogicalStream;
            }   else {
              if (preMethodlogicalStream == null){
                  preMethodlogicalStream = new LogicalStream();
              }
              logicalStream = preMethodlogicalStream;
            }
        }

        logicalStream.write( stdout, buf, off, len );
    }

}
