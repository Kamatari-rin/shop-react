import { useCartStore } from '../store/cartStore';
import { useEffect } from 'react';
import { formatPrice } from '../utils';
import { Link } from 'react-router-dom';
import keycloak from '../keycloak';

export default function CartPage() {
    const { cart, removeItem, updateQuantity, clearCart, syncWithServer, fetchCart } = useCartStore();

    useEffect(() => {
        fetchCart();
        if (keycloak.authenticated) {
            syncWithServer();
        }
    }, [fetchCart, syncWithServer, keycloak.authenticated]);

    const handleUpdateQuantity = async (productId: number, quantity: number) => {
        try {
            await updateQuantity(productId, quantity);
        } catch (error) {
            console.error('Ошибка обновления количества:', error);
            alert('Не удалось обновить количество');
        }
    };

    const handleRemoveItem = async (productId: number) => {
        try {
            await removeItem(productId);
        } catch (error) {
            console.error('Ошибка удаления товара:', error);
            alert('Не удалось удалить товар');
        }
    };

    const handleClearCart = async () => {
        try {
            await clearCart();
            alert('Корзина очищена');
        } catch (error) {
            console.error('Ошибка очистки корзины:', error);
            alert('Не удалось очистить корзину');
        }
    };

    if (!cart) {
        return <div className="container mx-auto p-4">Загрузка...</div>;
    }

    return (
        <div className="container mx-auto p-4">
            <h1 className="text-2xl font-bold mb-4">Корзина</h1>
            {cart.items.length === 0 ? (
                <p>Корзина пуста</p>
            ) : (
                <div className="space-y-4">
                    {cart.items.map((item) => (
                        <div
                            key={item.productId}
                            className="flex items-center gap-4 p-4 bg-white rounded-lg shadow-md"
                        >
                            <img
                                src={item.imageUrl || 'https://via.placeholder.com/64'}
                                alt={item.productName}
                                className="w-16 h-16 object-contain"
                            />
                            <div className="flex-grow">
                                <h3 className="text-lg font-semibold text-gray-900">{item.productName}</h3>
                                <p className="text-blue-600">{formatPrice(item.priceAtTime)}</p>
                            </div>
                            <div className="flex items-center gap-2">
                                <button
                                    onClick={() => handleUpdateQuantity(item.productId, item.quantity - 1)}
                                    className="px-2 py-1 bg-gray-200 rounded hover:bg-gray-300"
                                    disabled={item.quantity <= 1}
                                >
                                    -
                                </button>
                                <span>{item.quantity}</span>
                                <button
                                    onClick={() => handleUpdateQuantity(item.productId, item.quantity + 1)}
                                    className="px-2 py-1 bg-gray-200 rounded hover:bg-gray-300"
                                >
                                    +
                                </button>
                            </div>
                            <button
                                onClick={() => handleRemoveItem(item.productId)}
                                className="text-red-600 hover:text-red-700"
                            >
                                Удалить
                            </button>
                        </div>
                    ))}
                    <div className="flex justify-between items-center mt-4">
                        <p className="text-xl font-bold">
                            Итого: {formatPrice(cart.totalAmount)}
                        </p>
                        <div className="flex gap-4">
                            <button
                                onClick={handleClearCart}
                                className="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-colors"
                            >
                                Очистить корзину
                            </button>
                            {keycloak.authenticated ? (
                                <Link
                                    to="/create-order"
                                    className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
                                >
                                    Оформить заказ
                                </Link>
                            ) : (
                                <button
                                    onClick={() => keycloak.login()}
                                    className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
                                >
                                    Войти для оформления
                                </button>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}