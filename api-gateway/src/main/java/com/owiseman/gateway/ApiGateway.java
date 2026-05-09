package com.owiseman.gateway;

import com.owiseman.gateway.handler.AgentHandler;
import com.owiseman.gateway.handler.DocumentHandler;
import com.owiseman.gateway.handler.SystemHandler;
import com.owiseman.gateway.websocket.WebSocketConnection;
import com.owiseman.gateway.websocket.WebSocketHub;
import com.owiseman.runtime.RuntimeContext;
import com.owiseman.runtime.event.EventBus;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class ApiGateway {

    private static final Logger LOG = Logger.getLogger(ApiGateway.class.getName());

    private final int port;
    private final String host;
    private HttpServer httpServer;
    private final WebSocketHub wsHub;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private final AgentHandler agentHandler;
    private final DocumentHandler documentHandler;
    private final SystemHandler systemHandler;

    public ApiGateway() {
        this("0.0.0.0", 18080);
    }

    public ApiGateway(String host, int port) {
        this.host = host;
        this.port = port;
        this.wsHub = new WebSocketHub();
        this.agentHandler = new AgentHandler();
        this.documentHandler = new DocumentHandler();
        this.systemHandler = new SystemHandler();
    }

    public void start() throws IOException {
        if (running.getAndSet(true)) {
            throw new IllegalStateException("API Gateway already running");
        }

        httpServer = HttpServer.create(new InetSocketAddress(host, port), 0);
        httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        httpServer.createContext("/api/v1/agent", wrap(agentHandler));
        httpServer.createContext("/api/v1/document", wrap(documentHandler));
        httpServer.createContext("/api/v1/system", wrap(systemHandler));
        httpServer.createContext("/api/v1/ws", this::handleWebSocketUpgrade);
        httpServer.createContext("/", this::handleStaticFiles);

        httpServer.start();

        if (RuntimeContext.isInitializedStatic()) {
            RuntimeContext.getInstance().eventBus().subscribe("agent.task.created",
                    e -> wsHub.broadcast("agent.task.created", "{\"taskId\":\"" + e.get("taskId") + "\"}"));
            RuntimeContext.getInstance().eventBus().subscribe("task.completed",
                    e -> wsHub.broadcast("task.completed", "{\"taskId\":\"" + e.get("taskId") + "\"}"));
        }

        LOG.info("API Gateway started on http://" + host + ":" + port);
        LOG.info("  REST API:   http://" + host + ":" + port + "/api/v1/");
        LOG.info("  WebSocket:  ws://" + host + ":" + port + "/api/v1/ws");
        LOG.info("  Web UI:     http://" + host + ":" + port + "/");
    }

    public void stop() {
        if (!running.getAndSet(false)) return;

        if (httpServer != null) {
            httpServer.stop(2);
        }
        LOG.info("API Gateway stopped");
    }

    public WebSocketHub wsHub() {
        return wsHub;
    }

    public int port() {
        return port;
    }

    public boolean isRunning() {
        return running.get();
    }

    private HttpHandler wrap(com.owiseman.gateway.handler.RequestHandler handler) {
        return exchange -> {
            try {
                handler.handle(exchange);
            } catch (Exception e) {
                LOG.warning("Request handler error: " + e.getMessage());
                String json = "{\"code\":500,\"message\":\"Internal server error\",\"data\":null}";
                byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(500, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
        };
    }

    private void handleWebSocketUpgrade(HttpExchange exchange) throws IOException {
        String upgrade = exchange.getRequestHeaders().getFirst("Upgrade");
        if ("websocket".equalsIgnoreCase(upgrade)) {
            String clientKey = exchange.getRequestHeaders().getFirst("Sec-WebSocket-Key");
            if (clientKey == null) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }

            String acceptKey = WebSocketHub.computeAcceptKey(clientKey);
            exchange.getResponseHeaders().set("Upgrade", "websocket");
            exchange.getResponseHeaders().set("Connection", "Upgrade");
            exchange.getResponseHeaders().set("Sec-WebSocket-Accept", acceptKey);
            exchange.sendResponseHeaders(101, 0);

            LOG.info("WebSocket upgrade accepted");
        } else {
            exchange.sendResponseHeaders(400, -1);
        }
    }

    private void handleStaticFiles(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("/".equals(path)) {
            String html = getDashboardHtml();
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
    }

    private String getDashboardHtml() {
        return """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>OfficeAgent Dashboard</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;background:#0f172a;color:#e2e8f0;min-height:100vh}
.header{background:linear-gradient(135deg,#1e293b,#334155);padding:24px 32px;border-bottom:1px solid #475569;display:flex;align-items:center;gap:16px}
.header h1{font-size:24px;font-weight:700;background:linear-gradient(135deg,#60a5fa,#a78bfa);-webkit-background-clip:text;-webkit-text-fill-color:transparent}
.header .version{font-size:12px;color:#94a3b8;background:#1e293b;padding:4px 12px;border-radius:12px}
.container{max-width:1200px;margin:0 auto;padding:24px}
.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(340px,1fr));gap:20px;margin-top:20px}
.card{background:#1e293b;border:1px solid #334155;border-radius:12px;padding:20px}
.card h2{font-size:16px;color:#94a3b8;margin-bottom:16px;display:flex;align-items:center;gap:8px}
.card h2::before{content:'';width:4px;height:16px;background:linear-gradient(180deg,#60a5fa,#a78bfa);border-radius:2px}
textarea{width:100%;background:#0f172a;border:1px solid #334155;border-radius:8px;padding:12px;color:#e2e8f0;font-size:14px;resize:vertical;min-height:80px;outline:none}
textarea:focus{border-color:#60a5fa}
input[type=text]{width:100%;background:#0f172a;border:1px solid #334155;border-radius:8px;padding:10px 12px;color:#e2e8f0;font-size:14px;outline:none}
input[type=text]:focus{border-color:#60a5fa}
button{background:linear-gradient(135deg,#3b82f6,#8b5cf6);color:white;border:none;border-radius:8px;padding:10px 24px;font-size:14px;font-weight:600;cursor:pointer;transition:opacity .2s}
button:hover{opacity:.9}
button:disabled{opacity:.5;cursor:not-allowed}
.log{background:#0f172a;border:1px solid #334155;border-radius:8px;padding:12px;font-family:'Fira Code',monospace;font-size:12px;max-height:300px;overflow-y:auto;line-height:1.6}
.log .entry{padding:2px 0}
.log .info{color:#60a5fa}
.log .success{color:#34d399}
.log .error{color:#f87171}
.status-bar{display:flex;gap:12px;margin-top:20px;flex-wrap:wrap}
.status-item{background:#1e293b;border:1px solid #334155;border-radius:8px;padding:12px 20px;display:flex;align-items:center;gap:8px}
.status-dot{width:8px;height:8px;border-radius:50%;background:#34d399;animation:pulse 2s infinite}
@keyframes pulse{0%,100%{opacity:1}50%{opacity:.5}}
.api-list{font-size:13px;line-height:2}
.api-list code{background:#0f172a;padding:2px 8px;border-radius:4px;color:#60a5fa;font-size:12px}
.api-list .method{font-weight:700;display:inline-block;width:50px}
.method.post{color:#34d399}
.method.get{color:#60a5fa}
</style>
</head>
<body>
<div class="header">
<h1>OfficeAgent</h1>
<span class="version">v1.0.0</span>
</div>
<div class="container">
<div class="status-bar">
<div class="status-item"><span class="status-dot"></span>Runtime Active</div>
<div class="status-item" id="wsStatus">WebSocket: Connecting...</div>
<div class="status-item" id="agentCount">Active Agents: 0</div>
</div>
<div class="grid">
<div class="card">
<h2>Agent Task</h2>
<textarea id="goalInput" placeholder="Enter your task, e.g.: Help me create a quarterly report PPT..."></textarea>
<div style="margin-top:12px;display:flex;gap:8px;align-items:center">
<input type="text" id="docPath" placeholder="Document path (optional)">
<button onclick="executeAgent()" id="execBtn">Execute</button>
</div>
<div class="log" id="agentLog" style="margin-top:12px"><div class="entry info">Ready to accept tasks...</div></div>
</div>
<div class="card">
<h2>Document Import</h2>
<input type="text" id="importPath" placeholder="File path, e.g.: /path/to/report.pptx">
<div style="margin-top:12px"><button onclick="importDocument()">Import</button></div>
<div class="log" id="docLog" style="margin-top:12px"><div class="entry info">Waiting for document import...</div></div>
</div>
<div class="card">
<h2>API Reference</h2>
<div class="api-list">
<div><span class="method post">POST</span><code>/api/v1/agent/execute</code></div>
<div><span class="method post">POST</span><code>/api/v1/agent/patch</code></div>
<div><span class="method get">GET</span><code>/api/v1/agent/status</code></div>
<div><span class="method post">POST</span><code>/api/v1/document/import</code></div>
<div><span class="method get">GET</span><code>/api/v1/document/{id}</code></div>
<div><span class="method post">POST</span><code>/api/v1/document/export</code></div>
<div><span class="method get">GET</span><code>/api/v1/system/info</code></div>
<div><span class="method get">GET</span><code>/api/v1/system/health</code></div>
<div><span class="method get">GET</span><code>/api/v1/system/workers</code></div>
</div>
</div>
</div>
</div>
<script>
const API_BASE='/api/v1';
function addLog(el,msg,type='info'){const d=document.createElement('div');d.className='entry '+type;d.textContent='['+new Date().toLocaleTimeString()+'] '+msg;el.appendChild(d);el.scrollTop=el.scrollHeight}
async function executeAgent(){const goal=document.getElementById('goalInput').value;const docPath=document.getElementById('docPath').value;if(!goal){addLog(document.getElementById('agentLog'),'Please enter a task description','error');return}const btn=document.getElementById('execBtn');btn.disabled=true;addLog(document.getElementById('agentLog'),'Executing: '+goal);try{const body={goal:goal};if(docPath)body.documentPath=docPath;const r=await fetch(API_BASE+'/agent/execute',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)});const d=await r.json();if(d.code===0){addLog(document.getElementById('agentLog'),'Task completed! Agent: '+d.data.agentId+', Status: '+d.data.status,'success')}else{addLog(document.getElementById('agentLog'),'Task failed: '+d.message,'error')}}catch(e){addLog(document.getElementById('agentLog'),'Request error: '+e.message,'error')}finally{btn.disabled=false}}
async function importDocument(){const path=document.getElementById('importPath').value;if(!path){addLog(document.getElementById('docLog'),'Please enter file path','error');return}addLog(document.getElementById('docLog'),'Importing: '+path);try{const r=await fetch(API_BASE+'/document/import',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({filePath:path})});const d=await r.json();if(d.code===0){addLog(document.getElementById('docLog'),'Import successful! Document ID: '+d.data.documentId+', Pages: '+d.data.totalPages,'success')}else{addLog(document.getElementById('docLog'),'Import failed: '+d.message,'error')}}catch(e){addLog(document.getElementById('docLog'),'Request error: '+e.message,'error')}}
fetch(API_BASE+'/system/health').then(r=>r.json()).then(d=>{document.getElementById('wsStatus').textContent='Status: '+d.data.status}).catch(()=>{document.getElementById('wsStatus').textContent='Status: Offline'});
</script>
</body>
</html>
""";
    }
}
