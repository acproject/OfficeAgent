package com.owiseman.runtime.lifecycle;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public final class LifecycleManager {

    public enum State {
        CREATED,
        INITIALIZING,
        INITIALIZED,
        RUNNING,
        PAUSED,
        SHUTTING_DOWN,
        TERMINATED
    }

    private static final Logger LOG = Logger.getLogger(LifecycleManager.class.getName());

    private final AtomicReference<State> currentState = new AtomicReference<>(State.CREATED);

    public State currentState() {
        return currentState.get();
    }

    public void transitionTo(State target) {
        State current = currentState.get();
        if (!isValidTransition(current, target)) {
            throw new IllegalStateException(
                    "Invalid state transition: " + current + " -> " + target);
        }
        currentState.set(target);
        LOG.info("Lifecycle transition: " + current + " -> " + target);
    }

    public boolean compareAndTransition(State expected, State target) {
        if (!isValidTransition(expected, target)) {
            return false;
        }
        return currentState.compareAndSet(expected, target);
    }

    private boolean isValidTransition(State from, State to) {
        if (from == to) return true;
        return switch (from) {
            case CREATED -> to == State.INITIALIZING;
            case INITIALIZING -> to == State.INITIALIZED || to == State.SHUTTING_DOWN;
            case INITIALIZED -> to == State.RUNNING || to == State.SHUTTING_DOWN;
            case RUNNING -> to == State.PAUSED || to == State.SHUTTING_DOWN;
            case PAUSED -> to == State.RUNNING || to == State.SHUTTING_DOWN;
            case SHUTTING_DOWN -> to == State.TERMINATED;
            case TERMINATED -> false;
        };
    }

    public boolean isRunning() {
        State s = currentState.get();
        return s == State.RUNNING || s == State.INITIALIZED;
    }

    public boolean isTerminated() {
        return currentState.get() == State.TERMINATED;
    }

    public void awaitTermination() throws InterruptedException {
        while (currentState.get() != State.TERMINATED) {
            Thread.sleep(100);
        }
    }
}
