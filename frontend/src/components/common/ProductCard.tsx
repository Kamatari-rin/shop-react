import { ProductDTO } from '../../types';
import { addItemToCart, updateItemQuantity, removeItemFromCart } from '../../api/cart';
import { useCartStore } from '../../store';
import { Link } from 'react-router-dom';
import {formatPrice} from "../../utils";

interface ProductCardProps {
    product: ProductDTO;
}

export default function ProductCard({ product }: ProductCardProps) {
    const { cart, setCart } = useCartStore();

    const itemInCart = cart?.items.find((item) => item.productId === product.id);
    const quantity = itemInCart ? itemInCart.quantity : 0;

    const handleAddToCart = async () => {
        try {
            const updatedCart = await addItemToCart({
                productId: product.id,
                quantity: 1,
            });
            setCart(updatedCart);
        } catch (error) {
            console.error('Ошибка добавления в корзину:', error);
            alert('Не удалось добавить товар в корзину');
        }
    };

    const handleUpdateQuantity = async (newQuantity: number) => {
        try {
            if (newQuantity < 1) {
                const updatedCart = await removeItemFromCart(product.id);
                setCart(updatedCart);
            } else {
                const updatedCart = await updateItemQuantity({
                    productId: product.id,
                    quantity: newQuantity,
                });
                setCart(updatedCart);
            }
        } catch (error) {
            console.error('Ошибка обновления количества:', error);
            alert('Не удалось обновить количество');
        }
    };



    return (
        <div className="bg-white rounded-lg shadow-md overflow-hidden flex flex-col w-full max-w-xs mx-auto transition-transform hover:scale-105">
            <Link to={`/product/${product.id}`} className="relative w-full h-48 p-4">
                <img
                    src={product.imageUrl}
                    alt={product.name}
                    className="w-full h-full object-contain"
                />
            </Link>
            <div className="p-4 flex flex-col flex-grow">
                <div className="flex-grow">
                    <p className="text-xl font-bold text-blue-600">{formatPrice(product.price)}</p>
                    <Link to={`/product/${product.id}`}>
                        <h3 className="text-lg font-semibold text-gray-900 line-clamp-2 mt-2 hover:text-blue-600">
                            {product.name}
                        </h3>
                    </Link>
                </div>
                <div className="mt-8 mt-auto">
                    {quantity > 0 ? (
                        <div className="flex items-center gap-2 w-full">
                            <button
                                onClick={() => handleUpdateQuantity(quantity - 1)}
                                className="flex-1 px-4 py-2 bg-gray-200 rounded-l-lg hover:bg-gray-300"
                            >
                                −
                            </button>
                            <span className="flex-1 text-center">{quantity}</span>
                            <button
                                onClick={() => handleUpdateQuantity(quantity + 1)}
                                className="flex-1 px-4 py-2 bg-gray-200 rounded-r-lg hover:bg-gray-300"
                            >
                                +
                            </button>
                        </div>
                    ) : (
                        <button
                            onClick={handleAddToCart}
                            className="w-full bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
                        >
                            Добавить в корзину
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
}