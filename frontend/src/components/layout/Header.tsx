import { Link } from 'react-router-dom';
import { useCartStore } from '../../store';
import Modal from 'react-modal';
import { useEffect, useState } from 'react';
import keycloak from '../../keycloak';
import { toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { getWallet, creditBalance } from '../../api/wallet';

// Настройка react-modal
Modal.setAppElement('#root');

interface UserProfile {
    username?: string;
    email?: string;
    firstName?: string;
    lastName?: string;
}

interface WalletData {
    balance: number;
    userId: string;
}

interface ModalState {
    isOpen: boolean;
    mode: 'profile' | 'credit';
}

export default function Header() {
    const { cart } = useCartStore();
    const cartItemCount = cart?.items.reduce((sum, item) => sum + item.quantity, 0) || 0;
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [userProfile, setUserProfile] = useState<UserProfile>({});
    const [wallet, setWallet] = useState<WalletData | null>(null);
    const [modal, setModal] = useState<ModalState>({ isOpen: false, mode: 'profile' });
    const [creditAmount, setCreditAmount] = useState('');

    // Инициализация Keycloak и загрузка данных
    useEffect(() => {
        keycloak
            .init({ onLoad: 'check-sso', pkceMethod: 'S256' })
            .then((authenticated) => {
                setIsAuthenticated(authenticated);
                if (authenticated) {
                    setUserProfile({
                        username: keycloak.tokenParsed?.preferred_username,
                        email: keycloak.tokenParsed?.email,
                        firstName: keycloak.tokenParsed?.given_name,
                        lastName: keycloak.tokenParsed?.family_name,
                    });
                    fetchWallet();
                }
            })
            .catch((error) => {
                console.error('Keycloak initialization failed:', error);
                toast.error('Ошибка инициализации авторизации');
            });
    }, []);

    const fetchWallet = async () => {
        try {
            const walletData = await getWallet();
            setWallet(walletData);
        } catch (error) {
            console.error('Failed to fetch wallet:', error);
            toast.error('Не удалось загрузить баланс');
        }
    };

    const handleLogout = () => {
        keycloak
            .logout({ redirectUri: window.location.origin })
            .then(() => {
                setIsAuthenticated(false);
                setUserProfile({});
                setWallet(null);
                toast.success('Вы вышли из аккаунта');
            })
            .catch((error) => {
                console.error('Logout failed:', error);
                toast.error('Ошибка выхода');
            });
    };

    const handleCreditBalance = async () => {
        const amount = parseFloat(creditAmount);
        if (isNaN(amount) || amount <= 0) {
            toast.error('Введите корректную сумму для пополнения');
            return;
        }
        try {
            await creditBalance(amount);
            await fetchWallet(); // Обновляем баланс после пополнения
            setModal({ isOpen: false, mode: 'profile' });
            setCreditAmount('');
            toast.success('Баланс успешно пополнен');
        } catch (error) {
            console.error('Failed to credit balance:', error);
            toast.error('Не удалось пополнить баланс');
        }
    };

    const openModal = (mode: 'profile' | 'credit') => setModal({ isOpen: true, mode });
    const closeModal = () => {
        setModal({ isOpen: false, mode: 'profile' });
        setCreditAmount('');
    };

    return (
        <header className="bg-white shadow-md">
            <div className="container mx-auto p-4 flex justify-between items-center">
                <Link to="/" className="text-2xl font-bold text-gray-900">
                    Yandex Shop
                </Link>
                <nav className="flex gap-6 items-center">
                    <Link
                        to="/cart"
                        className="flex items-center gap-2 text-gray-700 hover:text-blue-600"
                    >
                        <svg
                            className="w-5 h-5"
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                            xmlns="http://www.w3.org/2000/svg"
                        >
                            <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth="2"
                                d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"
                            />
                        </svg>
                        Корзина
                        {cartItemCount > 0 && (
                            <span className="ml-1 bg-blue-600 text-white text-xs rounded-full px-2 py-1">
                                {cartItemCount}
                            </span>
                        )}
                    </Link>
                    {!isAuthenticated && (
                        <button
                            onClick={() => openModal('profile')}
                            className="text-gray-700 hover:text-blue-600"
                        >
                            Войти
                        </button>
                    )}
                    {isAuthenticated && (
                        <>
                            <Link
                                to="/orders"
                                className="flex items-center gap-2 text-gray-700 hover:text-blue-600"
                            >
                                <svg
                                    className="w-5 h-5"
                                    fill="none"
                                    stroke="currentColor"
                                    viewBox="0 0 24 24"
                                    xmlns="http://www.w3.org/2000/svg"
                                >
                                    <path
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                        strokeWidth="2"
                                        d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
                                    />
                                </svg>
                                Заказы
                            </Link>
                            <button
                                onClick={() => openModal('profile')}
                                className="flex items-center gap-2 text-gray-700 hover:text-blue-600"
                            >
                                <svg
                                    className="w-5 h-5"
                                    fill="none"
                                    stroke="currentColor"
                                    viewBox="0 0 24 24"
                                    xmlns="http://www.w3.org/2000/svg"
                                >
                                    <path
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                        strokeWidth="2"
                                        d="M5.121 17.804A13.937 13.937 0 0112 16c2.5 0 4.847.655 6.879 1.804M15 10a3 3 0 11-6 0 3 3 0 016 0zm6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                                    />
                                </svg>
                                Профиль
                            </button>
                        </>
                    )}
                </nav>
            </div>

            {/* Модальное окно для профиля/пополнения баланса */}
            <Modal
                isOpen={modal.isOpen}
                onRequestClose={closeModal}
                className="fixed inset-0 flex items-center justify-center p-4"
                overlayClassName="fixed inset-0 bg-black bg-opacity-50"
            >
                <div className="bg-white rounded-lg p-6 w-full max-w-md">
                    <div className="flex justify-between mb-4">
                        <h2 className="text-2xl font-bold">Профиль</h2>
                        <button
                            onClick={closeModal}
                            className="text-gray-500 hover:text-gray-700"
                        >
                            ×
                        </button>
                    </div>
                    {modal.mode === 'profile' && (
                        <>
                            <div className="mb-4">
                                <p><strong>Имя:</strong> {userProfile.firstName || 'Не указано'}</p>
                                <p><strong>Фамилия:</strong> {userProfile.lastName || 'Не указано'}</p>
                                <p><strong>Email:</strong> {userProfile.email || 'Не указано'}</p>
                                <p><strong>Баланс:</strong> {wallet?.balance || 0} ₽</p>
                            </div>
                            <button
                                onClick={() => openModal('credit')}
                                className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700 mb-4"
                            >
                                Пополнить баланс
                            </button>
                            <button
                                onClick={handleLogout}
                                className="w-full bg-gray-300 text-gray-700 py-2 rounded hover:bg-gray-400"
                            >
                                Выйти
                            </button>
                        </>
                    )}
                    {modal.mode === 'credit' && (
                        <>
                            <div className="mb-4">
                                <label className="block text-gray-700">Сумма для пополнения (₽)</label>
                                <input
                                    type="number"
                                    value={creditAmount}
                                    onChange={(e) => setCreditAmount(e.target.value)}
                                    className="w-full p-2 border rounded mt-1"
                                    min="1"
                                    step="0.01"
                                />
                            </div>
                            <button
                                onClick={handleCreditBalance}
                                className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700 mb-4"
                            >
                                Пополнить
                            </button>
                            <button
                                onClick={closeModal}
                                className="w-full bg-gray-300 text-gray-700 py-2 rounded hover:bg-gray-400"
                            >
                                Отмена
                            </button>
                        </>
                    )}
                </div>
            </Modal>
        </header>
    );
}