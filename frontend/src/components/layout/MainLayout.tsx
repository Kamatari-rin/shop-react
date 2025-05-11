import { Outlet } from 'react-router-dom';
import Header from './Header';

interface MainLayoutProps {
    children?: React.ReactNode;
}

export default function MainLayout({ children }: MainLayoutProps) {
    return (
        <div className="min-h-screen flex flex-col">
            <Header />
            <main className="flex-grow container mx-auto p-4">
                {children || <Outlet />} {}
            </main>
            <footer className="bg-gray-800 text-white p-4 text-center">
                © 2025 Yandex Shop
            </footer>
        </div>
    );
}