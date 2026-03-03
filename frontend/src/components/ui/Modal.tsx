import React, { useEffect, useRef } from 'react';


interface ModalProps {
    isOpen: boolean;
    onClose: () => void;
    title: string;
    children: React.ReactNode;
    actions?: React.ReactNode;
}

export const Modal: React.FC<ModalProps> = ({ isOpen, onClose, title, children, actions }) => {
    const modalRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const handleEscape = (e: KeyboardEvent) => {
            if (e.key === 'Escape') onClose();
        };

        if (isOpen) {
            document.addEventListener('keydown', handleEscape);
            document.body.style.overflow = 'hidden'; // prevent background scrolling
        }

        return () => {
            document.removeEventListener('keydown', handleEscape);
            document.body.style.overflow = 'unset';
        };
    }, [isOpen, onClose]);

    if (!isOpen) return null;

    return (
        <div className="modal-overlay" onClick={onClose} role="dialog" aria-modal="true">
            <div
                className="modal-content"
                ref={modalRef}
                onClick={e => e.stopPropagation()}
            >
                <div className="modal-header">
                    <h2>{title}</h2>
                    <button className="modal-close" onClick={onClose} aria-label="Fermer">
                        &times;
                    </button>
                </div>
                <div className="modal-body">
                    {children}
                </div>
                {actions && (
                    <div className="modal-footer">
                        {actions}
                    </div>
                )}
            </div>
        </div>
    );
};
