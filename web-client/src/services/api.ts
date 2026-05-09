import type {
  ApiResponse,
  AgentTaskRequest,
  AgentTaskResponse,
  DocumentImportRequest,
  DocumentResponse,
  SystemInfo,
  HealthStatus,
  WorkerInfo,
} from '../types/api';

const API_BASE = '/api/v1';

async function request<T>(path: string, options?: RequestInit): Promise<ApiResponse<T>> {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  return res.json();
}

export const agentApi = {
  execute(task: AgentTaskRequest): Promise<ApiResponse<AgentTaskResponse>> {
    return request('/agent/execute', {
      method: 'POST',
      body: JSON.stringify(task),
    });
  },

  patch(documentId: string, instruction: string): Promise<ApiResponse<unknown>> {
    return request('/agent/patch', {
      method: 'POST',
      body: JSON.stringify({ documentId, instruction }),
    });
  },

  status(): Promise<ApiResponse<{ activeAgents: number; runtimeInitialized: boolean }>> {
    return request('/agent/status');
  },
};

export const documentApi = {
  import(req: DocumentImportRequest): Promise<ApiResponse<DocumentResponse>> {
    return request('/document/import', {
      method: 'POST',
      body: JSON.stringify(req),
    });
  },

  get(documentId: string): Promise<ApiResponse<DocumentResponse>> {
    return request(`/document/${documentId}`);
  },

  list(): Promise<ApiResponse<string[]>> {
    return request('/document/');
  },

  export(documentId: string, outputPath: string): Promise<ApiResponse<unknown>> {
    return request('/document/export', {
      method: 'POST',
      body: JSON.stringify({ documentId, outputPath }),
    });
  },
};

export const systemApi = {
  info(): Promise<ApiResponse<SystemInfo>> {
    return request('/system/info');
  },

  health(): Promise<ApiResponse<HealthStatus>> {
    return request('/system/health');
  },

  workers(): Promise<ApiResponse<WorkerInfo[]>> {
    return request('/system/workers');
  },
};

export function createWebSocket(onMessage: (msg: unknown) => void): WebSocket {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const ws = new WebSocket(`${protocol}//${window.location.host}/api/v1/ws`);
  ws.onmessage = (event) => {
    try {
      onMessage(JSON.parse(event.data));
    } catch {
      onMessage(event.data);
    }
  };
  return ws;
}
