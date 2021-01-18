package enegade.uml.debugger.loopexit;

/*
 * Copyright 2020 enegade
 *
 * This file is part of the enegade.uml package.
 *
 * This file is distributed "AS IS", WITHOUT ANY WARRANTIES.
 *
 * For the full copyright and license information, please view the LICENSE
 * file distributed with this project.
 */

import enegade.uml.CallGraph;
import enegade.uml.type.MethodCall;

import java.util.List;
import java.util.Optional;

/**
 * @author enegade
 */
public class LoopDetector
{
    private boolean found = false;

    private MethodCall loopStartingMethodCall;

    private MethodCall loopEndingMethodCall;

    private CallGraph callGraph;

    public LoopDetector(CallGraph callGraph)
    {
        this.callGraph = callGraph;
    }

    public boolean detectLoop(MethodCall repeatedMethodCall)
    {
        this.loopStartingMethodCall = null;
        this.loopEndingMethodCall = null;
        this.found = false;

        if( repeatedMethodCall.getTypeName().equals("java.lang.ClassLoader") ) {
            if( repeatedMethodCall.getName().equals("loadClass") || repeatedMethodCall.getName().equals("checkPackageAccess") ) {
                return this.found;
            }
        }

        CallGraph.EntranceView currentEntranceView = callGraph.getEntranceView();
        List<CallGraph.EntranceView> entranceViewList = currentEntranceView.getEntrances();
        if (entranceViewList.isEmpty()) {
            return this.found;
        }

        MethodCall loopEndingMethodCallCandidate = entranceViewList.get( entranceViewList.size() - 1 ).getMethodCall().get();
        if ( repeatedMethodCall.getCodeIndex() == 0
                || loopEndingMethodCallCandidate.getCodeIndex() == -1
                || loopEndingMethodCallCandidate.getCodeIndex() < repeatedMethodCall.getCodeIndex() ) {
            return this.found;
        }

        MethodCall loopStartingMethodCallCandidate = null;
        for (CallGraph.EntranceView entranceView : entranceViewList) {
            loopStartingMethodCallCandidate = entranceView.getMethodCall().get();
            if ( loopStartingMethodCallCandidate.getTypeName().equals( repeatedMethodCall.getTypeName() )
                    && loopStartingMethodCallCandidate.getName().equals( repeatedMethodCall.getName() )
                    && loopStartingMethodCallCandidate.getCodeIndex() == repeatedMethodCall.getCodeIndex() ) {
                break;
            } else{
                loopStartingMethodCallCandidate = null;
            }
        }

        if (loopStartingMethodCallCandidate != null) {
            this.loopStartingMethodCall = loopStartingMethodCallCandidate;
            this.loopEndingMethodCall = loopEndingMethodCallCandidate;
            this.found = true;
        }

        return this.found;
    }

    public boolean isFound()
    {
        return found;
    }

    public Optional<MethodCall> getLoopStartingMethodCall()
    {
        return Optional.ofNullable(this.loopStartingMethodCall);
    }

    public Optional<MethodCall> getLoopEndingMethodCall()
    {
        return Optional.ofNullable(this.loopEndingMethodCall);
    }
}
