document.addEventListener('DOMContentLoaded', () => {
  // ====== INDEX PAGE (build & visualize) ======
  // Feature-detect the page by checking if the form exists
  const form = document.getElementById('form');
  if (form) {
    // Cache references to UI elements used on the index page
    const jsonEl = document.getElementById('json');     // <pre> that shows JSON result
    const vizEl  = document.getElementById('viz');      // container for visual tree
    const balancedEl = document.getElementById('balanced'); // checkbox


    form.addEventListener('submit', async (e) => {
      e.preventDefault();

      // Read user input
      const numbers = document.getElementById('numbers').value.trim();
      const balanced = balancedEl && balancedEl.checked ? 'true' : 'false';

      // Encode as application/x-www-form-urlencoded to match @RequestParam
      const body = new URLSearchParams({ numbers, balanced });

      try {
        // Call the backend processing route
        const res = await fetch('/process-numbers', {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body
        });

        // Handle non-200 responses gracefully
        if (!res.ok) {
          jsonEl.textContent = 'Error: ' + res.status + ' ' + (await res.text());
          vizEl.textContent = '—';
          return;
        }

        // Parse JSON tree (TreeNodeDTO) from server
        const tree = await res.json();

        // Pretty print JSON to the <pre>
        jsonEl.textContent = JSON.stringify(tree, null, 2);

        // Render a simple level-order visualization below
        renderTree(tree, vizEl);
      } catch (err) {
        // Network/parse errors end up here
        jsonEl.textContent = 'Network error: ' + err.message;
        vizEl.textContent = '—';
      }
    });
  }

  // ====== PREVIOUS PAGE (list stored trees) ======
  // Feature-detect the page by checking if the table exists
  const tbl = document.getElementById('tbl');
  if (tbl) {
    loadPrevious(tbl);
  }
});

// ---------- Helpers ----------

/**
 * Fetch all previous submissions and populate the table.
 * Expects rows like: [{ id, inputNumbers, createdAt, treeJson }, ...]
 */
async function loadPrevious(tbl) {
  try {
    const res = await fetch('/api/previous'); // GET history list
    if (!res.ok) {
      alert('Failed to load previous records: ' + res.status);
      return;
    }

    const rows = await res.json();
    const tbody = tbl.querySelector('tbody');
    tbody.innerHTML = ''; // clear existing rows

    // Create table rows dynamically
    for (const r of rows) {
      const tr = document.createElement('tr');

      const tdId = el('td', r.id);
      const tdCreated = el('td', formatDate(r.createdAt));
      const tdInput = el('td'); tdInput.appendChild(code(r.inputNumbers)); // show inputs as <code>
      const tdJson = el('td');  tdJson.appendChild(prettyJson(r.treeJson)); // pretty-print saved JSON

      tr.append(tdId, tdCreated, tdInput, tdJson);
      tbody.appendChild(tr);
    }
  } catch (err) {
    alert('Network error: ' + err.message);
  }
}

// ------------- node positioning & drawing (level-by-level, no SVG) -------------

/**
 * Render a simple level-order view of the tree.
 * For each level we create a row of nodes. Missing nodes are shown as '•' to preserve shape.
 * @param {object|null} root  TreeNodeDTO root { value, left, right }
 * @param {HTMLElement} container  Target element to render into
 */
function renderTree(root, container) {
  container.innerHTML = ''; // clear previous content
  if (!root) { container.textContent = 'No nodes'; return; }

  // Compute levels using BFS (with placeholder nulls to keep alignment)
  const levels = levelOrder(root);

  // Render each level as a row of node boxes
  for (const level of levels) {
    const row = document.createElement('div');
    row.className = 'level';

    for (const val of level) {
      const n = document.createElement('div');
      n.className = 'node';
      // nulls become '•' placeholders to keep column spacing even
      n.textContent = (val === null ? '•' : val);
      row.appendChild(n);
    }

    container.appendChild(row);
  }
}

/**
 * Breadth-first traversal that returns a 2D array of values by level.
 * Includes `null` placeholders so the visual layout stays aligned for sparse trees.
 *
 * Example output:
 * [
 *   [9],
 *   [4, null],
 *   [1, 7],
 *   [null, null, null, 8]
 * ]
 *
 * @param {object|null} root  TreeNodeDTO root
 * @returns {Array<Array<number|null>>} levels matrix
 */
function levelOrder(root) {
  const out = [];
  const q = [root];      // queue for BFS (start from root)

  // Process until queue is empty (or we break when no children found)
  while (q.length) {
    const size = q.length;  // number of nodes in this level
    const row = [];         // values for current level
    let hasChild = false;   // track if any node in this level has children

    // Dequeue exactly `size` nodes (one full level)
    for (let i = 0; i < size; i++) {
      const x = q.shift();

      if (x) {
        // Real node: push its value and enqueue both children
        row.push(x.value);
        q.push(x.left, x.right);

        // If either child exists, we must continue to next level
        hasChild = hasChild || x.left || x.right;
      } else {
        // Placeholder: keep shape by adding null and enqueueing two nulls
        row.push(null);
        q.push(null, null);
      }
    }

    // Add current level to output
    out.push(row);

    // If no node in this level had any children, stop (avoid infinite null levels)
    if (!hasChild) break;
  }

  return out;
}

// ---------- DOM utils ----------

/** Create an element with optional textContent */
function el(tag, text) {
  const e = document.createElement(tag);
  if (text != null) e.textContent = text;
  return e;
}

/** Wrap text in a <code> element (for monospace display) */
function code(text) {
  const c = document.createElement('code');
  c.textContent = text;
  return c;
}

/** Safely pretty-print JSON string into a <pre> (falls back to raw if not valid JSON) */
function prettyJson(raw) {
  const pre = document.createElement('pre');
  try {
    pre.textContent = JSON.stringify(JSON.parse(raw), null, 2);
  } catch {
    pre.textContent = raw;
  }
  return pre;
}

/** Format an ISO date string into the user's local date/time, fallback to original */
function formatDate(iso) {
  try {
    return new Date(iso).toLocaleString();
  } catch {
    return iso;
  }
}