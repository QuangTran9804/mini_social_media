const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:9090/api';

let authToken = null;

export function setAuthToken(token) {
  authToken = token;
}

export function clearAuthToken() {
  authToken = null;
}

async function http(path, opts = {}) {
  const { skipAuth, ...rest } = opts;
  const headers = {
    'Content-Type': 'application/json',
    ...(rest.headers || {})
  };
  if (!skipAuth && authToken) {
    headers.Authorization = `Bearer ${authToken}`;
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...rest,
    headers
  });

  if (!response.ok) {
    const contentType = response.headers.get('content-type') || '';
    let errorMessage = `Request failed: ${response.status}`;
    
    if (contentType.includes('application/json')) {
      try {
        const errorData = await response.json();
        if (errorData.message) {
          errorMessage = typeof errorData.message === 'string' 
            ? errorData.message 
            : JSON.stringify(errorData.message);
        } else if (errorData.error) {
          errorMessage = errorData.error;
        }
      } catch (e) {
        const text = await response.text().catch(() => '');
        errorMessage = text || errorMessage;
      }
    } else {
      const text = await response.text().catch(() => '');
      errorMessage = text || errorMessage;
    }
    
    throw new Error(errorMessage);
  }

  if (response.status === 204) return null;
  const ct = response.headers.get('content-type') || '';
  return ct.includes('application/json') ? response.json() : response.text();
}

export async function fetchUsers() {
  const data = await http('/users?page=1&size=50');
  return data?.result || [];
}

export async function fetchCurrentUser() {
  return http('/users/me');
}

export async function login(username, password) {
  return http('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
    skipAuth: true
  });
}

export async function register(userData) {
  return http('/auth/register', {
    method: 'POST',
    body: JSON.stringify(userData),
    skipAuth: true
  });
}

export async function listFriends() {
  return http('/friends');
}

export async function inboxRequests() {
  return http('/friend-requests/inbox');
}

export async function sentRequests() {
  return http('/friend-requests/sent');
}

export async function sendRequest(receiverId) {
  return http('/friend-requests', { method: 'POST', body: JSON.stringify({ receiverId }) });
}

export async function cancelRequest(requestId) {
  return http(`/friend-requests/${requestId}`, { method: 'DELETE' });
}

export async function acceptRequest(requestId) {
  return http(`/friend-requests/${requestId}/accept`, { method: 'POST' });
}

export async function rejectRequest(requestId) {
  return http(`/friend-requests/${requestId}/reject`, { method: 'POST' });
}

export async function unfriend(friendId) {
  return http(`/friends/${friendId}`, { method: 'DELETE' });
}

export async function fetchFeed() {
  return http('/posts');
}

export async function createPost(payload) {
  return http('/posts', { method: 'POST', body: JSON.stringify(payload) });
}

export async function toggleReaction(postId, reaction) {
  return http(`/posts/${postId}/likes`, {
    method: 'POST',
    body: JSON.stringify({ reaction })
  });
}

export async function fetchMessagesWith(otherUserId) {
  return http(`/messages/with/${otherUserId}`);
}

export async function sendMessageTo(receiverId, content) {
  return http('/messages/send', {
    method: 'POST',
    body: JSON.stringify({ receiverId, content })
  });
}

export async function markMessageRead(messageId) {
  return http(`/messages/${messageId}/read`, { method: 'POST' });
}
