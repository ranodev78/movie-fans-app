import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';

import { AUTH_SERVICE_BASE_URL } from '../api/ApiUrl';

interface AuthContextType {
    isAuthenticated: boolean;
    isLoading: boolean;
    user: string;
    checkAuth: () => Promise<void>;
    login: (credentials: { email: string; password: string }) => Promise<boolean>;
    logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const fetchWithCredentials = async (url: string, options: RequestInit = {}) => {
    const token = localStorage.getItem('access_token');

    const headers = {
        ...options.headers,
        ...(token && { Authorization: `Bearer ${token}` })
    }

    return fetch(url, {
        ...options,
        headers
    });
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [isLoading, setIsLoading] = useState(true); // Start with true for initial check
    const [user, setUser] = useState<string>('');

    const checkAuth = useCallback(async () => {
        setIsLoading(true);
        try {
            const response: Response = await fetchWithCredentials(AUTH_SERVICE_BASE_URL);
            if (response.ok) {
                const userData = await response.text();
                setUser(userData);
                setIsAuthenticated(true);
            } else {
                setUser('');
                setIsAuthenticated(false);
            }
        } catch (error) {
            console.error('Auth check failed:', error);
            setUser('');
            setIsAuthenticated(false);
        } finally {
            setIsLoading(false);
        }
    }, []);

    const login = useCallback(async (credentials: { email: string; password: string }): Promise<boolean> => {
        setIsLoading(true);
        try {
            const response = await fetchWithCredentials(`${AUTH_SERVICE_BASE_URL}/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(credentials),
            });

            if (response.ok) {
                const { access_token } = await response.json();
                if (access_token) {
                    localStorage.setItem('access_token', access_token);
                    await checkAuth();
                    return true;
                } else {
                    console.error('No access token in response');
                    return false;
                }
            } else {
                const errorData = await response.json().catch(() => ({ message: 'Login failed' }));
                console.error('Login failed:', errorData.message);
                return false;
            }
        } catch (error) {
            console.error('Login error:', error);
            return false;
        } finally {
            setIsLoading(false);
        }
    }, [checkAuth]);

    const logout = useCallback(async () => {
        setIsLoading(true);
        try {
            localStorage.removeItem('access_token'); // Clear token
            setUser('');
            setIsAuthenticated(false);
            window.location.href = '/landing';
        } catch (err) {
            console.error('Logout error:', err);
            setUser('');
            setIsAuthenticated(false);
            window.location.href = '/landing';
        } finally {
            setIsLoading(false);
        }
    }, []);

    // Check authentication on mount
    useEffect(() => {
        checkAuth();
    }, [checkAuth]);

    return (
        <AuthContext.Provider value={{
            isAuthenticated,
            isLoading,
            user,
            checkAuth,
            login,
            logout
        }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }

    return context;
};