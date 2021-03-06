package org.cru.redegg.reporting;

import com.google.common.base.Throwables;
import org.cru.redegg.qualifier.Fallback;
import org.cru.redegg.reporting.api.ErrorQueue;
import org.cru.redegg.reporting.api.ErrorReporter;
import org.cru.redegg.util.ErrorLog;
import org.cru.redegg.util.ProxyConstructor;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Matt Drees
 */
@ApplicationScoped
public class InMemoryErrorQueue implements ErrorQueue
{

    private final ErrorReporter primaryErrorReporter;

    private final ErrorReporter fallbackReporter;

    private final ErrorLog errorLog;

    private final ExecutorService executorService;

    @Inject
    public InMemoryErrorQueue(
        ErrorReporter primaryErrorReporter,
        @Fallback ErrorReporter fallbackReporter,
        ErrorLog errorLog)
    {
        this.primaryErrorReporter = primaryErrorReporter;
        this.fallbackReporter = fallbackReporter;
        this.errorLog = errorLog;

        //TODO: this should probably be configurable
        executorService = new ThreadPoolExecutor(
            0,
            10,
            5,
            TimeUnit.MINUTES,
            new ArrayBlockingQueue<Runnable>(1000));
    }

    @ProxyConstructor
    @SuppressWarnings("UnusedDeclaration")
    InMemoryErrorQueue() {
        this.primaryErrorReporter = null;
        this.fallbackReporter = null;
        this.errorLog = null;
        this.executorService = null;
    }

    @PreDestroy
    public void shutdown()
    {
        executorService.shutdown();
        try
        {
            int timeout = 20;
            TimeUnit timeUnit = TimeUnit.SECONDS;
            boolean completed = executorService.awaitTermination(timeout, timeUnit);
            if (!completed)
                errorLog.warn("unable to shut down report executor within " + timeout + " " + timeUnit.name().toLowerCase());
        }
        catch (InterruptedException e)
        {
            errorLog.error("report executor shutdown interrupted", e);
            // Lifecycle interceptor methods may not throw checked exceptions.
            // So to preserve the interruption, we have to set the interrupt flag.
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void enqueue(final ErrorReport report)
    {
        executorService.submit(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    primaryErrorReporter.send(report);
                }
                catch (Throwable t)
                {
                    errorLog.error("unable to send error report; using fallback reporter", t);

                    try
                    {
                        fallbackReporter.send(report);
                    }
                    catch (Throwable t2)
                    {
                        errorLog.error("unable to send error report with fallback reporter", t2);
                        //swallow t2
                    }

                    //propagate to kill this thread if necessary
                    throw Throwables.propagate(t);
                }
            }
        });
    }
}
