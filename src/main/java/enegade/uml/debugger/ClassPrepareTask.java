package enegade.uml.debugger;

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

import enegade.uml.Config;
import enegade.uml.jdi.ClassPrepareDebuggerEvent;
import enegade.uml.state.*;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * @author enegade
 */
public class ClassPrepareTask implements InitialStateStartingHandler
{
    private Config config;

    public ClassPrepareTask(Config config)
    {
        this.config = config;
    }

    @Override
    public void handleInitialState(StateBuilder stateBuilder)
    {
        String[] classNames = new String[] {
                this.config.getProperty("startup.from.class"),
                this.config.getProperty("startup.to.class"),
        };

        Arrays.stream(classNames)
                .filter(Predicate.not(String::isEmpty))
                .forEach(className -> this.addPrepareRequest(className, stateBuilder));

        if( stateBuilder.getRequestBuilders().isEmpty() ) {
            stateBuilder.setChangeState(false);
        }
    }

    private void addPrepareRequest(String className, StateBuilder stateBuilder)
    {
        TaskStateChanger<ClassPrepareDebuggerEvent> changer = new TaskStateChanger<>(this::handleClassPrepareState, this);

        ClassPrepareRequestBuilder requestBuilder = ClassPrepareRequestBuilder.from()
                .setStateChanger(changer)
                .setEventsRequested(true)
                .addClassFilter(className);

        stateBuilder.addRequest(requestBuilder);
    }

    public void handleClassPrepareState(ClassPrepareDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        stateBuilder.setChangeState(false);
    }
}
