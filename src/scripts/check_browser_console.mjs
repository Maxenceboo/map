import { spawn } from 'child_process';

const edgePath = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';
const port = 9333;

const edge = spawn(edgePath, [
  '--headless=new',
  `--remote-debugging-port=${port}`,
  '--no-sandbox',
  '--disable-gpu',
  'http://localhost:5173/',
]);

console.log('Edge launched, connecting to CDP...');

setTimeout(async () => {
  try {
    const listRes = await fetch(`http://127.0.0.1:${port}/json`);
    const tabs = await listRes.json();
    console.log('Tabs found:', tabs.length);
    const target = tabs.find((t) => t.url.includes('localhost:5173')) || tabs[0];
    if (!target) {
      console.log('No tab found');
      edge.kill();
      process.exit(1);
    }

    const ws = new WebSocket(target.webSocketDebuggerUrl);

    ws.onopen = () => {
      console.log('Connected to page WebSocket');
      ws.send(JSON.stringify({ id: 1, method: 'Runtime.enable' }));
      ws.send(JSON.stringify({ id: 2, method: 'Log.enable' }));
    };

    ws.onmessage = (event) => {
      const msg = JSON.parse(event.data);
      if (msg.method === 'Runtime.consoleAPICalled') {
        console.log('[BROWSER CONSOLE]', msg.params.type, ...msg.params.args.map((a) => a.value || a.description));
      }
      if (msg.method === 'Runtime.exceptionThrown') {
        console.error('[BROWSER EXCEPTION]', msg.params.exceptionDetails);
      }
    };

    setTimeout(() => {
      console.log('Done listening');
      ws.close();
      edge.kill();
      process.exit(0);
    }, 5000);
  } catch (err) {
    console.error('CDP connection error:', err);
    edge.kill();
    process.exit(1);
  }
}, 2000);
