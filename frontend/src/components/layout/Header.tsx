import { Link } from 'react-router-dom';
import {useCartStore} from "../../store";
import Modal from 'react-modal';
import {useEffect, useState} from "react";
import keycloak from '../../keycloak';


// Настройка react-modal
Modal.setAppElement('#root');

export default function Header() {
    const { cart } = useCartStore();
    const cartItemCount = cart?.items.reduce((sum, item) => sum + item.quantity, 0) || 0;
    const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);
    const [isRegisterModalOpen, setIsRegisterModalOpen] = useState(false);
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [userName, setUserName] = useState('');

    // Инициализация Keycloak
    useEffect(() => {
        keycloak
            .init({ onLoad: 'check-sso', pkceMethod: 'S256' })
            .then((authenticated) => {
                setIsAuthenticated(authenticated);
                if (authenticated) {
                    setUserName(keycloak.tokenParsed?.preferred_username || 'User');
                }
            })
            .catch((error) => {
                console.error('Keycloak initialization failed:', error);
            });
    }, []);

    const handleLogin = () => {
        keycloak.login();
    };

    const handleRegister = () => {
        keycloak.register();
    };

    const handleLogout = () => {
        keycloak.logout();
    };

    return (
        <header className="bg-white shadow-md">
            <div className="container mx-auto p-4 flex justify-between items-center">
                <Link to="/" className="text-2xl font-bold text-gray-900">
                    Yandex Shop
                </Link>
                <nav className="flex gap-6 items-center">
                    <Link to="/cart" className="flex items-center gap-2 text-gray-700 hover:text-blue-600">
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
                    <Link to="/orders" className="flex items-center gap-2 text-gray-700 hover:text-blue-600">
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
                    {isAuthenticated ? (
                        <div className="flex items-center gap-4">
                            <span className="text-gray-700">Привет, {userName}!</span>
                            <button
                                onClick={handleLogout}
                                className="text-gray-700 hover:text-blue-600"
                            >
                                Выйти
                            </button>
                        </div>
                    ) : (
                        <div className="flex gap-4">
                            <button
                                onClick={() => setIsLoginModalOpen(true)}
                                className="text-gray-700 hover:text-blue-600"
                            >
                                Войти
                            </button>
                            <button
                                onClick={() => setIsRegisterModalOpen(true)}
                                className="text-gray-700 hover:text-blue-600"
                            >
                                Регистрация
                            </button>
                        </div>
                    )}
                </nav>
            </div>

            {/* Модальное окно для входа */}
            <Modal
                isOpen={isLoginModalOpen}
                onRequestClose={() => setIsLoginModalOpen(false)}
                className="fixed inset-0 flex items-center justify-center p-4"
                overlayClassName="fixed inset-0 bg-black bg-opacity-50"
            >
                <div className="bg-white rounded-lg p-6 w-full max-w-md">
                    <h2 className="text-2xl font-bold mb-4">Вход</h2>
                    <button
                        onClick={handleLogin}
                        className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700"
                    >
                        Войти через Keycloak
                    </button>
                    <button
                        onClick={() => setIsLoginModalOpen(false)}
                        className="mt-4 w-full bg-gray-300 text-gray-700 py-2 rounded hover:bg-gray-400"
                    >
                        Отмена
                    </button>
                </div>
            </Modal>

            {/* Модальное окно для регистрации */}
            <Modal
                isOpen={isRegisterModalOpen}
                onRequestClose={() => setIsRegisterModalOpen(false)}
                className="fixed inset-0 flex items-center justify-center p-4"
                overlayClassName="fixed inset-0 bg-black bg-opacity-50"
            >
                <div className="bg-white rounded-lg p-6 w-full max-w-md">
                    <h2 className="text-2xl font-bold mb-4">Регистрация</h2>
                    <button
                        onClick={handleRegister}
                        className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700"
                    >
                        Зарегистрироваться через Keycloak
                    </button>
                    <button
                        onClick={() => setIsRegisterModalOpen(false)}
                        className="mt-4 w-full bg-gray-300 text-gray-700 py-2 rounded hover:bg-gray-400"
                    >
                        Отмена
                    </button>
                </div>
            </Modal>
        </header>
    );
}