// MovieLandingPage.jsx
import React, { useState, useEffect } from 'react';

import styles from './Landing.module.css';

const FEATURES = [
    { icon: '🎯', title: 'Smart Recommendations', text: 'AI-powered suggestions based on your viewing history and preferences. Discover your next favorite movie effortlessly.' },
    { icon: '📝', title: 'Personal Watchlists', text: 'Create unlimited watchlists, share with friends, and never forget a movie recommendation again.' },
    { icon: '🔥', title: 'Trending Content', text: 'Stay up-to-date with what\'s hot. Real-time trending movies and shows across all platforms.' },
    { icon: '⭐', title: 'Reviews & Ratings', text: 'Read trusted reviews, rate movies, and join a community of film enthusiasts worldwide.' },
    { icon: '📱', title: 'Cross-Platform Sync', text: 'Seamlessly sync across all your devices. Start on mobile, continue on desktop.' },
    { icon: '🎪', title: 'Exclusive Content', text: 'Access behind-the-scenes content, interviews, and exclusive previews from your favorite films.' }
];

const createParticles = () => {
    const particlesContainer = document.getElementById('particles-container');
    if (!particlesContainer) return;

    for (let i = 0; i < 50; i++) {
        const particle = document.createElement('div');
        particle.className = styles.particle;
        particle.style.left = Math.random() * 100 + '%';
        particle.style.animationDelay = Math.random() * 20 + 's';
        particle.style.animationDuration = (Math.random() * 10 + 15) + 's';
        particlesContainer.appendChild(particle);
    }
};

const handleButtonClick = (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();

    // Create ripple effect
    const button = e.currentTarget;
    const rect = button.getBoundingClientRect();
    const size = Math.max(rect.width, rect.height);
    const x = e.clientX - rect.left - size / 2;
    const y = e.clientY - rect.top - size / 2;

    const ripple = document.createElement('span');

    Object.assign(ripple.style, {
        width: size + 'px',
        height: size + 'px',
        left: x + 'px',
        top: y + 'px',
        position: 'absolute',
        borderRadius: '50%',
        background: 'rgba(255, 255, 255, 0.3)',
        transform: 'scale(0)',
        animation: 'ripple 0.6s linear',
        pointerEvents: 'none'
    });

    button.style.position = 'relative';
    button.style.overflow = 'hidden';
    button.appendChild(ripple);

    setTimeout(() => ripple.remove(), 600);

    window.location.href = '/login';
};

const Landing = () => {
    const [isScrolled, setIsScrolled] = useState<boolean>(false);
    const [hoveredElement, setHoveredElement] = useState<string | null>(null);
    const [revealedElements, setRevealedElements] = useState<Set<string>>(new Set());

    useEffect(() => {
        // Scroll handler
        const handleScroll = () => {
            setIsScrolled(window.scrollY > 50);
        };

        // Intersection observer for scroll reveals
        const observer = new IntersectionObserver(
            (entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting && entry.target instanceof HTMLElement) {
                        setRevealedElements(prev => new Set([...prev, entry.target.dataset.index || '']));
                    }
                });
            },
            { threshold: 0.1, rootMargin: '0px 0px -50px 0px' }
        );

        // Observe scroll reveal elements
        document.querySelectorAll('[data-scroll-reveal]').forEach((el, index) => {
            if (el instanceof HTMLElement) {
                el.dataset.index = index.toString();
                observer.observe(el);
            }
        });

        window.addEventListener('scroll', handleScroll);

        // Create particles
        createParticles();

        return () => {
            window.removeEventListener('scroll', handleScroll);
            observer.disconnect();
        };
    }, []);

    const getRevealClass = (index: string | number): string =>
        revealedElements.has(index.toString())
            ? `${styles.scrollReveal} ${styles.scrollRevealed}`
            : styles.scrollReveal;

    return (
        <div className={styles.container}>
            <div className={styles.bgAnimation}></div>
            <div id="particles-container" className={styles.particles}></div>

            <nav className={`${styles.nav} ${isScrolled ? styles.navScrolled : ''}`}>
                <div className={styles.navContainer}>
                    <div className={styles.logo}>CineVerse</div>
                    <div className={styles.navLinks}>
                        <a href="#" className={styles.navLink}>Movies</a>
                        <a href="#" className={styles.navLink}>TV Shows</a>
                        <a href="#" className={styles.navLink}>Watchlist</a>
                        <button className={styles.signinBtn} onClick={handleButtonClick}>
                            Sign In
                        </button>
                    </div>
                </div>
            </nav>

            <section className={styles.hero}>
                <div className={styles.heroContent}>
                    <h1 className={styles.heroTitle}>Your Ultimate Movie Experience</h1>
                    <p className={styles.heroText}>
                        Discover, track, and enjoy millions of movies and TV shows. Get personalized recommendations, create watchlists, and never miss what's trending.
                    </p>
                    <div className={styles.ctaButtons}>
                        <button
                            className={`${styles.ctaBtn} ${styles.ctaPrimary}`}
                            onClick={handleButtonClick}
                        >
                            🎬 Start Watching
                        </button>
                        <button
                            className={`${styles.ctaBtn} ${styles.ctaSecondary}`}
                            onClick={handleButtonClick}
                        >
                            📱 Download App
                        </button>
                    </div>
                </div>
            </section>

            <section className={styles.features}>
                <h2 className={getRevealClass('title')} data-scroll-reveal>
                    Why Choose CineVerse?
                </h2>
                <div className={styles.featuresGrid}>
                    {FEATURES.map((feature, index) => (
                        <div
                            key={index}
                            className={getRevealClass(index + 1)}
                            data-scroll-reveal
                        >
                            <div className={styles.featureCard}>
                                <div className={styles.featureIcon}>{feature.icon}</div>
                                <h3 className={styles.featureTitle}>{feature.title}</h3>
                                <p className={styles.featureText}>{feature.text}</p>
                            </div>
                        </div>
                    ))}
                </div>
            </section>
        </div>
    );
};

export default Landing;