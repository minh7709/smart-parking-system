# Stage 1: Build
FROM node:22-alpine AS build-stage

# Cài đặt libc6-compat để hỗ trợ các thư viện native (như rolldown/esbuild)
RUN apk add --no-cache libc6-compat

WORKDIR /app

# Copy file package trước để tận dụng cache của Docker
COPY package*.json ./
RUN npm install

# Copy toàn bộ code (loại trừ các file trong .dockerignore)
COPY . .

# Build dự án
RUN npm run build

# Stage 2: Nginx (để serve kết quả build)
FROM FROM nginx:1.27-alpine
COPY --from=build-stage /app/dist /usr/share/nginx/html
# Nếu bạn có file config nginx riêng, hãy copy vào:
# COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]