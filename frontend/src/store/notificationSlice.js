import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../api/client';

export const fetchNotifications = createAsyncThunk('notif/list', async () => (await api.get('/notifications')).data);
export const fetchUnread = createAsyncThunk('notif/unread', async () => (await api.get('/notifications/unread-count')).data.count);
export const markAllRead = createAsyncThunk('notif/readAll', async () => {
  await api.post('/notifications/read-all');
});

const slice = createSlice({
  name: 'notifications',
  initialState: { items: [], unread: 0 },
  reducers: {},
  extraReducers: (b) => {
    b.addCase(fetchNotifications.fulfilled, (s, a) => { s.items = a.payload; })
      .addCase(fetchUnread.fulfilled, (s, a) => { s.unread = a.payload; })
      .addCase(markAllRead.fulfilled, (s) => {
        s.unread = 0;
        s.items = s.items.map((n) => ({ ...n, seen: true }));
      });
  },
});

export default slice.reducer;
