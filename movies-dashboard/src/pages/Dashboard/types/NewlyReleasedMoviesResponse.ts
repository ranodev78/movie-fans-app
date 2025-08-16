import NewlyReleasedMovie from "./NewlyReleasedMovie";

export default interface NewlyReleasedMoviesResponse {
    page?: number;
    results: NewlyReleasedMovie[];
    total_pages?: number;
    total_results?: number;
}
