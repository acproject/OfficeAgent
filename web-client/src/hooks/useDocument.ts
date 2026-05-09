import { useState, useCallback } from 'react';
import { documentApi } from '../services/api';
import type { DocumentResponse } from '../types/api';

export function useDocument() {
  const [documents, setDocuments] = useState<Map<string, DocumentResponse>>(new Map());
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const importDocument = useCallback(async (filePath: string, format?: string) => {
    setLoading(true);
    setError(null);

    try {
      const res = await documentApi.import({ filePath, format });
      if (res.code === 0 && res.data) {
        setDocuments((prev) => new Map(prev).set(res.data!.documentId, res.data!));
        return res.data;
      } else {
        setError(res.message);
        return null;
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unknown error');
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const loadDocument = useCallback(async (documentId: string) => {
    setLoading(true);
    try {
      const res = await documentApi.get(documentId);
      if (res.code === 0 && res.data) {
        setDocuments((prev) => new Map(prev).set(documentId, res.data!));
        return res.data;
      }
      return null;
    } catch {
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  return { documents, loading, error, importDocument, loadDocument };
}
