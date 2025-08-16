import React, { useEffect, useState } from 'react';
import {
    Search,
    Mail,
    Bell,
    MoreHorizontal,
    Play,
    Settings,
    LogOut,
    Plus,
    UserPlus
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

import { useAuth } from '../../context/AuthContext';
import { useSearch } from '../../context/SearchContext';

import { MOVIE_SERVICE_BASE_URL } from '../../api/ApiUrl';

import MovieSearchResponse from '../SearchResult/types/MovieSearchResponse';
import MovieSearchResult from '../SearchResult/types/MovieSearchResult';
import NewlyReleasedMoviesResponse from './types/NewlyReleasedMoviesResponse';
import NewlyReleasedMovie from './types/NewlyReleasedMovie';

import NewlyReleasedMovies from './components/NewlyReleasedMovies/NewlyReleasedMovies';

import styles from './Dashboard.module.css';

interface Actor {
    id: string;
    name: string;
    role: string;
    avatar: string;
    isFollowing: boolean;
}

const Dashboard: React.FC = () => {
    const [activeTab, setActiveTab] = useState('Dashboard');
    const [isLoggingOut, setIsLoggingOut] = useState(false);
    const [dailyNewMovies, setDailyNewMovies] = useState<NewlyReleasedMovie[]>([]);

    const navigate = useNavigate();

    const { searchTerm, setSearchTerm, setSearchResults } = useSearch();
    const { logout, user } = useAuth();

    useEffect(() => {
        const fetchDailyNewMovie = async () => {
            try {
                const token = localStorage.getItem('access_token');

                const response = await fetch(`${MOVIE_SERVICE_BASE_URL}/api/v1.0/movies/daily-new`, {
                    method: 'GET',
                    headers: { 'Authorization': `Bearer ${token}` }
                });

                if (response.ok) {
                    const dailyNewMovieResponse = await response.json() as NewlyReleasedMoviesResponse;
                    setDailyNewMovies(prev => [...prev, ...dailyNewMovieResponse.results]);
                }
            } catch (err) {
                console.error('Error occurred while fetching daily new movie ', err);
            }
        };

        fetchDailyNewMovie();
    }, []);

    const handleLogout = async () => {
        if (isLoggingOut) return;

        setIsLoggingOut(true);
        try {
            await logout();
        } catch (error) {
            console.error('Logout failed:', error);
        }
    };

    const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => setSearchTerm(e.target.value);

    const handleSearchSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        if (!searchTerm.trim()) {
            return;
        }

        setSearchResults([]);

        try {
            const token = localStorage.getItem('access_token');

            const response: Response = await fetch(`${MOVIE_SERVICE_BASE_URL}/api/v1.0/movies/tmdb?q=${searchTerm}`, {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                const payload = await response.json() as MovieSearchResponse;
                setSearchResults((prev: MovieSearchResult[]) => [...prev, ...payload.results]);
            } 
        } catch (err) {
            console.error('Search error:', err);
        }

        navigate('/search');
    };

    const mentors: Actor[] = [
        {
            id: '1',
            name: 'Padhang Satrio',
            role: 'Mentor',
            avatar: '/api/placeholder/40/40',
            isFollowing: false
        },
        {
            id: '2',
            name: 'Zakir Horizontal',
            role: 'Mentor',
            avatar: '/api/placeholder/40/40',
            isFollowing: false
        },
        {
            id: '3',
            name: 'Leonardo Samsul',
            role: 'Mentor',
            avatar: '/api/placeholder/40/40',
            isFollowing: false
        }
    ];

    const progressCourses = [
        { title: 'UI/UX Design', progress: '2/8', color: 'purple' },
        { title: 'Branding', progress: '3/8', color: 'pink' },
        { title: 'FrontEnd', progress: '6/12', color: 'blue' }
    ];

    return (
        <div className={styles.dashboard}>

            {/* Main Content */}
            <div className={styles.mainContent}>
                {/* Header */}
                <header className={styles.header}>
                    <div className={styles.headerContent}>
                        <div>Movie Discovery</div>
                        <div className={styles.searchContainer}>
                            <Search className={styles.searchIcon} />
                            <form onSubmit={handleSearchSubmit}>
                                <input
                                    onChange={handleSearchChange}
                                    type="text"
                                    placeholder="Search your course..."
                                    className={styles.searchInput}
                                />
                            </form>
                        </div>
                        <div className={styles.headerActions}>
                            <button className={styles.headerButton}>
                                <Mail className={styles.headerIcon} />
                            </button>
                            <button className={styles.headerButton}>
                                <Bell className={styles.headerIcon} />
                            </button>
                            <div className={styles.userProfile}>
                                <div className={styles.userAvatar}></div>
                                <span className={styles.userName}>{user || 'Jason Ranti'}</span>
                            </div>
                        </div>
                    </div>
                </header>

                <div className={styles.contentWrapper}>
                    {/* Left Sidebar */}
                    <div className={styles.rightSidebar}>
                        {/* Statistics */}
                        <div className={styles.statsSection}>
                            <h3 className={styles.statsTitle}>Statistic</h3>
                            <div className={styles.statsCard}>
                                <div className={styles.statItem}>
                                    <span className={styles.statLabel}>This Month</span>
                                    <span className={styles.statValue}>39%</span>
                                </div>
                                <div className={styles.progressBar}>
                                    <div className={styles.progressFill} style={{ width: '39%' }}></div>
                                </div>
                            </div>
                        </div>

                        {/* Activity */}
                        <div className={styles.activitySection}>
                            <h3 className={styles.activityTitle}>Recent Activity</h3>
                            <div className={styles.activityList}>
                                <div className={styles.activityItem}>
                                    <div className={styles.activityIcon}>📚</div>
                                    <div className={styles.activityContent}>
                                        <p className={styles.activityText}>Completed Frontend course</p>
                                        <p className={styles.activityTime}>2 hours ago</p>
                                    </div>
                                </div>
                                <div className={styles.activityItem}>
                                    <div className={styles.activityIcon}>🎯</div>
                                    <div className={styles.activityContent}>
                                        <p className={styles.activityText}>Started UI/UX Design</p>
                                        <p className={styles.activityTime}>5 hours ago</p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Follow popular actors */}
                        <div className={styles.mentorSection}>
                            <div className={styles.mentorHeader}>
                                <h3 className={styles.mentorTitle}>Follow popular actors</h3>
                                <button className={styles.addMentorButton}>
                                    <Plus size={16} />
                                </button>
                            </div>

                            <div className={styles.mentorList}>
                                {mentors.map(mentor => (
                                    <div key={mentor.id} className={styles.mentorCard}>
                                        <div className={styles.mentorCardAvatar}></div>
                                        <div className={styles.mentorCardInfo}>
                                            <p className={styles.mentorCardName}>{mentor.name}</p>
                                            <p className={styles.mentorCardRole}>{mentor.role}</p>
                                        </div>
                                        <button className={styles.followButton}>
                                            <UserPlus size={12} />
                                            Follow
                                        </button>
                                    </div>
                                ))}
                            </div>

                            <button className={styles.seeAllMentors}>
                                See More
                            </button>
                        </div>

                        <div className={styles.sidebarFooter}>
                            <div className={styles.settingsNav}>
                                <button className={styles.settingsItem}>
                                    <Settings className={styles.navIcon} />
                                    Setting
                                </button>
                                <button
                                    className={`${styles.settingsItem} ${styles.logoutItem}`}
                                    onClick={handleLogout}
                                    disabled={isLoggingOut}
                                >
                                    <LogOut className={styles.navIcon} />
                                    {isLoggingOut ? 'Logging out...' : 'Logout'}
                                </button>
                            </div>
                        </div>
                    </div>

                    {/* Main Content Area */}
                    <main className={styles.main}>
                        {/* Hero Section */}
                        <div className={styles.hero}>
                            <div className={styles.heroContent}>
                                <p className={styles.heroSubtitle}>ONLINE COURSE</p>
                                <h1 className={styles.heroTitle}>
                                    Sharpen Your Skills with<br />
                                    Professional Online Courses
                                </h1>
                                <button className={styles.heroButton}>
                                    Join Now
                                    <Play className={styles.heroButtonIcon} />
                                </button>
                            </div>
                        </div>

                        {/* Progress Cards */}
                        <div className={styles.progressGrid}>
                            {progressCourses.map((course, index) => (
                                <div key={index} className={styles.progressCard}>
                                    <div className={styles.progressCardHeader}>
                                        <div className={`${styles.progressIcon} ${styles[`progressIcon${course.color}`]}`}>
                                            <span>●</span>
                                        </div>
                                        <button className={styles.moreButton}>
                                            <MoreHorizontal className={styles.moreIcon} />
                                        </button>
                                    </div>
                                    <p className={styles.progressText}>{course.progress} watched</p>
                                    <h3 className={styles.progressTitle}>{course.title}</h3>
                                </div>
                            ))}
                        </div>

                        {/* Newly Released Today */}
                        <NewlyReleasedMovies movies={dailyNewMovies} />

                        {/* Your Lesson */}
                        <div className={styles.lessonSection}>
                            <div className={styles.lessonHeader}>
                                <h2 className={styles.sectionTitle}>Your Lesson</h2>
                                <button className={styles.seeAllButton}>See all</button>
                            </div>

                            <div className={styles.lessonTable}>
                                <div className={styles.tableHeader}>
                                    <div>MENTOR</div>
                                    <div>TYPE</div>
                                    <div>DESC</div>
                                    <div>ACTION</div>
                                </div>
                                <div className={styles.tableRow}>
                                    <div className={styles.mentorCell}>
                                        <div className={styles.mentorAvatar}></div>
                                        <div className={styles.mentorDetails}>
                                            <p className={styles.mentorName}>Padhang Satrio</p>
                                            <p className={styles.lessonDate}>27/6/2024</p>
                                        </div>
                                    </div>
                                    <div className={styles.typeCell}>
                                        <span className={styles.typeTag}>UI/UX DESIGN</span>
                                    </div>
                                    <div className={styles.descCell}>Understand Of UI/UX Design</div>
                                    <div className={styles.actionCell}>
                                        <button className={styles.playButton}>
                                            <Play className={styles.playIcon} />
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </main>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;