import Genre from "./Genre";
import ProductionCompany from "./ProductionCompany";

export default interface MovieDetails {
    id: number;
    title: string;
    release_date: string;
    overview: string;
    genres: Genre[];
    runtime: number;
    vote_average: number;
    budget: number;
    revenue: number;
    tagline: string;
    poster_path: string | null;
    production_companies: ProductionCompany[];
};