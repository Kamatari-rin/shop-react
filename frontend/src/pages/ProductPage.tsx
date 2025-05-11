import { useQuery } from '@tanstack/react-query';
import { useParams } from 'react-router-dom';
import { getProductById } from '../api/catalog';
import { addItemToCart } from '../api/cart';
import { useCartStore } from '../store';


const TEST_USER_ID = '550e8400-e29b-41d4-a716-446655440000';

export default function ProductPage() {
    const { id } = useParams<{ id: string }>();
    const { setCart } = useCartStore();

    const { data: product, isLoading, error } = useQuery({
        queryKey: ['product', id],
        queryFn: () => getProductById(Number(id)),
    });

    const handleAddToCart = async () => {
        if (!product) return;
        try {
            const updatedCart = await addItemToCart(TEST_USER_ID, {
                productId: product.id,
                quantity: 1,
            });
            setCart(updatedCart);
        } catch (error) {
            console.error('Ошибка добавления в корзину:', error);
            alert('Не удалось добавить товар в корзину');
        }
    };

    // Форматирование цены: без копеек, с пробелами
    const formatPrice = (price: number) => {
        return Math.floor(price).toString().replace(/\B(?=(\d{3})+(?!\d))/g, ' ') + ' ₽';
    };

    if (isLoading) return <div className="container mx-auto p-4">Загрузка...</div>;
    if (error) return <div className="container mx-auto p-4">Ошибка: {(error as Error).message}</div>;
    if (!product) return <div className="container mx-auto p-4">Товар не найден</div>;

    return (
        <div className="container mx-auto p-4">
            <div className="bg-white rounded-lg shadow-md p-6 flex flex-col md:flex-row gap-6">
                <div className="md:w-1/2">
                    <img
                        src={product.imageUrl}
                        alt={product.name}
                        className="w-full h-96 object-contain rounded-lg"
                    />
                </div>
                <div className="md:w-1/2 flex flex-col gap-4">
                    <h1 className="text-3xl font-bold text-gray-900">{product.name}</h1>
                    <p className="text-2xl font-bold text-blue-600">{formatPrice(product.price)}</p>
                    <p className="text-gray-700">{product.description}</p>
                    <p className="text-gray-600">
                        Категория: <span className="font-semibold">{product.categoryName}</span>
                    </p>
                    <p className="text-gray-500 text-sm">
                        Создано: {new Date(product.createdAt).toLocaleDateString('ru-RU')}
                    </p>
                    <p className="text-gray-500 text-sm">
                        Обновлено: {new Date(product.updatedAt).toLocaleDateString('ru-RU')}
                    </p>
                    <button
                        onClick={handleAddToCart}
                        className="mt-4 bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition-colors"
                    >
                        Добавить в корзину
                    </button>
                </div>
            </div>
        </div>
    );
}