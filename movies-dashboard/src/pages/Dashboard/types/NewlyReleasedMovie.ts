export default interface NewlyReleasedMovie {
    adult?: boolean; // nullable, so marked optional
    id?: number;
    original_language?: string;
    releaseDate?: string; // Representing LocalDate as ISO string (e.g., "2023-10-25")
    genreIds: Array<string>
    overview?: string;
    popularity?: number;
    title?: string;
    vote_average?: number;
    vote_count?: number;
    poster_path?: string;
}
