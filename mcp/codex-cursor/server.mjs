// Minimal MCP for Pardis KMP development
// Extend with tools from pardis-kmp-delivery skill, token queries, etc.
import { Server } from '@modelcontextprotocol/sdk/server/index.js';

const server = new Server(
  { name: 'pardis-kmp-mcp', version: '0.1.0' },
  { capabilities: { tools: {} } }
);

console.log('Pardis KMP MCP ready (extend for design tokens, shared VM patterns, Supabase queries).');
