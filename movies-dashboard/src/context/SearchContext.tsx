import React, { createContext, useContext, useState, ReactNode } from 'react';

import MovieSearchResult from '../pages/SearchResult/types/MovieSearchResult';

interface SearchContextType {
    searchTerm: string;
    setSearchTerm: (term: string) => void;
    searchResults: MovieSearchResult[];
    setSearchResults: React.Dispatch<React.SetStateAction<MovieSearchResult[]>>;
}

const SearchContext = createContext<SearchContextType | undefined>(undefined);
 
export const SearchProvider = ({ children }: { children: ReactNode }) => {
    const [searchTerm, setSearchTerm] = useState('');
    const [searchResults, setSearchResults] = useState<MovieSearchResult[]>([]);

    return (
        <SearchContext.Provider value={{ searchTerm, setSearchTerm, searchResults, setSearchResults }}>
            {children}
        </SearchContext.Provider>
    );
};

export const useSearch = (): SearchContextType => {
    const context = useContext(SearchContext);
    if (!context) {
        throw new Error('useSearch must be used within a SearchProvider');
    }

    return context
}