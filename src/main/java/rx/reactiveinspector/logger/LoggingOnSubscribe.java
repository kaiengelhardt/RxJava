package rx.reactiveinspector.logger;

import java.util.UUID;

import rx.Observable.OnSubscribe;
import rx.Subscriber;

public class LoggingOnSubscribe<T> implements OnSubscribe<T>, Debuggable<T> {
	
	private OnSubscribe<T> onSubscribe;
		
	public LoggingOnSubscribe(OnSubscribe<T> onSubscribe) {
		this.onSubscribe = onSubscribe;
		RxLogger.getSharedLogger().logNodeCreated(this);
	}
	
	@Override
	public void call(Subscriber<? super T> subscriber) {
		onSubscribe.call(subscriber);
	}
	
	// MARK: - Debuggable
	
	private final UUID debugID = UUID.randomUUID();
	
	@Override
	public UUID getDebugID() {
		return debugID;
	}
	
	// MARK: - Private API
	
	public OnSubscribe<T> _getInnerOnSubscribe() {
		return onSubscribe;
	}
	
}
