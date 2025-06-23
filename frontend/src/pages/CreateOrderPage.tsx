import { useMutation } from '@tanstack/react-query';
import { useCartStore } from '../store';
import { createPurchase } from '../api/purchase';
import { PurchaseResponseDTO } from '../types';
import { useNavigate } from 'react-router-dom';
import { formatPrice } from '../utils';
import { Link } from 'react-router-dom';
import { useState } from 'react';

export default function CreateOrderPage() {
    const { cart } = useCartStore();
    const navigate = useNavigate();
    const [deliveryAddress, setDeliveryAddress] = useState('');
    const [paymentMethod, setPaymentMethod] = useState('card');

    const mutation = useMutation<PurchaseResponseDTO, Error, { userId: string }>({
        mutationFn: () => createPurchase(),
        onSuccess: () => {
            alert('Покупка успешно создана!');
            navigate('/orders');
        },
        onError: (error) => {
            console.error('Ошибка создания покупки:', error);
            alert('Не удалось создать покупку');
        },
    });

    const handleCreatePurchase = () => {
        if (!cart?.items.length) {
            alert('Корзина пуста');
            return;
        }
        if (!deliveryAddress.trim()) {
            alert('Пожалуйста, введите адрес доставки');
            return;
        }
        const userId = '550e8400-e29b-41d4-a716-446655440000';
        mutation.mutate({ userId });
    };

    return (
        <div className="container mx-auto p-6 max-w-3xl">
            <h1 className="text-4xl font-bold text-gray-900 mb-8">Оформление заказа</h1>
            {cart?.items.length === 0 || !cart ? (
                <p className="text-gray-600 text-2xl">Корзина пуста</p>
            ) : (
                <div className="bg-white rounded-2xl shadow-xl p-6 space-y-8">
                    <div className="space-y-4">
                        <h2 className="text-2xl font-semibold text-gray-800">Товары в заказе</h2>
                        <div className="grid gap-4">
                            {cart.items.map((item) => (
                                <div
                                    key={item.id}
                                    className="flex items-center p-4 bg-gray-50 rounded-xl shadow-md hover:shadow-lg transition-shadow duration-300"
                                >
                                    <img
                                        src={item.imageUrl}
                                        alt={item.productName}
                                        className="w-24 h-24 object-contain rounded-lg mr-4"
                                    />
                                    <div className="flex-1">
                                        <Link
                                            to={`/product/${item.productId}`}
                                            className="text-xl text-blue-600 hover:underline font-medium"
                                        >
                                            {item.productName}
                                        </Link>
                                        <div className="text-lg text-gray-600 mt-1">
                                            <span>Кол-во: {item.quantity}</span>
                                            <span className="ml-4">Стоимость: {formatPrice(item.priceAtTime)}</span>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                    <div className="space-y-4">
                        <h2 className="text-2xl font-semibold text-gray-800">Доставка</h2>
                        <input
                            type="text"
                            value={deliveryAddress}
                            onChange={(e) => setDeliveryAddress(e.target.value)}
                            placeholder="Введите адрес доставки"
                            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-lg"
                        />
                    </div>
                    <div className="space-y-4">
                        <h2 className="text-2xl font-semibold text-gray-800">Оплата</h2>
                        <select
                            value={paymentMethod}
                            onChange={(e) => setPaymentMethod(e.target.value)}
                            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-lg"
                        >
                            <option value="card">Кредитная карта</option>
                            <option value="cash">Наличными</option>
                        </select>
                    </div>
                    <div className="pt-6 border-t border-gray-200">
                        <p className="text-2xl font-bold text-gray-900">
                            Итого: <span className="text-blue-700">{formatPrice(cart.totalAmount)}</span>
                        </p>
                    </div>
                    <button
                        onClick={handleCreatePurchase}
                        className="w-full bg-gradient-to-r from-blue-600 to-blue-700 text-white py-4 rounded-xl shadow-lg hover:from-blue-700 hover:to-blue-800 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50 transition-all duration-300 text-xl"
                        disabled={mutation.isPending}
                    >
                        {mutation.isPending ? 'Обработка...' : 'Оформить заказ'}
                    </button>
                </div>
            )}
        </div>
    );
}