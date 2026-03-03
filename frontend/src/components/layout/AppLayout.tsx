import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from '../ui';

export const AppLayout: React.FC = () => {
    const [sidebarCollapsed, setSidebarCollapsed] = useState(false);

    return (
        <div className="app-container">
            <Sidebar onCollapse={setSidebarCollapsed} />
            <main
                className="main-content"
                style={{ marginLeft: sidebarCollapsed ? '60px' : '240px' }}
            >
                <div className="page-content">
                    <Outlet />
                </div>
            </main>
        </div>
    );
};
