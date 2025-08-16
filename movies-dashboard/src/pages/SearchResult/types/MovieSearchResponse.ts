import MovieSearchResult from "./MovieSearchResult";

export default interface MovieSearchResponse {
    page: number;
    results: MovieSearchResult[];
    total_results: number;
    total_pages: number;
};