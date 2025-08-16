import WatchProvider from "./WatchProvider";

export default interface WatchProviderRegion {
    link?: string;
    flatrate?: WatchProvider[];
    rent?: WatchProvider[];
    buy?: WatchProvider[];
}