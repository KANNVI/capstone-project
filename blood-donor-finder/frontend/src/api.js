export const GROUPS = ['O-', 'O+', 'A-', 'A+', 'B-', 'B+', 'AB-', 'AB+'];
export const CITIES = ['Gurugram', 'Delhi', 'Noida', 'Faridabad', 'Ghaziabad', 'Chandigarh'];

/** Small fetch wrapper: adds the JWT, parses JSON, throws Error(message) on failure. */
export async function api(path, { method = 'GET', body } = {}) {
  const headers = { 'Content-Type': 'application/json' };
  const token = localStorage.getItem('token');
  if (token) headers.Authorization = 'Bearer ' + token;
  const res = await fetch('/api' + path, { method, headers, body: body ? JSON.stringify(body) : undefined });
  if (!res.ok) {
    let msg = 'Request failed (' + res.status + ')';
    try { const j = await res.json(); msg = j.message || j.error || msg; } catch { /* no body */ }
    if (res.status === 401) { localStorage.removeItem('token'); localStorage.removeItem('user'); }
    throw new Error(msg);
  }
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}
