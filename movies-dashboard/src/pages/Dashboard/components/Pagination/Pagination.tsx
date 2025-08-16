import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

import styles from './Pagination.module.css';

interface PaginationProps {
    currentPage: number;
    totalPages: number;
    onPageChange: (page: number) => void;
}

const Pagination: React.FC<PaginationProps> = ({ currentPage, totalPages, onPageChange }) => {
    const handlePrevious = () => {
        if (currentPage > 1) {
            onPageChange(currentPage - 1);
        }
    }

    const handleNext = () => {
        if (currentPage < totalPages) {
            onPageChange(currentPage + 1);
        }
    }

    return (
        <div className={styles.pagination}>
            <button onClick={handlePrevious} disabled={currentPage === 1} className={styles.button}>
                <ChevronLeft size={20} />
            </button>
            <span className={styles.pageText}>{`Page ${currentPage} of ${totalPages}`}</span>
            <button onClick={handleNext} disabled={currentPage === totalPages} className={styles.button}>
                <ChevronRight size={20} />
            </button>
        </div>
    )
}

export default Pagination;