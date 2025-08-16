import React from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Heart } from 'lucide-react';

import { useSearch } from '../../context/SearchContext';

import { MOVIE_SERVICE_BASE_URL } from '../../api/ApiUrl';

import Movie from './types/Movie';
import MovieDetails from '../MovieDetails/types/MovieDetails';

import styles from './SearchResult.module.css';

const TMDB_IMAGE_BASE = 'https://image.tmdb.org/t/p/w185';

const SearchResult: React.FC = () => {
    const { searchTerm, setSearchTerm, searchResults, setSearchResults } = useSearch();

    const navigate = useNavigate();

    if (!searchResults.length) return <p>No results found.</p>;

    const handleMovieCardOnClick = async (movieId: number, movieName: string) => {
        navigate(`/movie/${movieId}`, { state: { movieName }});
    }

    return (
        <div className={styles.moviesGrid}>
            {searchResults.map(movie => (
                <div key={movie.id} className={styles.movieCard} onClick={(e: React.MouseEvent<HTMLDivElement>) => handleMovieCardOnClick(movie.id, movie.title)}>
                    <div className={styles.moviePosterContainer}>
                        <div className={styles.movieImage}>
                            {movie.poster_path ?
                                (<img src={`${TMDB_IMAGE_BASE}${movie.poster_path}`} alt={movie.title} className={styles.poster} />)
                                : (<div className={styles.noPoster}>No Image</div>)}
                        </div>
                    </div>
                    <div className={styles.courseContent}>
                        <h3 className={styles.movieTitle}>{movie.title}</h3>
                        <p className={styles.releaseDate}>{movie.release_date}</p>
                        <p className={styles.rating}>⭐ {movie.vote_average ?? 'N/A'}</p>
                    </div>
                    <div className={styles.overlay}>
                        <button className={styles.heartButton}>
                            <Heart className={styles.heartIcon} />
                        </button>
                        <h4>{movie.title}</h4>
                        <p className={styles.description}>{movie.overview}</p>
                    </div>
                </div>))}
        </div>
    );
};

export default SearchResult;
