package org.lwes.listener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.lwes.EventSystemException;
import org.lwes.util.Log;

/**
 * A threaded, queueing event processor. This class requires setting a class to
 * enqueue events (for example, a network listener) and a class to dequeue
 * events (for example, writing to disk).
 * 
 * @author Anthony Molinaro
 * @author Michael P. Lum
 */
public class ThreadedProcessor implements Runnable {
	/* a flag to tell whether or not the thread is running */
	private boolean running = false;

	/* the number of seconds to sleep */
	private int seconds = 30;

	/* the thread placing events into the queue */
	private ThreadedEnqueuer enqueuer = null;

	/* the thread dispatching events from the queue */
	private ThreadedDequeuer dequeuer = null;

	/* the enqueuer thread */
	private Thread enqueuerThread = null;

	/* the dequeuer thread */
	private Thread dequeuerThread = null;

	/* a watcher thread (myself) */
	private Thread watcherThread = null;

	/* the queue for events */
	private List<QueueElement> queue = null;

	/* the priority for the enqueuing thread */
	int enqueuerPriority = Thread.NORM_PRIORITY;
	
	/* the priority for the dequeuing thread */
	int dequeuerPriority = Thread.NORM_PRIORITY;
	
	/* the priority for the watcher thread */
	int watcherPriority = Thread.MIN_PRIORITY;

	/**
	 * Default constructor.
	 */
	public ThreadedProcessor() {
	}
	
	/**
	 * Gets the enqueuer being used by this event processor
	 * @return the ThreadedEnqueuer being used by this processor
	 */
	public ThreadedEnqueuer getEnqueuer() {
		return this.enqueuer;
	}
	
	/**
	 * Sets the enqueuer to use for this event processor.
	 * @param enqueuer the ThreadedEnqueuer to use
	 */
	public void setEnqueuer(ThreadedEnqueuer enqueuer) {
		this.enqueuer = enqueuer;
	}
	
	/**
	 * Gets the dequeuer being used by this event processor
	 * @return the ThreadedDequeuer being used by this processor
	 */
	public ThreadedDequeuer getDequeuer() {
		return this.dequeuer;
	}
	
	/**
	 * Sets the dequeuer to use for this event processor.
	 * @param dequeuer the ThreadedDequeuer to use
	 */
	public void setDequeuer(ThreadedDequeuer dequeuer) {
		this.dequeuer = dequeuer;
	}

	/**
	 * Returns the List being used as the queue
	 * @return the List object
	 */
	public synchronized List<QueueElement> getQueue() {
		return this.queue;
	}
	
	/**
	 * Sets the List being used as the queue.
	 * Warning: this list needs to be thread-synchronized!
	 * @param queue the List to use for this processor
	 */
	public synchronized void setQueue(List<QueueElement> queue) {
		this.queue = queue;
	}
	
	/**
	 * Returns the thread priority of the enqueuer.
	 * @return the thread priority
	 */
	public int getEnqueuerPriority() {
		return this.enqueuerPriority;
	}
	
	/**
	 * Sets the thread priority of the enqueuer.
	 * @param priority the thread priority to use
	 */
	public void setEnqueuerPriority(int priority) {
		this.enqueuerPriority = priority;
	}
	
	/**
	 * Returns the thread priority of the dequeuer.
	 * @return the thread priority
	 */
	public int getDequeuerPriority() {
		return this.dequeuerPriority;
	}
	
	/**
	 * Sets the thread priority of the dequeuer
	 * @param priority the thread priority to use
	 */
	public void setDequeuerPriority(int priority) {
		this.dequeuerPriority = priority;
	}
	
	/**
	 * Initializes the processor to handle events. Starts the enqueuer and
	 * dequeuer threads.
	 * 
	 * @throws EventSystemException
	 *             if there is a problem setting up the processor
	 */
	public void initialize() throws EventSystemException {
		if (enqueuer == null) {
			throw new EventSystemException(
					"Event enqueuer is not set, call setEnqueuer() first");
		}

		if (dequeuer == null) {
			throw new EventSystemException(
					"Event dequeuer is not set call setDequeuer() first");
		}

		/* create a queue if it doesn't exist */
		if (queue == null) {
			queue = Collections
					.synchronizedList(new LinkedList<QueueElement>());
		}

		/* make the queue available to the enqueuer and dequeuer */
		dequeuer.setQueue(queue);
		enqueuer.setQueue(queue);

		try {
			dequeuer.initialize();
			dequeuerThread = new Thread(dequeuer, "Dequeueing Thread");
			dequeuerThread.setPriority(dequeuerPriority);
			dequeuerThread.start();

			enqueuer.initialize();
			enqueuerThread = new Thread(enqueuer, "Enqueueing Thread");
			enqueuerThread.setPriority(enqueuerPriority);
			enqueuerThread.start();

			watcherThread = new Thread(this, "Watcher Thread");
			watcherThread.setPriority(watcherPriority);
			watcherThread.start();
		} catch (Exception ie) {
			throw new EventSystemException("Unable to start ThreadedProcessor",
					ie);
		}
	}

	/**
	 * Shuts down the event listener. Stops the enqueuer and dequeuer threads.
	 */
	public synchronized void shutdown() {
		running = false;
		dequeuer.shutdown();
		enqueuer.shutdown();
	}

	/**
	 * The thread's execution loop. Doesn't do much because the enqueue and
	 * dequeue threads do the heavy lifting.
	 */
	public final void run() {
		running = true;
		while (running) {
			try {
				Thread.sleep(seconds * 1000);
			} catch (InterruptedException ie) {
				Log.warning("ThreadedProcessor interrupted", ie);
			}
		}
	}
}