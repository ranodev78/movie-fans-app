import React, { useState, FormEvent, ChangeEvent, useEffect } from 'react';
import { Eye, EyeOff, Shield, AlertCircle, CheckCircle2, Loader2 } from 'lucide-react';

import { AUTH_SERVICE_BASE_URL, AUTH_SERVICE_USERS_PATH } from '../../api/ApiUrl';
import { fetchWithCredentials } from '../../context/AuthContext';

import styles from './LoginPage.module.css';

interface FormData {
    username: string;
    password: string;
}

const checkSession = async () => {
    try {
        const response = await fetchWithCredentials(AUTH_SERVICE_BASE_URL + AUTH_SERVICE_USERS_PATH);
        if (response.ok) {
            const userData = await response.text();
            if (userData !== '') {
                window.location.href = '/dashboard';
            }
        }
    } catch (err) {
        console.error('Error retrieving session: ', err)
    }
}

const LoginPage: React.FC = () => {
    const [formData, setFormData] = useState<FormData>({
        username: '',
        password: ''
    });
    const [showPassword, setShowPassword] = useState<boolean>(false);
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [error, setError] = useState<string>('');
    const [success, setSuccess] = useState<string>('');

    useEffect(() => {
        checkSession();
    }, []);

    const handleInputChange = (e: ChangeEvent<HTMLInputElement>): void => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));

        // Clear error when user starts typing
        if (error) {
            setError('');
        }
    };

    const handleSubmit = async (e: FormEvent<HTMLFormElement>): Promise<void> => {
        e.preventDefault();
        setIsLoading(true);
        setError('');
        setSuccess('');

        try {
            // Create form data for Spring Security's default form login
            const loginFormData: FormData = {
                username: formData.username,
                password: formData.password,
            };

            const response: Response = await fetch(AUTH_SERVICE_BASE_URL + '/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(loginFormData),
            });

            if (response.ok) {
                const data = await response.json();
                const token = data.access_token;

                if (token) {
                    localStorage.setItem('access_token', token);

                    setSuccess('Login successful! Redirecting...');

                    // Handle successful login - redirect or update app state
                    setTimeout(() => window.location.href = '/dashboard', 1500);
                } else {
                    setError('Login succeeded but token was not provided');
                }

            } else if (response.status === 401) {
                setError('Invalid username or password');
            } else if (response.status === 403) {
                setError('Account is disabled or locked');
            } else {
                setError('Login failed. Please try again.');
            }
        } catch (err: unknown) {
            setError('Network error. Please check your connection.');
        } finally {
            setIsLoading(false);
        }
    };

    const togglePasswordVisibility = (): void => setShowPassword(!showPassword);

    const isFormValid = formData.username.trim().length > 0 && formData.password.length >= 6;

    return (
        <div className={styles.container}>
            <div className={styles.backgroundPattern}></div>

            <div className={styles.wrapper}>
                <div className={styles.card}>
                    <div className={styles.header}>
                        <div className={styles.iconWrapper}>
                            <Shield className={styles.icon} />
                        </div>
                        <h1 className={styles.title}>Welcome Back</h1>
                        <p className={styles.subtitle}>Sign in to access your dashboard</p>
                    </div>

                    <div className={styles.formContainer}>
                        <form onSubmit={handleSubmit} className={styles.form}>
                            <div className={styles.formGroup}>
                                <label htmlFor="username" className={styles.label}>
                                    Username
                                </label>
                                <input
                                    type="text"
                                    id="username"
                                    name="username"
                                    value={formData.username}
                                    onChange={handleInputChange}
                                    className={styles.input}
                                    placeholder="Enter your username"
                                    required
                                    autoComplete="username"
                                />
                            </div>

                            <div className={styles.formGroup}>
                                <label htmlFor="password" className={styles.label}>
                                    Password
                                </label>
                                <div className={styles.passwordWrapper}>
                                    <input
                                        type={showPassword ? 'text' : 'password'}
                                        id="password"
                                        name="password"
                                        value={formData.password}
                                        onChange={handleInputChange}
                                        className={styles.passwordInput}
                                        placeholder="Enter your password"
                                        required
                                        autoComplete="current-password"
                                        minLength={6}
                                    />
                                    <button
                                        type="button"
                                        onClick={togglePasswordVisibility}
                                        className={styles.passwordToggle}
                                        aria-label={showPassword ? 'Hide password' : 'Show password'}
                                    >
                                        {showPassword ? <EyeOff className={styles.toggleIcon} /> : <Eye className={styles.toggleIcon} />}
                                    </button>
                                </div>
                            </div>

                            <div className={styles.rememberRow}>
                                <a href="/forgot-password" className={styles.forgotLink}>
                                    Forgot password?
                                </a>
                            </div>

                            {error && (
                                <div className={`${styles.message} ${styles.errorMessage}`}>
                                    <AlertCircle className={styles.messageIcon} />
                                    <span className={styles.messageText}>{error}</span>
                                </div>
                            )}

                            {success && (
                                <div className={`${styles.message} ${styles.successMessage}`}>
                                    <CheckCircle2 className={styles.messageIcon} />
                                    <span className={styles.messageText}>{success}</span>
                                </div>
                            )}

                            <button
                                type="submit"
                                disabled={!isFormValid || isLoading}
                                className={styles.submitButton}
                            >
                                {isLoading ? (
                                    <>
                                        <Loader2 className={`${styles.buttonIcon} ${styles.spinner}`} />
                                        <span>Signing in...</span>
                                    </>
                                ) : (
                                    <span>Sign In</span>
                                )}
                            </button>
                        </form>

                        <div className={styles.divider}>
                            <p className={styles.supportText}>
                                Need help accessing your account?{' '}
                                <a href="/support" className={styles.supportLink}>
                                    Contact Support
                                </a>
                            </p>
                        </div>
                    </div>
                </div>

                <div className={styles.footer}>
                    <p>© 2025 Your Company. All rights reserved.</p>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
