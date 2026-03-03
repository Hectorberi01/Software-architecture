import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { authApi } from '../api/authApi';
import { Button, Card } from '../components/ui';
import { useToast } from '../contexts/ToastContext';

export const RegisterPage: React.FC = () => {
    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        confirmPassword: ''
    });
    const [loading, setLoading] = useState(false);
    const { login } = useAuth();
    const { showToast } = useToast();
    const navigate = useNavigate();

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (formData.password !== formData.confirmPassword) {
            showToast('Les mots de passe ne correspondent pas', 'error');
            return;
        }
        setLoading(true);
        try {
            const { confirmPassword, ...registerData } = formData;
            const data = await authApi.register(registerData);
            login(data);
            showToast('Inscription réussie ! Bienvenue.', 'success');
            navigate('/parking');
        } catch (err: any) {
            showToast(err.message || 'Erreur lors de l\'inscription', 'error');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <Card className="auth-card">
                <div className="auth-header">
                    <h2>Créer un compte</h2>
                    <p>Rejoignez ParkReserve</p>
                </div>
                <form onSubmit={handleSubmit} className="auth-form">
                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="firstName">Prénom</label>
                            <input id="firstName" name="firstName" value={formData.firstName} onChange={handleChange} required />
                        </div>
                        <div className="form-group">
                            <label htmlFor="lastName">Nom</label>
                            <input id="lastName" name="lastName" value={formData.lastName} onChange={handleChange} required />
                        </div>
                    </div>
                    <div className="form-group">
                        <label htmlFor="email">Email</label>
                        <input id="email" type="email" name="email" value={formData.email} onChange={handleChange} required />
                    </div>
                    <div className="form-group">
                        <label htmlFor="password">Mot de passe</label>
                        <input id="password" type="password" name="password" minLength={6} value={formData.password} onChange={handleChange} required />
                    </div>
                    <div className="form-group">
                        <label htmlFor="confirmPassword">Confirmer mot de passe</label>
                        <input id="confirmPassword" type="password" name="confirmPassword" minLength={6} value={formData.confirmPassword} onChange={handleChange} required />
                    </div>
                    <Button type="submit" fullWidth disabled={loading}>
                        {loading ? 'Création en cours...' : 'S\'inscrire'}
                    </Button>
                </form>
                <div className="auth-footer">
                    <p>Déjà un compte ? <Link to="/login">Se connecter</Link></p>
                </div>
            </Card>
        </div>
    );
};
