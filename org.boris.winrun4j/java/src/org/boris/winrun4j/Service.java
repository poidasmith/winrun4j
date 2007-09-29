package org.boris.winrun4j;

/**
 * A windows service.
 */
public interface Service {
    boolean canHandlePowerEvent();
    boolean canPauseAndContinue();
    boolean canShutdown();
    boolean canStop();
    boolean getName();
    void start() throws ServiceException;
    void stop() throws ServiceException;
    void pause() throws ServiceException;
}
