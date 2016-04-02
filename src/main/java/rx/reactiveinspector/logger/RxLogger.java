package rx.reactiveinspector.logger;

import de.tuda.stg.reclipse.logger.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.UUID;

/**
 * @author Kai Engelhardt
 * 
 * This class is the central communication path to the Reactive Inspector plugin.
 */
public class RxLogger {
	
	private static RxLogger sharedLogger = new RxLogger();
	
	public static RxLogger getSharedLogger() {
		return sharedLogger;
	}

	private RemoteLoggerInterface remoteLogger;

	private RxLogger() {
		try {
			Registry registry = LocateRegistry.getRegistry();
			RemoteSessionInterface session = (RemoteSessionInterface) registry.lookup("RECLIPSE_LOGGER");
			remoteLogger = session.startSession(getBreakpointInformation());

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					remoteLogger.endSession();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static BreakpointInformation getBreakpointInformation() {
		StackTraceElement[] rawStackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement stackTraceElement = null;
		for (StackTraceElement element : rawStackTrace) {
			String className = element.getClassName();
			if (!(className.startsWith("java") || className.startsWith("scala") || className.startsWith("rx"))) {
				stackTraceElement = element;
				break;
			}
		}
		String className = stackTraceElement.getClassName();		
		
		int lineNumber = stackTraceElement.getLineNumber() + 1;
		String threadName = Thread.currentThread().getName();

		// TODO: Write a new source reader and fix this hard coded path.
		return new BreakpointInformation(".src/test/Test.java", className, lineNumber,threadName);
	}
	
	private <T> ReactiveVariable createReactiveVariable(Debuggable<T> debuggable, DependencyGraphHistoryType historyType) {
		ReactiveVariableType type = ReactiveVariableType.SIGNAL;
		
		if (debuggable instanceof LoggingOnSubscribe) {
			type = ReactiveVariableType.EVENT_HANDLER;
		} else if (debuggable instanceof LoggingSubscriber) {
			type = ReactiveVariableType.SIGNAL;
		}
		
		String simpleVarType = debuggable.getClass().getSimpleName();
		String varType = debuggable.getClass().getName();
		String varName = String.valueOf(debuggable.getDebugID().toString().substring(0, 5));
		
		if (debuggable instanceof LoggingOnSubscribe) {
			LoggingOnSubscribe<T> onSubscribe = (LoggingOnSubscribe<T>) debuggable;
			varType = onSubscribe._getInnerOnSubscribe().getClass().getName();
			simpleVarType = onSubscribe._getInnerOnSubscribe().getClass().getSimpleName();
		} else if (debuggable instanceof LoggingSubscriber) {
			LoggingSubscriber<T> subscriber = (LoggingSubscriber<T>) debuggable;
			varType = subscriber._getInnerSubscriber().getClass().getName();
			simpleVarType = subscriber._getInnerSubscriber().getClass().getSimpleName();
		}
		
		ReactiveVariable variable = new ReactiveVariable(debuggable.getDebugID(), type, -1, historyType, null, simpleVarType, varType, varName, "");
		
		return variable;
	}
	
	// MARK: - Logger
	
	public <T> void logNodeCreated(Debuggable<T> debuggable) {
		ReactiveVariable variable = createReactiveVariable(debuggable, DependencyGraphHistoryType.NODE_CREATED);
		try {
			remoteLogger.logNodeCreated(variable, getBreakpointInformation());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public <T, U> void logNodeAttached(Debuggable<T> source, Debuggable<U> destination) {
		ReactiveVariable variable = createReactiveVariable(source, DependencyGraphHistoryType.NODE_ATTACHED);
		try {
			remoteLogger.logNodeAttached(variable, destination.getDebugID(), getBreakpointInformation());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public <T> void logNodeValueChanged(Debuggable<T> debuggable, T value) {
		ReactiveVariable variable = createReactiveVariable(debuggable, DependencyGraphHistoryType.NODE_VALUE_SET);
		variable.setTypeFull(value.getClass().getName());
		variable.setTypeSimple(value.getClass().getSimpleName());
		variable.setValueString(value.toString());
		try {
			remoteLogger.logNodeValueSet(variable, getBreakpointInformation());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public <T> void logNodeError(Debuggable<T> debuggable, Throwable error) {
		if (error instanceof Exception) {
			Exception exception = (Exception) error;
			ReactiveVariable variable = createReactiveVariable(debuggable, DependencyGraphHistoryType.NODE_EVALUATION_ENDED_WITH_EXCEPTION);
			variable.setExceptionOccured(true);
			try {
				remoteLogger.logNodeEvaluationEndedWithException(variable, exception, getBreakpointInformation());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
}
