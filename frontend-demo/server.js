const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = process.env.PORT || 5173;
const ORDER_TARGET = process.env.ORDER_TARGET || 'http://localhost:8081';
const COST_TARGET = process.env.COST_TARGET || 'http://localhost:8082';

function sendFile(res, file, type) {
  fs.readFile(file, (err, data) => {
    if (err) {
      res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
      res.end('Archivo no encontrado');
      return;
    }
    res.writeHead(200, { 'Content-Type': type });
    res.end(data);
  });
}

function proxy(req, res, targetBase, prefix) {
  const target = new URL(req.url.replace(prefix, ''), targetBase);
  const options = {
    method: req.method,
    hostname: target.hostname,
    port: target.port,
    path: target.pathname + target.search,
    headers: { ...req.headers, host: target.host }
  };

  const proxyReq = http.request(options, proxyRes => {
    const headers = { ...proxyRes.headers, 'access-control-allow-origin': '*' };
    res.writeHead(proxyRes.statusCode || 500, headers);
    proxyRes.pipe(res);
  });

  proxyReq.on('error', err => {
    res.writeHead(502, { 'Content-Type': 'application/json; charset=utf-8' });
    res.end(JSON.stringify({ success: false, error: `No se pudo conectar con ${targetBase}: ${err.message}` }));
  });

  req.pipe(proxyReq);
}

http.createServer((req, res) => {
  if (req.method === 'OPTIONS') {
    res.writeHead(204, {
      'access-control-allow-origin': '*',
      'access-control-allow-methods': 'GET,POST,PUT,PATCH,DELETE,OPTIONS',
      'access-control-allow-headers': 'content-type,x-usuario'
    });
    res.end();
    return;
  }

  if (req.url.startsWith('/order-api/')) return proxy(req, res, ORDER_TARGET, '/order-api');
  if (req.url.startsWith('/cost-api/')) return proxy(req, res, COST_TARGET, '/cost-api');

  if (req.url === '/' || req.url.startsWith('/index.html')) {
    return sendFile(res, path.join(__dirname, 'index.html'), 'text/html; charset=utf-8');
  }

  res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
  res.end('Ruta no encontrada');
}).listen(PORT, () => {
  console.log(`Frontend demo: http://localhost:${PORT}`);
  console.log(`Proxy order-service -> ${ORDER_TARGET}`);
  console.log(`Proxy cost-service  -> ${COST_TARGET}`);
});
