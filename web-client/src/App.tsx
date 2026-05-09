import React from 'react';
import { ChatPanel, DocumentPanel } from './components/App';

function App() {
  return (
    <div style={{ minHeight: '100vh', background: '#0f172a', color: '#e2e8f0', fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif' }}>
      <header style={{ background: 'linear-gradient(135deg, #1e293b, #334155)', padding: '16px 32px', borderBottom: '1px solid #475569', display: 'flex', alignItems: 'center', gap: 16 }}>
        <h1 style={{ fontSize: 24, fontWeight: 700, background: 'linear-gradient(135deg, #60a5fa, #a78bfa)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent', margin: 0 }}>
          OfficeAgent
        </h1>
        <span style={{ fontSize: 12, color: '#94a3b8', background: '#1e293b', padding: '4px 12px', borderRadius: 12 }}>
          v1.0.0
        </span>
      </header>
      <main style={{ maxWidth: 1200, margin: '0 auto', padding: 24, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
        <ChatPanel />
        <DocumentPanel />
      </main>
    </div>
  );
}

export default App;
