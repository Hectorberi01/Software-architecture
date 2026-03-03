import React, { useState } from 'react';
import { checkInApi } from '../api/checkInApi';
import { Card, Button, Badge } from '../components/ui';

export const CheckInPage: React.FC = () => {
    // Dans la réalité, le QR code contient l'URL: /checkin/qr?spotCode=A01
    const searchParams = new URLSearchParams(window.location.search);
    const spotCode = searchParams.get('spotCode') || '';

    const [email, setEmail] = useState('');
    const [result, setResult] = useState<{ success: boolean, message: string } | null>(null);
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setResult(null);

        try {
            const res = await checkInApi.checkInQrCode(spotCode, email);
            setResult({ success: true, message: res.message });
        } catch (err: any) {
            setResult({ success: false, message: err.message || 'Check-in refusé.' });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ padding: '20px', maxWidth: '400px', margin: '10vh auto' }}>
            <Card>
                <h2 style={{ textAlign: 'center', marginBottom: 20 }}>Validation Place <Badge variant="info">{spotCode}</Badge></h2>

                {result ? (
                    <div className={`alert alert-${result.success ? 'success' : 'danger'}`}>
                        {result.message}
                    </div>
                ) : (
                    <form onSubmit={handleSubmit}>
                        <div className="form-group mb-4">
                            <label>Saisissez votre email pour valider le Check-in :</label>
                            <input
                                type="email"
                                className="form-control mt-2"
                                value={email}
                                onChange={e => setEmail(e.target.value)}
                                required
                            />
                        </div>
                        <Button type="submit" fullWidth disabled={loading}>
                            {loading ? 'Validation en cours...' : 'Je suis garé'}
                        </Button>
                    </form>
                )}
            </Card>
        </div>
    );
};
