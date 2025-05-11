import js from '@eslint/js';
import globals from 'globals';
import react from 'eslint-plugin-react';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import tseslint from 'typescript-eslint';
import prettier from 'eslint-plugin-prettier';
import prettierConfig from 'eslint-config-prettier';

export default tseslint.config(
    { ignores: ['dist'] },
    {
        extends: [
            js.configs.recommended,
            ...tseslint.configs.recommended,
            prettierConfig,
        ],
        files: ['**/*.{ts,tsx}'],
        languageOptions: {
            ecmaVersion: 2021,
            globals: globals.browser,
        },
        plugins: {
            react,
            'react-hooks': reactHooks,
            'react-refresh': reactRefresh,
            prettier,
        },
        rules: {
            ...reactHooks.configs.recommended.rules,
            'react-refresh/only-export-components': [
                'warn',
                { allowConstantExport: true },
            ],
            'prettier/prettier': 'error',
            'react/prop-types': 'off',
        },
        settings: {
            react: {
                version: 'detect',
            },
        },
    },
);