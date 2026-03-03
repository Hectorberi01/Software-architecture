import React, { useState, useEffect } from 'react';
import { adminApi } from '../api/adminApi';
import { UserAdminDto, CreateUserRequest, UpdateUserRequest } from '../types/admin';
import { Role } from '../types/auth';
import { Card, DataTable, Button, Badge, Modal, Column } from '../components/ui';
import { useToast } from '../contexts/ToastContext';

export const AdminPage: React.FC = () => {
    const { showToast } = useToast();
    const [users, setUsers] = useState<UserAdminDto[]>([]);
    const [loading, setLoading] = useState(true);

    // Modal state
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingUser, setEditingUser] = useState<UserAdminDto | null>(null);
    const [formData, setFormData] = useState<Partial<CreateUserRequest>>({
        firstName: '', lastName: '', email: '', password: '', role: 'EMPLOYEE'
    });

    useEffect(() => {
        loadUsers();
    }, []);

    const loadUsers = async () => {
        setLoading(true);
        try {
            setUsers(await adminApi.getAllUsers());
        } catch (err: any) {
            showToast(err.message, 'error');
        } finally {
            setLoading(false);
        }
    };

    const handleOpenCreate = () => {
        setEditingUser(null);
        setFormData({ firstName: '', lastName: '', email: '', password: '', role: 'EMPLOYEE' });
        setIsModalOpen(true);
    };

    const handleOpenEdit = (user: UserAdminDto) => {
        setEditingUser(user);
        setFormData({ firstName: user.firstName, lastName: user.lastName, email: user.email, role: user.role });
        setIsModalOpen(true);
    };

    const handleDelete = async (id: number) => {
        if (!confirm('Supprimer cet utilisateur ?')) return;
        try {
            await adminApi.deleteUser(id);
            showToast('Utilisateur supprimé', 'success');
            loadUsers();
        } catch (err: any) {
            showToast(err.message, 'error');
        }
    };

    const handleSave = async () => {
        try {
            if (editingUser) {
                // Update
                const updateReq: UpdateUserRequest = {
                    firstName: formData.firstName,
                    lastName: formData.lastName,
                    role: formData.role as Role
                };
                await adminApi.updateUser(editingUser.id, updateReq);
                showToast('Utilisateur mis à jour', 'success');
            } else {
                // Create
                if (!formData.password) { showToast('Mot de passe requis', 'error'); return; }
                await adminApi.createUser(formData as CreateUserRequest);
                showToast('Utilisateur créé', 'success');
            }
            setIsModalOpen(false);
            loadUsers();
        } catch (err: any) {
            showToast(err.message, 'error');
        }
    };

    const columns: Column<UserAdminDto>[] = [
        { header: 'ID', accessor: 'id' },
        { header: 'Email', accessor: 'email' },
        { header: 'Nom', accessor: (u: UserAdminDto) => `${u.firstName} ${u.lastName}` },
        { header: 'Rôle', accessor: (u: UserAdminDto) => <Badge variant={u.role === 'ADMIN' ? 'danger' : u.role === 'MANAGER' ? 'warning' : 'info'}>{u.role}</Badge> },
        { header: 'Actif', accessor: (u: UserAdminDto) => <Badge variant={u.enabled ? 'success' : 'default'}>{u.enabled ? 'OUI' : 'NON'}</Badge> },
        {
            header: 'Actions',
            accessor: (user: UserAdminDto) => (
                <div className="action-buttons">
                    <Button onClick={() => handleOpenEdit(user)} style={{ marginRight: 8, padding: '4px 8px', fontSize: 12 }}>Editer</Button>
                    <Button variant="danger" onClick={() => handleDelete(user.id)} style={{ padding: '4px 8px', fontSize: 12 }}>Supprimer</Button>
                </div>
            )
        }
    ];

    return (
        <div className="page-container">
            <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                    <h1>Profil Secrétaire (Admin)</h1>
                    <p>Gérez les comptes des collaborateurs</p>
                </div>
                <Button onClick={handleOpenCreate}>+ Nouvel Utilisateur</Button>
            </div>

            <Card>
                {loading ? 'Chargement...' : <DataTable data={users} columns={columns} keyExtractor={u => u.id} />}
            </Card>

            <Modal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title={editingUser ? 'Editer Utilisateur' : 'Créer Utilisateur'}
                actions={
                    <>
                        <Button variant="ghost" onClick={() => setIsModalOpen(false)}>Annuler</Button>
                        <Button onClick={handleSave}>Enregistrer</Button>
                    </>
                }
            >
                <div className="form-group mb-2">
                    <label>Prénom</label>
                    <input className="form-control" value={formData.firstName} onChange={e => setFormData({ ...formData, firstName: e.target.value })} />
                </div>
                <div className="form-group mb-2">
                    <label>Nom</label>
                    <input className="form-control" value={formData.lastName} onChange={e => setFormData({ ...formData, lastName: e.target.value })} />
                </div>
                <div className="form-group mb-2">
                    <label>Email</label>
                    <input className="form-control" type="email" disabled={!!editingUser} value={formData.email} onChange={e => setFormData({ ...formData, email: e.target.value })} />
                </div>
                {!editingUser && (
                    <div className="form-group mb-2">
                        <label>Mot de passe</label>
                        <input className="form-control" type="password" value={formData.password} onChange={e => setFormData({ ...formData, password: e.target.value })} />
                    </div>
                )}
                <div className="form-group mb-2">
                    <label>Rôle (Droits d'accès)</label>
                    <select className="form-control" value={formData.role} onChange={e => setFormData({ ...formData, role: e.target.value as Role })}>
                        <option value="EMPLOYEE">Employé (Max 5 jours ouvrés)</option>
                        <option value="MANAGER">Manager (Max 30 jours cal. + Stats)</option>
                        <option value="ADMIN">Secrétaire (Gestion complète)</option>
                    </select>
                </div>
            </Modal>
        </div>
    );
};
