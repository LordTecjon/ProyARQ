const http = require('http');

const PORT = process.env.PORT || 8090;
const routes = [
  { prefix: '/api/orders', target: 'http://localhost:8081' },
  { prefix: '/api/trip-costs', target: 'http://localhost:8082' },
  { prefix: '/api/billing', target: 'http://localhost:8087' }
];

function cors(res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,POST,PUT,PATCH,DELETE,OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type,X-Usuario');
}

function proxy(req, res, targetBase) {
  const target = new URL(req.url, targetBase);
  const options = {
    method: req.method,
    hostname: target.hostname,
    port: target.port,
    path: target.pathname + target.search,
    headers: { ...req.headers, host: target.host }
  };
  const upstream = http.request(options, upstreamRes => {
    cors(res);
    res.writeHead(upstreamRes.statusCode || 500, upstreamRes.headers);
    upstreamRes.pipe(res);
  });
  upstream.on('error', err => {
    cors(res);
    res.writeHead(502, { 'Content-Type': 'application/json; charset=utf-8' });
    res.end(JSON.stringify({ success: false, error: `API Gateway no pudo conectar con ${targetBase}: ${err.message}` }));
  });
  req.pipe(upstream);
}

http.createServer((req, res) => {
  cors(res);
  if (req.method === 'OPTIONS') return res.end();
  if (req.url === '/health') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    return res.end(JSON.stringify({ status: 'UP', gateway: 'api-gateway-demo' }));
  }
  const route = routes.find(r => req.url.startsWith(r.prefix));
  if (!route) {
    res.writeHead(404, { 'Content-Type': 'application/json' });
    return res.end(JSON.stringify({ success: false, error: 'Ruta no configurada en API Gateway' }));
  }
  proxy(req, res, route.target);
}).listen(PORT, () => {
  console.log(`API Gateway demo: http://localhost:${PORT}`);
  console.log('Routes: /api/orders -> 8081, /api/trip-costs -> 8082, /api/billing -> 8087');
});

