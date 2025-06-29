import axios from 'axios';
import keycloak from '../keycloak';

const api = axios.create({
    baseURL: 'http://localhost:8080',
});

api.interceptors.request.use(
    async (config) => {
        if (keycloak.authenticated) {
            try {
                await keycloak.updateToken(30); // Обновляем токен, если осталось менее 30 секунд
                config.headers.Authorization = `Bearer ${keycloak.token}`;
            } catch (error) {
                console.error('Failed to refresh token:', error);
                keycloak.login();
            }
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Функция для получения userId из токена
export const getUserId = (): string => {
    if (!keycloak.authenticated || !keycloak.tokenParsed) {
        throw new Error('User is not authenticated');
    }
    const sub = keycloak.tokenParsed.sub;
    if (!sub) {
        throw new Error('User ID (sub) not found in token');
    }
    return sub;
};

export default api;