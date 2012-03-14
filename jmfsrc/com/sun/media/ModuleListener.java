/*
 * @(#)ModuleListener.java	1.14 02/08/21
 *
 * Copyright (c) 1996-2002 Sun Microsystems, Inc.  All rights reserved.
 */

package com.sun.media;

import javax.media.Buffer;
import javax.media.Format;

/**
 * <CODE>ModuleListener</CODE> is an interface for handling asynchronous
 * events generated by <code>Modules</code>.
 *
 * <h2>Java Beans Support </h2>
 * If implementations of this interface are going to be used with
 * Java Beans they need to also implement either java.util.EventListener
 * or sunw.util.EventListener.
 *
 *
 * @see Module
 * 
 * @version 1.14, 02/08/21
 *
 */
public interface ModuleListener {

    /**
     * This method is called when the module has prefetched all the data.
     */
    public void bufferPrefetched(Module from);

    /**
     * This method is called when the module has reached the scheduled
     * stop time.
     */
    public void stopAtTime(Module from);

    /**
     * This method is called when the module has reached the end of media.
     */
    public void mediaEnded(Module from);

    /**
     * This method is called when the module has been resetted.
     */
    public void resetted(Module from);

    /**
     * This method is called when the module detected a data blockage.
     * @param blocked is set to true if the data is blocked, false if the 
     * data has been blocked but have now be released.
     */
    public void dataBlocked(Module from, boolean blocked);

    /**
     * This method is called when a module detects frame delay.
     * The number returned is the # of frames behind detected.
     */
    public void framesBehind(Module from, float framesBehind, InputConnector ic);

    /**
     * This method is called when a buffer with the marker bit set arrives.
     */
    public void markedDataArrived(Module from, Buffer buffer);

    /**
     * This method is called when a format change has occurred and
     * the module has successfully coped with it.
     */
    public void formatChanged(Module from, Format oldFormat, Format newFormat);

    /**
     * This method is called when a format change has occurred and
     * the Module has failed to deal with it.
     */
    public void formatChangedFailure(Module from, Format oldFormat, Format newFormat);

    /**
     * This method is called when the plugin controlled by the
     * module has been closed.
     */
    public void pluginTerminated(Module from);

    /**
     * This method is called when an irrecoverable internal error has 
     * occurred either on the plugin or on the controlling module.
     */
    public void internalErrorOccurred(Module from);

}
