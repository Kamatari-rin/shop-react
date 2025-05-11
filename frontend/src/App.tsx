import { BrowserRouter } from 'react-router-dom';
import MainLayout from './components/layout/MainLayout';
import { AppRoutes } from './routes';
import './index.css';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

const queryClient = new QueryClient();

function App() {
    return (
        <QueryClientProvider client={queryClient}>
            <BrowserRouter>
                <MainLayout>
                    <AppRoutes />
                </MainLayout>
            </BrowserRouter>
        </QueryClientProvider>
    );
}

export default App;