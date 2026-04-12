import React, { createContext, useContext, useState, useEffect } from 'react';
import API from '../api/axios';

const AuthContext = createContext(null);

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const stored = localStorage.getItem('user');
    if (stored) {
      try { setUser(JSON.parse(stored)); } catch {}
    }
    setLoading(false);
  }, []);

  const login = async (username, password) => {
    const res = await API.post('/auth/login', { username, password });
    const token = res.data.data?.accessToken || res.data.data?.token || res.data.accessToken || res.data.token;
    localStorage.setItem('token', token);

    // Decode username from JWT
    const payload = JSON.parse(atob(token.split('.')[1]));
    const userData = { username: payload.sub, token, role: 'ROLE_USER' };

    // Check if user is admin by trying admin endpoint
    try {
      await API.get('/orders');
      userData.role = 'ROLE_ADMIN';
    } catch {
      userData.role = 'ROLE_USER';
    }

    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
    return userData;
  };

  const register = async (username, email, password) => {
    await API.post('/auth/register', { username, email, password });
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  const isAdmin = user?.role === 'ROLE_ADMIN';

  return (
    <AuthContext.Provider value={{ user, login, register, logout, loading, isAdmin }}>
      {children}
    </AuthContext.Provider>
  );
};