import { useState, useEffect } from "react";
import { useLocation, useParams } from "react-router-dom";

import { MOVIE_SERVICE_BASE_URL } from "../../api/ApiUrl";

import MovieDetails from "./types/MovieDetails";
import WatchProvidersResponse from "./types/WatchProvidersResponse";
import WatchProviderRegion from "./types/WatchProviderRegion";

import styles from './ViewMovieDetails.module.css';

async function safeFetch<T>(url: string, token: string | null, isJson: boolean): Promise<T | null> {
    try {
        const response: Response = await fetch(url, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        return (isJson
            ? await response.json()
            : await response.text()) as T;
    } catch (err) {
        console.error(`Fetch failed for ${url} due to:`, err);
        return null;
    }
}

const ViewMovieDetails: React.FC = () => {
    const { id: movieId } = useParams<{ id: string }>();
    const location = useLocation();

    const state = location.state as { movieName: string };

    const [movie, setMovie] = useState<MovieDetails | null>(null);
    const [watchProviders, setWatchProviders] = useState<Record<string, WatchProviderRegion>>({});
    const [reviews, setReviews] = useState<string>('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchMovieDetails = async () => {
            const token = localStorage.getItem('access_token');

            const [movieDetailsResponse, watchProvidersResponse, summarizedReviewsResponse] = await Promise.all([
                safeFetch<MovieDetails>(`${MOVIE_SERVICE_BASE_URL}/api/v1.0/movies/tmdb/${movieId}`, token, true),
                safeFetch<WatchProvidersResponse>(`${MOVIE_SERVICE_BASE_URL}/api/v1.0/movies/tmdb/${movieId}/watch-providers`, token, true),
                safeFetch<string>(`${MOVIE_SERVICE_BASE_URL}/api/v1.0/movies/tmdb/${movieId}/reviews?name=${state.movieName}`, token, false)
            ]);

            if (!movieDetailsResponse) {
                console.warn('Movie details not available');
            } else {
                setMovie(prev => ({ ...prev, ...movieDetailsResponse }));
            }

            if (watchProvidersResponse?.results) {
                setWatchProviders(prev => ({
                    ...prev,
                    ...watchProvidersResponse.results
                }));
            } else {
                console.warn('Watch providers not available');
                setWatchProviders({});
            }

            if (!summarizedReviewsResponse) {
                console.warn('Could not summarize reviews for this movie');
            } else {
                setReviews(summarizedReviewsResponse);
            }
        };

        fetchMovieDetails();
    }, []);

    return (
        <div className={styles.movieDetails}>
            {movie?.poster_path && (
                <img
                    src={`https://image.tmdb.org/t/p/w500${movie?.poster_path}`}
                    alt={movie?.title}
                    className={styles.moviePoster}
                />
            )}
            <h1 className={styles.title}>{movie?.title}</h1>
            <p className={styles.releaseDate}>
                <span className={styles.label}>Release Date:</span> {movie?.release_date}
            </p>
            {movie?.tagline && (
                <p className={styles.tagline}>{movie?.tagline}</p>
            )}
            <p className={styles.overview}>{movie?.overview}</p>
            <p className={styles.genres}>
                <span className={styles.label}>Genres:</span> {movie?.genres.map(g => g.name).join(', ')}
            </p>
            <p className={styles.runtime}>
                <span className={styles.label}>Runtime:</span> {movie?.runtime} minutes
            </p>
            <p className={styles.rating}>
                <span className={styles.label}>Rating:</span> {movie?.vote_average}
            </p>
            <p className={styles.budget}>
                <span className={styles.label}>Budget:</span> ${movie?.budget.toLocaleString()}
            </p>
            <p className={styles.boxOffice}>
                <span className={styles.label}>Box Office:</span> ${movie?.revenue.toLocaleString()}
            </p>
            <p className={styles.productionCompanies}>
                <span className={styles.label}>Production Companies:</span> {movie?.production_companies.map(c => c.name).join(', ')}
            </p>
            {watchProviders['US']?.flatrate?.length
                ? <div>
                    <h3>Available for streaming on:</h3>
                    {watchProviders['US'].flatrate.map(({ provider_name }) => (
                        <p key={provider_name} className={styles.provider}>{provider_name}</p>
                    ))}
                </div>
                : <p>No streaming providers available in the US.</p>}
            {reviews && <div><p>{reviews}</p></div>}
        </div>
    )
}

export default ViewMovieDetails;