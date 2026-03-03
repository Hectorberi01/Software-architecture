import React from 'react';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: 'primary' | 'secondary' | 'danger' | 'ghost' | 'success';
    fullWidth?: boolean;
}

export const Button: React.FC<ButtonProps> = ({
    children,
    variant = 'primary',
    fullWidth = false,
    className = '',
    ...props
}) => {
    const baseClass = 'btn';
    const variantClass = `btn-${variant}`;
    const widthClass = fullWidth ? 'btn-full-width' : '';

    return (
        <button className={`${baseClass} ${variantClass} ${widthClass} ${className}`.trim()} {...props}>
            {children}
        </button>
    );
};
