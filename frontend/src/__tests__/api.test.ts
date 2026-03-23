import api from '@/lib/api';

describe('api interceptor', () => {
  beforeEach(() => {
    localStorage.clear();
    Object.defineProperty(document, 'cookie', {
      writable: true,
      value: '',
    });
  });

  it('adds authorization and csrf headers for mutating requests', async () => {
    localStorage.setItem('accessToken', 'token-123');
    document.cookie = 'XSRF-TOKEN=csrf-token';

    const config = await api.interceptors.request.handlers[0].fulfilled?.({
      method: 'post',
      headers: {},
    });

    expect(config?.headers?.Authorization).toBe('Bearer token-123');
    expect(config?.headers?.['X-XSRF-TOKEN']).toBe('csrf-token');
  });

  it('does not add csrf header for get requests', async () => {
    localStorage.setItem('accessToken', 'token-123');
    document.cookie = 'XSRF-TOKEN=csrf-token';

    const config = await api.interceptors.request.handlers[0].fulfilled?.({
      method: 'get',
      headers: {},
    });

    expect(config?.headers?.Authorization).toBe('Bearer token-123');
    expect(config?.headers?.['X-XSRF-TOKEN']).toBeUndefined();
  });
});
