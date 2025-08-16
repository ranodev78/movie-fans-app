import WatchProviderRegion from "./WatchProviderRegion";

export default interface WatchProvidersResponse {
    id: number;
    results: Record<string, WatchProviderRegion>;
};