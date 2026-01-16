FROM node:20-alpine AS builder
WORKDIR /app

# Копируем файлы зависимостей
COPY package*.json ./

# Устанавливаем зависимости
RUN npm ci

# Копируем исходный код
COPY . .

# Переменные окружения для сборки (должны быть переданы при сборке)
ARG VITE_API_BASE_URL
ARG VITE_WEBSOCKET_URL

ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ENV VITE_WEBSOCKET_URL=${VITE_WEBSOCKET_URL}

# Собираем приложение
RUN npm run build

# Production stage
FROM node:20-alpine
WORKDIR /app

# Устанавливаем serve для статического сервера
RUN npm install -g serve

# Копируем собранные файлы из builder
COPY --from=builder /app/dist ./dist

# Создаем непривилегированного пользователя и меняем владельца файлов
RUN addgroup -g 1001 -S nodejs && \
    adduser -S nextjs -u 1001 && \
    chown -R nextjs:nodejs /app

# Переключаемся на непривилегированного пользователя
USER nextjs

# Открываем порт
EXPOSE 5173

# Запускаем сервер
CMD ["serve", "-s", "dist", "-l", "5173"]
