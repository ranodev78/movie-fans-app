interface ImportMetaEnv {
    readonly VITE_AUTH_SERVICE_BASE_URL: string;
    readonly VITE_MOVIE_SERVICE_BASE_URL: string;
    readonly VITE_AUTH_SERVICE_USERS_PATH: string;
    readonly VITE_MOVIE_SERVICE_DAILY_NEW_PATH: string;
    readonly VITE_MOVIE_SERVICE_QUERY_MOVIE_PATH: string;
}

interface ImportMeta {
    readonly env: ImportMetaEnv
}