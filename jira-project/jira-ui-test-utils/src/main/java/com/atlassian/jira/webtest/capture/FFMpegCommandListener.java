package com.atlassian.jira.webtest.capture;

/**
 * Can be used to listen to the state of FFmpeg. 
 *
 * @since v4.2
 */
public interface FFMpegCommandListener
{
    void start();
    void outputLine(String line);
    void progress(FFMpegProgressEvent event);
    void end(int exitCode);
}
