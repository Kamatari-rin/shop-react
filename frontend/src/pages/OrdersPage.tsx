import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { getOrders, deleteOrder as deleteOrderApi, getOrderItems } from '../api/order';
import { OrderListDTO, OrderStatus } from '../types';
import { Link } from 'react-router-dom';

export default function OrdersPage() {
    const [filters, setFilters] = useState({
        status: '',
        startDate: '',
        endDate: '',
        page: 0,
        size: 10,
    });

    const [expandedOrderId, setExpandedOrderId] = useState<number | null>(null);

    const toggleExpand = (orderId: number) => {
        setExpandedOrderId(expandedOrderId === orderId ? null : orderId);
    };

    const { data, isLoading, error, refetch } = useQuery<OrderListDTO>({
        queryKey: ['orders', filters],
        queryFn: () => getOrders(filters),
    });

    const handleFilterChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        setFilters({ ...filters, [e.target.name]: e.target.value, page: 0 });
    };

    const handlePageChange = (newPage: number) => {
        setFilters({ ...filters, page: newPage });
    };

    const handleDeleteOrder = async (orderId: number) => {
        if (window.confirm('Вы уверены, что хотите удалить заказ?')) {
            try {
                await deleteOrderApi(orderId);
                refetch();
            } catch (error) {
                console.error('Ошибка удаления заказа:', error);
                alert('Не удалось удалить заказ');
            }
        }
    };

    if (isLoading) return <div className="container mx-auto p-6 text-center text-gray-600 text-xl">Загрузка...</div>;
    if (error) return <div className="container mx-auto p-6 text-center text-red-600 text-xl">Ошибка: {(error as Error).message}</div>;

    return (
        <div className="container mx-auto p-6 max-w-4xl">
            <h1 className="text-4xl font-bold text-gray-900 mb-8">Мои заказы</h1>

            <div className="mb-8 bg-white rounded-2xl shadow-md p-6">
                <h2 className="text-xl font-semibold text-gray-800 mb-4">Фильтры</h2>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <select
                        name="status"
                        value={filters.status}
                        onChange={handleFilterChange}
                        className="p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-lg"
                    >
                        <option value="">Все статусы</option>
                        <option value={OrderStatus.PENDING}>Ожидает</option>
                        <option value={OrderStatus.COMPLETED}>Завершён</option>
                        <option value={OrderStatus.CANCELLED}>Отменён</option>
                    </select>
                    <input
                        type="date"
                        name="startDate"
                        value={filters.startDate}
                        onChange={handleFilterChange}
                        className="p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-lg"
                    />
                    <input
                        type="date"
                        name="endDate"
                        value={filters.endDate}
                        onChange={handleFilterChange}
                        className="p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-lg"
                    />
                </div>
            </div>

            {data?.orders.length === 0 ? (
                <p className="text-gray-600 text-lg text-center">Заказов нет</p>
            ) : (
                <div className="space-y-6">
                    {data?.orders.map((order) => (
                        <div key={order.id} className="bg-white rounded-2xl shadow-xl overflow-hidden">
                            <button
                                onClick={() => toggleExpand(order.id)}
                                className="w-full p-6 text-left font-semibold text-gray-900 hover:bg-gray-50 focus:outline-none transition-colors flex justify-between items-center"
                            >
                                <span className="text-xl">
                                    Заказ #{order.id} - {order.status} ({new Date(order.orderDate).toLocaleDateString('ru-RU')}) - {order.totalAmount.toLocaleString('ru-RU')} ₽
                                </span>
                                <span className="text-blue-600">
                                    {expandedOrderId === order.id ? 'Скрыть' : 'Показать'}
                                </span>
                            </button>
                            {expandedOrderId === order.id && (
                                <div className="p-6 bg-gray-50 animate-slide-down">
                                    <h2 className="text-lg font-semibold text-gray-800 mb-4">Товары в заказе</h2>
                                    <OrderItems orderId={order.id} />
                                    <button
                                        onClick={() => handleDeleteOrder(order.id)}
                                        className="mt-4 bg-gradient-to-r from-red-600 to-red-700 text-white py-2 px-6 rounded-xl shadow-lg hover:from-red-700 hover:to-red-800 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-opacity-50 transition-all duration-300 text-lg"
                                    >
                                        Удалить заказ
                                    </button>
                                </div>
                            )}
                        </div>
                    ))}

                    <div className="flex justify-center gap-4 mt-8">
                        <button
                            onClick={() => handlePageChange(filters.page - 1)}
                            disabled={filters.page === 0}
                            className="px-6 py-3 bg-gradient-to-r from-gray-500 to-gray-600 text-white rounded-xl shadow-lg hover:from-gray-600 hover:to-gray-700 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-opacity-50 transition-all duration-300 disabled:bg-gray-400 disabled:cursor-not-allowed text-lg"
                        >
                            Назад
                        </button>
                        <span className="self-center text-lg text-gray-700">
                            Страница {filters.page + 1} из {data?.totalPages || 1}
                        </span>
                        <button
                            onClick={() => handlePageChange(filters.page + 1)}
                            disabled={filters.page + 1 >= (data?.totalPages || 1)}
                            className="px-6 py-3 bg-gradient-to-r from-gray-500 to-gray-600 text-white rounded-xl shadow-lg hover:from-gray-600 hover:to-gray-700 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-opacity-50 transition-all duration-300 disabled:bg-gray-400 disabled:cursor-not-allowed text-lg"
                        >
                            Вперед
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}

function OrderItems({ orderId }: { orderId: number }) {
    const { data, isLoading, error } = useQuery({
        queryKey: ['orderItems', orderId],
        queryFn: () => getOrderItems(orderId),
    });

    if (isLoading) return <div className="p-4 text-gray-600">Загрузка товаров...</div>;
    if (error) return <div className="p-4 text-red-600">Ошибка: {(error as Error).message}</div>;

    return (
        <ul className="space-y-4">
            {data?.items.map((item) => (
                <li
                    key={item.id}
                    className="flex items-center p-4 bg-gray-100 rounded-xl shadow-md hover:shadow-lg transition-shadow duration-300"
                >
                    <img
                        src={item.imageUrl}
                        alt={item.productName}
                        className="w-16 h-16 object-contain rounded-lg mr-4"
                    />
                    <div className="flex-1">
                        <Link
                            to={`/product/${item.productId}`}
                            className="text-lg text-blue-600 hover:underline font-medium"
                        >
                            {item.productName}
                        </Link>
                        <div className="text-md text-gray-600 mt-1">
                            <span>Кол-во: {item.quantity}</span>
                            <span className="ml-4">Стоимость: {item.price.toLocaleString('ru-RU')} ₽</span>
                        </div>
                    </div>
                </li>
            ))}
        </ul>
    );
}