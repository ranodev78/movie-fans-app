import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface ProtectedRouteProps {
    children: React.ReactNode;
    redirectTo?: string;
    fallback?: React.ReactNode;
}

const LoadingSpinner: React.FC = () => (
    <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        fontSize: '18px'
    }}>
        <div>
            <div style={{
                border: '4px solid #f3f3f3',
                borderTop: '4px solid #3498db',
                borderRadius: '50%',
                width: '40px',
                height: '40px',
                animation: 'spin 2s linear infinite',
                margin: '0 auto 16px'
            }}></div>
            <style dangerouslySetInnerHTML={{
                __html: `
                @keyframes spin {
                    0% { transform: rotate(0deg); }
                    100% { transform: rotate(360deg); }
                }
                `
            }} />
            Checking authentication...
        </div>
    </div>
);

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
    children,
    redirectTo = '/landing',
    fallback
}) => {
    const { isAuthenticated, isLoading } = useAuth();
    const location = useLocation();

    if (isLoading) {
        return <>{fallback || <LoadingSpinner />}</>;
    }

    if (!isAuthenticated) {
        // Save the attempted location for redirect after login
        return <Navigate
            to={redirectTo}
            state={{ from: location }}
            replace
        />;
    }

    return <>{children}</>;
};

export default ProtectedRoute;