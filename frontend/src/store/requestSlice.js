import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api, { errMsg } from '../api/client';

export const fetchMyRequests = createAsyncThunk('requests/mine', async (_, { rejectWithValue }) => {
  try {
    return (await api.get('/requests')).data;
  } catch (e) {
    return rejectWithValue(errMsg(e));
  }
});

export const createRequest = createAsyncThunk('requests/create', async (body, { rejectWithValue }) => {
  try {
    return (await api.post('/requests', body)).data;
  } catch (e) {
    return rejectWithValue(errMsg(e));
  }
});

const slice = createSlice({
  name: 'requests',
  initialState: { items: [], loaded: false, error: null },
  reducers: {},
  extraReducers: (b) => {
    b.addCase(fetchMyRequests.fulfilled, (s, a) => { s.items = a.payload; s.loaded = true; s.error = null; })
      .addCase(fetchMyRequests.rejected, (s, a) => { s.error = a.payload; s.loaded = true; })
      .addCase(createRequest.fulfilled, (s, a) => { s.items = [a.payload, ...s.items]; });
  },
});

export default slice.reducer;
