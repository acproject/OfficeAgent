package com.owiseman.agent.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class MemoryStore {

    private final ConcurrentHashMap<String, List<MemoryEntry>> conversationMemories = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<MemoryEntry> globalMemory = new ConcurrentLinkedQueue<>();
    private final int maxEntriesPerConversation;
    private final int maxGlobalEntries;

    public MemoryStore() {
        this(100, 1000);
    }

    public MemoryStore(int maxEntriesPerConversation, int maxGlobalEntries) {
        this.maxEntriesPerConversation = maxEntriesPerConversation;
        this.maxGlobalEntries = maxGlobalEntries;
    }

    public void addGlobal(MemoryEntry entry) {
        globalMemory.add(entry);
        while (globalMemory.size() > maxGlobalEntries) {
            globalMemory.poll();
        }
    }

    public void addConversation(String conversationId, MemoryEntry entry) {
        conversationMemories.computeIfAbsent(conversationId, k -> new ArrayList<>()).add(entry);
        List<MemoryEntry> entries = conversationMemories.get(conversationId);
        while (entries.size() > maxEntriesPerConversation) {
            entries.removeFirst();
        }
    }

    public List<MemoryEntry> getConversationMemory(String conversationId) {
        List<MemoryEntry> entries = conversationMemories.get(conversationId);
        return entries != null ? Collections.unmodifiableList(entries) : List.of();
    }

    public List<MemoryEntry> getGlobalMemory() {
        return List.copyOf(globalMemory);
    }

    public void clearConversation(String conversationId) {
        conversationMemories.remove(conversationId);
    }

    public void clearAll() {
        conversationMemories.clear();
        globalMemory.clear();
    }

    public record MemoryEntry(String role, String content, long timestamp) {
        public static MemoryEntry of(String role, String content) {
            return new MemoryEntry(role, content, System.currentTimeMillis());
        }
    }
}
