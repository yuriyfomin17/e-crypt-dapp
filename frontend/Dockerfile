# Step 1: Choose a base image
FROM node:20 as builder

# Step 2: Set the working directory in the container
WORKDIR /app

# Step 3: Copy package.json and package-lock.json (or yarn.lock)
COPY package*.json ./

# Step 4: Install dependencies
RUN npm install --frozen-lockfile

# Step 5: Copy the rest of your application's source code
COPY . .

# Step 6: Build your Next.js application
RUN npm run build

# Step 7: Use a smaller base image for the production build
FROM node:alpine

# Step 8: Set the working directory in the production image
WORKDIR /app

# Step 9: Copy over the build artifacts from the builder stage
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/public ./public
COPY --from=builder /app/package.json ./package.json
COPY --from=builder /app/src ./src

# Step 10: Expose the port Next.js runs on
EXPOSE 3001

# Step 11: Define the command to start your Next.js app
CMD ["npm", "run", "start"]


