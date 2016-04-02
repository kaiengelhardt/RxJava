package rx.reactiveinspector.logger;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import rx.Observable.OnSubscribe;
import rx.Subscriber;

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
