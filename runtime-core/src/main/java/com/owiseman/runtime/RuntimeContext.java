package com.owiseman.runtime;

import com.owiseman.runtime.event.EventBus;
import com.owiseman.runtime.lifecycle.LifecycleManager;
import com.owiseman.runtime.task.TaskScheduler;
import com.owiseman.runtime.task.VirtualThreadPool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class RuntimeContext {

    private static final Logger LOG = Logger.getLogger(RuntimeContext.class.getName());

    private static volatile RuntimeContext instance;

    private final LifecycleManager lifecycleManager;
    private final EventBus eventBus;
    private final TaskScheduler taskScheduler;
    private final VirtualThreadPool threadPool;
    private final Map<String, Object> contextData;
    private volatile boolean initialized;

    private RuntimeContext() {
        this.lifecycleManager = new LifecycleManager();
        this.eventBus = new EventBus();
        this.taskScheduler = new TaskScheduler();
        this.threadPool = new VirtualThreadPool();
        this.contextData = new ConcurrentHashMap<>();
        this.initialized = false;
    }

    public static RuntimeContext initialize() {
        if (instance == null) {
            synchronized (RuntimeContext.class) {
                if (instance == null) {
                    instance = new RuntimeContext();
                    instance.doInitialize();
                }
            }
        }
        return instance;
    }

    public static RuntimeContext getInstance() {
        if (instance == null) {
            throw new IllegalStateException("RuntimeContext not initialized. Call initialize() first.");
        }
        return instance;
    }

    private void doInitialize() {
        LOG.info("Initializing RuntimeContext...");
        threadPool.start();
        taskScheduler.start(threadPool);
        lifecycleManager.transitionTo(LifecycleManager.State.INITIALIZED);
        initialized = true;
        LOG.info("RuntimeContext initialized successfully");
    }

    public void shutdown() {
        LOG.info("Shutting down RuntimeContext...");
        lifecycleManager.transitionTo(LifecycleManager.State.SHUTTING_DOWN);
        taskScheduler.stop();
        threadPool.stop();
        lifecycleManager.transitionTo(LifecycleManager.State.TERMINATED);
        initialized = false;
        LOG.info("RuntimeContext shut down complete");
    }

    public LifecycleManager lifecycleManager() { return lifecycleManager; }
    public EventBus eventBus() { return eventBus; }
    public TaskScheduler taskScheduler() { return taskScheduler; }
    public VirtualThreadPool threadPool() { return threadPool; }

    public void putContextData(String key, Object value) {
        contextData.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getContextData(String key) {
        return (T) contextData.get(key);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public static boolean isInitializedStatic() {
        return instance != null && instance.initialized;
    }
}
