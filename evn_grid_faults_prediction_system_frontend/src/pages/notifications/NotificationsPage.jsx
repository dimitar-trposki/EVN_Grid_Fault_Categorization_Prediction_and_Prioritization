import { useState, useEffect } from 'react';
import notificationRepository from '../../api/notificationRepository';

const NotificationsPage = () => {
    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchNotifications();
    }, []);

    const fetchNotifications = async () => {
        try {
            const res = await notificationRepository.getMyNotifications();
            setNotifications(res.data);
        } catch (err) {
            console.error('Failed to load notifications', err);
        } finally {
            setLoading(false);
        }
    };

    const handleMarkAsRead = async (id) => {
        try {
            await notificationRepository.markAsRead(id);
            setNotifications(notifications.map(n =>
                n.id === id ? { ...n, isRead: true } : n
            ));
        } catch (err) {
            console.error('Failed to mark as read', err);
        }
    };

    const handleMarkAllAsRead = async () => {
        try {
            await notificationRepository.markAllAsRead();
            setNotifications(notifications.map(n => ({ ...n, isRead: true })));
        } catch (err) {
            console.error('Failed to mark all as read', err);
        }
    };

    const handleDelete = async (id) => {
        try {
            await notificationRepository.deleteNotification(id);
            setNotifications(notifications.filter(n => n.id !== id));
        } catch (err) {
            console.error('Failed to delete notification', err);
        }
    };

    const getTypeColor = (type) => {
        switch (type) {
            case 'ALERT': return '#ef4444';
            case 'WARNING': return '#f97316';
            case 'INFO': return '#3b82f6';
            default: return '#94a3b8';
        }
    };

    if (loading) return (
        <div style={styles.loadingContainer}>
            <p style={{ color: '#94a3b8' }}>Loading notifications...</p>
        </div>
    );

    return (
        <div style={styles.container}>
            <div style={styles.header}>
                <h1 style={styles.title}>Notifications</h1>
                <button style={styles.markAllBtn} onClick={handleMarkAllAsRead}>
                    Mark all as read
                </button>
            </div>

            {notifications.length === 0 ? (
                <div style={styles.empty}>
                    <p style={{ color: '#94a3b8' }}>No notifications</p>
                </div>
            ) : (
                <div style={styles.list}>
                    {notifications.map(n => (
                        <div key={n.id} style={{
                            ...styles.card,
                            opacity: n.isRead ? 0.6 : 1,
                            borderLeft: `4px solid ${getTypeColor(n.type)}`,
                        }}>
                            <div style={styles.cardHeader}>
                <span style={{
                    ...styles.typeBadge,
                    backgroundColor: getTypeColor(n.type),
                }}>
                  {n.type}
                </span>
                                <span style={styles.date}>
                  {new Date(n.createdAt).toLocaleString()}
                </span>
                            </div>
                            <h3 style={styles.notifTitle}>{n.title}</h3>
                            <p style={styles.message}>{n.message}</p>
                            <div style={styles.actions}>
                                {!n.isRead && (
                                    <button
                                        style={styles.readBtn}
                                        onClick={() => handleMarkAsRead(n.id)}
                                    >
                                        Mark as read
                                    </button>
                                )}
                                <button
                                    style={styles.deleteBtn}
                                    onClick={() => handleDelete(n.id)}
                                >
                                    Delete
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

const styles = {
    container: {
        padding: '2rem',
        backgroundColor: '#0f172a',
        minHeight: '100vh',
    },
    loadingContainer: {
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#0f172a',
    },
    header: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '1.5rem',
    },
    title: {
        color: '#f1f5f9',
        margin: 0,
    },
    markAllBtn: {
        padding: '0.5rem 1rem',
        borderRadius: '8px',
        border: 'none',
        backgroundColor: '#3b82f6',
        color: 'white',
        cursor: 'pointer',
    },
    empty: {
        textAlign: 'center',
        padding: '3rem',
    },
    list: {
        display: 'flex',
        flexDirection: 'column',
        gap: '1rem',
    },
    card: {
        backgroundColor: '#1e293b',
        borderRadius: '12px',
        padding: '1.5rem',
        border: '1px solid #334155',
    },
    cardHeader: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '0.75rem',
    },
    typeBadge: {
        padding: '0.25rem 0.75rem',
        borderRadius: '999px',
        color: 'white',
        fontSize: '0.75rem',
        fontWeight: 'bold',
    },
    date: {
        color: '#94a3b8',
        fontSize: '0.85rem',
    },
    notifTitle: {
        color: '#f1f5f9',
        margin: '0 0 0.5rem 0',
    },
    message: {
        color: '#94a3b8',
        margin: '0 0 1rem 0',
    },
    actions: {
        display: 'flex',
        gap: '0.5rem',
    },
    readBtn: {
        padding: '0.4rem 0.75rem',
        borderRadius: '6px',
        border: 'none',
        backgroundColor: '#3b82f6',
        color: 'white',
        cursor: 'pointer',
        fontSize: '0.85rem',
    },
    deleteBtn: {
        padding: '0.4rem 0.75rem',
        borderRadius: '6px',
        border: 'none',
        backgroundColor: '#ef4444',
        color: 'white',
        cursor: 'pointer',
        fontSize: '0.85rem',
    },
};

export default NotificationsPage;