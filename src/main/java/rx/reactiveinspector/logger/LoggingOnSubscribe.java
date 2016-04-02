package rx.reactiveinspector.logger;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import rx.Observable.OnSubscribe;
import rx.Subscriber;

/**
 * @author Kai Engelhardt
 * 
 * This class is a wrapper around OnSubscribe.
 * OnSubscribe has a one to one relationship with Observable, so it was
 * easier to wrap this class instead of Observable itself, which has way
 * more methods.
 * 
 * The class manages a global registry table for getting an OnSubscribe by debugID.
 * 
 * @param <T> Just passing the type to OnSubscribe<T>.
 */
public class LoggingOnSubscribe<T> implements OnSubscribe<T>, Debuggable<T> {
	
	@SuppressWarnings("rawtypes") // In this case a raw type is desirable
	private static Map<OnSubscribe, UUID> registryTable = Collections.synchronizedMap(new WeakHashMap<>());
	
	private OnSubscribe<T> onSubscribe;
		
	public LoggingOnSubscribe(OnSubscribe<T> onSubscribe) {
		this.onSubscribe = onSubscribe;
		registryTable.put(onSubscribe, debugID);
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
	
	public static <T> UUID getDebugIDForOnSubscribe(OnSubscribe<T> onSubscribe) {
		return registryTable.get(onSubscribe);
	}
	
	// MARK: - Private API
	
	public OnSubscribe<T> _getInnerOnSubscribe() {
		return onSubscribe;
	}
	
}
