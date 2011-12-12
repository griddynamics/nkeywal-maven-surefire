package org.apache.maven.surefire.report;

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Base class for file reporters.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public abstract class AbstractFileReporter
    extends AbstractTextReporter
{
    private final File reportsDirectory;

    private final boolean deleteOnStarting;

    private final String reportNameSuffix;

    AbstractFileReporter( boolean trimStackTrace, String format, File reportsDirectory )
    {
        this( trimStackTrace, format, reportsDirectory, null );
    }
    
    AbstractFileReporter( boolean trimStackTrace, String format, File reportsDirectory, String reportNameSuffix )
    {
        super( trimStackTrace, format );
        this.reportsDirectory = reportsDirectory;
        this.deleteOnStarting = false;
        this.reportNameSuffix = reportNameSuffix;
    }

    public void testSetStarting( ReportEntry report )
        throws ReporterException
    {
        super.testSetStarting( report );

        File reportFile = getReportFile( reportsDirectory, report.getName(), reportNameSuffix, ".txt" );

        File reportDir = reportFile.getParentFile();

        //noinspection ResultOfMethodCallIgnored
        reportDir.mkdirs();

        if ( deleteOnStarting && reportFile.exists() )
        {
            //noinspection ResultOfMethodCallIgnored
            reportFile.delete();
        }
      //System.err.println(this.hashCode()+"-"+getClass().getName() +"#testSetStarting: deleted reportFile "+reportFile.getName());

        try
        {
            PrintWriter writer = new PrintWriter( new FileWriter( reportFile ) );
          //System.err.println(getClass().getName() +"#testSetStarting: created reportFile "+reportFile.getName());

            writer.println( "-------------------------------------------------------------------------------" );

            writer.println( "Test set: " + report.getName() );

            writer.println( "-------------------------------------------------------------------------------" );

            setWriter(writer);
        }
        catch ( IOException e )
        {
            throw new ReporterException( "Unable to create file for report: " + e.getMessage(), e );
        }
    }

    public static File getReportFile( File reportsDirectory, String reportEntryName, 
            String reportNameSuffix, String fileExtension )
    {
        File reportFile;
        //System.err.println(" reportEntryName="+reportEntryName+ " reportNameSuffix="+reportNameSuffix+ " reportsDirectory="+reportsDirectory+ " fileExtension="+fileExtension);

        if ( reportNameSuffix != null && reportNameSuffix.length() > 0)
        {
            reportFile = new File( reportsDirectory, reportEntryName + "-" + reportNameSuffix + fileExtension );
        }
        else
        {
            reportFile = new File( reportsDirectory, reportEntryName + fileExtension );
        }
        return reportFile;
    }

    public void testSetCompleted( ReportEntry report )
        throws ReporterException
    {
        super.testSetCompleted( report );

      if (writer == null ){
        throw new NullPointerException(
          this.hashCode()+"-"+getClass().getName() + ":testSetCompleted: writer is null for report "+
            report.getName()+" - this error should have not impact"
        ) ;
      }else {
        //System.err.println(this.hashCode()+"-"+getClass().getName() +":testSetCompleted Gonna close the writer for report "+report.getName());
      }



        writer.flush();
        writer.close();
        writer = null;
    }
}
