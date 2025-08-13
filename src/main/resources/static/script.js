document.addEventListener('DOMContentLoaded', () => {
  // ====== INDEX PAGE (build & visualize) ======
  const form = document.getElementById('form');
  if (form) {
    const jsonEl = document.getElementById('json');
    const vizEl  = document.getElementById('viz');
    const balancedEl = document.getElementById('balanced');

    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const numbers = document.getElementById('numbers').value.trim();
      const balanced = balancedEl && balancedEl.checked ? 'true' : 'false';

      const body = new URLSearchParams({ numbers, balanced });

      try {
        const res = await fetch('/process-numbers', {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body
        });
        if (!res.ok) {
          jsonEl.textContent = 'Error: ' + res.status + ' ' + (await res.text());
          vizEl.textContent = '—';
          return;
        }
        const tree = await res.json();
        jsonEl.textContent = JSON.stringify(tree, null, 2);
        renderTree(tree, vizEl);
      } catch (err) {
        jsonEl.textContent = 'Network error: ' + err.message;
        vizEl.textContent = '—';
      }
    });
  }

  // ====== PREVIOUS PAGE (list stored trees) ======
  const tbl = document.getElementById('tbl');
  if (tbl) {
    loadPrevious(tbl);
  }
});

// ---------- Helpers ----------
async function loadPrevious(tbl) {
  try {
    const res = await fetch('/api/previous');
    if (!res.ok) {
      alert('Failed to load previous records: ' + res.status);
      return;
    }
    const rows = await res.json();
    const tbody = tbl.querySelector('tbody');
    tbody.innerHTML = '';

    for (const r of rows) {
      const tr = document.createElement('tr');

      const tdId = el('td', r.id);
      const tdCreated = el('td', formatDate(r.createdAt));
      const tdInput = el('td'); tdInput.appendChild(code(r.inputNumbers));
      const tdJson = el('td');  tdJson.appendChild(prettyJson(r.treeJson));

      tr.append(tdId, tdCreated, tdInput, tdJson);
      tbody.appendChild(tr);
    }
  } catch (err) {
    alert('Network error: ' + err.message);
  }
}

//-------------node positioned and drawn-------------------
function renderTree(root, container) {
  container.innerHTML = '';
  if (!root) { container.textContent = 'No nodes'; return; }

  const levels = levelOrder(root);
  for (const level of levels) {
    const row = document.createElement('div');
    row.className = 'level';
    for (const val of level) {
      const n = document.createElement('div');
      n.className = 'node';
      n.textContent = (val === null ? '•' : val);
      row.appendChild(n);
    }
    container.appendChild(row);
  }
}

function levelOrder(root) {
  const out = [];
  const q = [root];
  while (q.length) {
    const size = q.length;
    const row = [];
    let hasChild = false;
    for (let i = 0; i < size; i++) {
      const x = q.shift();
      if (x) {
        row.push(x.value);
        q.push(x.left, x.right);
        hasChild = hasChild || x.left || x.right;
      } else {
        row.push(null);
        q.push(null, null);
      }
    }
    out.push(row);
    if (!hasChild) break;
  }
  return out;
}

// ---------- DOM utils ----------
function el(tag, text) { const e = document.createElement(tag); if (text!=null) e.textContent = text; return e; }
function code(text) { const c = document.createElement('code'); c.textContent = text; return c; }
function prettyJson(raw) {
  const pre = document.createElement('pre');
  try { pre.textContent = JSON.stringify(JSON.parse(raw), null, 2); }
  catch { pre.textContent = raw; }
  return pre;
}
function formatDate(iso) { try { return new Date(iso).toLocaleString(); } catch { return iso; } }