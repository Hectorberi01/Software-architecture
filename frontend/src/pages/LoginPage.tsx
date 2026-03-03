import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { authApi } from '../api/authApi';
import { Button, Card } from '../components/ui';
import { useToast } from '../contexts/ToastContext';

export const LoginPage: React.FC = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const { login } = useAuth();
    const { showToast } = useToast();
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        try {
            const data = await authApi.login({ email, password });
            login(data);
            showToast('Connexion réussie', 'success');
            navigate('/parking');
        } catch (err: any) {
            showToast(err.message || 'Erreur de connexion', 'error');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <Card className="auth-card">
                <div className="auth-header">
                    <h2>Bon retour</h2>
                    <p>Connectez-vous pour continuer vers ParkReserve</p>
                </div>
                <form onSubmit={handleSubmit} className="auth-form">
                    <div className="form-group">
                        <label htmlFor="email">Email</label>
                        <input
                            id="email"
                            type="email"
                            value={email}
                            onChange={e => setEmail(e.target.value)}
                            required
                            autoFocus
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="password">Mot de passe</label>
                        <input
                            id="password"
                            type="password"
                            value={password}
                            onChange={e => setPassword(e.target.value)}
                            required
                        />
                    </div>
                    <Button type="submit" fullWidth disabled={loading}>
                        {loading ? 'Connexion en cours...' : 'Se connecter'}
                    </Button>
                </form>
                <div className="auth-footer">
                    <p>Pas encore de compte ? <Link to="/register">S'inscrire</Link></p>
                </div>
            </Card>
        </div>
    );
};
