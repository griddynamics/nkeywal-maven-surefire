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

import java.util.ArrayList;
import java.util.Map;
import org.apache.maven.surefire.common.junit4.JUnit4RunListener;
import org.apache.maven.surefire.report.ReportEntry;
import org.apache.maven.surefire.report.RunListener;

import org.apache.maven.surefire.report.SimpleReportEntry;
import org.apache.maven.surefire.report.StackTraceWriter;
import org.junit.runner.Description;
import org.junit.runner.Result;

public class JUnitCoreRunListener
    extends JUnit4RunListener
{
    private final Map<String, TestSet> classMethodCounts;

    /**
     * @param reporter          the report manager to log testing events to
     * @param classMethodCounts A map of methods
     */
    public JUnitCoreRunListener( RunListener reporter, Map<String, TestSet> classMethodCounts )
    {
        super( reporter );
        this.classMethodCounts = classMethodCounts;
    }

    /**
     * Called right before any tests from a specific class are run.
     *
     * @see org.junit.runner.notification.RunListener#testRunStarted(org.junit.runner.Description)
     */
    public void testRunStarted( final Description description )
        throws Exception
    {
        fillTestCountMap( description );

      ReportEntry re = new ReportEntry() {
        public String getSourceName() {
          return description.getMethodName();
        }

        public String getName() {
          return description.getClassName();
        }

        public String getGroup() {
          return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public StackTraceWriter getStackTraceWriter() {
          return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Integer getElapsed() {
          return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getMessage() {
          return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
      };


      reporter.testSetStarting( re ); // Not entirely meaningful as we can see
    }

    @Override
    public void testRunFinished( Result result )
        throws Exception
    {
        reporter.testSetCompleted( null );
    }

    private void fillTestCountMap( Description description )
    {
        final ArrayList<Description> children = description.getChildren();

        TestSet testSet = new TestSet( description );
        String itemTestClassName = null;
        for ( Description item : children )
        {
            if ( item.isTest() && item.getMethodName() != null )
            {
                testSet.incrementTestMethodCount();
                if ( itemTestClassName == null )
                {
                    itemTestClassName = item.getClassName();
                }
            }
            else if ( item.getChildren().size() > 0 )
            {
                fillTestCountMap( item );
            }
            else
            {
                classMethodCounts.put( item.getClassName(), testSet );
            }
        }
        if ( itemTestClassName != null )
        {
            classMethodCounts.put( itemTestClassName, testSet );
        }
    }

}
