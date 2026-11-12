import { configureStore } from '@reduxjs/toolkit';
import auth from './authSlice';
import requests from './requestSlice';
import donor from './donorSlice';
import notifications from './notificationSlice';

export const store = configureStore({
  reducer: { auth, requests, donor, notifications },
});
