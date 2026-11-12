import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api, { errMsg } from '../api/client';

const call = (type, fn) =>
  createAsyncThunk(type, async (arg, { rejectWithValue }) => {
    try {
      return await fn(arg);
    } catch (e) {
      return rejectWithValue(errMsg(e));
    }
  });

export const login = call('auth/login', async (body) => (await api.post('/auth/login', body)).data);
export const register = call('auth/register', async (body) => (await api.post('/auth/register', body)).data);
export const fetchProfile = call('auth/profile', async () => (await api.get('/users/me')).data);
export const setAvailability = call(
  'auth/availability',
  async (available) => (await api.put('/donors/availability', { available })).data
);
export const updateDonorProfile = call(
  'auth/updateDonor',
  async (body) => (await api.put('/donors/profile', body)).data
);

const token = localStorage.getItem('bc_token');

const slice = createSlice({
  name: 'auth',
  initialState: { token, user: null, loading: !!token, error: null },
  reducers: {
    logout(state) {
      localStorage.removeItem('bc_token');
      state.token = null;
      state.user = null;
      state.loading = false;
    },
    clearError(state) {
      state.error = null;
    },
  },
  extraReducers: (b) => {
    const authed = (state, { payload }) => {
      localStorage.setItem('bc_token', payload.token);
      state.token = payload.token;
      state.user = payload.user;
      state.loading = false;
      state.error = null;
    };
    b.addCase(login.fulfilled, authed)
      .addCase(register.fulfilled, authed)
      .addCase(login.rejected, (s, a) => { s.error = a.payload; })
      .addCase(register.rejected, (s, a) => { s.error = a.payload; })
      .addCase(fetchProfile.fulfilled, (s, a) => { s.user = a.payload; s.loading = false; })
      .addCase(fetchProfile.rejected, (s) => {
        localStorage.removeItem('bc_token');
        s.token = null; s.user = null; s.loading = false;
      })
      .addCase(setAvailability.fulfilled, (s, a) => { s.user = a.payload; })
      .addCase(updateDonorProfile.fulfilled, (s, a) => { s.user = a.payload; });
  },
});

export const { logout, clearError } = slice.actions;
export default slice.reducer;
