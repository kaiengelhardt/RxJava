package rx.reactiveinspector.logger;

import java.util.UUID;

import rx.Producer;
import rx.Subscriber;

/**
 * @author Kai Engelhardt
 * This class is a wrapper around Subscriber<T>.
 * It implements onNext, onCompleted and onError from the Observer interface
 * to send messages to the debugger. All public non-final methods are implemented
 * and forwarded to the Subscriber class.
 * 
 * The debugger gets the current values and thrown exceptions of a node from here.
 * 
 * @param <T> Just passing the type to Subscriber<T>
 */
public class LoggingSubscriber<T> extends Subscriber<T> implements Debuggable<T> {
	
	private Subscriber<? super T> subscriber;
	
	// MARK: - Logging
	
	public LoggingSubscriber(Subscriber<? super T> subscriber) {
		super(subscriber, true);
		this.subscriber = subscriber;
		RxLogger.getSharedLogger().logNodeCreated(this);
	}
	
	@Override
	public void onCompleted() {
		subscriber.onCompleted();
	}

	@Override
	public void onError(Throwable error) {
		RxLogger.getSharedLogger().logNodeError(this, error);
		subscriber.onError(error);
	}

	@Override
	public void onNext(T next) {
		RxLogger.getSharedLogger().logNodeValueChanged(this, next);
		subscriber.onNext(next);
	}
	
	// MARK: - Proxy
	
	@Override
	public void setProducer(Producer p) {
		subscriber.setProducer(p);
	}
	
	// MARK: - Debuggable
	
	private final UUID debugID = UUID.randomUUID();
	
	@Override
	public UUID getDebugID() {
		return debugID;
	}
	
	// MARK: - Private API
	
	public Subscriber<? super T> _getInnerSubscriber() {
		return subscriber;
	}
		
}
