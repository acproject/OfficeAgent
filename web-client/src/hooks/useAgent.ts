import { useState, useCallback, useRef } from 'react';
import { agentApi } from '../services/api';
import type { AgentTaskResponse } from '../types/api';

export function useAgent() {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<AgentTaskResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const abortRef = useRef<AbortController | null>(null);

  const execute = useCallback(async (goal: string, documentPath?: string) => {
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const res = await agentApi.execute({
        goal,
        documentPath,
        stream: false,
      });
      if (res.code === 0 && res.data) {
        setResult(res.data);
      } else {
        setError(res.message);
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unknown error');
    } finally {
      setLoading(false);
    }
  }, []);

  const cancel = useCallback(() => {
    abortRef.current?.abort();
    setLoading(false);
  }, []);

  return { loading, result, error, execute, cancel };
}
