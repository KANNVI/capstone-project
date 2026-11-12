import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api, { errMsg } from '../api/client';

export const fetchInbox = createAsyncThunk('donor/inbox', async (_, { rejectWithValue }) => {
  try {
    return (await api.get('/donor-requests/mine')).data;
  } catch (e) {
    return rejectWithValue(errMsg(e));
  }
});

export const fetchHistory = createAsyncThunk('donor/history', async (_, { rejectWithValue }) => {
  try {
    return (await api.get('/donors/history')).data;
  } catch (e) {
    return rejectWithValue(errMsg(e));
  }
});

/** action: accept | reject | start | complete */
export const respond = createAsyncThunk('donor/respond', async ({ id, action }, { rejectWithValue }) => {
  try {
    return (await api.post(`/donor-requests/${id}/${action}`)).data;
  } catch (e) {
    return rejectWithValue(errMsg(e));
  }
});

const slice = createSlice({
  name: 'donor',
  initialState: { inbox: [], history: [], loaded: false, error: null },
  reducers: { clearDonorError(s) { s.error = null; } },
  extraReducers: (b) => {
    b.addCase(fetchInbox.fulfilled, (s, a) => { s.inbox = a.payload; s.loaded = true; })
      .addCase(fetchInbox.rejected, (s, a) => { s.error = a.payload; s.loaded = true; })
      .addCase(fetchHistory.fulfilled, (s, a) => { s.history = a.payload; })
      .addCase(respond.fulfilled, (s, a) => {
        s.error = null;
        s.inbox = s.inbox.map((x) => (x.id === a.payload.id ? a.payload : x));
      })
      .addCase(respond.rejected, (s, a) => { s.error = a.payload; });
  },
});

export const { clearDonorError } = slice.actions;
export default slice.reducer;
