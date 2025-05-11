import { useQuery } from '@tanstack/react-query';
import { getCart, removeItemFromCart, updateItemQuantity, clearCart } from '../api/cart';
import { useCartStore } from '../store';
import { useEffect } from 'react';
import { formatPrice } from '../utils';
import { Link } from 'react-router-dom';

const TEST_USER_ID = '550e8400-e29b-41d4-a716-446655440000';

export default function CartPage() {
    const { cart, setCart } = useCartStore();

    const { data, isLoading, error } = useQuery({
        queryKey: ['cart', TEST_USER_ID],
        queryFn: () => getCart(TEST_USER_ID),
        enabled: true,
    });

    // Синхронизация с сервером
    useEffect(() => {
        if (data) {
            setCart(data);
        }
    }, [data, setCart]);

    const handleUpdateQuantity = async (productId: number, quantity: number) => {
        if (quantity < 1) return;
        try {
            const updatedCart = await updateItemQuantity(TEST_USER_ID, { productId, quantity });
            setCart(updatedCart);
        } catch (error) {
            console.error('Ошибка обновления количества:', error);
            alert('Не удалось обновить количество');
        }
    };

    const handleRemoveItem = async (productId: number) => {
        try {
            const updatedCart = await removeItemFromCart(TEST_USER_ID, productId);
            setCart(updatedCart);
        } catch (error) {
            console.error('Ошибка удаления товара:', error);
            alert('Не удалось удалить товар');
        }
    };

    const handleClearCart = async () => {
        try {
            await clearCart(TEST_USER_ID);
            setCart({ ...cart!, items: [], totalAmount: 0 });
            alert('Корзина очищена');
        } catch (error) {
            console.error('Ошибка очистки корзины:', error);
            alert('Не удалось очистить корзину');
        }
    };

    if (isLoading) return <div className="container mx-auto p-4">Загрузка...</div>;
    if (error) return <div className="container mx-auto p-4">Ошибка: {(error as Error).message}</div>;

    return (
        <div className="container mx-auto p-4">
            <h1 className="text-2xl font-bold mb-4">Корзина</h1>
            {cart?.items.length === 0 || !cart ? (
                <p>Корзина пуста</p>
            ) : (
                <div className="space-y-4">
                    {cart.items.map((item) => (
                        <div
                            key={item.id}
                            className="flex items-center gap-4 p-4 bg-white rounded-lg shadow-md"
                        >
                            <img
                                src={item.imageUrl}
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
                            <Link
                                to="/create-order"
                                className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
                            >
                                Оформить заказ
                            </Link>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}