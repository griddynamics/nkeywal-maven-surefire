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

import java.util.List;

/**
 * A reporter that broadcasts to other reporters
 *
 * @author Kristian Rosenvold
 */
public class MulticastingReporter
  implements Reporter {
  private final Reporter[] target;

  private final int size;

  private volatile long lastStartAt;

  public MulticastingReporter(List target) {
    size = target.size();
    this.target = (Reporter[]) target.toArray(new Reporter[target.size()]);
  }

  public void testSetStarting(final ReportEntry report) {
    Exec e = new Exec() {
      public void f(Reporter r) {
        r.testSetStarting(report);
      }
    };
    e.exec();

  }

  public void testSetCompleted(final ReportEntry report) {
    Exec e = new Exec() {
      public void f(Reporter r) {
        r.testSetCompleted(report);
      }
    };
    e.exec();
  }


  public void testStarting(final ReportEntry report) {
    lastStartAt = System.currentTimeMillis();
    Exec e = new Exec() {
      public void f(Reporter r) {
        r.testStarting(report);
      }
    };
    e.exec();
  }

  public void testSucceeded(final ReportEntry report) {
    Exec e = new Exec() {
      ReportEntry wrapped = wrap(report);
      public void f(Reporter r) {
        r.testSucceeded(wrapped);
      }
    };
    e.exec();
  }

  public void testError(final ReportEntry report, final String stdOut,final String stdErr) {
    Exec e = new Exec() {
      ReportEntry wrapped = wrap(report);
      public void f(Reporter r) {
        r.testError(wrapped, stdOut, stdErr);
      }
    };
    e.exec();
  }

  public void testFailed(final ReportEntry report, final String stdOut, final String stdErr) {
    Exec e = new Exec() {
      ReportEntry wrapped = wrap(report);
      public void f(Reporter r) {
        r.testFailed(wrapped, stdOut, stdErr);
      }
    };
    e.exec();
  }

  public void testSkipped(final ReportEntry report) {
    Exec e = new Exec() {
      ReportEntry wrapped = wrap(report);
      public void f(Reporter r) {
        r.testSkipped(wrapped);;
      }
    };
    e.exec();
  }

  private ReportEntry wrap(ReportEntry other) {
    if (other.getElapsed() != null) {
      return other;
    }
    return new CategorizedReportEntry(other.getSourceName(), other.getName(), other.getGroup(),
      other.getStackTraceWriter(), Integer.valueOf(
      (int) (System.currentTimeMillis() - this.lastStartAt)));
  }

  public void writeMessage(final String message) {
    Exec e = new Exec() {
      public void f(Reporter r) {
        r.writeMessage(message);
      }
    };
    e.exec();
  }

  public void writeMessage(final byte[] b, final int off, final int len) {
    Exec e = new Exec() {
      public void f(Reporter r) {
        r.writeMessage(b, off, len);
      }
    };
    e.exec();
  }

  public void reset() {
    Exec e = new Exec() {
      public void f(Reporter r) {
        r.reset();
      }
    };
    e.exec();
  }

  abstract class Exec {
    public abstract void f(Reporter r);

    public void exec() {

      RuntimeException possibleThrowable = null;
      for (int i = 0; i < target.length; i++) {
        try {
          f(target[i]);
        } catch (RuntimeException t) {
          if (possibleThrowable == null) {
            possibleThrowable = t;
          }
        }
      }
      if (possibleThrowable != null) {
        throw possibleThrowable;
      }
    }
  }

}
