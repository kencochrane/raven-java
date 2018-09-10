package io.sentry.environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages environment information on Sentry.
 * <p>
 * Manages information related to Sentry Runtime such as the name of the library or
 * whether or not the thread is managed by Sentry.
 */
public final class SentryEnvironment {
    /**
     * Indicates whether the current thread is managed by sentry or not.
     */
    protected static final ThreadLocal<AtomicInteger> SENTRY_THREAD = new ThreadLocal<AtomicInteger>() {
        @Override
        protected AtomicInteger initialValue() {
            return new AtomicInteger();
        }
    };
    private static final Logger logger = LoggerFactory.getLogger(SentryEnvironment.class);
    /**
     * Name of this SDK.
     */
    private static final String SDK_NAME = "sentry-java";
    /**
     * Version of this SDK. Lazily initialized to avoid locating the resource
     * at startup time.
     */
    private static volatile String sdkVersion = null;

    private SentryEnvironment() {
    }

    /**
     * Sets the current thread as managed by Sentry.
     * <p>
     * The logs generated by Threads managed by Sentry will not send logs to Sentry.
     * <p>
     * Recommended usage:
     * <pre>{@code
     * SentryEnvironment.startManagingThread();
     * try {
     *     // Some code that shouldn't generate Sentry logs.
     * } finally {
     *     SentryEnvironment.stopManagingThread();
     * }
     * }</pre>
     */
    public static void startManagingThread() {
        try {
            if (isManagingThread()) {
                logger.warn("Thread already managed by Sentry");
            }
        } finally {
            SENTRY_THREAD.get().incrementAndGet();
        }
    }

    /**
     * Sets the current thread as not managed by Sentry.
     * <p>
     * The logs generated by Threads not managed by Sentry will send logs to Sentry.
     */
    public static void stopManagingThread() {
        try {
            if (!isManagingThread()) {
                //Start managing the thread only to send the warning
                startManagingThread();
                logger.warn("Thread not yet managed by Sentry");
            }
        } finally {
            if (SENTRY_THREAD.get().decrementAndGet() == 0) {
                // Remove the ThreadLocal so we don't log leak warnings on Tomcat.
                // The next get/incr (if any) will re-initialize it to 0.
                SENTRY_THREAD.remove();
            }
        }
    }

    /**
     * Checks whether the current thread is managed by Sentry or not.
     *
     * @return {@code true} if the thread is managed by Sentry, {@code false} otherwise.
     */
    public static boolean isManagingThread() {
        return SENTRY_THREAD.get().get() > 0;
    }

    /**
     * Returns the name of the SDK.
     *
     * @return the name of the SDK.
     */
    public static String getSdkName() {
        return SDK_NAME;
    }

    /**
     * Returns the version of the SDK. Lazily initialized to avoid locating the resource
     * at startup time.
     *
     * @return the version of the SDK.
     */
    public static String getSdkVersion() {
        if (sdkVersion == null) {
            sdkVersion = ResourceBundle.getBundle("sentry-build").getString("build.name");
        }

        return sdkVersion;
    }

    /**
     * Returns SDK `name/version` string, used for HTTP User Agent, sentry_client, etc.
     *
     * @return SDK `name/version` string.
     */
    public static String getSentryName() {
        return getSdkName() + "/" + getSdkVersion();
    }
}
