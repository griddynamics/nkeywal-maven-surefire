package org.apache.maven.surefire.its;


import org.apache.maven.it.VerificationException;

import java.util.List;

public class RunOrderParallelForksIT extends RunOrderIT {

    @Override
    protected String getForkMode(){ return "perThread";}
}
