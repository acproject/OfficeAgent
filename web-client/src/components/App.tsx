import React, { useState } from 'react';
import { useAgent } from '../hooks/useAgent';
import { useDocument } from '../hooks/useDocument';
import type { DocumentResponse } from '../types/api';

export function ChatPanel() {
  const { loading, result, error, execute } = useAgent();
  const { importDocument, documents } = useDocument();
  const [goal, setGoal] = useState('');
  const [docPath, setDocPath] = useState('');

  const handleExecute = () => {
    if (!goal.trim()) return;
    execute(goal, docPath || undefined);
  };

  return (
    <div style={styles.panel}>
      <h2 style={styles.title}>Agent Chat</h2>
      <textarea
        style={styles.input}
        value={goal}
        onChange={(e) => setGoal(e.target.value)}
        placeholder="Enter your task, e.g.: Help me create a quarterly report PPT..."
        rows={4}
      />
      <div style={styles.row}>
        <input
          style={styles.pathInput}
          value={docPath}
          onChange={(e) => setDocPath(e.target.value)}
          placeholder="Document path (optional)"
        />
        <button style={styles.button} onClick={handleExecute} disabled={loading}>
          {loading ? 'Processing...' : 'Execute'}
        </button>
      </div>
      {error && <div style={styles.error}>{error}</div>}
      {result && (
        <div style={styles.result}>
          <div>Agent: {result.agentId}</div>
          <div>Status: {result.status}</div>
          <div>Duration: {result.durationMs}ms</div>
        </div>
      )}
    </div>
  );
}

export function DocumentPanel() {
  const { importDocument, loading, error, documents } = useDocument();
  const [filePath, setFilePath] = useState('');

  const handleImport = () => {
    if (!filePath.trim()) return;
    importDocument(filePath);
  };

  const docList = Array.from(documents.values());

  return (
    <div style={styles.panel}>
      <h2 style={styles.title}>Documents</h2>
      <div style={styles.row}>
        <input
          style={styles.pathInput}
          value={filePath}
          onChange={(e) => setFilePath(e.target.value)}
          placeholder="File path, e.g.: /path/to/report.pptx"
        />
        <button style={styles.button} onClick={handleImport} disabled={loading}>
          {loading ? 'Importing...' : 'Import'}
        </button>
      </div>
      {error && <div style={styles.error}>{error}</div>}
      <div style={styles.docList}>
        {docList.map((doc) => (
          <DocumentCard key={doc.documentId} doc={doc} />
        ))}
      </div>
    </div>
  );
}

function DocumentCard({ doc }: { doc: DocumentResponse }) {
  return (
    <div style={styles.card}>
      <div style={styles.cardHeader}>
        <strong>{doc.documentType}</strong>
        <span style={styles.cardId}>{doc.documentId}</span>
      </div>
      <div style={styles.cardBody}>
        <div>Source: {doc.sourcePath}</div>
        <div>Pages: {doc.totalPages}</div>
      </div>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  panel: {
    background: '#1e293b',
    border: '1px solid #334155',
    borderRadius: 12,
    padding: 20,
  },
  title: {
    fontSize: 16,
    color: '#a8d8ea',
    marginBottom: 16,
  },
  input: {
    width: '100%',
    background: '#0f172a',
    border: '1px solid #334155',
    borderRadius: 8,
    padding: 12,
    color: '#e2e8f0',
    fontSize: 14,
    resize: 'vertical',
    marginBottom: 12,
  },
  pathInput: {
    flex: 1,
    background: '#0f172a',
    border: '1px solid #334155',
    borderRadius: 8,
    padding: '10px 12px',
    color: '#e2e8f0',
    fontSize: 14,
  },
  row: {
    display: 'flex',
    gap: 8,
    alignItems: 'center',
  },
  button: {
    background: 'linear-gradient(135deg, #3b82f6, #8b5cf6)',
    color: 'white',
    border: 'none',
    borderRadius: 8,
    padding: '10px 24px',
    fontSize: 14,
    fontWeight: 600,
    cursor: 'pointer',
  },
  error: {
    color: '#f87171',
    marginTop: 8,
    fontSize: 13,
  },
  result: {
    background: '#0f172a',
    borderRadius: 8,
    padding: 12,
    marginTop: 12,
    color: '#34d399',
    fontSize: 13,
    lineHeight: 1.8,
  },
  docList: {
    marginTop: 12,
    display: 'flex',
    flexDirection: 'column',
    gap: 8,
  },
  card: {
    background: '#0f172a',
    borderRadius: 8,
    padding: 12,
  },
  cardHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    marginBottom: 4,
    color: '#60a5fa',
  },
  cardId: {
    fontSize: 11,
    color: '#64748b',
  },
  cardBody: {
    fontSize: 12,
    color: '#94a3b8',
    lineHeight: 1.6,
  },
};
