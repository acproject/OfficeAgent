export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface AgentTaskRequest {
  goal: string;
  documentPath?: string;
  documentType?: string;
  instruction?: string;
  stream?: boolean;
}

export interface AgentTaskResponse {
  agentId: string;
  planId: string;
  status: string;
  durationMs: number;
  documentId?: string;
}

export interface DocumentImportRequest {
  filePath: string;
  format?: string;
}

export interface DocumentResponse {
  documentId: string;
  documentType: string;
  sourcePath: string;
  totalPages: number;
  pages: PageSummary[];
}

export interface PageSummary {
  pageIndex: number;
  blockCount: number;
  blocks: BlockSummary[];
}

export interface BlockSummary {
  blockId: string;
  type: string;
  role: string;
  contentPreview: string | null;
}

export interface SystemInfo {
  name: string;
  version: string;
  javaVersion: string;
  os: string;
  processors: number;
  maxMemory: string;
  virtualThreadsEnabled: boolean;
}

export interface HealthStatus {
  status: string;
  runtime: string;
}

export interface WorkerInfo {
  type: string;
  code: string;
}

export interface WsMessage {
  topic: string;
  data: unknown;
}
