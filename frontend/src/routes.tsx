import { lazy, Suspense } from 'react';
import { Route, Routes } from 'react-router-dom';

const ShopPage = lazy(() => import('./pages/ShopPage'));
const ProductPage = lazy(() => import('./pages/ProductPage'));
const CartPage = lazy(() => import('./pages/CartPage'));
const OrdersPage = lazy(() => import('./pages/OrdersPage'));
const CreateOrderPage = lazy(() => import('./pages/CreateOrderPage'));

export function AppRoutes() {
    return (
        <Suspense fallback={<div>Загрузка...</div>}>
            <Routes>
                <Route path="/" element={<ShopPage />} />
                <Route path="/product/:id" element={<ProductPage />} />
                <Route path="/cart" element={<CartPage />} />
                <Route path="/orders" element={<OrdersPage />} />
                <Route path="/create-order" element={<CreateOrderPage />} />
            </Routes>
        </Suspense>
    );
}