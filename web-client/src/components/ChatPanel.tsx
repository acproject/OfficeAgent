import { useState } from 'react';
import { useAgent } from '../hooks/useAgent';

export default function ChatPanel() {
  const [input, setInput] = useState('');
  const [messages, setMessages] = useState<{ role: string; text: string }[]>([]);
  const { loading, execute } = useAgent();

  const handleSend = async () => {
    if (!input.trim()) return;
    const goal = input;
    setInput('');
    setMessages((prev) => [...prev, { role: 'user', text: goal }]);

    await execute(goal);

    setMessages((prev) => [
      ...prev,
      { role: 'agent', text: loading ? 'Processing...' : 'Task completed' },
    ]);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <div style={{ flex: 1, overflow: 'auto', padding: 16 }}>
        {messages.map((msg, i) => (
          <div
            key={i}
            style={{
              marginBottom: 8,
              padding: 12,
              borderRadius: 8,
              background: msg.role === 'user' ? '#1e3a5f' : '#1a3a2e',
              color: msg.role === 'user' ? '#60a5fa' : '#34d399',
            }}
          >
            <strong>{msg.role === 'user' ? 'You' : 'Agent'}:</strong> {msg.text}
          </div>
        ))}
      </div>
      <div style={{ display: 'flex', gap: 8, padding: 16 }}>
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && !e.shiftKey && handleSend()}
          placeholder="Enter task, e.g.: Help me create a quarterly report PPT..."
          style={{
            flex: 1,
            padding: 12,
            background: '#0f172a',
            border: '1px solid #334155',
            borderRadius: 8,
            color: '#e2e8f0',
            fontSize: 14,
          }}
        />
        <button
          onClick={handleSend}
          disabled={loading}
          style={{
            padding: '12px 24px',
            background: 'linear-gradient(135deg, #3b82f6, #8b5cf6)',
            color: 'white',
            border: 'none',
            borderRadius: 8,
            fontWeight: 600,
            cursor: loading ? 'not-allowed' : 'pointer',
          }}
        >
          Send
        </button>
      </div>
    </div>
  );
}
