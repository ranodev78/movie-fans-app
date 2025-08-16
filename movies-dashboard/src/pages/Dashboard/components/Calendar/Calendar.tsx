import React from 'react';
import {
    Search,
    Mail,
    Bell,
    MoreHorizontal,
    Play,
    Users,
    Book,
    CheckSquare,
    MessageCircle,
    Settings,
    LogOut,
    ChevronLeft,
    ChevronRight,
    Plus,
    UserPlus
} from 'lucide-react';

import styles from './Calendar.module.css';

const CALENDAR_DAYS = ['S', 'M', 'T', 'W', 'T', 'F', 'S'];

const Calendar: React.FC = () => {
    const currentDate = new Date();
    const currentMonth = currentDate.toLocaleString('default', { month: 'long', year: 'numeric' });

    return (
        <div className={styles.calendarSection}>
            <h3 className={styles.calendarTitle}>Calendar</h3>
            <div className={styles.calendarWidget}>
                <div className={styles.calendarHeader}>
                    <span className={styles.calendarMonth}>{currentMonth}</span>
                    <div className={styles.calendarNav}>
                        <button className={styles.calendarNavButton}>
                            <ChevronLeft size={16} />
                        </button>
                        <button className={styles.calendarNavButton}>
                            <ChevronRight size={16} />
                        </button>
                    </div>
                </div>
                <div className={styles.calendarGrid}>
                    {CALENDAR_DAYS.map((day, index) => (
                        <div key={index} className={`${styles.calendarDay} ${styles.calendarDayHeader}`}>
                            {day}
                        </div>
                    ))}
                    {Array.from({ length: 35 }, (_, i) => {
                        const day = i - 6;
                        const isToday = day === currentDate.getDate();
                        return (
                            <div
                                key={i + 7}
                                className={`${styles.calendarDay} ${isToday ? styles.calendarDayToday : ''}`}
                            >
                                {day > 0 && day <= 31 ? day : ''}
                            </div>
                        );
                    })}
                </div>
            </div>
        </div>
    )
};
