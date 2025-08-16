import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './route/ProtectedRoute';

import { AuthProvider } from './context/AuthContext';
import { SearchProvider } from './context/SearchContext';

import Dashboard from './pages/Dashboard/Dashboard';
import Landing from './pages/Landing/Landing';
import LoginPage from './pages/Login/LoginPage';
import SearchResult from './pages/SearchResult/SearchResult';
import ViewMovieDetails from './pages/MovieDetails/ViewMovieDetails';

function App() {
    return (
        <Router>
            <AuthProvider>
                <SearchProvider>
                    <Routes>
                        <Route
                            path="/dashboard"
                            element={
                                <ProtectedRoute>
                                    <Dashboard />
                                </ProtectedRoute>
                            }
                        />
                        <Route path='/search' element={<ProtectedRoute><SearchResult /></ProtectedRoute>} />
                    </Routes>
                </SearchProvider>
                <Routes>
                    {/* Public routes */}
                    <Route path="/landing" element={<Landing />} />
                    <Route path="/login" element={<LoginPage />} />

                    <Route
                        path='/movie/:id'
                        element={<ProtectedRoute><ViewMovieDetails /></ProtectedRoute>}
                    />

                    {/* Root redirect */}
                    <Route
                        path="/"
                        element={<Navigate to="/landing" replace />}
                    />
                </Routes>
                {/* 
                Fallback route
                <Route path="*" element={<Navigate to="/landing" replace />} /> */}
            </AuthProvider>
        </Router>
    );
}

export default App;