package com.getsentry.raven.buffer;

import com.getsentry.raven.connection.AsyncConnection;
import com.getsentry.raven.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;

/**
 * Stores {@link Event} objects to a directory on the filesystem and allows
 * them to be flushed to Sentry (and deleted) at a later time.
 */
public class DiskBuffer implements Buffer {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConnection.class);

    private int maxEvents;
    private File bufferDir;

    public static Buffer newDiskBuffer(File bufferDir, int maxEvents) {
        try {
            bufferDir.mkdirs();
            if (bufferDir.isDirectory() && bufferDir.canWrite()) {
                return new DiskBuffer(bufferDir, maxEvents);
            } else {
                logger.error("Could not create Raven offline event storage directory.");
            }
        } catch (Exception e) {
            logger.error("Could not create Raven offline event storage directory.", e);
        }

        return new NoopBuffer();
    }

    /**
     * Construct an DiskBuffer which stores errors in the specified directory on disk.
     *
     * @param bufferDir File representing directory to store buffered Events in
     * @param maxEvents The maximum number of events to store offline
     */
    private DiskBuffer(File bufferDir, int maxEvents) {
        super();

        this.bufferDir = bufferDir;
        this.maxEvents = maxEvents;

        logger.debug(Integer.toString(getNumStoredEvents()) + " stored events found in '" + bufferDir + "'.");
    }

    /**
     * Store a single event to the add directory. Java serialization is used and each
     * event is stored in a file named by its UUID.
     *
     * @param event Event to store in add directory
     */
    @Override
    public void add(Event event) {
        logger.debug("Adding Event '" + event.getId() + "' to offline storage.");

        if (getNumStoredEvents() >= maxEvents) {
            logger.warn("Skipping Event '" + event.getId() + "' because at least "
                + Integer.toString(maxEvents) + " events are already stored.");
            return;
        }

        String eventPath = bufferDir.getAbsolutePath() + "/" + event.getId();
        try (FileOutputStream fos = new FileOutputStream(new File(eventPath));
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(event);
        } catch (Exception e) {
            logger.error("Error writing Event '" + event.getId() + "' to offline storage.", e);
        }

        logger.debug(Integer.toString(getNumStoredEvents()) + " stored events are now in '" + bufferDir + "'.");
    }

    /**
     * Deletes a buffered {@link Event{ from disk.
     *
     * @param event Event to delete from the disk.
     */
    @Override
    public void discard(Event event) {
        File eventFile = new File(bufferDir, event.getId().toString());
        if (eventFile.exists()) {
            eventFile.delete();
        }
    }

    @Override
    public Iterator<Event> getEvents() {
        // TODO: impl
        return new Iterator<Event>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public Event next() {
                return null;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private int getNumStoredEvents() {
        return bufferDir.listFiles().length;
    }
}
