import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

interface SidebarProps {
    onCollapse?: (collapsed: boolean) => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ onCollapse }) => {
    const { user, logout } = useAuth();
    const [collapsed, setCollapsed] = useState(false);

    const toggleCollapse = () => {
        const newState = !collapsed;
        setCollapsed(newState);
        if (onCollapse) onCollapse(newState);
    };

    const isManagerOrAdmin = user?.role === 'MANAGER' || user?.role === 'ADMIN';
    const isAdmin = user?.role === 'ADMIN';

    return (
        <aside className={`sidebar ${collapsed ? 'collapsed' : ''}`} aria-label="Navigation principale">
            <div className="sidebar-header">
                {!collapsed && <h2>ParkReserve</h2>}
                {collapsed && <h2 className="collapsed-title">PR</h2>}
                <button onClick={toggleCollapse} className="menu-toggle-btn" aria-label="Développer/Réduire le menu">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="3" y1="12" x2="21" y2="12"></line><line x1="3" y1="6" x2="21" y2="6"></line><line x1="3" y1="18" x2="21" y2="18"></line></svg>
                </button>
            </div>

            <nav className="sidebar-nav">
                <ul>
                    <li>
                        <NavLink to="/parking" className={({ isActive }) => isActive ? 'active' : ''}>
                            <span className="nav-icon">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"></circle><path d="M9 16V8h4a2 2 0 0 1 0 4H9"></path></svg>
                            </span>
                            {!collapsed && <span>Parking</span>}
                        </NavLink>
                    </li>

                    <li>
                        <NavLink to="/reservations" className={({ isActive }) => isActive ? 'active' : ''}>
                            <span className="nav-icon">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                            </span>
                            {!collapsed && <span>Mes réservations</span>}
                        </NavLink>
                    </li>

                    {isManagerOrAdmin && (
                        <li>
                            <NavLink to="/analytics" className={({ isActive }) => isActive ? 'active' : ''}>
                                <span className="nav-icon">
                                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline></svg>
                                </span>
                                {!collapsed && <span>Analytique</span>}
                            </NavLink>
                        </li>
                    )}

                    {isAdmin && (
                        <li>
                            <NavLink to="/admin" className={({ isActive }) => isActive ? 'active' : ''}>
                                <span className="nav-icon">
                                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
                                </span>
                                {!collapsed && <span>Administration</span>}
                            </NavLink>
                        </li>
                    )}
                </ul>
            </nav>

            <div className="sidebar-footer">
                <div className="user-info">
                    <span className="nav-icon">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                    </span>
                    {!collapsed && (
                        <div className="user-details">
                            <span className="user-name">{user?.firstName} {user?.lastName}</span>
                            <span className="user-role">{user?.role}</span>
                        </div>
                    )}
                </div>
                <button className="logout-btn" onClick={logout} aria-label="Déconnexion">
                    <span className="nav-icon">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
                    </span>
                    {!collapsed && <span>Déconnexion</span>}
                </button>
            </div>
        </aside>
    );
};
