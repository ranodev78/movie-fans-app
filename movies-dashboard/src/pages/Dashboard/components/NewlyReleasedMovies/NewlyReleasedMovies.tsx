import React, { useState } from 'react';
import { Heart } from 'lucide-react';

import NewlyReleasedMovie from '../../types/NewlyReleasedMovie';
import Pagination from '../Pagination/Pagination';

import styles from './NewlyReleasedMovies.module.css';

interface Props {
    movies: NewlyReleasedMovie[];
}

const MAX_ITEMS_PER_ROW = 5;
const MAX_ROWS = 2
const ITEMS_PER_PAGE = MAX_ITEMS_PER_ROW * MAX_ROWS;

const getGenreClass = (genre: string) => {
    const camelCase = genre.replace(/\s+/g, '');
    return styles[`genre${camelCase}`] || '';
};

const NewlyReleasedMovies: React.FC<Props> = ({ movies }) => {
    const [currentPage, setCurrentPage] = useState(1);

    const totalPages = Math.ceil(movies.length / ITEMS_PER_PAGE);
    const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
    const paginatedMovies = movies.slice(startIndex, startIndex + ITEMS_PER_PAGE);

    return (
        <div className={styles.continueSection}>
            <div className={styles.sectionHeader}>
                <h2 className={styles.sectionTitle}>Newly Released Today</h2>
                <Pagination currentPage={currentPage} totalPages={totalPages} onPageChange={setCurrentPage} />
            </div>

            <div className={styles.coursesGrid}>
                {paginatedMovies.map((movie) => (
                    <div key={movie.id} className={styles.courseCard}>
                        <div className={styles.courseImageContainer}>
                            <div className={styles.courseImage}>
                                <img
                                    src={`https://image.tmdb.org/t/p/w500${movie.posterPath}`}
                                    alt={movie.title}
                                />
                            </div>
                        </div>
                        <div className={styles.courseContent}>
                            <h3 className={styles.courseTitle}>{movie.title}</h3>
                            {movie.genreIds.map((genre) => (
                                <span key={genre} className={`${styles.genreBadge} ${getGenreClass(genre)}`}>
                                    {genre}
                                </span>
                            ))}
                        </div>
                        <div className={styles.overlay}>
                            <button className={styles.heartButton}>
                                <Heart className={styles.heartIcon} />
                            </button>
                            <h4>{movie.title}</h4>
                            <p className={styles.description}>{movie.overview}</p>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default NewlyReleasedMovies;